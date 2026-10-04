package dev.mahlernim.timelinevisualizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RoutePointSpacingTest {
    @Test
    fun allPointsKeepsTheSameJourney() {
        val journey = Journey.from(jitteredStop(), 2025)

        assertSame(journey, journey.withMinimumPointSpacing(RoutePointSpacing.ALL_POINTS.kilometers))
    }

    @Test
    fun spacingCollapsesJitterButKeepsBothEnds() {
        val points = jitteredStop()
        val journey = Journey.from(points, 2025)

        val spaced = journey.withMinimumPointSpacing(RoutePointSpacing.METERS_250.kilometers)

        assertTrue("Expected fewer points, got ${spaced.points.size}", spaced.points.size < points.size / 4)
        assertEquals(points.first(), spaced.points.first())
        assertEquals(points.last(), spaced.points.last())
        spaced.points.zipWithNext().dropLast(1).forEach { (before, after) ->
            assertTrue(haversineKm(before, after) >= RoutePointSpacing.METERS_250.kilometers)
        }
        assertTrue(spaced.totalDistanceKm < journey.totalDistanceKm)
    }

    @Test
    fun spacingKeepsRouteBreaksAndInferredTransfers() {
        val first = (0..40).map { point(it, 37.50 + it * 0.0001, 127.00) }
        val second = (41..80).map { point(it, 35.10 + (it - 41) * 0.0001, 129.00) }
        val third = (81..120).map { point(it, 33.50 + (it - 81) * 0.0001, 126.50) }
        val journey = Journey.fromSections(
            listOf(first, second + third),
            TimelinePeriod.sameYear(2025),
            inferredTransferBeforePointIndices = listOf(first.size + second.size),
        )

        val spaced = journey.withMinimumPointSpacing(RoutePointSpacing.METERS_100.kilometers)

        assertEquals(1, spaced.breakBeforePointIndices.size)
        assertEquals(1, spaced.inferredTransferBeforePointIndices.size)
        val breakIndex = spaced.breakBeforePointIndices.single()
        val transferIndex = spaced.inferredTransferBeforePointIndices.single()
        assertEquals(first.last(), spaced.points[breakIndex - 1])
        assertEquals(second.first(), spaced.points[breakIndex])
        assertEquals(second.last(), spaced.points[transferIndex - 1])
        assertEquals(third.first(), spaced.points[transferIndex])
        assertTrue(spaced.points.size < journey.points.size)
    }

    @Test
    fun semanticEpisodesStayOnTheSameRouteSegments() {
        val points = (0..200).map { point(it, 0.0, it * 0.0005) }
        val base = Journey.from(points, 2025)
        val episode = JourneySemanticEpisode(
            startKm = base.cumulativeDistanceKm[37] + 0.01,
            endKm = base.cumulativeDistanceKm[161] - 0.01,
            origin = points[37],
            destination = points[161],
        )
        val journey = base.copy(semanticEpisodes = listOf(episode))

        val spaced = journey.withMinimumPointSpacing(RoutePointSpacing.METERS_500.kilometers)
        val remapped = spaced.semanticEpisodes.single()

        assertTrue(spaced.points.size < points.size / 4)
        assertEquals(episode.origin, remapped.origin)
        assertEquals(episode.destination, remapped.destination)
        val start = spaced.positionAtDistance(remapped.startKm).point
        val end = spaced.positionAtDistance(remapped.endKm).point
        assertEquals(journey.positionAtDistance(episode.startKm).point.longitude, start.longitude, 1e-9)
        assertEquals(journey.positionAtDistance(episode.endKm).point.longitude, end.longitude, 1e-9)
    }

    @Test
    fun unknownStoredSpacingFallsBackToAllPoints() {
        assertEquals(RoutePointSpacing.ALL_POINTS, RoutePointSpacing.fromMeters(42))
        assertEquals(RoutePointSpacing.KILOMETERS_1, RoutePointSpacing.fromMeters(1_000))
    }

    private fun jitteredStop(): List<GeoPoint> = (0..300).map { index ->
        val jitter = if (index % 2 == 0) 0.0002 else -0.0002
        point(index, 37.5665 + jitter + index * 0.00002, 126.9780 - jitter)
    }

    private fun point(minute: Int, latitude: Double, longitude: Double) = GeoPoint(
        Instant.parse("2025-06-01T00:00:00Z").plusSeconds(minute * 60L),
        latitude,
        longitude,
    )
}
