package dev.mahlernim.timelinevisualizer.ui

import androidx.lifecycle.ViewModel
import dev.mahlernim.timelinevisualizer.data.LocationFilterMode
import dev.mahlernim.timelinevisualizer.model.RoutePointSpacing
import dev.mahlernim.timelinevisualizer.render.CameraSettings
import dev.mahlernim.timelinevisualizer.render.DistanceUnitPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val camera: CameraSettings,
    val distanceUnit: DistanceUnitPreference,
    val locationFilter: LocationFilterMode,
    val simplifyRouteDetail: Boolean,
    val keepPastRoutesVisible: Boolean,
    val hideDates: Boolean = false,
    val pastRouteOpacity: Int = CameraSettings.DEFAULT_PAST_ROUTE_OPACITY,
    val zoomSmoothness: Int = CameraSettings.DEFAULT_ZOOM_SMOOTHNESS,
    val routePointSpacing: RoutePointSpacing = RoutePointSpacing.DEFAULT,
) {
    /** Applies the display choices kept outside camera preferences to a camera draft. */
    fun withDisplayChoices(settings: CameraSettings): CameraSettings = settings.copy(
        keepPastRoutesVisible = keepPastRoutesVisible,
        pastRouteOpacity = pastRouteOpacity,
        zoomSmoothness = zoomSmoothness,
    )
}

class SettingsViewModel(
    private val cameraPreferences: CameraSettingsPreferences,
    private val distanceUnitPreferences: DistanceUnitPreferences,
    private val locationFilterPreferences: LocationFilterPreferences,
    private val timelineDisplayPreferences: TimelineDisplayPreferences,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        SettingsState(
            camera = cameraPreferences.load(),
            distanceUnit = distanceUnitPreferences.load(),
            locationFilter = locationFilterPreferences.load(),
            simplifyRouteDetail = timelineDisplayPreferences.simplifyRouteDetail(),
            keepPastRoutesVisible = timelineDisplayPreferences.keepPastRoutesVisible(),
            hideDates = timelineDisplayPreferences.hideDates(),
            pastRouteOpacity = timelineDisplayPreferences.pastRouteOpacity(),
            zoomSmoothness = timelineDisplayPreferences.zoomSmoothness(),
            routePointSpacing = timelineDisplayPreferences.routePointSpacing(),
        ),
    )
    val state: StateFlow<SettingsState> = mutableState.asStateFlow()

    fun updateCamera(settings: CameraSettings) {
        cameraPreferences.save(settings)
        mutableState.value = mutableState.value.copy(camera = settings)
    }

    fun resetVideoDefaults() {
        mutableState.value = mutableState.value.copy(
            camera = cameraPreferences.reset(),
        )
    }

    fun updateDistanceUnit(preference: DistanceUnitPreference) {
        distanceUnitPreferences.save(preference)
        mutableState.value = mutableState.value.copy(distanceUnit = preference)
    }

    fun updateLocationFilter(mode: LocationFilterMode) {
        locationFilterPreferences.save(mode)
        mutableState.value = mutableState.value.copy(locationFilter = mode)
    }

    fun updateHideDates(enabled: Boolean) {
        timelineDisplayPreferences.setHideDates(enabled)
        mutableState.value = mutableState.value.copy(hideDates = enabled)
    }

    fun updateSimplifyRouteDetail(enabled: Boolean) {
        timelineDisplayPreferences.setSimplifyRouteDetail(enabled)
        mutableState.value = mutableState.value.copy(simplifyRouteDetail = enabled)
    }

    fun updateKeepPastRoutesVisible(enabled: Boolean) {
        timelineDisplayPreferences.setKeepPastRoutesVisible(enabled)
        mutableState.value = mutableState.value.copy(keepPastRoutesVisible = enabled)
    }

    fun updatePastRouteOpacity(percent: Int) {
        timelineDisplayPreferences.setPastRouteOpacity(percent)
        mutableState.value = mutableState.value.copy(pastRouteOpacity = timelineDisplayPreferences.pastRouteOpacity())
    }

    fun updateZoomSmoothness(percent: Int) {
        timelineDisplayPreferences.setZoomSmoothness(percent)
        mutableState.value = mutableState.value.copy(zoomSmoothness = timelineDisplayPreferences.zoomSmoothness())
    }

    fun updateRoutePointSpacing(spacing: RoutePointSpacing) {
        timelineDisplayPreferences.setRoutePointSpacing(spacing)
        mutableState.value = mutableState.value.copy(routePointSpacing = spacing)
    }
}
