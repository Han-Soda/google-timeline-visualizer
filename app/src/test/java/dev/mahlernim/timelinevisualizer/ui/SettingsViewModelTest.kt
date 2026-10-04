package dev.mahlernim.timelinevisualizer.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.mahlernim.timelinevisualizer.data.LocationFilterMode
import dev.mahlernim.timelinevisualizer.model.RoutePointSpacing
import dev.mahlernim.timelinevisualizer.render.CameraMovement
import dev.mahlernim.timelinevisualizer.render.CameraSettings
import dev.mahlernim.timelinevisualizer.render.DistanceUnitPreference
import dev.mahlernim.timelinevisualizer.render.LongTripCompression
import dev.mahlernim.timelinevisualizer.render.VideoQuality
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsViewModelTest {
    private lateinit var cameraPreferences: CameraSettingsPreferences
    private lateinit var distancePreferences: DistanceUnitPreferences
    private lateinit var filterPreferences: LocationFilterPreferences
    private lateinit var timelineDisplayPreferences: TimelineDisplayPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        cameraPreferences = CameraSettingsPreferences(context)
        distancePreferences = DistanceUnitPreferences(context)
        filterPreferences = LocationFilterPreferences(context)
        timelineDisplayPreferences = TimelineDisplayPreferences(context)
        clearPreferences()
    }

    @After
    fun tearDown() = clearPreferences()

    @Test
    fun updatesArePersistedAndExposedAsOneState() {
        val viewModel = viewModel()
        val camera = CameraSettings(
            cameraMovement = CameraMovement.DYNAMIC,
            longTripCompression = LongTripCompression.STRONG,
            videoQuality = VideoQuality.PORTRAIT,
        )

        viewModel.updateCamera(camera)
        viewModel.updateDistanceUnit(DistanceUnitPreference.MILES)
        viewModel.updateLocationFilter(LocationFilterMode.OFF)
        assertEquals(false, viewModel.state.value.hideDates)
        viewModel.updateHideDates(true)
        viewModel.updateSimplifyRouteDetail(true)
        viewModel.updateKeepPastRoutesVisible(true)
        viewModel.updatePastRouteOpacity(70)
        viewModel.updateZoomSmoothness(85)
        viewModel.updateRoutePointSpacing(RoutePointSpacing.METERS_250)

        assertEquals(
            SettingsState(
                camera,
                DistanceUnitPreference.MILES,
                LocationFilterMode.OFF,
                simplifyRouteDetail = true,
                keepPastRoutesVisible = true,
                hideDates = true,
                pastRouteOpacity = 70,
                zoomSmoothness = 85,
                routePointSpacing = RoutePointSpacing.METERS_250,
            ),
            viewModel.state.value,
        )
        assertEquals(viewModel.state.value, viewModel().state.value)
        assertEquals(true, viewModel().state.value.hideDates)
        assertEquals(camera, cameraPreferences.load())
        assertEquals(DistanceUnitPreference.MILES, distancePreferences.load())
        assertEquals(LocationFilterMode.OFF, filterPreferences.load())
        assertEquals(true, timelineDisplayPreferences.simplifyRouteDetail())
        assertEquals(true, timelineDisplayPreferences.keepPastRoutesVisible())
        assertEquals(70, timelineDisplayPreferences.pastRouteOpacity())
        assertEquals(85, timelineDisplayPreferences.zoomSmoothness())
        assertEquals(RoutePointSpacing.METERS_250, timelineDisplayPreferences.routePointSpacing())
    }

    @Test
    fun displayChoicesStartWithDefaultsAndStayInRange() {
        val viewModel = viewModel()

        assertEquals(CameraSettings.DEFAULT_ZOOM_SMOOTHNESS, viewModel.state.value.zoomSmoothness)
        assertEquals(CameraSettings.DEFAULT_PAST_ROUTE_OPACITY, viewModel.state.value.pastRouteOpacity)
        assertEquals(RoutePointSpacing.ALL_POINTS, viewModel.state.value.routePointSpacing)

        viewModel.updateZoomSmoothness(150)
        viewModel.updatePastRouteOpacity(0)

        assertEquals(CameraSettings.MAX_ZOOM_SMOOTHNESS, viewModel.state.value.zoomSmoothness)
        assertEquals(CameraSettings.MIN_PAST_ROUTE_OPACITY, viewModel.state.value.pastRouteOpacity)
    }

    @Test
    fun displayChoicesSurviveCameraResets() {
        val viewModel = viewModel()
        viewModel.updateKeepPastRoutesVisible(true)
        viewModel.updateZoomSmoothness(40)
        viewModel.updatePastRouteOpacity(90)

        viewModel.resetVideoDefaults()
        val draft = viewModel.state.value.withDisplayChoices(viewModel.state.value.camera)

        assertEquals(true, draft.keepPastRoutesVisible)
        assertEquals(40, draft.zoomSmoothness)
        assertEquals(90, draft.pastRouteOpacity)
        assertEquals(CameraSettings.DEFAULT.cameraMovement, draft.cameraMovement)
    }

    @Test
    fun resetPreservesRegionalAndTimelinePreferences() {
        val viewModel = viewModel()
        viewModel.updateCamera(CameraSettings.DEFAULT.copy(videoQuality = VideoQuality.ULTRA))
        viewModel.updateDistanceUnit(DistanceUnitPreference.MILES)
        viewModel.updateLocationFilter(LocationFilterMode.OFF)

        viewModel.resetVideoDefaults()

        assertEquals(CameraSettings.DEFAULT, viewModel.state.value.camera)
        assertEquals(DistanceUnitPreference.MILES, viewModel.state.value.distanceUnit)
        assertEquals(LocationFilterMode.OFF, viewModel.state.value.locationFilter)
    }

    private fun viewModel() = SettingsViewModel(
        cameraPreferences,
        distancePreferences,
        filterPreferences,
        timelineDisplayPreferences,
    )

    private fun clearPreferences() {
        cameraPreferences.reset()
        distancePreferences.clear()
        filterPreferences.reset()
        timelineDisplayPreferences.clear()
    }
}
