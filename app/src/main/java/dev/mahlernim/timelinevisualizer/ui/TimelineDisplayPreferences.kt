package dev.mahlernim.timelinevisualizer.ui

import android.content.Context
import androidx.core.content.edit
import dev.mahlernim.timelinevisualizer.model.RoutePointSpacing
import dev.mahlernim.timelinevisualizer.render.CameraSettings

class TimelineDisplayPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun hideDates(): Boolean = preferences.getBoolean(KEY_HIDE_DATES, false)

    fun setHideDates(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_HIDE_DATES, enabled) }
    }

    fun simplifyRouteDetail(): Boolean = preferences.getBoolean(KEY_SIMPLIFY_ROUTE_DETAIL, false)

    fun setSimplifyRouteDetail(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_SIMPLIFY_ROUTE_DETAIL, enabled) }
    }

    fun keepPastRoutesVisible(): Boolean = preferences.getBoolean(KEY_KEEP_PAST_ROUTES_VISIBLE, false)

    fun setKeepPastRoutesVisible(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_KEEP_PAST_ROUTES_VISIBLE, enabled) }
    }

    fun pastRouteOpacity(): Int = preferences.getInt(
        KEY_PAST_ROUTE_OPACITY,
        CameraSettings.DEFAULT_PAST_ROUTE_OPACITY,
    ).coerceIn(CameraSettings.MIN_PAST_ROUTE_OPACITY, CameraSettings.MAX_PAST_ROUTE_OPACITY)

    fun setPastRouteOpacity(percent: Int) {
        preferences.edit {
            putInt(
                KEY_PAST_ROUTE_OPACITY,
                percent.coerceIn(CameraSettings.MIN_PAST_ROUTE_OPACITY, CameraSettings.MAX_PAST_ROUTE_OPACITY),
            )
        }
    }

    fun zoomSmoothness(): Int = preferences.getInt(
        KEY_ZOOM_SMOOTHNESS,
        CameraSettings.DEFAULT_ZOOM_SMOOTHNESS,
    ).coerceIn(CameraSettings.MIN_ZOOM_SMOOTHNESS, CameraSettings.MAX_ZOOM_SMOOTHNESS)

    fun setZoomSmoothness(percent: Int) {
        preferences.edit {
            putInt(
                KEY_ZOOM_SMOOTHNESS,
                percent.coerceIn(CameraSettings.MIN_ZOOM_SMOOTHNESS, CameraSettings.MAX_ZOOM_SMOOTHNESS),
            )
        }
    }

    fun routePointSpacing(): RoutePointSpacing =
        RoutePointSpacing.fromMeters(preferences.getInt(KEY_ROUTE_POINT_SPACING, RoutePointSpacing.DEFAULT.meters))

    fun setRoutePointSpacing(spacing: RoutePointSpacing) {
        preferences.edit { putInt(KEY_ROUTE_POINT_SPACING, spacing.meters) }
    }

    fun clear() = preferences.edit { clear() }

    private companion object {
        const val PREFERENCES_NAME = "timeline-display-settings"
        const val KEY_HIDE_DATES = "hide-dates"
        const val KEY_SIMPLIFY_ROUTE_DETAIL = "simplify-route-detail"
        const val KEY_KEEP_PAST_ROUTES_VISIBLE = "keep-past-routes-visible"
        const val KEY_PAST_ROUTE_OPACITY = "past-route-opacity"
        const val KEY_ZOOM_SMOOTHNESS = "zoom-smoothness"
        const val KEY_ROUTE_POINT_SPACING = "route-point-spacing-meters"
    }
}
