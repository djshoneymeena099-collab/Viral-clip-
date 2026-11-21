package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ExportHistoryEntity
import com.example.data.model.ExportPlatform
import com.example.data.model.formatFileSize
import com.example.data.model.formatTimecodeMs
import com.example.ui.components.GlassStudioCard
import com.example.ui.components.PlatformPill
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ViralGradients
import com.example.ui.theme.VoidBlack
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportsHistoryScreen(
    exports: List<ExportHistoryEntity>,
    onPlayExport: (ExportHistoryEntity) -> Unit,
    onShareExport: (ExportHistoryEntity) -> Unit,
    onDeleteExport: (ExportHistoryEntity) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViralGradients.screenBackdrop),
        contentAlignment = Alignment.TopCenter
    ) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Download History",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Saved 9:16 MP4 files in Movies/ViralClipAI",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    if (exports.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear All", color = AlertCrimson)
                        }
                    }
                }
            }

            if (exports.isEmpty()) {
                item {
                    GlassStudioCard(highlighted = true) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DownloadDone,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "No Exported MP4 Clips Yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Generate clips in Create or open any clip in the 9:16 Editor and tap Export MP4 to save ready-to-post Reels, Shorts, and TikToks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(exports, key = { it.id }) { item ->
                    val platform = ExportPlatform.entries.firstOrNull { it.name == item.platform }
                        ?: ExportPlatform.INSTAGRAM_REELS

                    GlassStudioCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_history_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!item.thumbnailUrlOrPath.isNullOrBlank()) {
                                val model: Any = if (item.thumbnailUrlOrPath.startsWith("http")) {
                                    item.thumbnailUrlOrPath
                                } else File(item.thumbnailUrlOrPath)
                                AsyncImage(
                                    model = model,
                                    contentDescription = item.clipTitle,
                                    modifier = Modifier
                                        .size(width = 62.dp, height = 84.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(width = 62.dp, height = 84.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ObsidianElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Movie, contentDescription = null, tint = NeonBlue)
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.clipTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.projectTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PlatformPill(platform = platform)
                                    Text(
                                        text = "${item.resolution} • ${formatTimecodeMs(item.durationMs)} • ${formatFileSize(item.fileSizeBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonBlue
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = dateFormatter.format(Date(item.exportedAtMs)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }

                            IconButton(onClick = { onDeleteExport(item) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.cd_delete_item),
                                    tint = AlertCrimson.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onPlayExport(item) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonBlue,
                                    contentColor = VoidBlack
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Play MP4", style = MaterialTheme.typography.labelLarge)
                            }
                            OutlinedButton(
                                onClick = { onShareExport(item) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                border = BorderStroke(1.dp, NeonPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
