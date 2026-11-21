package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CaptionLanguage
import com.example.data.model.ExportPlatform
import com.example.data.model.ImportSourceType
import com.example.data.model.MomentType
import com.example.data.model.ProcessingProgress
import com.example.data.model.UserProfileSettings
import com.example.data.model.formatDurationSec
import com.example.data.model.formatTimecodeMs
import com.example.ui.components.AnimatedNeonWaveform
import com.example.ui.components.GlassStudioCard
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.SampleAiNoticeBanner
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
import com.example.viewmodel.ImportFormState
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateStudioScreen(
    importState: ImportFormState,
    processingProgress: ProcessingProgress,
    userSettings: UserProfileSettings,
    isSampleAiMode: Boolean,
    onSelectSourceType: (ImportSourceType) -> Unit,
    onYouTubeUrlChange: (String) -> Unit,
    onFetchYouTube: () -> Unit,
    onLocalVideoPicked: (android.net.Uri) -> Unit,
    onClipCountChange: (Int) -> Unit,
    onDurationSecChange: (Int) -> Unit,
    onToggleMoment: (MomentType) -> Unit,
    onLanguageChange: (CaptionLanguage) -> Unit,
    onPlatformChange: (ExportPlatform) -> Unit,
    onGenerateClips: () -> Unit,
    onOpenPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onLocalVideoPicked(uri)
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onLocalVideoPicked(uri)
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Futuristic Hero Landing Section
            HeroLandingCard(
                userSettings = userSettings,
                onCtaClick = {
                    scope.launch {
                        scrollState.animateScrollTo(680)
                    }
                },
                onOpenPlans = onOpenPlans
            )

            // 2. Live Processing System Panel (shown during AI analysis)
            AnimatedVisibility(visible = processingProgress.isActive) {
                ProcessingProgressCard(progress = processingProgress)
            }

            // 3. Step 1: Import Source Card (YouTube URL + Local Video)
            GlassStudioCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepNumberBadge("1")
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Import Your Long Video",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "Paste a YouTube link or upload directly from device",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Source Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VoidBlack.copy(alpha = 0.7f))
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ImportSourceType.entries.forEach { source ->
                        val selected = importState.sourceType == source
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) ViralGradients.primaryAction
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { onSelectSourceType(source) }
                                .testTag("source_tab_${source.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (source == ImportSourceType.YOUTUBE_URL)
                                        Icons.Rounded.Link else Icons.Rounded.VideoLibrary,
                                    contentDescription = null,
                                    tint = if (selected) TextPrimary else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = source.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (importState.sourceType == ImportSourceType.YOUTUBE_URL) {
                    OutlinedTextField(
                        value = importState.youtubeUrlInput,
                        onValueChange = onYouTubeUrlChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_url_input"),
                        placeholder = {
                            Text(
                                "https://www.youtube.com/watch?v=...",
                                color = TextMuted
                            )
                        },
                        label = { Text("Paste YouTube Video URL") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Rounded.PlayCircleOutline, contentDescription = null, tint = NeonBlue)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = NeonBlue,
                            unfocusedLabelColor = TextMuted,
                            cursorColor = NeonBlue
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onFetchYouTube,
                            enabled = !importState.isFetchingMetadata,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("fetch_youtube_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianElevated,
                                contentColor = NeonBlue
                            ),
                            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (importState.isFetchingMetadata) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonBlue
                                )
                            } else {
                                Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Fetch Video Info", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("attach_source_video_button"),
                            border = BorderStroke(1.dp, ObsidianBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (importState.selectedLocalVideo != null) "Source Linked" else "Attach File",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.youtube_copyright_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    // Fetched YouTube Metadata Preview
                    importState.fetchedYouTube?.let { yt ->
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = VoidBlack.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ViralEmerald.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = yt.thumbnailUrl,
                                    contentDescription = yt.title,
                                    modifier = Modifier
                                        .size(width = 98.dp, height = 62.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = ViralEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Metadata Ready",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ViralEmerald
                                        )
                                    }
                                    Text(
                                        text = yt.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${yt.authorName} • Est. ${formatDurationSec(yt.estimatedDurationSec)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Direct Local Video Upload (Zero-Permission Photo Picker + SAF File Picker)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("pick_gallery_video_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianElevated,
                                contentColor = TextPrimary
                            ),
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.VideoLibrary, contentDescription = null, tint = NeonPurple)
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery Video", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { openDocumentLauncher.launch(arrayOf("video/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("pick_file_video_button"),
                            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = NeonBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("Browse Files", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    importState.selectedLocalVideo?.let { local ->
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = VoidBlack.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ViralEmerald.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!local.thumbnailPath.isNullOrBlank()) {
                                    AsyncImage(
                                        model = File(local.thumbnailPath),
                                        contentDescription = local.displayName,
                                        modifier = Modifier
                                            .size(width = 96.dp, height = 60.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 96.dp, height = 60.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ObsidianElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.MovieCreation, contentDescription = null, tint = NeonBlue)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = local.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Duration: ${formatTimecodeMs(local.durationMs)} • ${local.width}×${local.height}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonBlue
                                    )
                                    Text(
                                        text = "Ready for 9:16 Full HD MP4 clipping",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ViralEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Step 2: AI Clipping Configuration Card
            GlassStudioCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepNumberBadge("2")
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Viral Clipping Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Choose clip count, duration, mood & multilingual subtitles",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Number of Short Clips (5, 10, 15, 20)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Number of Viral Clips", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        "${userSettings.plan.title} • Max ${userSettings.plan.maxClipsPerGeneration}/run",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonBlue
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { count ->
                        val locked = count > userSettings.plan.maxClipsPerGeneration
                        val selected = importState.requestedClipCount == count
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onClipCountChange(count) }
                                .testTag("clip_count_$count"),
                            color = when {
                                selected -> DeepViolet
                                locked -> VoidBlack.copy(alpha = 0.45f)
                                else -> ObsidianElevated
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (selected) NeonBlue else ObsidianBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (locked) {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = "Requires higher plan",
                                        tint = HighlightAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) TextPrimary else if (locked) TextMuted else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Custom Clip Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Clip Duration", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        "${importState.targetClipDurationSec} seconds",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple
                    )
                }
                Slider(
                    value = importState.targetClipDurationSec.toFloat(),
                    onValueChange = { onDurationSecChange(it.toInt()) },
                    valueRange = 10f..90f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonBlue,
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = ObsidianElevated
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { sec ->
                        val active = importState.targetClipDurationSec == sec
                        FilterChip(
                            selected = active,
                            onClick = { onDurationSecChange(sec) },
                            label = { Text("${sec}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Viral Moments Detection
                Text("AI Moment Detection", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MomentType.entries.forEach { moment ->
                        val selected = moment in importState.selectedMoments
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleMoment(moment) },
                            label = { Text("${moment.emoji} ${moment.label}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonBlue.copy(alpha = 0.2f),
                                selectedLabelColor = NeonBlue,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = ObsidianBorder,
                                selectedBorderColor = NeonBlue
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // AI Caption Language (English / Hindi / Hinglish)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Subtitles, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AI Subtitles Language", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CaptionLanguage.entries.forEach { lang ->
                        val selected = importState.captionLanguage == lang
                        FilterChip(
                            selected = selected,
                            onClick = { onLanguageChange(lang) },
                            label = { Text(lang.nativeLabel) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("caption_lang_${lang.name.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple.copy(alpha = 0.28f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Target Export Platform Preset
                Text("Export Platform Preset (9:16 Full HD)", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportPlatform.entries.forEach { platform ->
                        val selected = importState.targetPlatform == platform
                        FilterChip(
                            selected = selected,
                            onClick = { onPlatformChange(platform) },
                            label = { Text(platform.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta.copy(alpha = 0.22f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = ObsidianBorder,
                                selectedBorderColor = NeonMagenta
                            )
                        )
                    }
                }

                if (importState.errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = AlertCrimson.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = importState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = AlertCrimson,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                NeonPrimaryButton(
                    text = "GENERATE ${importState.requestedClipCount} VIRAL CLIPS",
                    onClick = onGenerateClips,
                    icon = Icons.Rounded.AutoAwesome,
                    enabled = !processingProgress.isActive,
                    isLoading = processingProgress.isActive,
                    modifier = Modifier.fillMaxWidth(),
                    testTagName = "generate_viral_clips_button"
                )
            }

            if (isSampleAiMode) {
                SampleAiNoticeBanner()
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroLandingCard(
    userSettings: UserProfileSettings,
    onCtaClick: () -> Unit,
    onOpenPlans: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, ViralGradients.subtleBorder, shape)
            .background(ObsidianCard)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_studio_1795237610500),
            contentDescription = stringResource(R.string.cd_hero_art),
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp),
            contentScale = ContentScale.Crop
        )

        // Atmospheric obsidian-to-neon overlay so headline pops crisply
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoidBlack.copy(alpha = 0.45f),
                            VoidBlack.copy(alpha = 0.72f),
                            VoidBlack.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = NeonPurple.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.hero_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenPlans),
                    color = VoidBlack.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, HighlightAmber.copy(alpha = 0.6f))
                ) {
                    val remaining = (userSettings.plan.monthlyClipLimit - userSettings.clipsGeneratedThisMonth)
                        .coerceAtLeast(0)
                    Text(
                        text = if (userSettings.plan.monthlyClipLimit >= 9999)
                            "${userSettings.plan.title} • ∞"
                        else "${userSettings.plan.title} • $remaining left",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighlightAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.hero_headline),
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )

            Text(
                text = stringResource(R.string.hero_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(6.dp))

            NeonPrimaryButton(
                text = stringResource(R.string.hero_cta),
                onClick = onCtaClick,
                icon = Icons.Rounded.MovieCreation,
                modifier = Modifier.fillMaxWidth(),
                testTagName = "hero_create_viral_clips_button"
            )
        }
    }
}

@Composable
private fun ProcessingProgressCard(progress: ProcessingProgress) {
    GlassStudioCard(highlighted = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = NeonBlue
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = progress.stageTitle.ifBlank { "Analyzing Video…" },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = progress.stageDetail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = "${(progress.fraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = NeonBlue
            )
        }

        Spacer(Modifier.height(12.dp))
        AnimatedNeonWaveform(
            isPlaying = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        )
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress.fraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = NeonPurple,
            trackColor = ObsidianElevated
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Step ${progress.currentStep} of ${progress.totalSteps}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun StepNumberBadge(number: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(ViralGradients.primaryAction),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary
        )
    }
}
