package com.yourbrand.lumameter.pro.ui.meter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourbrand.lumameter.pro.R
import com.yourbrand.lumameter.pro.domain.exposure.ExposureMode
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.viewmodel.MeterDefaults
import com.yourbrand.lumameter.pro.viewmodel.MeterStatus
import com.yourbrand.lumameter.pro.viewmodel.MeterUiState
import com.yourbrand.lumameter.pro.viewmodel.MeterViewModel
import java.util.Locale

@Composable
fun MeterRoute(
    viewModel: MeterViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionRequiredMessage = stringResource(R.string.camera_permission_required)
    var hasCameraPermission by remember { mutableStateOf(context.hasCameraPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            viewModel.onCameraError(permissionRequiredMessage)
        }
    }

    MeterScreen(
        uiState = uiState,
        hasCameraPermission = hasCameraPermission,
        onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onReadingAvailable = viewModel::onFrameAnalyzed,
        onCameraError = viewModel::onCameraError,
        onExposureModeSelected = viewModel::setExposureMode,
        onMeteringModeSelected = viewModel::setMeteringMode,
        onMeteringPointChanged = viewModel::setMeteringPoint,
        onIsoSelected = viewModel::setIso,
        onApertureSelected = viewModel::setAperture,
        onShutterSelected = viewModel::setShutterSeconds,
        onCompensationChanged = viewModel::setCompensation,
        onCalibrationChanged = viewModel::setCalibrationOffset,
        onAeLockToggled = viewModel::toggleAeLock,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MeterScreen(
    uiState: MeterUiState,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onCameraError: (String) -> Unit,
    onExposureModeSelected: (ExposureMode) -> Unit,
    onMeteringModeSelected: (MeteringMode) -> Unit,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onIsoSelected: (Int) -> Unit,
    onApertureSelected: (Double) -> Unit,
    onShutterSelected: (Double) -> Unit,
    onCompensationChanged: (Float) -> Unit,
    onCalibrationChanged: (Float) -> Unit,
    onAeLockToggled: () -> Unit,
) {
    var showControls by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showControls) {
        ModalBottomSheet(
            onDismissRequest = { showControls = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            MeterControlSheet(
                uiState = uiState,
                onExposureModeSelected = onExposureModeSelected,
                onIsoSelected = onIsoSelected,
                onApertureSelected = onApertureSelected,
                onShutterSelected = onShutterSelected,
                onCompensationChanged = onCompensationChanged,
                onCalibrationChanged = onCalibrationChanged,
                onClose = { showControls = false },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (hasCameraPermission) {
            MeterCameraPreview(
                modifier = Modifier.fillMaxSize(),
                meteringMode = uiState.meteringMode,
                meteringPoint = uiState.meteringPoint,
                isAeLocked = uiState.isAeLocked,
                onMeteringPointChanged = onMeteringPointChanged,
                onReadingAvailable = onReadingAvailable,
                onCameraError = onCameraError,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.42f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.56f),
                            ),
                        )
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MeterHeader(
                    statusLabel = meterStatusLabel(uiState),
                    onOpenControls = { showControls = true },
                )
                MeterHeroCard(uiState = uiState)
                Spacer(modifier = Modifier.weight(1f))
                MeterBottomPanel(
                    uiState = uiState,
                    onExposureModeSelected = onExposureModeSelected,
                    onMeteringModeSelected = onMeteringModeSelected,
                    onAeLockToggled = onAeLockToggled,
                    onOpenControls = { showControls = true },
                )
            }
        } else {
            PermissionEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(24.dp),
                onRequestPermission = onRequestPermission,
            )
        }
    }
}

