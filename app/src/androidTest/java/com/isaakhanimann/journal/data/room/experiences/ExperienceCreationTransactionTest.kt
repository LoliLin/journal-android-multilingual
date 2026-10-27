package com.isaakhanimann.journal.data.room.experiences

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaakhanimann.journal.data.room.AppDatabase
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ExperienceCreationTransactionTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ExperienceDao
    private val time = Instant.parse("2024-01-01T12:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).build()
        dao = db.experienceDao()
    }

    @After
    fun tearDown() = db.close()

    private fun experience(id: Int, title: String) = Experience(
        id = id, title = title, text = "", sortDate = time, location = null
    )

    private fun ingestion(name: String, experienceId: Int) = Ingestion(
        substanceName = name, time = time, administrationRoute = AdministrationRoute.ORAL,
        dose = null, isDoseAnEstimate = false, estimatedDoseStandardDeviation = null,
        units = null, experienceId = experienceId, notes = null, stomachFullness = null,
        consumerName = null, customUnitId = null
    )

    private fun companion(name: String) = SubstanceCompanion(name, AdaptiveColor.BLUE)

    @Test
    fun concurrentCreationReturnsDistinctIdsAndKeepsEachIngestionWithItsExperience() = runBlocking {
        dao.insert(experience(42, "existing"))
        val ids = coroutineScope {
            (1..12).map { index ->
                async(Dispatchers.IO) {
                    dao.insertIngestionExperienceAndCompanion(
                        ingestion("substance-$index", 42),
                        experience(42, "new-$index"),
                        companion("substance-$index")
                    )
                }
            }.awaitAll()
        }
        assertEquals(12, ids.toSet().size)
        assertTrue(ids.all { it > 0 && it != 42 })
        val rows = dao.getSortedExperiencesWithIngestionsFlow().first()
        assertEquals(13, rows.size)
        assertEquals("existing", rows.single { it.experience.id == 42 }.experience.title)
        ids.forEachIndexed { index, id ->
            val row = rows.single { it.experience.id == id }
            assertEquals("new-${index + 1}", row.experience.title)
            assertEquals("substance-${index + 1}", row.ingestions.single().substanceName)
            assertEquals(id, row.ingestions.single().experienceId)
        }
    }

    @Test
    fun existingExperiencePathAndSplitStillUseTheirActualIds() = runBlocking {
        dao.insert(experience(7, "existing"))
        dao.insertIngestionAndCompanion(ingestion("existing-substance", 7), companion("existing-substance"))
        val original = dao.getSortedExperiencesWithIngestionsFlow().first().single().ingestions.single()
        assertEquals(7, original.experienceId)
        val splitId = dao.insertExperienceAndUpdateIngestion("split", original)
        assertNotEquals(7, splitId)
        val rows = dao.getSortedExperiencesWithIngestionsFlow().first()
        assertTrue(rows.single { it.experience.id == 7 }.ingestions.isEmpty())
        assertEquals(splitId, rows.single { it.experience.id == splitId }.ingestions.single().experienceId)
    }

    @Test
    fun failureAfterInsertsRollsBackExperienceAndIngestion() = runBlocking {
        db.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_companion BEFORE INSERT ON SubstanceCompanion " +
                "BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        var failed = false
        try {
            dao.insertIngestionExperienceAndCompanion(
                ingestion("failure", 0), experience(0, "failure"), companion("failure")
            )
        } catch (expected: android.database.SQLException) {
            failed = true
        }
        assertTrue(failed)
        assertTrue(dao.getSortedExperiencesWithIngestionsFlow().first().isEmpty())
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM ingestion").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }
}
