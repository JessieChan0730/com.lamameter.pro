package com.yourbrand.lumameter.pro.ui.meter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiObjects
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Fluorescent
import androidx.compose.material.icons.rounded.GridOff
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.yourbrand.lumameter.pro.R
import com.yourbrand.lumameter.pro.data.calibration.CalibrationBackupCodec
import com.yourbrand.lumameter.pro.domain.exposure.AnalysisTool
import com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset
import com.yourbrand.lumameter.pro.domain.exposure.ExposureMode
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.domain.exposure.ReferenceGridType
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import com.yourbrand.lumameter.pro.domain.exposure.WhiteBalanceCondition
import com.yourbrand.lumameter.pro.domain.exposure.WhiteBalanceReading
import com.yourbrand.lumameter.pro.ui.components.HistogramChart
import com.yourbrand.lumameter.pro.ui.components.MeterChoiceChip
import com.yourbrand.lumameter.pro.ui.components.MeterDialog
import com.yourbrand.lumameter.pro.ui.components.MeterPanel
import com.yourbrand.lumameter.pro.ui.components.MeterSnackbarHost
import com.yourbrand.lumameter.pro.ui.components.MeterSnackbarPosition
import com.yourbrand.lumameter.pro.ui.components.MeterSnackbarStatus
import com.yourbrand.lumameter.pro.ui.components.showMeterSnackbar
import com.yourbrand.lumameter.pro.ui.locale.AppLanguage
import com.yourbrand.lumameter.pro.ui.theme.AppColorTheme
import com.yourbrand.lumameter.pro.ui.theme.AppThemeMode
import com.yourbrand.lumameter.pro.ui.theme.MeterPalettePreview
import com.yourbrand.lumameter.pro.viewmodel.CalibrationImportStrategy
import com.yourbrand.lumameter.pro.viewmodel.MeterDefaults
import com.yourbrand.lumameter.pro.viewmodel.MeterStatus
import com.yourbrand.lumameter.pro.viewmodel.MeterUiState
import com.yourbrand.lumameter.pro.viewmodel.MeterViewModel
import com.yourbrand.lumameter.pro.viewmodel.PersistedMeterSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

private enum class MeterPage {
    MAIN,
    SETTINGS,
    APERTURE_LIBRARY,
    SHUTTER_LIBRARY,
    CALIBRATION,
}

private enum class MeterSheet {
    EXPOSURE_MODE,
    ADD_APERTURE,
    ADD_SHUTTER,
    ADD_CALIBRATION_PRESET,
}

private enum class ZoomControlMode {
    PRESETS,
    SLIDER,
}

private data class SelectorItemBounds(
    val offsetPx: Float,
    val widthPx: Int,
)

private data class SliderScaleStop(
    val value: Float,
    val label: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterRoute(
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    colorTheme: AppColorTheme,
    onColorThemeChanged: (AppColorTheme) -> Unit,
    language: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    liveMeteringEnabled: Boolean,
    onLiveMeteringEnabledChanged: (Boolean) -> Unit,
    guideGridEnabled: Boolean,
    onGuideGridEnabledChanged: (Boolean) -> Unit,
    referenceGridType: ReferenceGridType,
    onReferenceGridTypeChanged: (ReferenceGridType) -> Unit,
    histogramEnabled: Boolean,
    onHistogramEnabledChanged: (Boolean) -> Unit,
    levelIndicatorEnabled: Boolean,
    onLevelIndicatorEnabledChanged: (Boolean) -> Unit,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    onViewfinderAspectRatioChanged: (ViewfinderAspectRatio) -> Unit,
    initialSettings: PersistedMeterSettings = PersistedMeterSettings(),
    onSettingsChanged: ((PersistedMeterSettings) -> Unit)? = null,
    viewModel: MeterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MeterViewModel(
                    initialSettings = initialSettings,
                    onSettingsChanged = onSettingsChanged,
                ) as T
            }
        },
    ),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionRequiredMessage = stringResource(R.string.camera_permission_required)
    var hasCameraPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    var currentPage by rememberSaveable { mutableStateOf(MeterPage.MAIN) }
    var activeSheet by rememberSaveable { mutableStateOf<MeterSheet?>(null) }
    var editingPresetId by rememberSaveable { mutableStateOf<String?>(null) }
    var presetEditorOriginalOffset by rememberSaveable { mutableStateOf(0.0) }
    val normalizedReferenceGridType = remember(referenceGridType, viewfinderAspectRatio) {
        ReferenceGridType.normalizeForViewfinder(
            referenceGridType = referenceGridType,
            viewfinderAspectRatio = viewfinderAspectRatio,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            viewModel.onCameraError(permissionRequiredMessage)
        }
    }

    LaunchedEffect(liveMeteringEnabled) {
        viewModel.setLiveMeteringEnabled(liveMeteringEnabled)
    }

    BackHandler(enabled = currentPage != MeterPage.MAIN || activeSheet != null) {
        if (activeSheet != null) {
            activeSheet = null
        } else {
            currentPage = MeterPage.MAIN
        }
    }

    activeSheet?.let { sheet ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                if (sheet == MeterSheet.ADD_CALIBRATION_PRESET) {
                    viewModel.previewCalibrationOffset(presetEditorOriginalOffset.toFloat())
                    editingPresetId = null
                }
                activeSheet = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.imePadding(),
        ) {
            when (sheet) {
                MeterSheet.EXPOSURE_MODE -> ExposureModeSheet(
                    currentMode = uiState.exposureMode,
                    onSelect = {
                        viewModel.setExposureMode(it)
                        activeSheet = null
                    },
                )

                MeterSheet.ADD_APERTURE -> ValueInputSheet(
                    title = stringResource(R.string.add_aperture_value),
                    label = stringResource(R.string.aperture_value_input),
                    placeholder = "f/1.8",
                    supportingText = stringResource(R.string.aperture_input_hint),
                    invalidText = stringResource(R.string.aperture_input_invalid),
                    duplicateText = stringResource(R.string.value_already_exists),
                    outOfRangeText = stringResource(R.string.aperture_input_out_of_range),
                    keyboardType = KeyboardType.Decimal,
                    currentValues = uiState.apertureOptions,
                    parser = ::parseApertureInput,
                    validator = { it in 1.0..32.0 },
                    onSubmit = {
                        viewModel.addApertureOption(it)
                        activeSheet = null
                    },
                    onDismiss = { activeSheet = null },
                )

                MeterSheet.ADD_SHUTTER -> ValueInputSheet(
                    title = stringResource(R.string.add_shutter_value),
                    label = stringResource(R.string.shutter_value_input),
                    placeholder = "1/320",
                    supportingText = stringResource(R.string.shutter_input_hint),
                    invalidText = stringResource(R.string.shutter_input_invalid),
                    duplicateText = stringResource(R.string.value_already_exists),
                    outOfRangeText = stringResource(R.string.shutter_input_out_of_range),
                    keyboardType = KeyboardType.Text,
                    currentValues = uiState.shutterOptions,
                    parser = ::parseShutterInput,
                    validator = { it in MIN_SHUTTER_SECONDS..MAX_SHUTTER_SECONDS },
                    onSubmit = {
                        viewModel.addShutterOption(it)
                        activeSheet = null
                    },
                    onDismiss = { activeSheet = null },
                )

                MeterSheet.ADD_CALIBRATION_PRESET -> {
                    val editing = editingPresetId?.let { id ->
                        uiState.calibrationPresets.find { it.id == id }
                    }
                    CalibrationEditorSheet(
                        uiState = uiState,
                        editing = editing,
                        onOffsetPreview = viewModel::previewCalibrationOffset,
                        onSaveNew = { name, notes ->
                            viewModel.addCalibrationPreset(name, notes)
                            activeSheet = null
                            editingPresetId = null
                        },
                        onSaveEdit = { id, name, notes, offset ->
                            val wasActive = uiState.activeCalibrationPresetId == id
                            viewModel.updateCalibrationPreset(id, name, notes, offset)
                            if (!wasActive) {
                                viewModel.previewCalibrationOffset(presetEditorOriginalOffset.toFloat())
                            }
                            activeSheet = null
                            editingPresetId = null
                        },
                        onCancel = {
                            viewModel.previewCalibrationOffset(presetEditorOriginalOffset.toFloat())
                            activeSheet = null
                            editingPresetId = null
                        },
                    )
                }
            }
        }
    }

    AnimatedContent(
        targetState = currentPage,
        label = "meter_page",
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                slideInHorizontally(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                ) { it / 4 } + fadeIn(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                ) { -it / 5 } + fadeOut(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                )
            } else {
                slideInHorizontally(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                ) { -it / 4 } + fadeIn(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                ) { it / 5 } + fadeOut(
                    animationSpec = tween(durationMillis = PAGE_TRANSITION_DURATION_MILLIS),
                )
            }
        },
    ) { page ->
        when (page) {
            MeterPage.MAIN -> MeterMainPage(
                uiState = uiState,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onReadingAvailable = viewModel::onFrameAnalyzed,
                onCameraError = viewModel::onCameraError,
                onAnalysisToolSelected = viewModel::setAnalysisTool,
                onMeteringModeSelected = viewModel::setMeteringMode,
                onMeteringPointChanged = viewModel::setMeteringPoint,
                onZoomCapabilityResolved = viewModel::updateZoomCapability,
                onZoomRatioChanged = viewModel::setZoomRatio,
                onIsoSelected = viewModel::setIso,
                onNdFilterSelected = viewModel::setNdFilter,
                onCompensationChanged = viewModel::setCompensation,
                onAeLockToggled = viewModel::toggleAeLock,
                referenceGridType = normalizedReferenceGridType.takeIf { guideGridEnabled },
                showHistogram = histogramEnabled,
                showLevelIndicator = levelIndicatorEnabled,
                viewfinderAspectRatio = viewfinderAspectRatio,
                onPreviewTapped = viewModel::requestManualMetering,
                onOpenModeSheet = { activeSheet = MeterSheet.EXPOSURE_MODE },
                onOpenCalibration = { currentPage = MeterPage.CALIBRATION },
                onOpenApertureLibrary = {
                    viewModel.setExposureMode(ExposureMode.APERTURE_PRIORITY)
                    currentPage = MeterPage.APERTURE_LIBRARY
                },
                onOpenShutterLibrary = {
                    viewModel.setExposureMode(ExposureMode.SHUTTER_PRIORITY)
                    currentPage = MeterPage.SHUTTER_LIBRARY
                },
                onOpenSettings = { currentPage = MeterPage.SETTINGS },
            )

            MeterPage.SETTINGS -> SettingsPage(
                themeMode = themeMode,
                onThemeModeChanged = onThemeModeChanged,
                colorTheme = colorTheme,
                onColorThemeChanged = onColorThemeChanged,
                language = language,
                onLanguageChanged = { selectedLanguage ->
                    if (selectedLanguage != language) {
                        onLanguageChanged(selectedLanguage)
                    }
                },
                liveMeteringEnabled = liveMeteringEnabled,
                onLiveMeteringEnabledChanged = onLiveMeteringEnabledChanged,
                guideGridEnabled = guideGridEnabled,
                onGuideGridEnabledChanged = onGuideGridEnabledChanged,
                referenceGridType = normalizedReferenceGridType,
                onReferenceGridTypeChanged = onReferenceGridTypeChanged,
                histogramEnabled = histogramEnabled,
                onHistogramEnabledChanged = onHistogramEnabledChanged,
                levelIndicatorEnabled = levelIndicatorEnabled,
                onLevelIndicatorEnabledChanged = onLevelIndicatorEnabledChanged,
                viewfinderAspectRatio = viewfinderAspectRatio,
                onViewfinderAspectRatioChanged = onViewfinderAspectRatioChanged,
                onBack = { currentPage = MeterPage.MAIN },
            )

            MeterPage.APERTURE_LIBRARY -> ValueLibraryPage(
                title = stringResource(R.string.aperture_library_title),
                description = stringResource(R.string.aperture_library_description),
                values = uiState.apertureOptions,
                selectedValue = uiState.selectedAperture,
                addButtonLabel = stringResource(R.string.add_aperture_value),
                onBack = { currentPage = MeterPage.MAIN },
                onSelect = {
                    viewModel.setExposureMode(ExposureMode.APERTURE_PRIORITY)
                    viewModel.setAperture(it)
                    currentPage = MeterPage.MAIN
                },
                onDelete = viewModel::removeApertureOption,
                onAdd = { activeSheet = MeterSheet.ADD_APERTURE },
                canDelete = { candidate ->
                    MeterDefaults.apertureValues.none { valuesEqual(it, candidate) }
                },
                valueFormatter = ::formatAperture,
            )

            MeterPage.SHUTTER_LIBRARY -> ValueLibraryPage(
                title = stringResource(R.string.shutter_library_title),
                description = stringResource(R.string.shutter_library_description),
                values = uiState.shutterOptions,
                selectedValue = uiState.selectedShutterSeconds,
                addButtonLabel = stringResource(R.string.add_shutter_value),
                onBack = { currentPage = MeterPage.MAIN },
                onSelect = {
                    viewModel.setExposureMode(ExposureMode.SHUTTER_PRIORITY)
                    viewModel.setShutterSeconds(it)
                    currentPage = MeterPage.MAIN
                },
                onDelete = viewModel::removeShutterOption,
                onAdd = { activeSheet = MeterSheet.ADD_SHUTTER },
                canDelete = { candidate ->
                    MeterDefaults.shutterValues.none { valuesEqual(it, candidate) }
                },
                valueFormatter = ::formatShutter,
            )

            MeterPage.CALIBRATION -> CalibrationPage(
                uiState = uiState,
                onBack = { currentPage = MeterPage.MAIN },
                onSelectPreset = viewModel::selectCalibrationPreset,
                onDeletePreset = viewModel::deleteCalibrationPreset,
                onAddPreset = {
                    presetEditorOriginalOffset = uiState.calibrationOffsetEv
                    editingPresetId = null
                    activeSheet = MeterSheet.ADD_CALIBRATION_PRESET
                },
                onEditPreset = { preset ->
                    presetEditorOriginalOffset = uiState.calibrationOffsetEv
                    editingPresetId = preset.id
                    viewModel.previewCalibrationOffset(preset.offsetEv.toFloat())
                    activeSheet = MeterSheet.ADD_CALIBRATION_PRESET
                },
                onImportPresets = viewModel::importCalibrationPresets,
            )
        }
    }
}

