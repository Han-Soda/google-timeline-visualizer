package dev.mahlernim.timelinevisualizer.render

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CameraZoomSmoothingTest {
    @Test
    fun zeroSmoothnessKeepsTheOriginalResponse() {
        assertEquals(0, CameraZoomSmoothing.radiusSamples(0, 481))
        val spans = step()

        assertArrayEquals(spans, CameraZoomSmoothing.smoothLogSpans(spans, 0), 0.0)
    }

    @Test
    fun radiusGrowsWithSmoothnessAndIsClamped() {
        val radii = listOf(-20, 0, 30, 60, 100, 150).map { CameraZoomSmoothing.radiusSamples(it, 481) }

        assertEquals(0, radii[0])
        assertTrue("Radii should grow: $radii", radii[2] in 1 until radii[3] && radii[3] < radii[4])
        assertEquals(radii[4], radii[5])
    }

    @Test
    fun smoothingNeverTightensAnySample() {
        val spans = DoubleArray(300) { index -> if ((index / 17) % 3 == 0) -1.0 else -6.0 + (index % 5) * 0.1 }
        val smoothed = CameraZoomSmoothing.smoothLogSpans(spans, CameraZoomSmoothing.radiusSamples(100, spans.size))

        spans.indices.forEach { index -> assertTrue(smoothed[index] >= spans[index]) }
    }

    @Test
    fun smoothingSpreadsAStepOverManySamples() {
        val spans = step()
        val raw = maxStep(spans)
        val smoothed = CameraZoomSmoothing.smoothLogSpans(spans, CameraZoomSmoothing.radiusSamples(60, spans.size))

        assertEquals(5.0, raw, 1e-12)
        assertTrue("Largest change ${maxStep(smoothed)} was not much smaller than $raw", maxStep(smoothed) < raw / 6)
        assertEquals(spans.first(), smoothed.first(), 1e-12)
        assertEquals(spans.last(), smoothed.last(), 1e-12)
    }

    private fun step() = DoubleArray(481) { index -> if (index in 200..300) -2.0 else -7.0 }

    private fun maxStep(values: DoubleArray): Double =
        values.toList().zipWithNext().maxOf { (before, after) -> abs(after - before) }
}
