package com.isaakhanimann.journal.ui.tabs.journal.experience.timeline

import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DurationRange
import com.isaakhanimann.journal.data.substances.classes.roa.DurationUnits
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.DataForOneEffectLine
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class AllTimelinesGroupingTest {
    private val firstTime = Instant.parse("2026-10-23T08:00:00Z")
    private val secondTime = firstTime.plusSeconds(60 * 60)
    private val immediateDuration = RoaDuration(
        onset = DurationRange(min = 10f, max = 20f, units = DurationUnits.MINUTES),
        comeup = null,
        peak = null,
        offset = null,
        total = null,
        afterglow = null
    )

    private fun line(
        time: Instant,
        duration: RoaDuration?,
        substance: String = "Methylphenidate",
        route: AdministrationRoute = AdministrationRoute.ORAL
    ) = DataForOneEffectLine(
        substanceName = substance,
        route = route,
        roaDuration = duration,
        height = 1f,
        horizontalWeight = 0.5f,
        color = AdaptiveColor.BLUE,
        startTime = time,
        endTime = null
    )

    private fun assertDifferentReleaseProfilesRemainSeparate(lines: List<DataForOneEffectLine>) {
        val groups = groupEffectLinesByProfile(lines)
        assertEquals(2, groups.size)
        assertEquals(
            listOf(lines.single { it.roaDuration == null }),
            groups.single { it.first().roaDuration == null }
        )
        assertEquals(
            listOf(lines.single { it.roaDuration == immediateDuration }),
            groups.single { it.first().roaDuration == immediateDuration }
        )
    }

    @Test
    fun extendedReleaseBeforeImmediateReleaseDoesNotHideImmediateCurve() {
        assertDifferentReleaseProfilesRemainSeparate(
            listOf(line(firstTime, null), line(secondTime, immediateDuration))
        )
    }

    @Test
    fun immediateReleaseBeforeExtendedReleaseDoesNotGiveExtendedACurve() {
        assertDifferentReleaseProfilesRemainSeparate(
            listOf(line(firstTime, immediateDuration), line(secondTime, null))
        )
    }

    @Test
    fun sameDurationSameSubstanceAndRouteCombine() {
        val first = line(firstTime, immediateDuration)
        val second = line(secondTime, immediateDuration)
        assertEquals(
            listOf(listOf(first, second)),
            groupEffectLinesByProfile(listOf(first, second))
        )
    }

    @Test
    fun structurallyEqualDurationsFromDistinctInstancesCombine() {
        // Both duration types are data classes; grouping must compare their values.
        val sameValue = immediateDuration.copy(onset = immediateDuration.onset!!.copy())
        assertNotSame(immediateDuration, sameValue)
        assertNotSame(immediateDuration.onset, sameValue.onset)
        assertEquals(immediateDuration, sameValue)
        val first = line(firstTime, immediateDuration)
        val second = line(secondTime, sameValue)
        assertEquals(
            listOf(listOf(first, second)),
            groupEffectLinesByProfile(listOf(first, second))
        )
    }

    @Test
    fun interleavedProfilesPreserveOriginalSubstanceAndRouteDrawOrder() {
        val oralImmediate = line(firstTime, immediateDuration)
        val caffeine = line(firstTime.plusSeconds(1), immediateDuration, substance = "Caffeine")
        val sublingual = line(
            firstTime.plusSeconds(2),
            immediateDuration,
            route = AdministrationRoute.SUBLINGUAL
        )
        val oralExtended = line(secondTime, null)
        val secondCaffeine = line(secondTime.plusSeconds(3), immediateDuration, substance = "Caffeine")
        assertEquals(
            listOf(
                listOf(oralImmediate),
                listOf(oralExtended),
                listOf(sublingual),
                listOf(caffeine, secondCaffeine)
            ),
            groupEffectLinesByProfile(
                listOf(oralImmediate, caffeine, sublingual, oralExtended, secondCaffeine)
            )
        )
    }

    @Test
    fun differentSubstancesAndRoutesDoNotShareAGroup() {
        val oralMph = line(firstTime, immediateDuration)
        val otherSubstance = line(secondTime, immediateDuration, substance = "Caffeine")
        val otherRoute = line(secondTime, immediateDuration, route = AdministrationRoute.SUBLINGUAL)
        val groups = groupEffectLinesByProfile(listOf(oralMph, otherSubstance, otherRoute))
        assertEquals(3, groups.size)
        assertEquals(
            setOf(listOf(oralMph), listOf(otherSubstance), listOf(otherRoute)),
            groups.toSet()
        )
    }

    @Test
    fun sameSubstanceAndRouteWithUnknownDurationsKeepTheirPointsTogether() {
        val first = line(firstTime, null)
        val second = line(secondTime, null)
        assertEquals(
            listOf(listOf(first, second)),
            groupEffectLinesByProfile(listOf(first, second))
        )
    }
}
