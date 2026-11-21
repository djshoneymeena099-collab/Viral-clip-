package com.example.data.video

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.data.local.GeneratedClipEntity
import com.example.data.local.VideoProjectEntity
import com.example.data.model.CaptionLanguage
import com.example.data.model.CaptionStyle
import com.example.data.model.ExportPlatform
import com.example.data.model.SubtitleCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ExportedMp4Result(
    val filePath: String,
    val mediaStoreUri: String?,
    val fileSizeBytes: Long,
    val resolution: String,
    val durationMs: Long
)

class VideoExportEngine(private val context: Context) {

    private val httpClient = OkHttpClient()

    @OptIn(UnstableApi::class)
    suspend fun exportClipToMp4(
        project: VideoProjectEntity,
        clip: GeneratedClipEntity,
        fullHd: Boolean,
        onProgress: (Int) -> Unit
    ): Result<ExportedMp4Result> {
        val outWidth = if (fullHd) 1080 else 720
        val outHeight = if (fullHd) 1920 else 1280
        val resolutionLabel = "${outWidth}×${outHeight}"

        val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
        val sanitizedTitle = clip.title
            .replace(Regex("""[^A-Za-z0-9]+"""), "_")
            .trim('_')
            .take(36)
            .ifBlank { "viral_clip" }
        val fileName = "ViralClip_${sanitizedTitle}_${System.currentTimeMillis() % 100000}.mp4"
        val tempOutputFile = File(exportsDir, fileName)
        if (tempOutputFile.exists()) tempOutputFile.delete()

        val clipDurationMs = (clip.endMs - clip.startMs).coerceIn(5_000L, 90_000L)
        val cues = SubtitleCodec.decode(clip.subtitlesJson)
        val language = CaptionLanguage.entries.firstOrNull { it.name == clip.captionLanguage }
            ?: CaptionLanguage.HINGLISH
        val style = CaptionStyle.entries.firstOrNull { it.name == clip.captionStyle }
            ?: CaptionStyle.NEON_POP
        val platform = ExportPlatform.entries.firstOrNull { it.name == clip.exportPlatform }
            ?: ExportPlatform.INSTAGRAM_REELS

        return try {
            val subtitleOverlay = SubtitleBitmapOverlay(
                width = outWidth,
                height = outHeight,
                clipDurationMs = clipDurationMs,
                cues = cues,
                language = language,
                captionStyle = style,
                includeEmojis = clip.emojiCaptionsEnabled,
                animatedHighlight = clip.animatedCaptionsEnabled,
                introEnabled = clip.introEnabled,
                introText = clip.introText,
                outroEnabled = clip.outroEnabled,
                outroText = clip.outroText,
                platform = platform
            )

            val editedMediaItem = if (!project.localVideoUri.isNullOrBlank()) {
                val inputUri = Uri.parse(project.localVideoUri)
                val mediaItem = MediaItem.Builder()
                    .setUri(inputUri)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(clip.startMs.coerceAtLeast(0L))
                            .setEndPositionMs(clip.endMs.coerceAtLeast(clip.startMs + 3_000L))
                            .build()
                    )
                    .build()

                val videoEffects: List<Effect> = listOf(
                    Presentation.createForWidthAndHeight(
                        outWidth,
                        outHeight,
                        Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
                    ),
                    OverlayEffect(listOf(subtitleOverlay))
                )
                val audioProcessors: List<AudioProcessor> = if (clip.removeSilenceEnabled) {
                    listOf(SilenceSkippingAudioProcessor().apply { setEnabled(true) })
                } else {
                    emptyList()
                }

                EditedMediaItem.Builder(mediaItem)
                    .setEffects(Effects(audioProcessors, videoEffects))
                    .build()
            } else {
                // Authorized YouTube metadata mode: render a real 1080x1920 vertical kinetic-caption Reel
                val posterFile = buildVerticalPosterImage(
                    thumbnailUrlOrPath = project.thumbnailUrlOrPath,
                    width = outWidth,
                    height = outHeight,
                    title = project.title
                )
                val promoDurationMs = clipDurationMs.coerceAtMost(15_000L)
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.fromFile(posterFile))
                    .setImageDurationMs(promoDurationMs)
                    .build()

                val videoEffects: List<Effect> = listOf(
                    Presentation.createForWidthAndHeight(
                        outWidth,
                        outHeight,
                        Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
                    ),
                    OverlayEffect(listOf(subtitleOverlay))
                )

                EditedMediaItem.Builder(mediaItem)
                    .setFrameRate(24)
                    .setRemoveAudio(true)
                    .setEffects(Effects(emptyList(), videoEffects))
                    .build()
            }

            runTransformerOnMainThread(
                editedMediaItem = editedMediaItem,
                outputPath = tempOutputFile.absolutePath,
                onProgress = onProgress
            )

            val savedMediaStoreUri = saveToPublicGallery(tempOutputFile, fileName)
            val fileSize = tempOutputFile.length()

