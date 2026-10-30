/*
 * Copyright (c) 2026.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 */
package com.isaakhanimann.journal.ui.tabs.settings

import com.isaakhanimann.journal.data.room.experiences.entities.ShulginRatingOption
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.isNullable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

internal data class ImportStructuralDiagnostic(
    val path: String,
    val expected: String,
    val actual: String
)

internal class ImportDiagnosticsException(
    val diagnostics: List<ImportStructuralDiagnostic>
) : IllegalArgumentException("Journal import has structural errors")

/** Checks the backup serializer shape before decoding, collecting independent mismatches. */
internal fun validateJournalImportStructure(element: JsonElement) {
    val errors = mutableListOf<ImportStructuralDiagnostic>()
    inspect(element, JournalExport.serializer().descriptor, "$", errors)
    if (errors.isNotEmpty()) throw ImportDiagnosticsException(errors)
}

private fun inspect(
    value: JsonElement,
    descriptor: SerialDescriptor,
    path: String,
    errors: MutableList<ImportStructuralDiagnostic>,
    allowUnknownEnum: Boolean = false
) {
    if (value == JsonNull) {
        if (!descriptor.isNullable) errors += ImportStructuralDiagnostic(path, expectedType(descriptor), "null")
        return
    }
    if (descriptor.kind == SerialKind.ENUM) {
        val primitive = value as? JsonPrimitive
        if (primitive == null || !primitive.isString) {
            errors += ImportStructuralDiagnostic(path, expectedType(descriptor), actualType(value))
        } else if (!allowUnknownEnum && descriptor.getElementIndex(primitive.content) < 0) {
            errors += ImportStructuralDiagnostic(path, "${descriptor.serialName} value", "unknown string")
        }
        return
    }
    when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> {
            val obj = value as? JsonObject ?: return mismatch(errors, path, "object", value)
            for (index in 0 until descriptor.elementsCount) {
                val name = descriptor.getElementName(index)
                val child = obj[name]
                val childDescriptor = descriptor.getElementDescriptor(index)
                if (child == null) {
                    if (!descriptor.isElementOptional(index)) {
                        errors += ImportStructuralDiagnostic("$path.$name", expectedType(childDescriptor), "missing")
                    }
                } else {
                    inspect(
                        child,
                        childDescriptor,
                        "$path.$name",
                        errors,
                        allowUnknownEnum = descriptor.isElementOptional(index) || childDescriptor.isNullable
                    )
                }
            }
        }
        StructureKind.LIST -> {
            val array = value as? JsonArray ?: return mismatch(errors, path, "array", value)
            val itemDescriptor = descriptor.getElementDescriptor(0)
            array.forEachIndexed { index, child -> inspect(child, itemDescriptor, "$path[$index]", errors) }
        }
        StructureKind.MAP -> {
            val arrayOrObject = value as? JsonObject ?: return mismatch(errors, path, "object", value)
            val itemDescriptor = descriptor.getElementDescriptor(1)
            arrayOrObject.forEach { (key, child) -> inspect(child, itemDescriptor, "$path[${JsonPrimitive(key)}]", errors) }
        }
        else -> {
            if (!primitiveMatches(value, descriptor)) {
                errors += ImportStructuralDiagnostic(path, expectedType(descriptor), actualType(value))
            } else if (descriptor.serialName == "ShulginRatingOption") {
                val primitive = value as? JsonPrimitive
                if (primitive?.isString == true && ShulginRatingOption.entries.none { it.rawValue == primitive.content }) {
                    errors += ImportStructuralDiagnostic(path, "ShulginRatingOption value", "unknown string")
                }
            }
        }
    }
}

private fun primitiveMatches(value: JsonElement, descriptor: SerialDescriptor): Boolean {
    val primitive = value as? JsonPrimitive ?: return false
    val isUnquoted = !primitive.isString
    return when (descriptor.kind) {
        PrimitiveKind.STRING -> primitive.isString
        PrimitiveKind.BOOLEAN -> isUnquoted && primitive.booleanOrNull != null
        PrimitiveKind.BYTE -> isUnquoted && primitive.longOrNull?.let { it in Byte.MIN_VALUE..Byte.MAX_VALUE } == true
        PrimitiveKind.SHORT -> isUnquoted && primitive.longOrNull?.let { it in Short.MIN_VALUE..Short.MAX_VALUE } == true
        PrimitiveKind.INT -> isUnquoted && primitive.longOrNull?.let { it in Int.MIN_VALUE..Int.MAX_VALUE } == true
        PrimitiveKind.LONG -> isUnquoted && if (descriptor.serialName == "Instant") {
            primitive.doubleOrNull != null
        } else {
            primitive.longOrNull != null
        }
        PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> isUnquoted && primitive.doubleOrNull != null
        SerialKind.ENUM -> primitive.isString
        else -> true // Keep serializer-defined compatibility for custom primitive formats.
    }
}

private fun expectedType(descriptor: SerialDescriptor): String = when (descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> "object"
    StructureKind.LIST -> "array"
    StructureKind.MAP -> "object"
    PrimitiveKind.STRING, SerialKind.ENUM -> "string"
    PrimitiveKind.BOOLEAN -> "boolean"
    PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT -> "integer"
    PrimitiveKind.LONG -> if (descriptor.serialName == "Instant") "number" else "integer"
    PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> "number"
    else -> descriptor.serialName.substringAfterLast('.')
}

private fun actualType(value: JsonElement): String = when (value) {
    JsonNull -> "null"
    is JsonObject -> "object"
    is JsonArray -> "array"
    is JsonPrimitive -> when {
        value.isString -> "string"
        value.booleanOrNull != null -> "boolean"
        value.longOrNull != null || value.doubleOrNull != null -> "number"
        else -> "value"
    }
}

private fun mismatch(
    errors: MutableList<ImportStructuralDiagnostic>,
    path: String,
    expected: String,
    actual: JsonElement
) {
    errors += ImportStructuralDiagnostic(path, expected, actualType(actual))
}