@Composable
private fun MeterMainPage(
    uiState: MeterUiState,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onCameraError: (String) -> Unit,
    onAnalysisToolSelected: (AnalysisTool) -> Unit,
    onMeteringModeSelected: (MeteringMode) -> Unit,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onZoomCapabilityResolved: (Float, Float) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    onIsoSelected: (Int) -> Unit,
    onNdFilterSelected: (Int) -> Unit,
    onCompensationChanged: (Float) -> Unit,
    onAeLockToggled: () -> Unit,
    referenceGridType: ReferenceGridType?,
    showHistogram: Boolean,
    showLevelIndicator: Boolean,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    onPreviewTapped: () -> Unit,
    onOpenModeSheet: () -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenApertureLibrary: () -> Unit,
    onOpenShutterLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var viewportTopPx by remember { mutableStateOf<Float?>(null) }
    var statusRowBottomPx by remember { mutableStateOf<Float?>(null) }
    var isToolDrawerOpen by rememberSaveable { mutableStateOf(false) }
    val showPinnedSummary by remember(hasCameraPermission, viewportTopPx, statusRowBottomPx) {
        derivedStateOf {
            shouldShowPinnedSummary(
                hasCameraPermission = hasCameraPermission,
                viewportTopPx = viewportTopPx,
                statusRowBottomPx = statusRowBottomPx,
            )
        }
    }
    BackHandler(enabled = isToolDrawerOpen) {
        isToolDrawerOpen = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background,
                    ),
                )
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    viewportTopPx = coordinates.positionInWindow().y
                }
                .safeDrawingPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MainHeader(
                    uiState = uiState,
                    isToolDrawerOpen = isToolDrawerOpen,
                    onToggleToolDrawer = { isToolDrawerOpen = !isToolDrawerOpen },
                    onOpenModeSheet = onOpenModeSheet,
                    onOpenSettings = onOpenSettings,
                )
            }

            if (hasCameraPermission) {
                item {
                    MeterPanel(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            StatusRow(
                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                    statusRowBottomPx =
                                        coordinates.positionInWindow().y + coordinates.size.height
                                },
                                uiState = uiState,
                            )
                            if (uiState.analysisTool == AnalysisTool.METER) {
                                MeteringTabs(
                                    currentMode = uiState.meteringMode,
                                    onMeteringModeSelected = onMeteringModeSelected,
                                )
                            }
                            PreviewSection(
                                uiState = uiState,
                                onReadingAvailable = onReadingAvailable,
                                onCameraError = onCameraError,
                                onMeteringPointChanged = onMeteringPointChanged,
                                onZoomCapabilityResolved = onZoomCapabilityResolved,
                                onZoomRatioChanged = onZoomRatioChanged,
                                referenceGridType = referenceGridType,
                                showHistogram = showHistogram,
                                showLevelIndicator = showLevelIndicator,
                                viewfinderAspectRatio = viewfinderAspectRatio,
                                onPreviewTapped = onPreviewTapped,
                            )
                            if (uiState.analysisTool == AnalysisTool.METER) {
                                ExposureSummaryRow(
                                    uiState = uiState,
                                    onOpenApertureLibrary = onOpenApertureLibrary,
                                    onOpenShutterLibrary = onOpenShutterLibrary,
                                )
                                ExposureAdjustmentRow(
                                    uiState = uiState,
                                    onAeLockToggled = onAeLockToggled,
                                    onCompensationChanged = onCompensationChanged,
                                    onOpenCalibration = onOpenCalibration,
                                )
                                IsoSelector(
                                    values = uiState.isoOptions,
                                    selectedValue = uiState.selectedIso,
                                    onIsoSelected = onIsoSelected,
                                )
                                NdFilterSelector(
                                    values = uiState.ndFilterOptions,
                                    selectedValue = uiState.selectedNdFilter,
                                    onNdFilterSelected = onNdFilterSelected,
                                )
                            } else {
                                WhiteBalanceSection(uiState = uiState)
                            }
                        }
                    }
                }
            } else {
                item {
                    MeterPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(fraction = 0.92f),
                    ) {
                        PermissionEmptyState(
                            modifier = Modifier.fillMaxSize(),
                            onRequestPermission = onRequestPermission,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showPinnedSummary && uiState.analysisTool == AnalysisTool.METER,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            enter = slideInVertically { height -> -height / 2 } +
                fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutVertically { height -> -height / 2 } +
                fadeOut(animationSpec = tween(durationMillis = 140)),
        ) {
            PinnedMeterSummaryBar(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
            )
        }

        AnimatedVisibility(
            visible = isToolDrawerOpen,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 140)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.20f))
                    .pointerInput(Unit) {
                        detectTapGestures { isToolDrawerOpen = false }
                    },
            )
        }

        AnimatedVisibility(
            visible = isToolDrawerOpen,
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(start = 16.dp, top = 12.dp),
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                initialOffsetX = { -it },
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                targetOffsetX = { -it / 3 },
            ) + fadeOut(animationSpec = tween(durationMillis = 140)),
        ) {
            AnalysisToolDrawer(
                currentTool = uiState.analysisTool,
                onToolSelected = { tool ->
                    onAnalysisToolSelected(tool)
                    isToolDrawerOpen = false
                },
            )
        }
    }
}

