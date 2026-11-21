package com.example.data.video

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import com.example.data.model.CaptionLanguage
import com.example.data.model.CaptionStyle
import com.example.data.model.ExportPlatform
import com.example.data.model.SubtitleCue

@OptIn(UnstableApi::class)
class SubtitleBitmapOverlay(
    private val width: Int,
    private val height: Int,
    private val clipDurationMs: Long,
    private val cues: List<SubtitleCue>,
    private val language: CaptionLanguage,
    private val captionStyle: CaptionStyle,
    private val includeEmojis: Boolean,
    private val animatedHighlight: Boolean,
    private val introEnabled: Boolean,
    private val introText: String,
    private val outroEnabled: Boolean,
    private val outroText: String,
    private val platform: ExportPlatform
) : BitmapOverlay() {

    private var cachedKey: String? = null
    private var cachedBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val currentMs = (presentationTimeUs / 1000L).coerceAtLeast(0L)
        val showIntro = introEnabled && introText.isNotBlank() && currentMs < 2_200L
        val showOutro = outroEnabled && outroText.isNotBlank() &&
            clipDurationMs > 3_500L && currentMs >= (clipDurationMs - 2_300L)

        val activeCue = cues.firstOrNull { currentMs in it.startOffsetMs..it.endOffsetMs }
        val wordPhase = if (animatedHighlight && activeCue != null) {
            val cueLen = (activeCue.endOffsetMs - activeCue.startOffsetMs).coerceAtLeast(500L)
            val progress = ((currentMs - activeCue.startOffsetMs).toFloat() / cueLen.toFloat()).coerceIn(0f, 1f)
            (progress * 4f).toInt()
        } else 0

        val frameKey = "${showIntro}_${showOutro}_${activeCue?.id ?: -1}_$wordPhase"
        if (frameKey == cachedKey) {
            return cachedBitmap
        }
        cachedKey = frameKey

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Top subtle studio platform pill
        drawPlatformTag(canvas)

        // Optional Intro Hook Card (first 2.2s)
        if (showIntro) {
            drawBannerCard(
                canvas = canvas,
                text = introText,
                topY = height * 0.16f,
                accentStart = Color.parseColor("#7C3AED"),
                accentEnd = Color.parseColor("#00E5FF")
            )
        }

        // Synchronized Animated Subtitle Cue
        if (activeCue != null && !showOutro) {
            val captionText = activeCue.textForLanguage(language, includeEmojis)
            if (captionText.isNotBlank()) {
                drawCaptionBlock(canvas, captionText, wordPhase)
            }
        }

        // Optional Outro CTA Card (last 2.3s)
        if (showOutro) {
            drawBannerCard(
                canvas = canvas,
                text = outroText,
                topY = height * 0.44f,
                accentStart = Color.parseColor("#EC4899"),
                accentEnd = Color.parseColor("#A855F7")
            )
        }

        cachedBitmap = bitmap
        return bitmap
    }

    private fun drawPlatformTag(canvas: Canvas) {
        val scale = width / 1080f
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 10, 11, 22)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * scale
            shader = LinearGradient(
                0f, 0f, 340f * scale, 0f,
                Color.parseColor("#A855F7"),
                Color.parseColor("#00E5FF"),
                Shader.TileMode.CLAMP
            )
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val label = "VIRAL CLIP AI • ${platform.shortLabel.uppercase()}"
        val pillW = textPaint.measureText(label) + 56f * scale
        val pillH = 60f * scale
        val left = (width - pillW) / 2f
        val top = 74f * scale
        val rect = RectF(left, top, left + pillW, top + pillH)
        canvas.drawRoundRect(rect, 30f * scale, 30f * scale, pillPaint)
        canvas.drawRoundRect(rect, 30f * scale, 30f * scale, borderPaint)
        canvas.drawText(label, rect.centerX(), rect.centerY() + 10f * scale, textPaint)
    }

    private fun drawBannerCard(
        canvas: Canvas,
        text: String,
        topY: Float,
        accentStart: Int,
        accentEnd: Int
    ) {
        val scale = width / 1080f
        val horizontalPadding = 84f * scale
        val maxTextWidth = (width - horizontalPadding * 2f).toInt().coerceAtLeast(200)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(6f * scale, 1f)
            .build()

        val boxPadX = 42f * scale
        val boxPadY = 28f * scale
        val cardLeft = (width - maxTextWidth) / 2f - boxPadX * 0.5f
        val cardRight = width - cardLeft
        val cardRect = RectF(
            cardLeft,
            topY,
            cardRight,
            topY + layout.height + boxPadY * 2f
        )

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cardRect.left, cardRect.top, cardRect.right, cardRect.bottom,
                accentStart, accentEnd, Shader.TileMode.CLAMP
            )
            alpha = 235
        }
        canvas.drawRoundRect(cardRect, 28f * scale, 28f * scale, bgPaint)

        canvas.save()
        canvas.translate((width - maxTextWidth) / 2f, topY + boxPadY)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawCaptionBlock(canvas: Canvas, text: String, phase: Int) {
        val scale = width / 1080f
        val horizontalMargin = 72f * scale
        val maxTextWidth = (width - horizontalMargin * 2f).toInt().coerceAtLeast(240)

        val textColor = when (captionStyle) {
            CaptionStyle.NEON_POP -> if (phase % 2 == 0) Color.WHITE else Color.parseColor("#00E5FF")
            CaptionStyle.BOLD_VIRAL -> Color.parseColor("#FFE600")
            CaptionStyle.MINIMAL_GLASS -> Color.WHITE
            CaptionStyle.HIGHLIGHTER -> Color.parseColor("#090910")
        }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 58f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            if (captionStyle != CaptionStyle.HIGHLIGHTER) {
                setShadowLayer(12f * scale, 0f, 4f * scale, Color.argb(230, 0, 0, 0))
            }
        }

        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(8f * scale, 1.05f)
            .build()

        val padX = 36f * scale
        val padY = 26f * scale
        val blockBottom = height * 0.80f
        val blockTop = (blockBottom - layout.height - padY * 2f).coerceAtLeast(height * 0.56f)
        val bgRect = RectF(
            horizontalMargin - padX * 0.5f,
            blockTop,
            width - (horizontalMargin - padX * 0.5f),
            blockTop + layout.height + padY * 2f
        )

        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            when (captionStyle) {
                CaptionStyle.NEON_POP -> color = Color.argb(185, 11, 12, 24)
                CaptionStyle.BOLD_VIRAL -> color = Color.argb(175, 0, 0, 0)
                CaptionStyle.MINIMAL_GLASS -> color = Color.argb(145, 22, 24, 42)
                CaptionStyle.HIGHLIGHTER -> shader = LinearGradient(
                    bgRect.left, bgRect.top, bgRect.right, bgRect.bottom,
                    Color.parseColor("#00E5FF"),
                    Color.parseColor("#A855F7"),
                    Shader.TileMode.CLAMP
                )
            }
        }

        canvas.drawRoundRect(bgRect, 26f * scale, 26f * scale, boxPaint)

        if (captionStyle == CaptionStyle.NEON_POP) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.5f * scale
                shader = LinearGradient(
                    bgRect.left, bgRect.top, bgRect.right, bgRect.bottom,
                    Color.parseColor("#A855F7"),
                    Color.parseColor("#00E5FF"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(bgRect, 26f * scale, 26f * scale, borderPaint)
        }

        canvas.save()
        canvas.translate(horizontalMargin, blockTop + padY)
        layout.draw(canvas)
        canvas.restore()
    }
}
