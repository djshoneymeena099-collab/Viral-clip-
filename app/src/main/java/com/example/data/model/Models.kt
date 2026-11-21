package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class ImportSourceType(val label: String) {
    YOUTUBE_URL("YouTube Link"),
    LOCAL_VIDEO("Device Video")
}

enum class MomentType(val label: String, val emoji: String) {
    VIRAL_HOOK("Viral Hook", "🔥"),
    FUNNY("Funny", "😂"),
    EMOTIONAL("Emotional", "🥹"),
    MOTIVATIONAL("Motivational", "⚡"),
    HIGH_ENERGY("High Energy", "🚀")
}

enum class CaptionLanguage(val label: String, val nativeLabel: String) {
    ENGLISH("English", "English"),
    HINDI("Hindi", "हिन्दी"),
    HINGLISH("Hinglish", "Hinglish")
}

enum class CaptionStyle(val label: String) {
    NEON_POP("Neon Pop"),
    BOLD_VIRAL("Bold Viral"),
    MINIMAL_GLASS("Minimal Glass"),
    HIGHLIGHTER("Highlighter")
}

enum class ExportPlatform(
    val label: String,
    val shortLabel: String,
    val aspectLabel: String,
    val maxRecommendedSec: Int
) {
    INSTAGRAM_REELS("Instagram Reels", "Reels", "9:16 • 1080×1920", 90),
    YOUTUBE_SHORTS("YouTube Shorts", "Shorts", "9:16 • 1080×1920", 60),
    TIKTOK("TikTok", "TikTok", "9:16 • 1080×1920", 60)
}

enum class BackgroundMusic(val label: String, val bpm: String, val mood: String) {
    NONE("Original Audio Only", "—", "Clean"),
    LOFI_CHILL("Midnight Lo-Fi", "82 BPM", "Chill"),
    PHONK_ENERGY("Cyber Phonk", "134 BPM", "Viral"),
    CINEMATIC_RISE("Epic Horizon", "96 BPM", "Emotional"),
    UPBEAT_POP("Neon Pulse", "118 BPM", "Upbeat")
}

enum class SubscriptionPlan(
    val title: String,
    val priceMonthly: String,
    val monthlyClipLimit: Int,
    val maxClipsPerGeneration: Int,
    val tagline: String,
    val features: List<String>
) {
    FREE(
        title = "Free Starter",
        priceMonthly = "$0 / mo",
        monthlyClipLimit = 10,
        maxClipsPerGeneration = 5,
        tagline = "Try AI short clipping with zero setup",
        features = listOf(
            "10 viral clips per month",
            "Up to 5 clips per video",
            "1080×1920 Full HD MP4 export",
            "English, Hindi & Hinglish captions"
        )
    ),
    CREATOR_PRO(
        title = "Creator Pro",
        priceMonthly = "$19 / mo",
        monthlyClipLimit = 150,
        maxClipsPerGeneration = 15,
        tagline = "Built for daily Reels, Shorts & TikTok creators",
        features = listOf(
            "150 viral clips per month",
            "Generate up to 15 clips at once",
            "Smart silence removal & intro/outro cards",
            "Batch MP4 export + all caption animations"
        )
    ),
    STUDIO_ULTRA(
        title = "Studio Ultra",
        priceMonthly = "$49 / mo",
        monthlyClipLimit = 9999,
        maxClipsPerGeneration = 20,
        tagline = "Unlimited agency-grade video clipping power",
        features = listOf(
            "Unlimited monthly viral clips",
            "Bulk 20-clip AI generation per video",
            "Priority 1080×1920 Full HD rendering",
            "Custom brand hooks & multi-platform kits"
        )
    )
}

data class SubtitleCue(
    val id: Int,
    val startOffsetMs: Long,
    val endOffsetMs: Long,
    val textEnglish: String,
    val textHindi: String,
    val textHinglish: String,
    val emoji: String = "🔥"
) {
    fun textForLanguage(language: CaptionLanguage, includeEmoji: Boolean): String {
        val base = when (language) {
            CaptionLanguage.ENGLISH -> textEnglish
            CaptionLanguage.HINDI -> textHindi
            CaptionLanguage.HINGLISH -> textHinglish
        }
        return if (includeEmoji && emoji.isNotBlank()) "$base $emoji" else base
    }
}

object SubtitleCodec {
    fun encode(cues: List<SubtitleCue>): String {
        val arr = JSONArray()
        cues.forEach { cue ->
            val obj = JSONObject().apply {
                put("id", cue.id)
                put("startOffsetMs", cue.startOffsetMs)
                put("endOffsetMs", cue.endOffsetMs)
                put("en", cue.textEnglish)
                put("hi", cue.textHindi)
                put("hinglish", cue.textHinglish)
                put("emoji", cue.emoji)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decode(json: String): List<SubtitleCue> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { index ->
                val obj = arr.getJSONObject(index)
                SubtitleCue(
                    id = obj.optInt("id", index + 1),
                    startOffsetMs = obj.optLong("startOffsetMs", index * 3000L),
                    endOffsetMs = obj.optLong("endOffsetMs", (index + 1) * 3000L),
                    textEnglish = obj.optString("en", ""),
                    textHindi = obj.optString("hi", ""),
                    textHinglish = obj.optString("hinglish", ""),
                    emoji = obj.optString("emoji", "🔥")
                )
            }
        }.getOrDefault(emptyList())
    }
}

data class YouTubeMetadata(
    val videoId: String,
    val originalUrl: String,
    val title: String,
    val authorName: String,
    val thumbnailUrl: String,
    val estimatedDurationSec: Int
)

data class LocalVideoInfo(
    val uriString: String,
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val thumbnailPath: String?
)

data class ProcessingProgress(
    val isActive: Boolean = false,
    val currentStep: Int = 0,
    val totalSteps: Int = 4,
    val stageTitle: String = "",
    val stageDetail: String = "",
    val fraction: Float = 0f
)

data class ExportProgressState(
    val isExporting: Boolean = false,
    val clipTitle: String = "",
    val currentClipIndex: Int = 1,
    val totalClips: Int = 1,
    val progressPercent: Int = 0,
    val statusText: String = "",
    val lastExportedUri: String? = null,
    val lastExportedPath: String? = null,
    val errorMessage: String? = null
)

data class UserProfileSettings(
    val displayName: String = "Creator Studio",
    val handle: String = "@viralcreator",
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val clipsGeneratedThisMonth: Int = 0,
    val defaultLanguage: CaptionLanguage = CaptionLanguage.HINGLISH,
    val defaultPlatform: ExportPlatform = ExportPlatform.INSTAGRAM_REELS,
    val fullHdExport: Boolean = true,
    val autoRemoveSilence: Boolean = true,
    val defaultEmojiCaptions: Boolean = true
)

fun formatTimecodeMs(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L).toInt()
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}

fun formatDurationSec(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val hrs = safe / 3600
    val mins = (safe % 3600) / 60
    val secs = safe % 60
    return if (hrs > 0) "%d:%02d:%02d".format(hrs, mins, secs) else "%02d:%02d".format(mins, secs)
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "—"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}
