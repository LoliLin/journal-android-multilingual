package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.main.navigation.routers.NavArgKeys.ui.main.navigation.routers

/**
 * Navigation argument keys for type-safe route compatibility.
 * These provide the old string keys that ViewModels expect in SavedStateHandle.
 */
object NavArgKeys {
    const val NavArgKeys.SUBSTANCE_NAME_KEY = "substanceName"
    const val NavArgKeys.ADMINISTRATION_ROUTE_KEY = "administrationRoute"
    const val NavArgKeys.CUSTOM_UNIT_ID_KEY = "customUnitId"
    const val NavArgKeys.CUSTOM_SUBSTANCE_ID_KEY = "customSubstanceId"
    const val NavArgKeys.DOSE_KEY = "dose"
    const val NavArgKeys.ESTIMATED_DOSE_STANDARD_DEVIATION_KEY = "estimatedDoseStandardDeviation"
    const val NavArgKeys.IS_ESTIMATE_KEY = "isEstimate"
    const val NavArgKeys.UNITS_KEY = "units"
    const val NavArgKeys.EXPERIENCE_ID_KEY = "experienceId"
    const val NavArgKeys.INGESTION_ID_KEY = "ingestionId"
    const val NavArgKeys.CONSUMER_NAME_KEY = "consumerName"
}