@Composable
private fun MainHeader(
    uiState: MeterUiState,
    isToolDrawerOpen: Boolean,
    onToggleToolDrawer: () -> Unit,
    onOpenModeSheet: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val whiteBalanceReading = uiState.liveReading?.whiteBalanceReading

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolDrawerButton(
                isOpen = isToolDrawerOpen,
                onClick = onToggleToolDrawer,
            )

            if (uiState.analysisTool == AnalysisTool.METER) {
                MeterChoiceChip(
                    label = exposureModeLabel(uiState.exposureMode),
                    selected = true,
                    trailingIcon = Icons.Rounded.ArrowDropDown,
                    onClick = onOpenModeSheet,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isWhiteBalanceTool = uiState.analysisTool == AnalysisTool.WHITE_BALANCE

            Surface(
                modifier = if (isWhiteBalanceTool) {
                    Modifier.width(86.dp)
                } else {
                    Modifier
                },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 3.dp,
            ) {
                Column(
                    modifier = if (isWhiteBalanceTool) {
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    } else {
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    },
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = if (uiState.analysisTool == AnalysisTool.METER) {
                            stringResource(R.string.live_ev_short)
                        } else {
                            stringResource(R.string.white_balance_kelvin_short)
                        },
                        modifier = if (isWhiteBalanceTool) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.offset(x = 2.dp)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = if (isWhiteBalanceTool) TextAlign.End else TextAlign.Start,
                    )
                    if (uiState.analysisTool == AnalysisTool.METER) {
                        Text(
                            text = formatEv(uiState.exposureResult.sceneEv100),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Text(
                            text = whiteBalanceReading?.let { formatKelvin(it.kelvin) } ?: "--",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 3.dp,
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.open_settings),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    modifier: Modifier = Modifier,
    uiState: MeterUiState,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mainStatusLabel(uiState),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (uiState.analysisTool == AnalysisTool.METER) {
            val reading = uiState.liveReading
            if (reading != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeterChoiceChip(
                        label = "L ${formatLuma(reading.meteredLuma)}",
                        shape = RoundedCornerShape(999.dp),
                    )
                    MeterChoiceChip(
                        label = "AVG ${formatLuma(reading.averageLuma)}",
                        shape = RoundedCornerShape(999.dp),
                    )
                }
            }
        } else {
            uiState.liveReading?.whiteBalanceReading?.let { reading ->
                WhiteBalanceBiasChip(reading = reading)
            }
        }
    }
}

@Composable
private fun PinnedMeterSummaryBar(
    uiState: MeterUiState,
    modifier: Modifier = Modifier,
) {
    val reading = uiState.liveReading

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PinnedSummaryBadge(text = exposureModeLabel(uiState.exposureMode))
            PinnedSummaryBadge(
                text = meteringModeLabel(uiState.meteringMode),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            PinnedSummaryMetric(
                label = stringResource(R.string.live_ev_short),
                value = formatEv(uiState.exposureResult.sceneEv100),
            )
            PinnedSummaryMetric(
                label = "L",
                value = reading?.let { formatLuma(it.meteredLuma) } ?: "--",
            )
            PinnedSummaryMetric(
                label = "AVG",
                value = reading?.let { formatLuma(it.averageLuma) } ?: "--",
            )
        }
    }
}

@Composable
private fun PinnedSummaryBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun PinnedSummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun ToolDrawerButton(
    isOpen: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (isOpen) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        },
        tonalElevation = 3.dp,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = stringResource(R.string.open_tool_drawer),
                tint = if (isOpen) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun AnalysisToolDrawer(
    currentTool: AnalysisTool,
    onToolSelected: (AnalysisTool) -> Unit,
) {
    MeterPanel(
        modifier = Modifier.width(228.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tool_drawer_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.tool_drawer_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnalysisTool.entries.forEach { tool ->
                    val selected = currentTool == tool
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToolSelected(tool) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
                        },
                        tonalElevation = if (selected) 2.dp else 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = analysisToolIcon(
                                    tool = tool,
                                ),
                                contentDescription = null,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Text(
                                text = analysisToolLabel(tool),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeteringTabs(
    currentMode: MeteringMode,
    onMeteringModeSelected: (MeteringMode) -> Unit,
) {
    val density = LocalDensity.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            val modes = MeteringMode.entries
            val spacing = 4.dp
            val tabHeight = 48.dp
            val tabWidth = (maxWidth - spacing * (modes.size - 1)) / modes.size
            val selectedIndex = modes.indexOf(currentMode).coerceAtLeast(0)
            val indicatorOffsetPx by animateFloatAsState(
                targetValue = with(density) { ((tabWidth + spacing) * selectedIndex).toPx() },
                animationSpec = tween(
                    durationMillis = 180,
                    easing = FastOutSlowInEasing,
                ),
                label = "metering_tab_indicator_offset_px",
            )

            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = indicatorOffsetPx }
                    .width(tabWidth)
                    .height(tabHeight),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                modes.forEach { mode ->
                    val selected = currentMode == mode
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(durationMillis = 180),
                        label = "metering_tab_content",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(tabHeight)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onMeteringModeSelected(mode) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = meteringModeLabel(mode),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            ),
                            color = contentColor,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewSection(
    uiState: MeterUiState,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onCameraError: (String) -> Unit,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onZoomCapabilityResolved: (Float, Float) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    referenceGridType: ReferenceGridType?,
    showHistogram: Boolean,
    showLevelIndicator: Boolean,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    onPreviewTapped: () -> Unit,
) {
    var zoomControlMode by rememberSaveable(uiState.isZoomSupported) {
        mutableStateOf(ZoomControlMode.PRESETS)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PreviewCard(
            uiState = uiState,
            onReadingAvailable = onReadingAvailable,
            onCameraError = onCameraError,
            onMeteringPointChanged = onMeteringPointChanged,
            onZoomCapabilityResolved = onZoomCapabilityResolved,
            onZoomRatioChanged = onZoomRatioChanged,
            referenceGridType = referenceGridType,
            showHistogram = showHistogram,
            showLevelIndicator = showLevelIndicator,
            viewfinderAspectRatio = viewfinderAspectRatio,
            onPreviewTapped = onPreviewTapped,
        )

        if (uiState.isZoomSupported) {
            ZoomControlSection(
                uiState = uiState,
                controlMode = zoomControlMode,
                onZoomRatioChanged = onZoomRatioChanged,
                onRequestSliderMode = { zoomControlMode = ZoomControlMode.SLIDER },
                onRequestPresetMode = { zoomControlMode = ZoomControlMode.PRESETS },
            )
        }
    }
}

@Composable
private fun WhiteBalanceSection(
    uiState: MeterUiState,
) {
    val reading = uiState.liveReading?.whiteBalanceReading

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            WhiteBalanceConditionTrack(
                modifier = Modifier.fillMaxWidth(),
                reading = reading,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (reading == null) {
                Text(
                    text = stringResource(R.string.white_balance_placeholder_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val icon = whiteBalanceConditionIcon(reading.condition) ?: Icons.Rounded.WbSunny

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = whiteBalanceConditionLabel(reading.condition),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(
                                    R.string.white_balance_condition_reference,
                                    formatKelvin(reading.condition.referenceKelvin),
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Text(
                        text = whiteBalanceConditionDescription(reading.condition),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun WhiteBalanceConditionTrack(
    reading: WhiteBalanceReading?,
    modifier: Modifier = Modifier,
) {
    val selectedCondition = reading?.condition ?: WhiteBalanceCondition.SUNLIGHT
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.padding(start = 4.dp, top = 6.dp, end = 4.dp, bottom = 0.dp),
    ) {
        val conditions = WhiteBalanceCondition.entries
        val itemCount = conditions.size
        val itemGap = 8.dp
        val totalGap = itemGap * (itemCount - 1)
        val markerWidth = ((maxWidth - totalGap) / itemCount).coerceIn(60.dp, 80.dp)
        val edgePadding = ((maxWidth - markerWidth) / 2f).coerceAtLeast(0.dp)
        val listState = rememberLazyListState()
        val selectedIndex = remember(selectedCondition) {
            conditions.indexOf(selectedCondition).coerceAtLeast(0)
        }
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val markerWidthPx = with(density) { markerWidth.toPx() }
        val itemGapPx = with(density) { itemGap.toPx() }
        val selectedTrackCenterPx = remember(selectedIndex, markerWidthPx, itemGapPx) {
            whiteBalanceTrackCenterPx(
                index = selectedIndex,
                markerWidthPx = markerWidthPx,
                itemGapPx = itemGapPx,
            )
        }

        LaunchedEffect(selectedIndex, maxWidth, markerWidth) {
            listState.animateScrollToItem(index = selectedIndex)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clipToBounds(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(2.dp)
                    .height(8.dp)
                    .padding(bottom = 2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.68f),
                        shape = CircleShape,
                    ),
            )
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(),
                state = listState,
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(itemGap),
                contentPadding = PaddingValues(
                    start = edgePadding,
                    top = 6.dp,
                    end = edgePadding,
                    bottom = 8.dp,
                ),
            ) {
                itemsIndexed(
                    items = conditions,
                    key = { _, condition -> condition.name },
                ) { index, condition ->
                    val emphasis = whiteBalanceTrackEmphasis(
                        itemCenterPx = whiteBalanceTrackCenterPx(
                            index = index,
                            markerWidthPx = markerWidthPx,
                            itemGapPx = itemGapPx,
                        ),
                        currentPositionPx = selectedTrackCenterPx,
                        containerWidthPx = containerWidthPx,
                    )
                    WhiteBalanceConditionTrackItem(
                        modifier = Modifier.width(markerWidth),
                        condition = condition,
                        isCurrentCondition = reading?.condition == condition,
                        emphasis = emphasis,
                    )
                }
            }
        }
    }
}

@Composable
private fun WhiteBalanceConditionTrackItem(
    condition: WhiteBalanceCondition,
    isCurrentCondition: Boolean,
    emphasis: Float,
    modifier: Modifier = Modifier,
) {
    val animatedEmphasis by animateFloatAsState(
        targetValue = emphasis,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "whiteBalanceConditionEmphasis",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isCurrentCondition) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f + animatedEmphasis * 0.5f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f + animatedEmphasis * 0.7f)
        },
        label = "whiteBalanceConditionContent",
    )
    val scale by animateFloatAsState(
        targetValue = 0.86f + animatedEmphasis * 0.14f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "whiteBalanceConditionScale",
    )

    Column(
        modifier = modifier.graphicsLayer {
            alpha = 0.22f + animatedEmphasis * 0.78f
            scaleX = scale
            scaleY = scale
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = whiteBalanceConditionIcon(condition) ?: Icons.Rounded.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(if (isCurrentCondition) 28.dp else 24.dp),
            tint = contentColor,
        )
        Text(
            text = whiteBalanceConditionLabel(condition),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrentCondition) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WhiteBalanceBiasChip(
    reading: WhiteBalanceReading,
    modifier: Modifier = Modifier,
) {
    val bias = whiteBalanceBias(reading)
    val accentColor = whiteBalanceBiasColor(bias.direction)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatSignedKelvin(bias.deltaKelvin),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = accentColor,
                        shape = CircleShape,
                    ),
            )
            Text(
                text = whiteBalanceBiasLabel(bias.direction),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PreviewCard(
    uiState: MeterUiState,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onCameraError: (String) -> Unit,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onZoomCapabilityResolved: (Float, Float) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    referenceGridType: ReferenceGridType?,
    showHistogram: Boolean,
    showLevelIndicator: Boolean,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    onPreviewTapped: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.08f),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(viewfinderAspectRatio.ratio),
        ) {
            MeterCameraPreview(
                modifier = Modifier.fillMaxSize(),
                meteringMode = uiState.meteringMode,
                meteringPoint = uiState.meteringPoint,
                hasCustomSpotMeteringPoint = uiState.hasCustomSpotMeteringPoint,
                viewfinderAspectRatio = viewfinderAspectRatio,
                isAeLocked = uiState.isAeLocked,
                requestedZoomRatio = uiState.zoomRatio,
                enableSpotMetering = uiState.analysisTool == AnalysisTool.METER,
                showMeteringReticle = uiState.analysisTool == AnalysisTool.METER,
                referenceGridType = referenceGridType,
                showLevelIndicator = showLevelIndicator,
                onMeteringPointChanged = onMeteringPointChanged,
                onPreviewTapped = onPreviewTapped,
                onReadingAvailable = onReadingAvailable,
                onZoomCapabilityResolved = onZoomCapabilityResolved,
                onZoomRatioApplied = onZoomRatioChanged,
                onCameraError = onCameraError,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.42f),
                            ),
                        )
                    ),
            )

            if (showHistogram) {
                val histogram = uiState.liveReading?.histogram
                if (histogram != null) {
                    HistogramChart(
                        histogram = histogram,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(
                                width = (maxWidth * 0.34f)
                                    .coerceAtLeast(92.dp)
                                    .coerceAtMost(120.dp),
                                height = (maxHeight * 0.22f)
                                    .coerceAtLeast(56.dp)
                                    .coerceAtMost(72.dp),
                            ),
                    )
                }
            }
            val previewTapHint = resolvePreviewTapHint(
                analysisTool = uiState.analysisTool,
                meteringMode = uiState.meteringMode,
                isLiveMeteringEnabled = uiState.isLiveMeteringEnabled,
                hasCustomSpotMeteringPoint = uiState.hasCustomSpotMeteringPoint,
            )
            previewTapHint?.let { hint ->
                Text(
                    text = when (hint) {
                        PreviewTapHint.TAP_TO_METER -> stringResource(R.string.tap_to_meter)
                        PreviewTapHint.TAP_TO_METER_ONCE -> stringResource(R.string.tap_to_meter_once)
                        PreviewTapHint.TAP_TO_REPOSITION_METER -> stringResource(R.string.tap_to_reposition_meter)
                        PreviewTapHint.TAP_TO_SAMPLE_WHITE_BALANCE -> stringResource(R.string.tap_to_sample_white_balance)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.72f)
                        .padding(14.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ZoomControlSection(
    uiState: MeterUiState,
    controlMode: ZoomControlMode,
    onZoomRatioChanged: (Float) -> Unit,
    onRequestSliderMode: () -> Unit,
    onRequestPresetMode: () -> Unit,
) {
    val toggleDescription = if (controlMode == ZoomControlMode.PRESETS) {
        stringResource(R.string.switch_zoom_to_slider)
    } else {
        stringResource(R.string.switch_zoom_to_buttons)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.zoom_control),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(
                        onClick = {
                            if (controlMode == ZoomControlMode.PRESETS) {
                                onRequestSliderMode()
                            } else {
                                onRequestPresetMode()
                            }
                        },
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = toggleDescription,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = formatZoomRatio(uiState.zoomRatio),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            AnimatedContent(
                targetState = controlMode,
                transitionSpec = {
                    slideInVertically { height -> height / 3 } + fadeIn() togetherWith
                        slideOutVertically { height -> -height / 3 } + fadeOut()
                },
                label = "zoom_control_mode",
            ) { targetMode ->
                when (targetMode) {
                    ZoomControlMode.PRESETS -> ZoomPresetButtons(
                        uiState = uiState,
                        onZoomRatioChanged = onZoomRatioChanged,
                        onRequestSliderMode = onRequestSliderMode,
                    )

                    ZoomControlMode.SLIDER -> ZoomSliderPanel(
                        uiState = uiState,
                        onZoomRatioChanged = onZoomRatioChanged,
                        onRequestPresetMode = onRequestPresetMode,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverflowAwareSingleLineRow(
    modifier: Modifier = Modifier,
    spacing: Dp,
    content: @Composable RowScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    var containerWidthPx by remember { mutableStateOf(0) }
    var contentWidthPx by remember { mutableStateOf(0) }
    val isOverflowing = containerWidthPx > 0 && contentWidthPx > containerWidthPx

    LaunchedEffect(isOverflowing) {
        if (!isOverflowing && scrollState.value != 0) {
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                val nextWidth = coordinates.size.width
                if (containerWidthPx != nextWidth) {
                    containerWidthPx = nextWidth
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    state = scrollState,
                    enabled = isOverflowing,
                ),
            contentAlignment = if (isOverflowing) Alignment.CenterStart else Alignment.Center,
        ) {
            Row(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    val nextWidth = coordinates.size.width
                    if (contentWidthPx != nextWidth) {
                        contentWidthPx = nextWidth
                    }
                },
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
private fun ZoomPresetButtons(
    uiState: MeterUiState,
    onZoomRatioChanged: (Float) -> Unit,
    onRequestSliderMode: () -> Unit,
) {
    OverflowAwareSingleLineRow(
        spacing = 8.dp,
    ) {
        uiState.zoomPresets.forEach { preset ->
            MeterChoiceChip(
                label = formatZoomRatio(preset.ratio),
                modifier = Modifier.graphicsLayer {
                    alpha = if (preset.enabled) 1f else 0.42f
                },
                selected = preset.selected,
                selectedShadowElevation = 0.dp,
                onClick = if (preset.enabled) {
                    {
                        if (preset.selected) {
                            onRequestSliderMode()
                        } else {
                            onZoomRatioChanged(preset.ratio)
                        }
                    }
                } else {
                    null
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZoomSliderPanel(
    uiState: MeterUiState,
    onZoomRatioChanged: (Float) -> Unit,
    onRequestPresetMode: () -> Unit,
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    )
    val scaleStops = remember(
        uiState.minZoomRatio,
        uiState.maxZoomRatio,
        uiState.zoomPresets,
    ) {
        buildZoomScaleStops(uiState)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .observeZoomSliderTap(onTap = onRequestPresetMode),
        ) {
            Slider(
                value = uiState.zoomRatio,
                onValueChange = onZoomRatioChanged,
                valueRange = uiState.minZoomRatio..uiState.maxZoomRatio,
                colors = sliderColors,
                track = { sliderState ->
                    CompactSliderTrack(
                        sliderState = sliderState,
                        colors = sliderColors,
                    )
                },
            )
        }
        SliderScale(
            stops = scaleStops,
            valueRange = uiState.minZoomRatio..uiState.maxZoomRatio,
            labelWidth = 38.dp,
        )
    }
}

@Composable
private fun ExposureSummaryRow(
    uiState: MeterUiState,
    onOpenApertureLibrary: () -> Unit,
    onOpenShutterLibrary: () -> Unit,
) {
    val isApertureAdjustable = uiState.exposureMode == ExposureMode.APERTURE_PRIORITY
    val isShutterAdjustable = uiState.exposureMode == ExposureMode.SHUTTER_PRIORITY

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposureSummaryItem(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_aperture),
                value = formatAperture(uiState.exposureResult.aperture),
                emphasized = isApertureAdjustable,
                locked = !isApertureAdjustable,
                enabled = isApertureAdjustable,
                onClick = onOpenApertureLibrary,
            )
            MetricDivider()
            ExposureSummaryItem(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_shutter),
                value = formatShutter(uiState.exposureResult.shutterSeconds),
                emphasized = isShutterAdjustable,
                locked = !isShutterAdjustable,
                enabled = isShutterAdjustable,
                onClick = onOpenShutterLibrary,
            )
            MetricDivider()
            ExposureSummaryItem(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_iso),
                value = stringResource(R.string.iso_inline_value, uiState.exposureResult.iso),
                emphasized = true,
                locked = false,
                enabled = false,
                onClick = {},
            )
        }
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    )
}

@Composable
private fun ExposureSummaryItem(
    label: String,
    value: String,
    emphasized: Boolean,
    locked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = Color.Transparent,
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (locked) {
                    InlineMetricIcon(
                        imageVector = Icons.Rounded.Lock,
                        tint = labelColor,
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun InlineMetricIcon(
    imageVector: ImageVector,
    tint: Color,
) {
    Icon(
        modifier = Modifier.size(12.dp),
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposureAdjustmentRow(
    uiState: MeterUiState,
    onAeLockToggled: () -> Unit,
    onCompensationChanged: (Float) -> Unit,
    onOpenCalibration: () -> Unit,
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    )
    val compensationScaleStops = remember {
        (-3..3).map { SliderScaleStop(value = it.toFloat()) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (uiState.isLiveMeteringEnabled) {
                Surface(
                    modifier = Modifier.weight(0.32f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (uiState.isAeLocked) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    },
                    onClick = onAeLockToggled,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = if (uiState.isAeLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = null,
                            tint = if (uiState.isAeLocked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = stringResource(R.string.ae_lock_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.isAeLocked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = if (uiState.isAeLocked) {
                                stringResource(R.string.state_on)
                            } else {
                                stringResource(R.string.state_off)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.isAeLocked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                )
            }

            Column(
                modifier = Modifier
                    .weight(if (uiState.isLiveMeteringEnabled) 0.68f else 1f)
                    .padding(end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.exposure_compensation_tight),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = formatSignedEv(uiState.compensationEv),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TextButton(
                        onClick = onOpenCalibration,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.calibration_short),
                            maxLines = 1,
                        )
                    }
                }
                Slider(
                    value = uiState.compensationEv.toFloat(),
                    onValueChange = onCompensationChanged,
                    valueRange = -3f..3f,
                    steps = 19,
                    colors = sliderColors,
                    track = { sliderState ->
                        CompactSliderTrack(
                            sliderState = sliderState,
                            colors = sliderColors,
                        )
                    },
                )
                SliderScale(
                    stops = compensationScaleStops,
                    valueRange = -3f..3f,
                    tickHeight = 5.dp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "-3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.calibration_chip,
                            formatSignedEv(uiState.calibrationOffsetEv),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = "+3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IsoSelector(
    values: List<Int>,
    selectedValue: Int,
    onIsoSelected: (Int) -> Unit,
) {
    val chipBounds = remember(values) { mutableStateMapOf<Int, SelectorItemBounds>() }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val selectedBounds = chipBounds[selectedValue]
    val indicatorOffsetPx by animateFloatAsState(
        targetValue = selectedBounds?.offsetPx ?: 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing,
        ),
        label = "iso_selector_indicator_offset_px",
    )
    val indicatorWidthPx by animateFloatAsState(
        targetValue = selectedBounds?.widthPx?.toFloat() ?: 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing,
        ),
        label = "iso_selector_indicator_width_px",
    )
    val indicatorWidth = with(density) { indicatorWidthPx.toDp() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_iso),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.iso_value, selectedValue),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                if (selectedBounds != null && indicatorWidthPx > 0f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { translationX = indicatorOffsetPx }
                            .width(indicatorWidth)
                            .height(40.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    values.forEach { iso ->
                        val selected = selectedValue == iso
                        val contentColor by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = tween(durationMillis = 180),
                            label = "iso_selector_content",
                        )

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onIsoSelected(iso) }
                                .onGloballyPositioned { coordinates ->
                                    val nextBounds = SelectorItemBounds(
                                        offsetPx = coordinates.positionInParent().x,
                                        widthPx = coordinates.size.width,
                                    )
                                    if (chipBounds[iso] != nextBounds) {
                                        chipBounds[iso] = nextBounds
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = iso.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                ),
                                color = contentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NdFilterSelector(
    values: List<Int>,
    selectedValue: Int,
    onNdFilterSelected: (Int) -> Unit,
) {
    val chipBounds = remember(values) { mutableStateMapOf<Int, SelectorItemBounds>() }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val selectedBounds = chipBounds[selectedValue]
    val indicatorOffsetPx by animateFloatAsState(
        targetValue = selectedBounds?.offsetPx ?: 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing,
        ),
        label = "nd_selector_indicator_offset_px",
    )
    val indicatorWidthPx by animateFloatAsState(
        targetValue = selectedBounds?.widthPx?.toFloat() ?: 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing,
        ),
        label = "nd_selector_indicator_width_px",
    )
    val indicatorWidth = with(density) { indicatorWidthPx.toDp() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_nd_filter),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (selectedValue <= 1) {
                    stringResource(R.string.nd_filter_none)
                } else {
                    stringResource(R.string.nd_filter_value, selectedValue)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                if (selectedBounds != null && indicatorWidthPx > 0f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { translationX = indicatorOffsetPx }
                            .width(indicatorWidth)
                            .height(40.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    values.forEach { factor ->
                        val selected = selectedValue == factor
                        val contentColor by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = tween(durationMillis = 180),
                            label = "nd_selector_content",
                        )

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNdFilterSelected(factor) }
                                .onGloballyPositioned { coordinates ->
                                    val nextBounds = SelectorItemBounds(
                                        offsetPx = coordinates.positionInParent().x,
                                        widthPx = coordinates.size.width,
                                    )
                                    if (chipBounds[factor] != nextBounds) {
                                        chipBounds[factor] = nextBounds
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (factor <= 1) {
                                    stringResource(R.string.nd_filter_none)
                                } else {
                                    stringResource(R.string.nd_filter_value, factor)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                ),
                                color = contentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    colorTheme: AppColorTheme,
    onColorThemeChanged: (AppColorTheme) -> Unit,
    language: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    liveMeteringEnabled: Boolean,
    onLiveMeteringEnabledChanged: (Boolean) -> Unit,
    guideGridEnabled: Boolean,
    onGuideGridEnabledChanged: (Boolean) -> Unit,
    referenceGridType: ReferenceGridType,
    onReferenceGridTypeChanged: (ReferenceGridType) -> Unit,
    histogramEnabled: Boolean,
    onHistogramEnabledChanged: (Boolean) -> Unit,
    levelIndicatorEnabled: Boolean,
    onLevelIndicatorEnabledChanged: (Boolean) -> Unit,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    onViewfinderAspectRatioChanged: (ViewfinderAspectRatio) -> Unit,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var showDisplayModes by rememberSaveable { mutableStateOf(false) }
    var showColorThemes by rememberSaveable { mutableStateOf(false) }
    var showLanguages by rememberSaveable { mutableStateOf(false) }
    val darkThemePreview = themeMode.resolveDarkTheme(isSystemInDarkTheme())
    val availableReferenceGridTypes = remember(viewfinderAspectRatio) {
        viewfinderAspectRatio.supportedReferenceGridTypes()
    }
    val normalizedReferenceGridType = remember(referenceGridType, viewfinderAspectRatio) {
        ReferenceGridType.normalizeForViewfinder(
            referenceGridType = referenceGridType,
            viewfinderAspectRatio = viewfinderAspectRatio,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background,
                    ),
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PageHeader(
                title = stringResource(R.string.settings_title),
                onBack = onBack,
            )

            MeterPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_metering_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    SettingsToggleCard(
                        title = stringResource(R.string.settings_live_metering),
                        subtitle = if (liveMeteringEnabled) {
                            stringResource(R.string.settings_live_metering_description)
                        } else {
                            stringResource(R.string.settings_single_metering_description)
                        },
                        checked = liveMeteringEnabled,
                        checkedIcon = Icons.Rounded.PhotoCamera,
                        uncheckedIcon = Icons.Rounded.CameraAlt,
                        onCheckedChange = onLiveMeteringEnabledChanged,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_preview_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    ViewfinderAspectRatioCard(
                        selectedAspectRatio = viewfinderAspectRatio,
                        onAspectRatioSelected = onViewfinderAspectRatioChanged,
                    )
                    SettingsToggleCard(
                        title = stringResource(R.string.settings_reference_grid),
                        subtitle = stringResource(R.string.settings_reference_grid_description),
                        checked = guideGridEnabled,
                        checkedIcon = Icons.Rounded.GridOn,
                        uncheckedIcon = Icons.Rounded.GridOff,
                        onCheckedChange = onGuideGridEnabledChanged,
                    )
                    AnimatedVisibility(visible = guideGridEnabled) {
                        ReferenceGridTypeCard(
                            selectedType = normalizedReferenceGridType,
                            availableTypes = availableReferenceGridTypes,
                            onTypeSelected = onReferenceGridTypeChanged,
                        )
                    }
                    SettingsToggleCard(
                        title = stringResource(R.string.settings_histogram),
                        subtitle = stringResource(R.string.settings_histogram_description),
                        checked = histogramEnabled,
                        checkedIcon = Icons.Rounded.Equalizer,
                        uncheckedIcon = Icons.Rounded.Equalizer,
                        onCheckedChange = onHistogramEnabledChanged,
                    )
                    SettingsToggleCard(
                        title = stringResource(R.string.settings_level_indicator),
                        subtitle = stringResource(R.string.settings_level_indicator_description),
                        checked = levelIndicatorEnabled,
                        checkedIcon = Icons.Rounded.Straighten,
                        uncheckedIcon = Icons.Rounded.Straighten,
                        onCheckedChange = onLevelIndicatorEnabledChanged,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_theme_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_theme_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SettingsDisclosureCard(
                            title = stringResource(R.string.settings_display_mode_title),
                            expanded = showDisplayModes,
                            onClick = { showDisplayModes = !showDisplayModes },
                        )
                        AnimatedVisibility(visible = showDisplayModes) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ThemeOptionCard(
                                    title = stringResource(R.string.theme_system),
                                    subtitle = stringResource(R.string.theme_system_description),
                                    icon = Icons.Rounded.BrightnessAuto,
                                    selected = themeMode == AppThemeMode.SYSTEM,
                                    onClick = { onThemeModeChanged(AppThemeMode.SYSTEM) },
                                )
                                ThemeOptionCard(
                                    title = stringResource(R.string.theme_light),
                                    subtitle = stringResource(R.string.theme_light_description),
                                    icon = Icons.Rounded.Brightness6,
                                    selected = themeMode == AppThemeMode.LIGHT,
                                    onClick = { onThemeModeChanged(AppThemeMode.LIGHT) },
                                )
                                ThemeOptionCard(
                                    title = stringResource(R.string.theme_dark),
                                    subtitle = stringResource(R.string.theme_dark_description),
                                    icon = Icons.Rounded.Brightness4,
                                    selected = themeMode == AppThemeMode.DARK,
                                    onClick = { onThemeModeChanged(AppThemeMode.DARK) },
                                )
                            }
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SettingsDisclosureCard(
                            title = stringResource(R.string.settings_language_title),
                            expanded = showLanguages,
                            onClick = { showLanguages = !showLanguages },
                        )
                        AnimatedVisibility(visible = showLanguages) {
                            LanguageChoiceCard(
                                selectedLanguage = language,
                                onLanguageSelected = onLanguageChanged,
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_color_theme_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        ThemePaletteDisclosureCard(
                            selectedTheme = colorTheme,
                            darkThemePreview = darkThemePreview,
                            expanded = showColorThemes,
                            onClick = { showColorThemes = !showColorThemes },
                        )
                        AnimatedVisibility(visible = showColorThemes) {
                            ThemePaletteSelector(
                                selectedTheme = colorTheme,
                                darkThemePreview = darkThemePreview,
                                onThemeSelected = onColorThemeChanged,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewfinderAspectRatioCard(
    selectedAspectRatio: ViewfinderAspectRatio,
    onAspectRatioSelected: (ViewfinderAspectRatio) -> Unit,
) {
    val ratioScrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_viewfinder_ratio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.settings_viewfinder_ratio_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(ratioScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ViewfinderAspectRatio.entries.forEach { option ->
                    MeterChoiceChip(
                        label = option.storageValue,
                        selected = option == selectedAspectRatio,
                        onClick = { onAspectRatioSelected(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceGridTypeCard(
    selectedType: ReferenceGridType,
    availableTypes: List<ReferenceGridType>,
    onTypeSelected: (ReferenceGridType) -> Unit,
) {
    val typeScrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_reference_grid_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.settings_reference_grid_type_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(typeScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableTypes.forEach { option ->
                    MeterChoiceChip(
                        label = stringResource(option.labelRes()),
                        selected = option == selectedType,
                        onClick = { onTypeSelected(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    checkedIcon: ImageVector,
    uncheckedIcon: ImageVector,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = { onCheckedChange(!checked) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (checked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.background
                },
            ) {
                Icon(
                    modifier = Modifier.padding(12.dp),
                    imageVector = if (checked) checkedIcon else uncheckedIcon,
                    contentDescription = null,
                    tint = if (checked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

private fun ReferenceGridType.labelRes(): Int {
    return when (this) {
        ReferenceGridType.THIRDS -> R.string.reference_grid_type_thirds
        ReferenceGridType.GOLDEN_SPIRAL -> R.string.reference_grid_type_golden_spiral
        ReferenceGridType.DIAGONAL -> R.string.reference_grid_type_diagonal
    }
}

@Composable
private fun SettingsDisclosureCard(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "${title}_arrow",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Icon(
            modifier = Modifier.graphicsLayer {
                rotationZ = arrowRotation
            },
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LanguageChoiceCard(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    val systemLabel = stringResource(R.string.language_system)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppLanguage.entries.forEach { option ->
            LanguageOptionCard(
                title = option.displayLabel(systemLabel),
                selected = option == selectedLanguage,
                onClick = { onLanguageSelected(option) },
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    SettingsOptionCard(
        title = title,
        subtitle = subtitle,
        icon = icon,
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun LanguageOptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    SettingsOptionCard(
        title = title,
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun SettingsOptionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.background
                    },
                ) {
                    Icon(
                        modifier = Modifier.padding(12.dp),
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    if (subtitle != null) 4.dp else 0.dp,
                ),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                subtitle?.let { supportingText ->
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ThemePaletteDisclosureCard(
    selectedTheme: AppColorTheme,
    darkThemePreview: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "theme_palette_arrow",
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (expanded) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (expanded) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.background
                },
            ) {
                Icon(
                    modifier = Modifier.padding(12.dp),
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = if (expanded) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (expanded) {
                        stringResource(R.string.settings_color_theme_description)
                    } else {
                        stringResource(
                            R.string.settings_color_theme_current,
                            stringResource(selectedTheme.titleRes()),
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (expanded) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                ThemePalettePreviewRow(
                    preview = selectedTheme.preview(darkThemePreview),
                    selected = expanded,
                )
            }

            Icon(
                modifier = Modifier.graphicsLayer {
                    rotationZ = arrowRotation
                },
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = if (expanded) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ThemePaletteSelector(
    selectedTheme: AppColorTheme,
    darkThemePreview: Boolean,
    onThemeSelected: (AppColorTheme) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(AppColorTheme.entries) { option ->
            ThemePaletteOptionCard(
                title = stringResource(option.titleRes()),
                subtitle = stringResource(option.descriptionRes()),
                preview = option.preview(darkThemePreview),
                selected = option == selectedTheme,
                onClick = { onThemeSelected(option) },
            )
        }
    }
}

@Composable
private fun ThemePaletteOptionCard(
    title: String,
    subtitle: String,
    preview: MeterPalettePreview,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThemePalettePreviewRow(
                    preview = preview,
                    selected = selected,
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun ThemePalettePreviewRow(
    preview: MeterPalettePreview,
    selected: Boolean,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    }
    val swatchColors = listOf(
        preview.background,
        preview.surface,
        preview.accent,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        swatchColors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(if (index == swatchColors.lastIndex) 22.dp else 18.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(width = 1.dp, color = borderColor, shape = CircleShape),
            )
        }
    }
}

private fun AppColorTheme.titleRes(): Int {
    return when (this) {
        AppColorTheme.CLASSIC_AMBER -> R.string.color_theme_classic_amber
        AppColorTheme.CYAN_TECH -> R.string.color_theme_cyan_tech
        AppColorTheme.NAVY_ORANGE -> R.string.color_theme_navy_orange
        AppColorTheme.INDUSTRIAL_GREEN -> R.string.color_theme_industrial_green
        AppColorTheme.SOFT_VIOLET -> R.string.color_theme_soft_violet
        AppColorTheme.PAPER_LIGHT -> R.string.color_theme_paper_light
    }
}

private fun AppColorTheme.descriptionRes(): Int {
    return when (this) {
        AppColorTheme.CLASSIC_AMBER -> R.string.color_theme_classic_amber_description
        AppColorTheme.CYAN_TECH -> R.string.color_theme_cyan_tech_description
        AppColorTheme.NAVY_ORANGE -> R.string.color_theme_navy_orange_description
        AppColorTheme.INDUSTRIAL_GREEN -> R.string.color_theme_industrial_green_description
        AppColorTheme.SOFT_VIOLET -> R.string.color_theme_soft_violet_description
        AppColorTheme.PAPER_LIGHT -> R.string.color_theme_paper_light_description
    }
}

@Composable
private fun ValueLibraryPage(
    title: String,
    description: String,
    values: List<Double>,
    selectedValue: Double,
    addButtonLabel: String,
    onBack: () -> Unit,
    onSelect: (Double) -> Unit,
    onDelete: (Double) -> Unit,
    onAdd: () -> Unit,
    canDelete: (Double) -> Boolean,
    valueFormatter: (Double) -> String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.background,
                    ),
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PageHeader(
                title = title,
                onBack = onBack,
            )

            MeterPanel(
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(values) { value ->
                            ValueRow(
                                value = valueFormatter(value),
                                selected = valuesEqual(value, selectedValue),
                                isCustom = canDelete(value),
                                onClick = { onSelect(value) },
                                onDelete = if (canDelete(value)) {
                                    { onDelete(value) }
                                } else {
                                    null
                                },
                            )
                        }
                    }

                    Button(
                        onClick = onAdd,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(addButtonLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueRow(
    value: String,
    selected: Boolean,
    isCustom: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = if (isCustom) {
                        stringResource(R.string.custom_value)
                    } else {
                        stringResource(R.string.default_value)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (onDelete != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            },
                            shape = CircleShape,
                        )
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.delete_custom_value),
                        modifier = Modifier.size(16.dp),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExposureModeSheet(
    currentMode: ExposureMode,
    onSelect: (ExposureMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = stringResource(R.string.priority_mode),
            style = MaterialTheme.typography.titleLarge,
        )
        ExposureMode.entries.forEach { mode ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (currentMode == mode) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
                },
                onClick = { onSelect(mode) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = exposureModeLabel(mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = when (mode) {
                                ExposureMode.APERTURE_PRIORITY -> stringResource(R.string.aperture_mode_description)
                                ExposureMode.SHUTTER_PRIORITY -> stringResource(R.string.shutter_mode_description)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (currentMode == mode) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalibrationPage(
    uiState: MeterUiState,
    onBack: () -> Unit,
    onSelectPreset: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onAddPreset: () -> Unit,
    onEditPreset: (CalibrationPreset) -> Unit,
    onImportPresets: (List<CalibrationPreset>, String?, CalibrationImportStrategy) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exportSuccessTemplate = stringResource(R.string.calibration_export_success)
    val exportFailedMessage = stringResource(R.string.calibration_export_failed)
    val importSuccessTemplate = stringResource(R.string.calibration_import_success)
    val importFailedMessage = stringResource(R.string.calibration_import_failed)
    val importEmptyMessage = stringResource(R.string.calibration_import_empty)

    var pendingImport by remember {
        mutableStateOf<Pair<List<CalibrationPreset>, String?>?>(null)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val json = CalibrationBackupCodec.serializeToJson(
            presets = uiState.calibrationPresets,
            activeId = uiState.activeCalibrationPresetId,
        )
        val written = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream")
        }
        scope.launch {
            if (written.isSuccess) {
                snackbarHostState.showMeterSnackbar(
                    exportSuccessTemplate.format(uiState.calibrationPresets.size),
                    status = MeterSnackbarStatus.Success,
                )
            } else {
                snackbarHostState.showMeterSnackbar(
                    exportFailedMessage,
                    status = MeterSnackbarStatus.Error,
                )
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val raw = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: error("Unable to open input stream")
        }
        val payload = raw.mapCatching { text ->
            CalibrationBackupCodec.parseFromJson(text).getOrThrow()
        }
        scope.launch {
            payload.fold(
                onSuccess = { result ->
                    if (result.presets.isEmpty()) {
                        snackbarHostState.showMeterSnackbar(
                            importEmptyMessage,
                            status = MeterSnackbarStatus.Warning,
                        )
                    } else {
                        pendingImport = result.presets to result.activeId
                    }
                },
                onFailure = {
                    snackbarHostState.showMeterSnackbar(
                        importFailedMessage,
                        status = MeterSnackbarStatus.Error,
                    )
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.background,
                    ),
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CalibrationPageHeader(
                title = stringResource(R.string.calibration_title),
                onBack = onBack,
                onExport = {
                    exportLauncher.launch("luma_calibration.json")
                },
                onImport = {
                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                },
                exportEnabled = uiState.calibrationPresets.isNotEmpty(),
            )

            MeterPanel(
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.calibration_presets_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (uiState.calibrationPresets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            CalibrationEmptyState()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(
                                items = uiState.calibrationPresets,
                                key = { it.id },
                            ) { preset ->
                                PresetRow(
                                    preset = preset,
                                    selected = preset.id == uiState.activeCalibrationPresetId,
                                    onClick = { onSelectPreset(preset.id) },
                                    onEdit = { onEditPreset(preset) },
                                    onDelete = { onDeletePreset(preset.id) },
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onAddPreset,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_calibration))
                    }
                }
            }
        }

        MeterSnackbarHost(
            hostState = snackbarHostState,
            position = MeterSnackbarPosition.Top,
        )
    }

    pendingImport?.let { (presets, activeId) ->
        MeterDialog(
            onDismissRequest = { pendingImport = null },
            title = stringResource(R.string.calibration_import_dialog_title),
            content = {
                Text(
                    text = stringResource(
                        R.string.calibration_import_dialog_message,
                        presets.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            actions = {
                TextButton(onClick = {
                    onImportPresets(presets, activeId, CalibrationImportStrategy.MERGE_KEEP_EXISTING)
                    pendingImport = null
                    scope.launch {
                        snackbarHostState.showMeterSnackbar(
                            importSuccessTemplate.format(presets.size),
                            status = MeterSnackbarStatus.Success,
                        )
                    }
                }) {
                    Text(stringResource(R.string.calibration_import_merge))
                }
                TextButton(onClick = {
                    onImportPresets(presets, activeId, CalibrationImportStrategy.REPLACE)
                    pendingImport = null
                    scope.launch {
                        snackbarHostState.showMeterSnackbar(
                            importSuccessTemplate.format(presets.size),
                            status = MeterSnackbarStatus.Success,
                        )
                    }
                }) {
                    Text(stringResource(R.string.calibration_import_replace))
                }
            },
        )
    }
}

@Composable
private fun CalibrationPageHeader(
    title: String,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    exportEnabled: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuItemColors = MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.onSurface,
        leadingIconColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 3.dp,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.calibration_export)) },
                    enabled = exportEnabled,
                    colors = menuItemColors,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onExport()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.calibration_import)) },
                    colors = menuItemColors,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FileUpload,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onImport()
                    },
                )
            }
        }
    }
}

@Composable
private fun CalibrationEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(24.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(18.dp)
                    .size(28.dp),
            )
        }
        Text(
            text = stringResource(R.string.calibration_empty_state),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PresetRow(
    preset: CalibrationPreset,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.preset_offset_label,
                        formatSignedEv(preset.offsetEv),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (preset.notes.isNotBlank()) {
                    Text(
                        text = preset.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.edit_preset),
                        modifier = Modifier.size(18.dp),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        },
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.delete_preset),
                        modifier = Modifier.size(18.dp),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.62f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalibrationEditorSheet(
    uiState: MeterUiState,
    editing: CalibrationPreset?,
    onOffsetPreview: (Float) -> Unit,
    onSaveNew: (name: String, notes: String) -> Unit,
    onSaveEdit: (id: String, name: String, notes: String, offsetEv: Double) -> Unit,
    onCancel: () -> Unit,
) {
    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var notes by rememberSaveable(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    val canSave = name.isNotBlank()

    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    )
    val calibrationScaleStops = remember {
        (-5..5).map { v ->
            SliderScaleStop(
                value = v.toFloat(),
                label = when (v) {
                    -5 -> "-5"
                    0 -> "0"
                    5 -> "+5"
                    else -> null
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(
                if (editing == null) {
                    R.string.calibration_preset_editor_title_new
                } else {
                    R.string.calibration_preset_editor_title_edit
                },
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.calibration_sheet_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8A8A8A),
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.preset_name_input)) },
            placeholder = { Text(stringResource(R.string.preset_name_hint)) },
            supportingText = {
                Text(
                    if (name.isNotEmpty() && name.isBlank()) {
                        stringResource(R.string.preset_name_required)
                    } else {
                        stringResource(R.string.preset_name_hint)
                    },
                )
            },
            isError = name.isNotEmpty() && name.isBlank(),
            singleLine = true,
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.preset_notes_input)) },
            placeholder = { Text(stringResource(R.string.preset_notes_hint)) },
            maxLines = 3,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.calibration_offset,
                    formatSignedEv(uiState.calibrationOffsetEv),
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(
                onClick = { onOffsetPreview(0f) },
                enabled = uiState.calibrationOffsetEv != 0.0,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.calibration_reset), maxLines = 1)
            }
        }

        Slider(
            value = uiState.calibrationOffsetEv.toFloat(),
            onValueChange = onOffsetPreview,
            valueRange = -5f..5f,
            steps = 99,
            colors = sliderColors,
            track = { sliderState ->
                CompactSliderTrack(
                    sliderState = sliderState,
                    colors = sliderColors,
                )
            },
        )
        SliderScale(
            stops = calibrationScaleStops,
            valueRange = -5f..5f,
            labelWidth = 20.dp,
            tickHeight = 5.dp,
            horizontalInset = 10.dp,
            labelTextStyle = MaterialTheme.typography.labelMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    if (editing == null) {
                        onSaveNew(name, notes)
                    } else {
                        onSaveEdit(editing.id, name, notes, uiState.calibrationOffsetEv)
                    }
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.save))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ValueInputSheet(
    title: String,
    label: String,
    placeholder: String,
    supportingText: String,
    invalidText: String,
    duplicateText: String,
    outOfRangeText: String,
    keyboardType: KeyboardType,
    currentValues: List<Double>,
    parser: (String) -> Double?,
    validator: (Double) -> Boolean,
    onSubmit: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val parsedValue = remember(input) { parser(input) }
    val duplicated = parsedValue?.let { parsed ->
        currentValues.any { valuesEqual(it, parsed) }
    } == true
    val inRange = parsedValue?.let(validator) == true
    val canSubmit = parsedValue != null && inRange && !duplicated

    val helperText = when {
        input.isBlank() -> supportingText
        parsedValue == null -> invalidText
        duplicated -> duplicateText
        !inRange -> outOfRangeText
        else -> supportingText
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            supportingText = { Text(helperText) },
            isError = input.isNotBlank() && !canSubmit,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { parsedValue?.let(onSubmit) },
            enabled = canSubmit,
        ) {
            Text(stringResource(R.string.add_value))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactSliderTrack(
    sliderState: SliderState,
    colors: SliderColors,
    enabled: Boolean = true,
) {
    SliderDefaults.Track(
        sliderState = sliderState,
        enabled = enabled,
        colors = colors,
        drawStopIndicator = null,
        drawTick = { _, _ -> },
        thumbTrackGapSize = 0.dp,
        trackInsideCornerSize = 0.dp,
    )
}

@Composable
private fun SliderScale(
    stops: List<SliderScaleStop>,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 32.dp,
    tickHeight: Dp = 6.dp,
    horizontalInset: Dp = 0.dp,
    labelTextStyle: TextStyle? = null,
) {
    if (stops.isEmpty()) {
        return
    }

    val hasLabels = stops.any { it.label != null }
    val totalHeight = if (hasLabels) tickHeight + 18.dp else tickHeight
    val tickColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val resolvedLabelTextStyle = labelTextStyle ?: MaterialTheme.typography.labelSmall
    val range = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
    ) {
        val availableWidth = (maxWidth - horizontalInset * 2).coerceAtLeast(0.dp)
        val maxLabelOffset = if (maxWidth > labelWidth) maxWidth - labelWidth else 0.dp
        val maxTickOffset = if (maxWidth > 1.dp) maxWidth - 1.dp else 0.dp
        val labelOffsetY = tickHeight + 4.dp

        stops.forEach { stop ->
            val fraction = ((stop.value - valueRange.start) / range).coerceIn(0f, 1f)
            val centerOffset = horizontalInset + availableWidth * fraction
            val rawTickOffset = centerOffset - 0.5.dp
            val tickOffset = when {
                rawTickOffset < 0.dp -> 0.dp
                rawTickOffset > maxTickOffset -> maxTickOffset
                else -> rawTickOffset
            }

            Box(
                modifier = Modifier
                    .offset(x = tickOffset)
                    .width(1.dp)
                    .height(tickHeight)
                    .background(tickColor),
            )

            stop.label?.let { label ->
                val rawLabelOffset = centerOffset - labelWidth / 2
                val labelOffset = when {
                    rawLabelOffset < 0.dp -> 0.dp
                    rawLabelOffset > maxLabelOffset -> maxLabelOffset
                    else -> rawLabelOffset
                }
                val labelTextAlign = when {
                    rawLabelOffset < 0.dp -> TextAlign.Start
                    rawLabelOffset > maxLabelOffset -> TextAlign.End
                    else -> TextAlign.Center
                }

                Text(
                    text = label,
                    modifier = Modifier
                        .offset(x = labelOffset, y = labelOffsetY)
                        .width(labelWidth),
                    style = resolvedLabelTextStyle,
                    color = labelColor,
                    textAlign = labelTextAlign,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PageHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 3.dp,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PermissionEmptyState(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    modifier = Modifier
                        .padding(18.dp)
                        .size(28.dp),
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = stringResource(R.string.permission_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.permission_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.grant_camera_permission))
            }
        }
    }
}

private fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
}

internal fun shouldShowPinnedSummary(
    hasCameraPermission: Boolean,
    viewportTopPx: Float?,
    statusRowBottomPx: Float?,
): Boolean {
    if (!hasCameraPermission || viewportTopPx == null || statusRowBottomPx == null) {
        return false
    }
    return statusRowBottomPx <= viewportTopPx + PINNED_SUMMARY_VISIBILITY_EPSILON_PX
}

private fun formatEv(value: Double): String {
    return String.format(Locale.getDefault(), "%.1f", value)
}

private fun formatSignedEv(value: Double): String {
    return if (value >= 0) {
        "+${String.format(Locale.getDefault(), "%.1f", value)}"
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun formatAperture(value: Double): String {
    return "f/${String.format(Locale.getDefault(), "%.1f", value)}"
}

internal fun formatShutter(value: Double): String {
    return if (value >= 1.0) {
        val display = if (value >= 10 || value % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%.0f", value)
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
        "${display}s"
    } else {
        val reciprocal = (1.0 / value).roundToInt().coerceAtLeast(1)
        if (reciprocal <= 1) "1s" else "1/$reciprocal"
    }
}

private fun formatLuma(value: Double): String {
    return String.format(Locale.getDefault(), "%.0f", value)
}

private fun formatKelvin(value: Int): String {
    return "${value}K"
}

private fun formatSignedKelvin(value: Int): String {
    return if (value > 0) {
        "+${value}K"
    } else {
        "${value}K"
    }
}

private fun formatZoomRatio(value: Float): String {
    val roundedWhole = value.roundToInt().toFloat()
    return if (abs(value - roundedWhole) < 0.05f) {
        "${roundedWhole.roundToInt()}x"
    } else {
        String.format(Locale.getDefault(), "%.1fx", value)
    }
}

private fun buildZoomScaleStops(uiState: MeterUiState): List<SliderScaleStop> {
    val minZoom = uiState.minZoomRatio
    val maxZoom = uiState.maxZoomRatio

    return buildList {
        add(SliderScaleStop(value = minZoom, label = formatZoomRatio(minZoom)))
        uiState.zoomPresets
            .asSequence()
            .filter { it.enabled }
            .map { it.ratio }
            .filter { ratio ->
                abs(ratio - minZoom) > 0.12f && abs(ratio - maxZoom) > 0.12f
            }
            .sorted()
            .forEach { ratio ->
                add(SliderScaleStop(value = ratio, label = formatZoomRatio(ratio)))
            }
        if (abs(maxZoom - minZoom) > 0.12f) {
            add(SliderScaleStop(value = maxZoom, label = formatZoomRatio(maxZoom)))
        }
    }
}

private fun Modifier.observeZoomSliderTap(
    onTap: () -> Unit,
): Modifier {
    return pointerInput(onTap) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Final,
            )
            val touchSlop = viewConfiguration.touchSlop
            var isTap = true
            var latestDistance = 0f
            var pointerPressed = true

            while (pointerPressed) {
                val event = awaitPointerEvent(pass = PointerEventPass.Final)
                val pointerChange = event.changes.firstOrNull { it.id == down.id } ?: break
                val delta = pointerChange.position - down.position
                latestDistance = hypot(delta.x.toDouble(), delta.y.toDouble()).toFloat()
                if (latestDistance > touchSlop) {
                    isTap = false
                }
                pointerPressed = event.changes.any { it.id == down.id && it.pressed }
            }

            if (isTap && latestDistance <= touchSlop) {
                onTap()
            }
        }
    }
}

private fun parseApertureInput(input: String): Double? {
    val normalized = input.trim()
        .lowercase(Locale.getDefault())
        .removePrefix("f/")
        .removePrefix("f")
        .trim()
    return normalized.toDoubleOrNull()
}

private fun parseShutterInput(input: String): Double? {
    val normalized = input.trim()
        .lowercase(Locale.getDefault())
        .removeSuffix("s")
        .replace(" ", "")

    if (normalized.contains("/")) {
        val parts = normalized.split("/")
        if (parts.size != 2) {
            return null
        }
        val numerator = parts[0].toDoubleOrNull()
        val denominator = parts[1].toDoubleOrNull()
        if (numerator == null || denominator == null || denominator == 0.0) {
            return null
        }
        return numerator / denominator
    }

    return normalized.toDoubleOrNull()
}

@Composable
private fun exposureModeLabel(mode: ExposureMode): String {
    return when (mode) {
        ExposureMode.APERTURE_PRIORITY -> stringResource(R.string.exposure_mode_aperture_priority)
        ExposureMode.SHUTTER_PRIORITY -> stringResource(R.string.exposure_mode_shutter_priority)
    }
}

@Composable
private fun analysisToolLabel(tool: AnalysisTool): String {
    return when (tool) {
        AnalysisTool.METER -> stringResource(R.string.analysis_tool_meter)
        AnalysisTool.WHITE_BALANCE -> stringResource(R.string.analysis_tool_white_balance)
    }
}

private fun analysisToolIcon(
    tool: AnalysisTool,
): ImageVector {
    return when (tool) {
        AnalysisTool.METER -> Icons.Rounded.Equalizer
        AnalysisTool.WHITE_BALANCE -> Icons.Rounded.WbSunny
    }
}

@Composable
private fun meteringModeLabel(mode: MeteringMode): String {
    return when (mode) {
        MeteringMode.AVERAGE -> stringResource(R.string.metering_mode_average)
        MeteringMode.CENTER_WEIGHTED -> stringResource(R.string.metering_mode_center_weighted)
        MeteringMode.SPOT -> stringResource(R.string.metering_mode_spot)
    }
}

@Composable
private fun whiteBalanceConditionLabel(condition: WhiteBalanceCondition): String {
    return when (condition) {
        WhiteBalanceCondition.CANDLE -> stringResource(R.string.white_balance_condition_candle)
        WhiteBalanceCondition.TUNGSTEN -> stringResource(R.string.white_balance_condition_tungsten)
        WhiteBalanceCondition.FLUORESCENT -> stringResource(R.string.white_balance_condition_fluorescent)
        WhiteBalanceCondition.SUNLIGHT -> stringResource(R.string.white_balance_condition_sunlight)
        WhiteBalanceCondition.CLOUDY -> stringResource(R.string.white_balance_condition_cloudy)
        WhiteBalanceCondition.SHADE -> stringResource(R.string.white_balance_condition_shade)
    }
}

@Composable
private fun whiteBalanceConditionDescription(condition: WhiteBalanceCondition): String {
    return when (condition) {
        WhiteBalanceCondition.CANDLE -> stringResource(R.string.white_balance_condition_candle_description)
        WhiteBalanceCondition.TUNGSTEN -> stringResource(R.string.white_balance_condition_tungsten_description)
        WhiteBalanceCondition.FLUORESCENT -> stringResource(R.string.white_balance_condition_fluorescent_description)
        WhiteBalanceCondition.SUNLIGHT -> stringResource(R.string.white_balance_condition_sunlight_description)
        WhiteBalanceCondition.CLOUDY -> stringResource(R.string.white_balance_condition_cloudy_description)
        WhiteBalanceCondition.SHADE -> stringResource(R.string.white_balance_condition_shade_description)
    }
}

private fun whiteBalanceConditionIcon(
    condition: WhiteBalanceCondition?,
): ImageVector? {
    return when (condition) {
        WhiteBalanceCondition.CANDLE -> Icons.Rounded.EmojiObjects
        WhiteBalanceCondition.TUNGSTEN -> Icons.Rounded.Lightbulb
        WhiteBalanceCondition.FLUORESCENT -> Icons.Rounded.Fluorescent
        WhiteBalanceCondition.SUNLIGHT -> Icons.Rounded.WbSunny
        WhiteBalanceCondition.CLOUDY -> Icons.Rounded.Cloud
        WhiteBalanceCondition.SHADE -> Icons.Rounded.NightlightRound
        null -> null
    }
}

@Composable
private fun whiteBalanceBiasLabel(direction: WhiteBalanceBiasDirection): String {
    return when (direction) {
        WhiteBalanceBiasDirection.WARM -> stringResource(R.string.white_balance_bias_warm)
        WhiteBalanceBiasDirection.COOL -> stringResource(R.string.white_balance_bias_cool)
        WhiteBalanceBiasDirection.NEUTRAL -> stringResource(R.string.white_balance_bias_neutral)
    }
}

private fun whiteBalanceBias(reading: WhiteBalanceReading): WhiteBalanceBiasUiModel {
    val deltaKelvin = reading.condition.referenceKelvin - reading.kelvin
    val direction = when {
        deltaKelvin > 0 -> WhiteBalanceBiasDirection.WARM
        deltaKelvin < 0 -> WhiteBalanceBiasDirection.COOL
        else -> WhiteBalanceBiasDirection.NEUTRAL
    }
    return WhiteBalanceBiasUiModel(
        deltaKelvin = deltaKelvin,
        direction = direction,
    )
}

private fun whiteBalanceBiasColor(direction: WhiteBalanceBiasDirection): Color {
    return when (direction) {
        WhiteBalanceBiasDirection.WARM -> Color(0xFFE5963B)
        WhiteBalanceBiasDirection.COOL -> Color(0xFF4A90E2)
        WhiteBalanceBiasDirection.NEUTRAL -> Color(0xFF9CA3AF)
    }
}

private fun whiteBalanceTrackCenterPx(
    index: Int,
    markerWidthPx: Float,
    itemGapPx: Float,
): Float {
    return index * (markerWidthPx + itemGapPx) + markerWidthPx / 2f
}

private fun whiteBalanceTrackEmphasis(
    itemCenterPx: Float,
    currentPositionPx: Float,
    containerWidthPx: Float,
): Float {
    val falloffDistancePx = (containerWidthPx * 0.62f).coerceAtLeast(1f)
    val normalized = 1f - abs(itemCenterPx - currentPositionPx) / falloffDistancePx
    return normalized.coerceIn(0.08f, 1f)
}

@Composable
private fun meterStatusLabel(uiState: MeterUiState): String {
    val cameraError = uiState.cameraError
    if (cameraError != null) {
        return cameraError
    }

    if (!uiState.isLiveMeteringEnabled) {
        return when {
            uiState.isManualMeterPending -> stringResource(R.string.status_single_metering_pending)
            uiState.liveReading == null -> stringResource(R.string.status_single_metering_ready)
            uiState.isAeLocked -> stringResource(R.string.status_ae_lock_active)
            else -> stringResource(R.string.status_single_metering_done)
        }
    }

    return when (uiState.meterStatus) {
        MeterStatus.WAITING -> stringResource(R.string.status_waiting_live_meter)
        MeterStatus.LOCKED -> stringResource(R.string.status_ae_lock_active)
        MeterStatus.LIVE -> stringResource(R.string.status_live_metering)
        MeterStatus.MANUAL -> stringResource(R.string.status_single_metering_done)
    }
}

@Composable
private fun whiteBalanceStatusLabel(uiState: MeterUiState): String {
    val cameraError = uiState.cameraError
    if (cameraError != null) {
        return cameraError
    }

    if (!uiState.isLiveMeteringEnabled) {
        return when {
            uiState.isManualMeterPending -> stringResource(R.string.status_single_white_balance_pending)
            uiState.liveReading?.whiteBalanceReading == null -> stringResource(R.string.status_single_white_balance_ready)
            else -> stringResource(R.string.status_single_white_balance_done)
        }
    }

    return stringResource(R.string.status_live_white_balance)
}

@Composable
private fun mainStatusLabel(uiState: MeterUiState): String {
    return if (uiState.analysisTool == AnalysisTool.WHITE_BALANCE) {
        whiteBalanceStatusLabel(uiState)
    } else {
        meterStatusLabel(uiState)
    }
}

internal enum class PreviewTapHint {
    TAP_TO_METER,
    TAP_TO_METER_ONCE,
    TAP_TO_REPOSITION_METER,
    TAP_TO_SAMPLE_WHITE_BALANCE,
}

internal fun resolvePreviewTapHint(
    analysisTool: AnalysisTool,
    meteringMode: MeteringMode,
    isLiveMeteringEnabled: Boolean,
    hasCustomSpotMeteringPoint: Boolean,
): PreviewTapHint? {
    if (analysisTool == AnalysisTool.WHITE_BALANCE) {
        return if (isLiveMeteringEnabled) {
            null
        } else {
            PreviewTapHint.TAP_TO_SAMPLE_WHITE_BALANCE
        }
    }

    return when {
        !isLiveMeteringEnabled -> PreviewTapHint.TAP_TO_METER_ONCE
        meteringMode != MeteringMode.SPOT -> null
        hasCustomSpotMeteringPoint -> PreviewTapHint.TAP_TO_REPOSITION_METER
        else -> PreviewTapHint.TAP_TO_METER
    }
}

private fun valuesEqual(
    first: Double,
    second: Double,
): Boolean {
    return abs(first - second) < 0.0001
}

private const val MIN_SHUTTER_SECONDS = 1.0 / 8000.0
private const val MAX_SHUTTER_SECONDS = 30.0
private const val PAGE_TRANSITION_DURATION_MILLIS = 220
private const val PINNED_SUMMARY_VISIBILITY_EPSILON_PX = 1f
private data class WhiteBalanceBiasUiModel(
    val deltaKelvin: Int,
    val direction: WhiteBalanceBiasDirection,
)

private enum class WhiteBalanceBiasDirection {
    WARM,
    COOL,
    NEUTRAL,
}
