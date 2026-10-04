package dev.mahlernim.timelinevisualizer.render

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Eases camera zoom changes without tightening the framing that any camera sample requires.
 *
 * Log spans are first widened to the largest span within the smoothing radius, then averaged with
 * a raised-cosine window of the same radius. Every window that contributes to a sample also
 * contains that sample's own requirement, so the result never drops below the input: the camera
 * starts widening before a long move and settles in gradually afterwards, and the marker stays
 * inside the recentering zone.
 */
internal object CameraZoomSmoothing {
    /** The largest radius, as a share of the journey on each side of a sample. */
    private const val MAX_RADIUS_FRACTION = 0.07

    fun radiusSamples(smoothness: Int, sampleCount: Int): Int {
        if (sampleCount < 3) return 0
        val amount = smoothness.coerceIn(CameraSettings.MIN_ZOOM_SMOOTHNESS, CameraSettings.MAX_ZOOM_SMOOTHNESS) /
            CameraSettings.MAX_ZOOM_SMOOTHNESS.toDouble()
        return (amount * MAX_RADIUS_FRACTION * (sampleCount - 1)).roundToInt()
    }

    fun smoothLogSpans(logSpans: DoubleArray, radius: Int): DoubleArray {
        val size = logSpans.size
        if (radius <= 0 || size < 3) return logSpans.copyOf()
        val widened = DoubleArray(size) { index ->
            var widest = logSpans[index]
            for (other in max(0, index - radius)..min(size - 1, index + radius)) {
                widest = max(widest, logSpans[other])
            }
            widest
        }
        val weights = DoubleArray(radius + 1) { offset ->
            0.5 * (1.0 + cos(PI * offset / (radius + 1)))
        }
        return DoubleArray(size) { index ->
            var total = 0.0
            var weightSum = 0.0
            for (other in max(0, index - radius)..min(size - 1, index + radius)) {
                val weight = weights[abs(other - index)]
                total += widened[other] * weight
                weightSum += weight
            }
            // Rounding must never leave a sample tighter than its own requirement.
            max(total / weightSum, logSpans[index])
        }
    }
}
