package com.example.data.network

import com.example.data.model.YouTubeMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class YouTubeMetadataService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    fun extractVideoId(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return null

        val patterns = listOf(
            Regex("""(?:youtube\.com/watch\?.*v=)([A-Za-z0-9_-]{11})"""),
            Regex("""(?:youtu\.be/)([A-Za-z0-9_-]{11})"""),
            Regex("""(?:youtube\.com/shorts/)([A-Za-z0-9_-]{11})"""),
            Regex("""(?:youtube\.com/embed/)([A-Za-z0-9_-]{11})"""),
            Regex("""(?:youtube\.com/live/)([A-Za-z0-9_-]{11})""")
        )
        for (regex in patterns) {
            val match = regex.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }
        if (Regex("""^[A-Za-z0-9_-]{11}$""").matches(trimmed)) {
            return trimmed
        }
        return null
    }

    suspend fun fetchMetadata(rawUrl: String): Result<YouTubeMetadata> = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(rawUrl)
            ?: return@withContext Result.failure(
                IllegalArgumentException("Please enter a valid YouTube video URL (youtube.com/watch?v=… or youtu.be/…).")
            )

        val canonicalUrl = "https://www.youtube.com/watch?v=$videoId"
        val encodedUrl = URLEncoder.encode(canonicalUrl, "UTF-8")
        val oEmbedUrl = "https://www.youtube.com/oembed?url=$encodedUrl&format=json"

        try {
            val request = Request.Builder()
                .url(oEmbedUrl)
                .header("User-Agent", "ViralClipAI-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("Could not fetch video metadata (HTTP ${response.code}). Check that the YouTube video is public.")
                    )
                }
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val title = json.optString("title", "YouTube Video ($videoId)")
                val author = json.optString("author_name", "YouTube Creator")
                val thumbnail = json.optString(
                    "thumbnail_url",
                    "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                )
                // YouTube oEmbed does not expose raw duration without API key;
                // estimate a realistic long-form duration (or exact duration when user attaches local source)
                val estimatedDurationSec = if (rawUrl.contains("/shorts/")) 58 else 640

                Result.success(
                    YouTubeMetadata(
                        videoId = videoId,
                        originalUrl = canonicalUrl,
                        title = title,
                        authorName = author,
                        thumbnailUrl = thumbnail,
                        estimatedDurationSec = estimatedDurationSec
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Network error fetching YouTube metadata: ${e.localizedMessage ?: "Check connection"}")
            )
        }
    }
}
