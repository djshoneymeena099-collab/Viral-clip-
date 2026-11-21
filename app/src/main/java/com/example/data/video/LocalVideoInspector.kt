package com.example.data.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.LocalVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LocalVideoInspector(private val context: Context) {

    suspend fun inspectVideo(uri: Uri): Result<LocalVideoInfo> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.coerceAtLeast(1_000L)
                ?: 30_000L

            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
                ?: 1920
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
                ?: 1080

            val displayName = resolveDisplayName(uri)
                ?: "Video_${System.currentTimeMillis() % 10000}.mp4"

            val frameTimeUs = (durationMs * 1000L / 5L).coerceAtLeast(500_000L)
            val frame = retriever.getFrameAtTime(frameTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0L)

            val thumbPath = frame?.let { bitmap ->
                saveThumbnailBitmap(bitmap)
            }
            frame?.recycle()

            Result.success(
                LocalVideoInfo(
                    uriString = uri.toString(),
                    displayName = displayName,
                    durationMs = durationMs,
                    width = width,
                    height = height,
                    thumbnailPath = thumbPath
                )
            )
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Unable to inspect selected video: ${e.localizedMessage ?: "Unsupported format"}")
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        }.getOrNull()
    }

    private fun saveThumbnailBitmap(bitmap: Bitmap): String? {
        return runCatching {
            val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
            val outFile = File(dir, "thumb_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 86, stream)
            }
            outFile.absolutePath
        }.getOrNull()
    }
}
