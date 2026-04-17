package com.yourbrand.lumameter.pro.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class MeterSnackbarStatus {
    Success,
    Error,
    Warning,
}

enum class MeterSnackbarPosition {
    Top,
    Bottom,
}

private data class MeterSnackbarColors(
    val containerColor: Color,
    val textColor: Color,
    val iconTint: Color,
    val icon: ImageVector,
)

private data class MeterSnackbarVisuals(
    override val message: String,
    val status: MeterSnackbarStatus,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

suspend fun SnackbarHostState.showMeterSnackbar(
    message: String,
    status: MeterSnackbarStatus = MeterSnackbarStatus.Warning,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    showSnackbar(
        MeterSnackbarVisuals(
            message = message,
            status = status,
            duration = duration,
        ),
    )
}

@Composable
fun BoxScope.MeterSnackbarHost(
    hostState: SnackbarHostState,
    position: MeterSnackbarPosition = MeterSnackbarPosition.Top,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .align(
                when (position) {
                    MeterSnackbarPosition.Top -> Alignment.TopCenter
                    MeterSnackbarPosition.Bottom -> Alignment.BottomCenter
                },
            )
            .safeDrawingPadding()
            .padding(contentPadding),
    ) { data ->
        val status = (data.visuals as? MeterSnackbarVisuals)?.status ?: MeterSnackbarStatus.Warning
        MeterSnackbar(
            message = data.visuals.message,
            status = status,
        )
    }
}

@Composable
private fun MeterSnackbar(
    message: String,
    status: MeterSnackbarStatus,
) {
    val colors = when (status) {
        MeterSnackbarStatus.Success -> MeterSnackbarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            textColor = MaterialTheme.colorScheme.onSurface,
            iconTint = Color(0xFF2F8F4E),
            icon = Icons.Rounded.CheckCircle,
        )
        MeterSnackbarStatus.Error -> MeterSnackbarColors(
            containerColor = Color(0xFFF8E0DD),
            textColor = Color(0xFF8D352B),
            iconTint = Color(0xFF8D352B),
            icon = Icons.Rounded.ErrorOutline,
        )
        MeterSnackbarStatus.Warning -> MeterSnackbarColors(
            containerColor = Color(0xFFFFF6DD),
            textColor = Color(0xFF7A5A00),
            iconTint = Color(0xFFB07C00),
            icon = Icons.Rounded.WarningAmber,
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.containerColor,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = colors.icon,
                contentDescription = null,
                tint = colors.iconTint,
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
