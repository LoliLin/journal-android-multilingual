package com.isaakhanimann.journal.ui.tabs.journal.experience.timeline

import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DurationRange
import com.isaakhanimann.journal.data.substances.classes.roa.DurationUnits
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.DataForOneEffectLine
import java.time.Instant
import org.junit.Assert.assertEquals
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
    fun unspecifiedAndImmediateReleaseWithSameProfileStillCombine() {
        // Both unspecified and immediate release resolve to the same catalog duration.
        val unspecified = line(firstTime, immediateDuration)
        val immediate = line(secondTime, immediateDuration)
        assertEquals(
            listOf(listOf(unspecified, immediate)),
            groupEffectLinesByProfile(listOf(unspecified, immediate))
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
