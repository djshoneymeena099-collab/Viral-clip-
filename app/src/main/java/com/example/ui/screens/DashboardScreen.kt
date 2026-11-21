package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.GeneratedClipEntity
import com.example.data.local.VideoProjectEntity
import com.example.data.model.CaptionLanguage
import com.example.data.model.ExportPlatform
import com.example.data.model.ExportProgressState
import com.example.data.model.ImportSourceType
import com.example.data.model.MomentType
import com.example.data.model.SubtitleCodec
import com.example.data.model.formatTimecodeMs
import com.example.ui.components.ExportProgressBanner
import com.example.ui.components.GlassStudioCard
import com.example.ui.components.MomentTypePill
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.PlatformPill
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.components.ViralScorePill
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ViralEmerald
import com.example.ui.theme.ViralGradients
import com.example.ui.theme.VoidBlack
import java.io.File

@Composable
fun DashboardScreen(
    projects: List<VideoProjectEntity>,
    selectedProject: VideoProjectEntity?,
    selectedProjectClips: List<GeneratedClipEntity>,
    exportProgress: ExportProgressState,
    isSampleAiMode: Boolean,
    onSelectProject: (Long?) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onAttachLocalVideo: (Long, android.net.Uri) -> Unit,
    onEditClip: (Long, Long) -> Unit,
    onExportClip: (GeneratedClipEntity) -> Unit,
    onExportAllClips: (Long) -> Unit,
    onPlayExported: (String?, String?) -> Unit,
    onShareExported: (String?, String?) -> Unit,
    onDismissExportBanner: () -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attachVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val projectId = selectedProject?.id
        if (uri != null && projectId != null) {
            onAttachLocalVideo(projectId, uri)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViralGradients.screenBackdrop),
        contentAlignment = Alignment.TopCenter
    ) {
        if (selectedProject != null) {
            // Project Detail -> Generated Clips Workspace
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSelectProject(null) },
                            modifier = Modifier.testTag("project_detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = TextPrimary
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedProject.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${selectedProject.channelOrAuthor} • ${selectedProjectClips.size} viral clips",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                item {
                    ExportProgressBanner(
                        state = exportProgress,
                        onPlayExported = onPlayExported,
                        onShareExported = onShareExported,
                        onDismiss = onDismissExportBanner
                    )
                }

                item {
                    GlassStudioCard(highlighted = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProjectThumbnailBox(
                                thumbnail = selectedProject.thumbnailUrlOrPath,
                                modifier = Modifier.size(width = 104.dp, height = 64.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!selectedProject.localVideoUri.isNullOrBlank())
                                        "Local Video Connected" else "YouTube Metadata Mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (!selectedProject.localVideoUri.isNullOrBlank()) ViralEmerald else NeonBlue
                                )
                                Text(
                                    text = "Duration ${formatTimecodeMs(selectedProject.durationMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(6.dp))
                                if (selectedProject.localVideoUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            attachVideoLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Attach Source MP4", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        NeonPrimaryButton(
                            text = "EXPORT ALL ${selectedProjectClips.size} CLIPS (MP4)",
                            onClick = { onExportAllClips(selectedProject.id) },
                            icon = Icons.Rounded.DownloadForOffline,
                            enabled = !exportProgress.isExporting && selectedProjectClips.isNotEmpty(),
                            isLoading = exportProgress.isExporting,
                            modifier = Modifier.fillMaxWidth(),
                            testTagName = "export_all_clips_button"
                        )
                    }
                }

                if (isSampleAiMode) {
                    item { SampleAiNoticeBanner() }
                }

                items(selectedProjectClips, key = { it.id }) { clip ->
                    GeneratedClipCard(
                        clip = clip,
                        isExporting = exportProgress.isExporting,
                        onEdit = { onEditClip(selectedProject.id, clip.id) },
                        onExport = { onExportClip(clip) }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        } else {
            // Projects Overview & Recent Videos
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Creator Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "My Projects, Recent Videos & Generated Clips",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Button(
                            onClick = onNavigateToCreate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepViolet,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dashboard_new_project_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New")
                        }
                    }
                }

                if (projects.isEmpty()) {
                    item {
                        EmptyProjectsCard(onNavigateToCreate = onNavigateToCreate)
                    }
                } else {
                    // Recent Videos Horizontal Carousel
                    item {
                        Text(
                            text = "Recent Videos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(projects.take(6), key = { "recent_${it.id}" }) { project ->
                                RecentVideoTile(
                                    project = project,
                                    onClick = { onSelectProject(project.id) }
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "My Projects (${projects.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    items(projects, key = { it.id }) { project ->
                        ProjectListCard(
                            project = project,
                            onOpen = { onSelectProject(project.id) },
                            onDelete = { onDeleteProject(project.id) }
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyProjectsCard(onNavigateToCreate: () -> Unit) {
    GlassStudioCard(highlighted = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_studio_1795237611926),
                contentDescription = stringResource(R.string.cd_empty_projects),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your Viral Studio Awaits",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Paste a YouTube link or import a long video to automatically generate 5 to 20 ready-to-post 9:16 Reels, Shorts, and TikToks.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))
            NeonPrimaryButton(
                text = "CREATE MY FIRST CLIPS",
                onClick = onNavigateToCreate,
                icon = Icons.Rounded.AutoAwesome,
                modifier = Modifier.fillMaxWidth(),
                testTagName = "empty_create_clips_button"
            )
        }
    }
}

@Composable
private fun RecentVideoTile(
    project: VideoProjectEntity,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .width(196.dp)
            .clip(shape)
            .background(ObsidianElevated)
            .border(1.dp, ObsidianBorder, shape)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        ProjectThumbnailBox(
            thumbnail = project.thumbnailUrlOrPath,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = project.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${project.requestedClipCount} clips • ${formatTimecodeMs(project.durationMs)}",
            style = MaterialTheme.typography.labelSmall,
            color = NeonBlue
        )
    }
}

@Composable
private fun ProjectListCard(
    project: VideoProjectEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val sourceLabel = if (project.sourceType == ImportSourceType.LOCAL_VIDEO.name) "Device Video" else "YouTube"
    val platform = ExportPlatform.entries.firstOrNull { it.name == project.targetPlatform }
        ?: ExportPlatform.INSTAGRAM_REELS

    GlassStudioCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProjectThumbnailBox(
                thumbnail = project.thumbnailUrlOrPath,
                modifier = Modifier.size(width = 96.dp, height = 64.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformPill(platform = platform)
                    Text(
                        text = "$sourceLabel • ${project.requestedClipCount} clips",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_project_${project.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.cd_delete_item),
                    tint = AlertCrimson.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun GeneratedClipCard(
    clip: GeneratedClipEntity,
    isExporting: Boolean,
    onEdit: () -> Unit,
    onExport: () -> Unit
) {
    val moment = MomentType.entries.firstOrNull { it.name == clip.momentType } ?: MomentType.VIRAL_HOOK
    val language = CaptionLanguage.entries.firstOrNull { it.name == clip.captionLanguage } ?: CaptionLanguage.HINGLISH
    val platform = ExportPlatform.entries.firstOrNull { it.name == clip.exportPlatform } ?: ExportPlatform.INSTAGRAM_REELS
    val previewCue = SubtitleCodec.decode(clip.subtitlesJson).firstOrNull()

    GlassStudioCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generated_clip_card_${clip.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MomentTypePill(momentType = moment)
            ViralScorePill(score = clip.viralScore)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = clip.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = clip.viralReason,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(Modifier.height(10.dp))

        // Timecode + Format metadata bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = VoidBlack.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Text(
                    text = "${formatTimecodeMs(clip.startMs)} – ${formatTimecodeMs(clip.endMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            PlatformPill(platform = platform)
            Text(
                text = "Captions: ${language.nativeLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        if (previewCue != null) {
            Spacer(Modifier.height(10.dp))
            Surface(
                color = DeepViolet.copy(alpha = 0.14f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "“${previewCue.textForLanguage(language, clip.emojiCaptionsEnabled)}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("edit_clip_button_${clip.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ObsidianElevated,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Edit 9:16 Clip", style = MaterialTheme.typography.labelLarge)
            }

            Button(
                onClick = onExport,
                enabled = !isExporting,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("export_clip_button_${clip.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue,
                    contentColor = VoidBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export MP4", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ProjectThumbnailBox(
    thumbnail: String?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    if (!thumbnail.isNullOrBlank()) {
        val model: Any = if (thumbnail.startsWith("http")) thumbnail else File(thumbnail)
        AsyncImage(
            model = model,
            contentDescription = "Project thumbnail",
            modifier = modifier
                .clip(shape)
                .background(ObsidianElevated),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(ObsidianElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Movie, contentDescription = null, tint = NeonBlue)
        }
    }
}