            Result.success(
                ExportedMp4Result(
                    filePath = tempOutputFile.absolutePath,
                    mediaStoreUri = savedMediaStoreUri?.toString(),
                    fileSizeBytes = fileSize,
                    resolution = resolutionLabel,
                    durationMs = clipDurationMs
                )
            )
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("MP4 Export failed: ${e.localizedMessage ?: "Codec error"}")
            )
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun runTransformerOnMainThread(
        editedMediaItem: EditedMediaItem,
        outputPath: String,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val handler = Handler(Looper.getMainLooper())
            val progressHolder = ProgressHolder()
            var finished = false

            val listener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    finished = true
                    onProgress(100)
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    finished = true
                    if (continuation.isActive) {
                        continuation.resumeWithException(exportException)
                    }
                }
            }

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .addListener(listener)
                .build()

            transformer.start(editedMediaItem, outputPath)

            val poller = object : Runnable {
                override fun run() {
                    if (finished || !continuation.isActive) return
                    val state = transformer.getProgress(progressHolder)
                    if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress(progressHolder.progress.coerceIn(1, 99))
                    }
                    if (!finished && state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                        handler.postDelayed(this, 250L)
                    }
                }
            }
            handler.postDelayed(poller, 250L)

            continuation.invokeOnCancellation {
                finished = true
                handler.removeCallbacks(poller)
                transformer.cancel()
            }
        }
    }

    private suspend fun buildVerticalPosterImage(
        thumbnailUrlOrPath: String?,
        width: Int,
        height: Int,
        title: String
    ): File = withContext(Dispatchers.IO) {
        val posterBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(posterBitmap)

        // Futuristic dark gradient canvas
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    Color.parseColor("#0B0719"),
                    Color.parseColor("#06060B"),
                    Color.parseColor("#061724")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val thumbBitmap = loadThumbnailBitmap(thumbnailUrlOrPath)
        if (thumbBitmap != null) {
            // Soft dimmed full-bleed background copy
            val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 55 }
            val scaleFull = maxOf(width.toFloat() / thumbBitmap.width, height.toFloat() / thumbBitmap.height)
            val fullW = (thumbBitmap.width * scaleFull).toInt()
            val fullH = (thumbBitmap.height * scaleFull).toInt()
            val leftFull = (width - fullW) / 2
            val topFull = (height - fullH) / 2
            canvas.drawBitmap(
                thumbBitmap,
                null,
                Rect(leftFull, topFull, leftFull + fullW, topFull + fullH),
                dimPaint
            )

            // Crisp centered 16:9 hero preview card inside 9:16 vertical frame
            val cardW = (width * 0.90f).toInt()
            val cardH = (cardW * 9f / 16f).toInt()
            val cardLeft = (width - cardW) / 2
            val cardTop = (height * 0.30f).toInt()

            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    cardLeft.toFloat(), cardTop.toFloat(),
                    (cardLeft + cardW).toFloat(), (cardTop + cardH).toFloat(),
                    Color.parseColor("#A855F7"),
                    Color.parseColor("#00E5FF"),
                    Shader.TileMode.CLAMP
                )
            }
            val borderPx = (width * 0.008f).toInt().coerceAtLeast(6)
            canvas.drawRect(
                (cardLeft - borderPx).toFloat(),
                (cardTop - borderPx).toFloat(),
                (cardLeft + cardW + borderPx).toFloat(),
                (cardTop + cardH + borderPx).toFloat(),
                glowPaint
            )
            canvas.drawBitmap(
                thumbBitmap,
                null,
                Rect(cardLeft, cardTop, cardLeft + cardW, cardTop + cardH),
                Paint(Paint.ANTI_ALIAS_FLAG)
            )
            thumbBitmap.recycle()
        } else {
            // Draw decorative neon accent lines if offline
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, height * 0.35f, width.toFloat(), height * 0.65f,
                    Color.parseColor("#A855F7"),
                    Color.parseColor("#00E5FF"),
                    Shader.TileMode.CLAMP
                )
                strokeWidth = 10f
            }
            canvas.drawLine(width * 0.15f, height * 0.42f, width * 0.85f, height * 0.42f, linePaint)
            canvas.drawLine(width * 0.25f, height * 0.46f, width * 0.75f, height * 0.46f, linePaint)
        }

        val tempDir = File(context.cacheDir, "posters").apply { mkdirs() }
        val posterFile = File(tempDir, "poster_${System.currentTimeMillis()}.jpg")
        FileOutputStream(posterFile).use { stream ->
            posterBitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        }
        posterBitmap.recycle()
        posterFile
    }

    private fun loadThumbnailBitmap(source: String?): Bitmap? {
        if (source.isNullOrBlank()) return null
        return runCatching {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                val request = Request.Builder().url(source).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return null
                    val bytes = response.body?.bytes() ?: return null
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            } else {
                BitmapFactory.decodeFile(source)
            }
        }.getOrNull()
    }

    private suspend fun saveToPublicGallery(sourceFile: File, displayName: String): Uri? =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext null
            runCatching {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MOVIES}/ViralClipAI"
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values) ?: return@runCatching null

                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(out)
                    }
                }
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            }.getOrNull()
        }
}
