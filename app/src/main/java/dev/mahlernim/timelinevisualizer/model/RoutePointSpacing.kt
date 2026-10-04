package dev.mahlernim.timelinevisualizer.model

/**
 * User-selectable minimum distance between the Timeline points that the animation follows.
 *
 * Larger spacing animates fewer points: GPS jitter and dense stops collapse into a calmer route,
 * while [ALL_POINTS] keeps every point of the selected journey.
 */
enum class RoutePointSpacing(val meters: Int) {
    ALL_POINTS(0),
    METERS_25(25),
    METERS_50(50),
    METERS_100(100),
    METERS_250(250),
    METERS_500(500),
    KILOMETERS_1(1_000),
    KILOMETERS_2(2_000),
    KILOMETERS_5(5_000),
    ;

    val kilometers: Double get() = meters / 1_000.0

    companion object {
        val DEFAULT = ALL_POINTS

        fun fromMeters(meters: Int): RoutePointSpacing = entries.firstOrNull { it.meters == meters } ?: DEFAULT
    }
}

/**
 * Returns a journey that keeps only points at least [minimumSpacingKm] from the previously kept
 * point.
 *
 * The first and last points, both sides of every route break and inferred transfer, and the
 * segments that hold semantic episode boundaries are always kept, so the journey keeps its
 * topology. Episodes are remapped onto the thinned distance axis.
 */
fun Journey.withMinimumPointSpacing(minimumSpacingKm: Double): Journey {
    if (!(minimumSpacingKm > 0.0) || points.size <= 2) return this
    val required = BooleanArray(points.size)
    required[0] = true
    required[points.lastIndex] = true
    (breakBeforePointIndices + inferredTransferBeforePointIndices).forEach { index ->
        required[index] = true
        required[index - 1] = true
    }
    semanticEpisodes.forEach { episode ->
        listOf(episode.startKm, episode.endKm).forEach { distanceKm ->
            val position = positionAtDistance(distanceKm)
            required[position.fromIndex] = true
            required[position.toIndex] = true
        }
    }

    val kept = ArrayList<Int>(points.size)
    for (index in points.indices) {
        val keep = required[index] || haversineKm(points[kept.last()], points[index]) >= minimumSpacingKm
        if (keep) kept += index
    }
    if (kept.size == points.size) return this

    val newIndexByOriginal = IntArray(points.size) { -1 }
    kept.forEachIndexed { newIndex, originalIndex -> newIndexByOriginal[originalIndex] = newIndex }
    val keptPoints = kept.map(points::get)
    val keptBreaks = breakBeforePointIndices.map { newIndexByOriginal[it] }
    val keptTransfers = inferredTransferBeforePointIndices.map { newIndexByOriginal[it] }
    val breakSet = keptBreaks.toSet()
    val keptDistances = DoubleArray(keptPoints.size)
    for (index in 1 until keptPoints.size) {
        keptDistances[index] = keptDistances[index - 1] + if (index in breakSet) {
            0.0
        } else {
            haversineKm(keptPoints[index - 1], keptPoints[index])
        }
    }
    val totalKm = keptDistances.lastOrNull() ?: 0.0

    fun remap(distanceKm: Double): Double {
        val position = positionAtDistance(distanceKm)
        val from = newIndexByOriginal[position.fromIndex]
        val to = newIndexByOriginal[position.toIndex]
        val start = keptDistances[from]
        return (start + (keptDistances[to] - start) * position.segmentFraction).coerceIn(0.0, totalKm)
    }

    val remappedEpisodes = semanticEpisodes.mapNotNull { episode ->
        val startKm = remap(episode.startKm)
        val endKm = remap(episode.endKm)
        if (endKm > startKm) episode.copy(startKm = startKm, endKm = endKm) else null
    }.sortedBy { it.startKm }

    return Journey.fromBreakIndices(
        keptPoints,
        period,
        keptBreaks,
        keptTransfers,
        remappedEpisodes,
    )
}
