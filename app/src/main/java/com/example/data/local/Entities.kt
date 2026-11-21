package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "video_projects")
data class VideoProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val channelOrAuthor: String,
    val sourceType: String, // YOUTUBE_URL or LOCAL_VIDEO
    val sourceUrl: String,
    val localVideoUri: String?,
    val thumbnailUrlOrPath: String?,
    val durationMs: Long,
    val requestedClipCount: Int,
    val targetClipDurationSec: Int,
    val captionLanguage: String,
    val targetPlatform: String,
    val isSampleAi: Boolean = true,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "generated_clips",
    foreignKeys = [
        ForeignKey(
            entity = VideoProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class GeneratedClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectId: Long,
    val clipIndex: Int,
    val title: String,
    val hookLine: String,
    val momentType: String, // MomentType.name
    val viralScore: Int,
    val viralReason: String,
    val startMs: Long,
    val endMs: Long,
    val captionLanguage: String, // CaptionLanguage.name
    val captionStyle: String, // CaptionStyle.name
    val emojiCaptionsEnabled: Boolean = true,
    val animatedCaptionsEnabled: Boolean = true,
    val smartCropBiasX: Float = 0.5f, // 0f = left, 0.5f = center, 1f = right
    val removeSilenceEnabled: Boolean = true,
    val backgroundMusic: String = "NONE",
    val musicVolume: Float = 0.25f,
    val introEnabled: Boolean = true,
    val introText: String = "",
    val outroEnabled: Boolean = true,
    val outroText: String = "Follow for more viral drops 🔥",
    val exportPlatform: String = "INSTAGRAM_REELS",
    val subtitlesJson: String = "[]",
    val isSampleAi: Boolean = true,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "export_history")
data class ExportHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectId: Long,
    val clipId: Long,
    val clipTitle: String,
    val projectTitle: String,
    val platform: String,
    val resolution: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val filePath: String,
    val mediaStoreUri: String?,
    val thumbnailUrlOrPath: String?,
    val exportedAtMs: Long = System.currentTimeMillis()
)