@Composable
private fun MeterHeader(
    statusLabel: String,
    onOpenControls: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onOpenControls) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.open_controls),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeterHeroCard(
    uiState: MeterUiState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        ),
        shape = RoundedCornerShape(32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.live_ev, formatEv(uiState.exposureResult.sceneEv100)),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(
                    R.string.mode_summary,
                    meteringModeLabel(uiState.meteringMode),
                    exposureModeLabel(uiState.exposureMode),
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(
                    label = stringResource(R.string.chip_comp),
                    value = "${formatSignedEv(uiState.compensationEv)} EV",
                )
                StatusChip(
                    label = stringResource(R.string.chip_cal),
                    value = "${formatSignedEv(uiState.calibrationOffsetEv)} EV",
                )
                StatusChip(
                    label = stringResource(R.string.chip_luma),
                    value = formatLuma(uiState.liveReading?.meteredLuma ?: 0.0),
                )
                StatusChip(
                    label = stringResource(R.string.chip_avg),
                    value = formatLuma(uiState.liveReading?.averageLuma ?: 0.0),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeterBottomPanel(
    uiState: MeterUiState,
    onExposureModeSelected: (ExposureMode) -> Unit,
    onMeteringModeSelected: (MeteringMode) -> Unit,
    onAeLockToggled: () -> Unit,
    onOpenControls: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_aperture),
                    value = formatAperture(uiState.exposureResult.aperture),
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_shutter),
                    value = formatShutter(uiState.exposureResult.shutterSeconds),
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_iso),
                    value = stringResource(R.string.iso_value, uiState.exposureResult.iso),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposureMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.exposureMode == mode,
                        onClick = { onExposureModeSelected(mode) },
                        label = { Text(exposureModeLabel(mode)) },
                    )
                }
                MeteringMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.meteringMode == mode,
                        onClick = { onMeteringModeSelected(mode) },
                        label = { Text(meteringModeLabel(mode)) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (mode) {
                                    MeteringMode.AVERAGE -> Icons.Rounded.GridOn
                                    MeteringMode.CENTER_WEIGHTED -> Icons.Rounded.Adjust
                                    MeteringMode.SPOT -> Icons.Rounded.CenterFocusStrong
                                },
                                contentDescription = null,
                            )
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onAeLockToggled,
                ) {
                    Icon(
                        imageVector = if (uiState.isAeLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (uiState.isAeLocked) {
                            stringResource(R.string.disable_ae_lock)
                        } else {
                            stringResource(R.string.enable_ae_lock)
                        }
                    )
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenControls,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.controls))
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeterControlSheet(
    uiState: MeterUiState,
    onExposureModeSelected: (ExposureMode) -> Unit,
    onIsoSelected: (Int) -> Unit,
    onApertureSelected: (Double) -> Unit,
    onShutterSelected: (Double) -> Unit,
    onCompensationChanged: (Float) -> Unit,
    onCalibrationChanged: (Float) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.controls),
            style = MaterialTheme.typography.titleLarge,
        )

        SettingGroup(title = stringResource(R.string.priority_mode)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposureMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.exposureMode == mode,
                        onClick = { onExposureModeSelected(mode) },
                        label = { Text(exposureModeLabel(mode)) },
                    )
                }
            }
        }

        SettingGroup(title = stringResource(R.string.label_iso)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MeterDefaults.isoValues.forEach { iso ->
                    FilterChip(
                        selected = uiState.selectedIso == iso,
                        onClick = { onIsoSelected(iso) },
                        label = { Text(stringResource(R.string.iso_value, iso)) },
                    )
                }
            }
        }

        if (uiState.exposureMode == ExposureMode.APERTURE_PRIORITY) {
            SettingGroup(title = stringResource(R.string.label_aperture)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MeterDefaults.apertureValues.forEach { aperture ->
                        FilterChip(
                            selected = uiState.selectedAperture == aperture,
                            onClick = { onApertureSelected(aperture) },
                            label = { Text(formatAperture(aperture)) },
                        )
                    }
                }
            }
        } else {
            SettingGroup(title = stringResource(R.string.label_shutter)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MeterDefaults.shutterValues.forEach { shutter ->
                        FilterChip(
                            selected = uiState.selectedShutterSeconds == shutter,
                            onClick = { onShutterSelected(shutter) },
                            label = { Text(formatShutter(shutter)) },
                        )
                    }
                }
            }
        }

        SettingGroup(
            title = stringResource(
                R.string.exposure_compensation,
                formatSignedEv(uiState.compensationEv),
            )
        ) {
            Slider(
                value = uiState.compensationEv.toFloat(),
                onValueChange = onCompensationChanged,
                valueRange = -3f..3f,
            )
        }

        SettingGroup(
            title = stringResource(
                R.string.calibration_offset,
                formatSignedEv(uiState.calibrationOffsetEv),
            )
        ) {
            Slider(
                value = uiState.calibrationOffsetEv.toFloat(),
                onValueChange = onCalibrationChanged,
                valueRange = -2f..2f,
            )
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.done))
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SettingGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        content()
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(36.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        modifier = Modifier.padding(18.dp),
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
}

private fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
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

private fun formatShutter(value: Double): String {
    return if (value >= 1.0) {
        val display = if (value >= 10 || value % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%.0f", value)
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
        "${display}s"
    } else {
        "1/${(1.0 / value).toInt().coerceAtLeast(1)}"
    }
}

private fun formatLuma(value: Double): String {
    return String.format(Locale.getDefault(), "%.0f", value)
}

@Composable
private fun exposureModeLabel(mode: ExposureMode): String {
    return when (mode) {
        ExposureMode.APERTURE_PRIORITY -> stringResource(R.string.exposure_mode_aperture_priority)
        ExposureMode.SHUTTER_PRIORITY -> stringResource(R.string.exposure_mode_shutter_priority)
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
private fun meterStatusLabel(uiState: MeterUiState): String {
    val cameraError = uiState.cameraError
    if (cameraError != null) {
        return cameraError
    }

    return when (uiState.meterStatus) {
        MeterStatus.WAITING -> stringResource(R.string.status_waiting_live_meter)
        MeterStatus.LOCKED -> stringResource(R.string.status_ae_lock_active)
        MeterStatus.LIVE -> stringResource(R.string.status_live_metering)
    }
}
