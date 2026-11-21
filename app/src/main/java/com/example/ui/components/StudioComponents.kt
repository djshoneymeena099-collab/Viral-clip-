package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.ExportPlatform
import com.example.data.model.ExportProgressState
import com.example.data.model.MomentType
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ViralEmerald
import com.example.ui.theme.ViralGradients
import com.example.ui.theme.VoidBlack
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GlassStudioCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val borderBrush = if (highlighted) {
        ViralGradients.subtleBorder
    } else {
        Brush.linearGradient(listOf(ObsidianBorder.copy(alpha = 0.7f), ObsidianBorder.copy(alpha = 0.25f)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(ViralGradients.cardGlow)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun NeonPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTagName: String = "neon_primary_button"
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(
                if (enabled && !isLoading) ViralGradients.primaryAction
                else Brush.horizontalGradient(listOf(ObsidianElevated, ObsidianElevated))
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = TextPrimary
                )
                Spacer(Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) TextPrimary else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) TextPrimary else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ViralScorePill(score: Int, modifier: Modifier = Modifier) {
    val accent = when {
        score >= 92 -> ViralEmerald
        score >= 84 -> NeonBlue
        else -> NeonPurple
    }
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$score% VIRAL",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MomentTypePill(momentType: MomentType, modifier: Modifier = Modifier) {
    val color = when (momentType) {
        MomentType.VIRAL_HOOK -> NeonMagenta
        MomentType.FUNNY -> HighlightAmber
        MomentType.EMOTIONAL -> NeonPurple
        MomentType.MOTIVATIONAL -> NeonBlue
        MomentType.HIGH_ENERGY -> ViralEmerald
    }
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = "${momentType.emoji} ${momentType.label}",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PlatformPill(platform: ExportPlatform, modifier: Modifier = Modifier) {
    val accent = when (platform) {
        ExportPlatform.INSTAGRAM_REELS -> NeonMagenta
        ExportPlatform.YOUTUBE_SHORTS -> AlertCrimson
        ExportPlatform.TIKTOK -> NeonBlue
    }
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Text(
            text = platform.shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SampleAiNoticeBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepViolet.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = NeonBlue,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ai_sample_notice_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.ai_sample_notice_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AnimatedNeonWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = modifier) {
        val barCount = 28
        val gap = 4.dp.toPx()
        val barWidth = ((size.width - gap * (barCount - 1)) / barCount).coerceAtLeast(3f)
        for (i in 0 until barCount) {
            val normalized = i.toFloat() / barCount.toFloat()
            val wave = if (isPlaying) {
                (sin(phase + i * 0.55f) * 0.5f + 0.5f) * 0.78f + 0.22f
            } else {
                0.28f + (sin(i * 0.8f) * 0.5f + 0.5f) * 0.35f
            }
            val barH = size.height * wave
            val x = i * (barWidth + gap)
            val y = (size.height - barH) / 2f
            val barColor = if (normalized < 0.5f) NeonPurple else NeonBlue
            drawRoundRect(
                color = barColor.copy(alpha = if (isPlaying) 0.92f else 0.55f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun ExportProgressBanner(
    state: ExportProgressState,
    onPlayExported: (String?, String?) -> Unit,
    onShareExported: (String?, String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = state.isExporting || state.lastExportedPath != null || state.errorMessage != null
    if (!visible) return

    GlassStudioCard(
        modifier = modifier.fillMaxWidth(),
        highlighted = true,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                state.isExporting -> CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = NeonBlue
                )
                state.errorMessage != null -> Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = AlertCrimson,
                    modifier = Modifier.size(22.dp)
                )
                else -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = ViralEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.errorMessage != null) "Export Issue" else state.clipTitle.ifBlank { "MP4 Export" },
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.errorMessage ?: state.statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.errorMessage != null) AlertCrimson else TextSecondary
                )
            }
            if (!state.isExporting) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss export notice",
                        tint = TextMuted
                    )
                }
            }
        }

        if (state.isExporting) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (state.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = NeonBlue,
                trackColor = ObsidianElevated
            )
        } else if (state.lastExportedPath != null && state.errorMessage == null) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onPlayExported(state.lastExportedUri, state.lastExportedPath) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("play_exported_mp4_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonBlue,
                        contentColor = VoidBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Play MP4", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onShareExported(state.lastExportedUri, state.lastExportedPath) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_exported_mp4_button"),
                    border = BorderStroke(1.dp, NeonPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

fun resolveShareableVideoUri(context: Context, mediaStoreUri: String?, filePath: String?): Uri? {
    if (!filePath.isNullOrBlank()) {
        val file = File(filePath)
        if (file.exists()) {
            return runCatching {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }.getOrNull()
        }
    }
    if (!mediaStoreUri.isNullOrBlank()) {
        return runCatching { Uri.parse(mediaStoreUri) }.getOrNull()
    }
    return null
}

fun launchVideoPlayer(context: Context, mediaStoreUri: String?, filePath: String?): Boolean {
    val uri = resolveShareableVideoUri(context, mediaStoreUri, filePath) ?: return false
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/mp4")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return runCatching {
        context.startActivity(Intent.createChooser(intent, "Play MP4 Clip"))
        true
    }.getOrDefault(false)
}

fun launchVideoShare(context: Context, mediaStoreUri: String?, filePath: String?, title: String): Boolean {
    val uri = resolveShareableVideoUri(context, mediaStoreUri, filePath) ?: return false
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "$title — Created with Viral Clip AI")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return runCatching {
        context.startActivity(Intent.createChooser(intent, "Share Viral Clip MP4"))
        true
    }.getOrDefault(false)
}
