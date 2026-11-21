package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.GeneratedClipEntity
import com.example.data.local.VideoProjectEntity
import com.example.data.model.BackgroundMusic
import com.example.data.model.CaptionLanguage
import com.example.data.model.CaptionStyle
import com.example.data.model.ExportPlatform
import com.example.data.model.ExportProgressState
import com.example.data.model.SubtitleCodec
import com.example.data.model.SubtitleCue
import com.example.data.model.formatTimecodeMs
import com.example.ui.components.AnimatedNeonWaveform
import com.example.ui.components.ExportProgressBanner
import com.example.ui.components.GlassStudioCard
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.components.ViralScorePill
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
import com.example.ui.theme.ViralGradients
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import java.io.File

private enum class EditorTab(val label: String, val icon: ImageVector) {
    TRIM_CROP("Trim & Crop", Icons.Rounded.ContentCut),
    CAPTIONS("Captions", Icons.Rounded.Subtitles),
    MUSIC("Music", Icons.Rounded.MusicNote),
    INTRO_OUTRO("Intro/Outro", Icons.Rounded.AutoFixHigh)
}

@Composable
fun ClipEditorScreen(
    project: VideoProjectEntity?,
    clip: GeneratedClipEntity?,
    fullHdExport: Boolean,
    exportProgress: ExportProgressState,
    isSampleAiMode: Boolean,
    onBack: () -> Unit,
    onAttachLocalVideo: (Long, Uri) -> Unit,
    onUpdateClip: ((GeneratedClipEntity) -> GeneratedClipEntity) -> Unit,
    onUpdateCueText: (Int, CaptionLanguage, String) -> Unit,
    onExportClip: (GeneratedClipEntity, ExportPlatform) -> Unit,
    onPlayExported: (String?, String?) -> Unit,
    onShareExported: (String?, String?) -> Unit,
    onDismissExportBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (project == null || clip == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ViralGradients.screenBackdrop),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a clip from Projects to open the 9:16 Editor.", color = TextSecondary)
        }
        return
    }

    val attachLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onAttachLocalVideo(project.id, uri)
    }

    val cues = remember(clip.subtitlesJson) { SubtitleCodec.decode(clip.subtitlesJson) }
    val language = CaptionLanguage.entries.firstOrNull { it.name == clip.captionLanguage }
        ?: CaptionLanguage.HINGLISH
    val captionStyle = CaptionStyle.entries.firstOrNull { it.name == clip.captionStyle }
        ?: CaptionStyle.NEON_POP
    val selectedPlatform = ExportPlatform.entries.firstOrNull { it.name == clip.exportPlatform }
        ?: ExportPlatform.INSTAGRAM_REELS
    val selectedMusic = BackgroundMusic.entries.firstOrNull { it.name == clip.backgroundMusic }
        ?: BackgroundMusic.NONE

    val clipDurationMs = (clip.endMs - clip.startMs).coerceAtLeast(5_000L)
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember(clip.id) { mutableLongStateOf(0L) }
    var activeTabIndex by remember { mutableIntStateOf(0) }

    // Simulated clock when editing a YouTube metadata-only project (no local file attached yet)
    LaunchedEffect(project.localVideoUri, isPlaying, clipDurationMs) {
        if (project.localVideoUri.isNullOrBlank()) {
            while (isPlaying) {
                delay(150L)
                currentPositionMs = (currentPositionMs + 150L) % clipDurationMs
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViralGradients.screenBackdrop),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 880.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clip.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "9:16 Vertical Editor • ${formatTimecodeMs(clip.startMs)}–${formatTimecodeMs(clip.endMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonBlue
                    )
                }
                ViralScorePill(score = clip.viralScore)
            }

            // Export Progress / Success Banner
            ExportProgressBanner(
                state = exportProgress,
                onPlayExported = onPlayExported,
                onShareExported = onShareExported,
                onDismiss = onDismissExportBanner
            )

            // 9:16 Vertical Preview Stage
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VerticalNineBySixteenPreview(
                    project = project,
                    clip = clip,
                    cues = cues,
                    language = language,
                    captionStyle = captionStyle,
                    platform = selectedPlatform,
                    clipDurationMs = clipDurationMs,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    onPositionUpdate = { currentPositionMs = it },
                    onTogglePlay = { isPlaying = !isPlaying },
                    onAttachLocalFile = {
                        attachLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )
            }

            // Editor Tool Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditorTab.entries.forEachIndexed { index, tab ->
                    val selected = activeTabIndex == index
                    FilterChip(
                        selected = selected,
                        onClick = { activeTabIndex = index },
                        leadingIcon = {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("editor_tab_${tab.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepViolet,
                            selectedLabelColor = TextPrimary,
                            selectedLeadingIconColor = NeonBlue,
                            containerColor = ObsidianElevated,
                            labelColor = TextSecondary,
                            iconColor = TextMuted
                        )
                    )
                }
            }

            // Active Editor Panel
            when (EditorTab.entries[activeTabIndex]) {
                EditorTab.TRIM_CROP -> TrimAndCropPanel(
                    projectDurationMs = project.durationMs.coerceAtLeast(15_000L),
                    clip = clip,
                    onUpdateClip = onUpdateClip
                )
                EditorTab.CAPTIONS -> CaptionsEditorPanel(
                    clip = clip,
                    language = language,
                    captionStyle = captionStyle,
                    cues = cues,
                    onUpdateClip = onUpdateClip,
                    onUpdateCueText = onUpdateCueText
                )
                EditorTab.MUSIC -> MusicEditorPanel(
                    selectedMusic = selectedMusic,
                    musicVolume = clip.musicVolume,
                    onSelectMusic = { music -> onUpdateClip { it.copy(backgroundMusic = music.name) } },
                    onVolumeChange = { vol -> onUpdateClip { it.copy(musicVolume = vol) } }
                )
                EditorTab.INTRO_OUTRO -> IntroOutroPanel(
                    clip = clip,
                    onUpdateClip = onUpdateClip
                )
            }

            // Export Options Card (1080x1920 Full HD, Reels / Shorts / TikTok)
            GlassStudioCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Export Format & Resolution", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Surface(
                        color = NeonBlue.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (fullHdExport) "1080×1920 Full HD MP4" else "720×1280 HD MP4",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportPlatform.entries.forEach { platform ->
                        val selected = selectedPlatform == platform
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateClip { it.copy(exportPlatform = platform.name) } },
                            label = { Text(platform.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                NeonPrimaryButton(
                    text = "EXPORT ${selectedPlatform.shortLabel.uppercase()} MP4 (${if (fullHdExport) "1080p" else "720p"})",
                    onClick = { onExportClip(clip, selectedPlatform) },
                    icon = Icons.Rounded.Download,
                    enabled = !exportProgress.isExporting,
                    isLoading = exportProgress.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    testTagName = "editor_export_mp4_button"
                )
            }

            if (isSampleAiMode) {
                SampleAiNoticeBanner()
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VerticalNineBySixteenPreview(
    project: VideoProjectEntity,
    clip: GeneratedClipEntity,
    cues: List<SubtitleCue>,
    language: CaptionLanguage,
    captionStyle: CaptionStyle,
    platform: ExportPlatform,
    clipDurationMs: Long,
    isPlaying: Boolean,
    currentPositionMs: Long,
    onPositionUpdate: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onAttachLocalFile: () -> Unit
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(22.dp)
    val showIntro = clip.introEnabled && clip.introText.isNotBlank() && currentPositionMs < 2_200L
    val showOutro = clip.outroEnabled && clip.outroText.isNotBlank() &&
        clipDurationMs > 3_500L && currentPositionMs >= (clipDurationMs - 2_300L)
    val activeCue = cues.firstOrNull { currentPositionMs in it.startOffsetMs..it.endOffsetMs }

    Box(
        modifier = Modifier
            .width(248.dp)
            .aspectRatio(9f / 16f)
            .clip(shape)
            .background(VoidBlack)
            .border(1.5.dp, ViralGradients.subtleBorder, shape)
            .clickable(onClick = onTogglePlay)
    ) {
        if (!project.localVideoUri.isNullOrBlank()) {
            // Real Media3 ExoPlayer Preview cropped to 9:16
            val exoPlayer = remember(project.localVideoUri, clip.startMs, clip.endMs) {
                ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.Builder()
                        .setUri(Uri.parse(project.localVideoUri))
                        .setClippingConfiguration(
                            MediaItem.ClippingConfiguration.Builder()
                                .setStartPositionMs(clip.startMs.coerceAtLeast(0L))
                                .setEndPositionMs(clip.endMs.coerceAtLeast(clip.startMs + 3_000L))
                                .build()
                        )
                        .build()
                    setMediaItem(mediaItem)
                    repeatMode = Player.REPEAT_MODE_ONE
                    prepare()
                    playWhenReady = true
                }
            }

            LaunchedEffect(isPlaying, exoPlayer) {
                exoPlayer.playWhenReady = isPlaying
            }

            LaunchedEffect(exoPlayer) {
                while (true) {
                    onPositionUpdate(exoPlayer.currentPosition.coerceAtLeast(0L))
                    delay(120L)
                }
            }

            DisposableEffect(exoPlayer) {
                onDispose { exoPlayer.release() }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { it.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // YouTube Metadata 9:16 Studio Canvas Preview
            if (!project.thumbnailUrlOrPath.isNullOrBlank()) {
                val model: Any = if (project.thumbnailUrlOrPath.startsWith("http")) {
                    project.thumbnailUrlOrPath
                } else File(project.thumbnailUrlOrPath)

                AsyncImage(
                    model = model,
                    contentDescription = project.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.25f
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NeonBlue.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = model,
                        contentDescription = project.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            AnimatedNeonWaveform(
                isPlaying = isPlaying,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp, start = 18.dp, end = 18.dp)
                    .fillMaxWidth()
                    .height(22.dp)
            )
        }

        // Top Platform Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
            color = VoidBlack.copy(alpha = 0.72f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.55f))
        ) {
            Text(
                text = "9:16 • ${platform.shortLabel.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Live Intro Banner Overlay
        if (showIntro) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp, start = 14.dp, end = 14.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ViralGradients.primaryAction)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = clip.introText,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Live Synchronized Animated Captions Overlay
        if (activeCue != null && !showOutro) {
            val captionText = activeCue.textForLanguage(language, clip.emojiCaptionsEnabled)
            if (captionText.isNotBlank()) {
                val boxBg = when (captionStyle) {
                    CaptionStyle.NEON_POP -> VoidBlack.copy(alpha = 0.78f)
                    CaptionStyle.BOLD_VIRAL -> Color.Black.copy(alpha = 0.72f)
                    CaptionStyle.MINIMAL_GLASS -> ObsidianCard.copy(alpha = 0.65f)
                    CaptionStyle.HIGHLIGHTER -> NeonBlue
                }
                val textColor = when (captionStyle) {
                    CaptionStyle.NEON_POP -> NeonBlue
                    CaptionStyle.BOLD_VIRAL -> Color(0xFFFFE600)
                    CaptionStyle.MINIMAL_GLASS -> TextPrimary
                    CaptionStyle.HIGHLIGHTER -> VoidBlack
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp, start = 14.dp, end = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(boxBg)
                        .then(
                            if (captionStyle == CaptionStyle.NEON_POP) {
                                Modifier.border(1.dp, NeonPurple, RoundedCornerShape(12.dp))
                            } else Modifier
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = captionText,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Live Outro Banner Overlay
        if (showOutro) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(NeonMagenta, DeepViolet)))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = clip.outroText,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Play/Pause Floating Control + Timecode
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = VoidBlack.copy(alpha = 0.78f),
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_pause),
                        tint = NeonBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${formatTimecodeMs(currentPositionMs)} / ${formatTimecodeMs(clipDurationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VoidBlack.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        if (project.localVideoUri.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAttachLocalFile),
                color = DeepViolet.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Attach MP4", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun TrimAndCropPanel(
    projectDurationMs: Long,
    clip: GeneratedClipEntity,
    onUpdateClip: ((GeneratedClipEntity) -> GeneratedClipEntity) -> Unit
) {
    GlassStudioCard {
        Text("Trim & 9:16 Smart Crop", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Start Time", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(formatTimecodeMs(clip.startMs), style = MaterialTheme.typography.labelSmall, color = NeonBlue)
        }
        Slider(
            value = clip.startMs.toFloat().coerceIn(0f, (projectDurationMs - 5_000L).coerceAtLeast(1_000L).toFloat()),
            onValueChange = { newStart ->
                val safeStart = newStart.toLong()
                val safeEnd = clip.endMs.coerceAtLeast(safeStart + 5_000L).coerceAtMost(projectDurationMs)
                onUpdateClip { it.copy(startMs = safeStart, endMs = safeEnd) }
            },
            valueRange = 0f..(projectDurationMs - 5_000L).coerceAtLeast(1_000L).toFloat(),
            colors = SliderDefaults.colors(thumbColor = NeonBlue, activeTrackColor = NeonPurple)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("End Time", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(formatTimecodeMs(clip.endMs), style = MaterialTheme.typography.labelSmall, color = NeonPurple)
        }
        val minEnd = (clip.startMs + 5_000L).coerceAtMost(projectDurationMs)
        Slider(
            value = clip.endMs.toFloat().coerceIn(minEnd.toFloat(), projectDurationMs.toFloat()),
            onValueChange = { newEnd ->
                onUpdateClip { it.copy(endMs = newEnd.toLong().coerceAtLeast(minEnd)) }
            },
            valueRange = minEnd.toFloat()..projectDurationMs.toFloat().coerceAtLeast(minEnd.toFloat() + 1f),
            colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonBlue)
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Smart Crop Focus (9:16)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
                when {
                    clip.smartCropBiasX < 0.35f -> "Left Subject"
                    clip.smartCropBiasX > 0.65f -> "Right Subject"
                    else -> "Center Auto-Track"
                },
                style = MaterialTheme.typography.labelSmall,
                color = NeonBlue
            )
        }
        Slider(
            value = clip.smartCropBiasX,
            onValueChange = { bias -> onUpdateClip { it.copy(smartCropBiasX = bias) } },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = NeonBlue, activeTrackColor = NeonPurple)
        )

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Remove Silent Portions", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(
                    "Applies Media3 silence-skipping audio processor during MP4 render",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Switch(
                checked = clip.removeSilenceEnabled,
                onCheckedChange = { checked -> onUpdateClip { it.copy(removeSilenceEnabled = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonBlue)
            )
        }
    }
}

@Composable
private fun CaptionsEditorPanel(
    clip: GeneratedClipEntity,
    language: CaptionLanguage,
    captionStyle: CaptionStyle,
    cues: List<SubtitleCue>,
    onUpdateClip: ((GeneratedClipEntity) -> GeneratedClipEntity) -> Unit,
    onUpdateCueText: (Int, CaptionLanguage, String) -> Unit
) {
    GlassStudioCard {
        Text("AI Subtitles & Animated Captions", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        // Language switcher (English / Hindi / Hinglish)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionLanguage.entries.forEach { lang ->
                FilterChip(
                    selected = language == lang,
                    onClick = { onUpdateClip { it.copy(captionLanguage = lang.name) } },
                    label = { Text(lang.nativeLabel) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepViolet,
                        selectedLabelColor = TextPrimary,
                        containerColor = ObsidianElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("Caption Visual Style", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionStyle.entries.forEach { style ->
                FilterChip(
                    selected = captionStyle == style,
                    onClick = { onUpdateClip { it.copy(captionStyle = style.name) } },
                    label = { Text(style.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonBlue.copy(alpha = 0.22f),
                        selectedLabelColor = NeonBlue,
                        containerColor = ObsidianElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Emoji Captions 🔥😂⚡", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Switch(
                checked = clip.emojiCaptionsEnabled,
                onCheckedChange = { checked -> onUpdateClip { it.copy(emojiCaptionsEnabled = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonPurple)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Animated Word Highlight", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Switch(
                checked = clip.animatedCaptionsEnabled,
                onCheckedChange = { checked -> onUpdateClip { it.copy(animatedCaptionsEnabled = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonBlue)
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "Synchronized Subtitle Cues (${language.nativeLabel})",
            style = MaterialTheme.typography.titleSmall,
            color = NeonBlue
        )
        Spacer(Modifier.height(8.dp))
        cues.forEach { cue ->
            val currentText = cue.textForLanguage(language, includeEmoji = false)
            OutlinedTextField(
                value = currentText,
                onValueChange = { updated -> onUpdateCueText(cue.id, language, updated) },
                label = {
                    Text("${formatTimecodeMs(cue.startOffsetMs)} – ${formatTimecodeMs(cue.endOffsetMs)}  ${cue.emoji}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = NeonBlue,
                    unfocusedLabelColor = TextMuted
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun MusicEditorPanel(
    selectedMusic: BackgroundMusic,
    musicVolume: Float,
    onSelectMusic: (BackgroundMusic) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    GlassStudioCard {
        Text("Background Music Preset", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BackgroundMusic.entries.forEach { track ->
                val selected = track == selectedMusic
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectMusic(track) },
                    color = if (selected) DeepViolet.copy(alpha = 0.25f) else ObsidianElevated,
                    border = BorderStroke(1.dp, if (selected) NeonBlue else ObsidianBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = if (selected) NeonBlue else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(track.label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("${track.mood} • ${track.bpm}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        if (selected) {
                            Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = NeonBlue)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Music Mix Volume", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("${(musicVolume * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = NeonPurple)
        }
        Slider(
            value = musicVolume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonBlue)
        )
    }
}

@Composable
private fun IntroOutroPanel(
    clip: GeneratedClipEntity,
    onUpdateClip: ((GeneratedClipEntity) -> GeneratedClipEntity) -> Unit
) {
    GlassStudioCard {
        Text("Intro Hook & Outro CTA Cards", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable 2s Intro Hook Card", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Switch(
                checked = clip.introEnabled,
                onCheckedChange = { checked -> onUpdateClip { it.copy(introEnabled = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonBlue)
            )
        }
        OutlinedTextField(
            value = clip.introText,
            onValueChange = { txt -> onUpdateClip { it.copy(introText = txt) } },
            label = { Text("Intro Hook Text") },
            enabled = clip.introEnabled,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = ObsidianBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable 2s Outro CTA Card", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Switch(
                checked = clip.outroEnabled,
                onCheckedChange = { checked -> onUpdateClip { it.copy(outroEnabled = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonMagenta)
            )
        }
        OutlinedTextField(
            value = clip.outroText,
            onValueChange = { txt -> onUpdateClip { it.copy(outroText = txt) } },
            label = { Text("Outro Call-to-Action Text") },
            enabled = clip.outroEnabled,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonMagenta,
                unfocusedBorderColor = ObsidianBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
