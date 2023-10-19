package io.github.drumber.kitsune.util

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import io.github.drumber.kitsune.util.extensions.toPx
import kotlin.math.min

class RoundBitmapDrawable(private val bitmap: Bitmap) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var scaledBitmap: Bitmap? = null
    private var tint: ColorStateList? = null
    private val borderWidth = 1f.toPx()
    private var isSelected = false

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val width = bounds.width()
        val height = bounds.height()
        // use the smaller dimension as the circle size
        val size = min(width, height)

        // calculate the rectangle to center the bitmap
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val right = left + size
        val bottom = top + size

        rect.set(left, top, right, bottom)

        val tint = this.tint
        if (tint != null) {
            paint.color = tint.defaultColor
        } else {
            paint.color = Color.TRANSPARENT
        }

        // draw circular background
        paint.shader = null
        canvas.drawCircle(rect.centerX(), rect.centerY(), size / 2f, paint)

        // draw the circular bitmap
        val bitmap = scaledBitmap ?: updateScaledBitmap(bounds)
        val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = bitmapShader
        canvas.drawCircle(rect.centerX(), rect.centerY(), size / 2f, paint)

        if (isSelected && tint != null) {
            // draw circular border when selected
            borderPaint.color = tint.getColorForState(state, tint.defaultColor)
            borderPaint.strokeWidth = borderWidth
            borderPaint.style = Paint.Style.STROKE
            val strokeRadius = (size - borderWidth) / 2f
            canvas.drawCircle(rect.centerX(), rect.centerY(), strokeRadius, borderPaint)
        }
    }

    override fun setTintList(tint: ColorStateList?) {
        if (this.tint != tint) {
            this.tint = tint
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        borderPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateScaledBitmap(bounds)
    }

    override fun invalidateSelf() {
        super.invalidateSelf()
        scaledBitmap?.recycle()
        scaledBitmap = null
    }

    override fun onStateChange(state: IntArray): Boolean {
        val tint = tint ?: return false
        val stateColor = tint.getColorForState(state, tint.defaultColor)
        val isSelected = stateColor != tint.defaultColor
        if (this.isSelected != isSelected) {
            this.isSelected = isSelected
            invalidateSelf()
            return true
        }
        return false
    }

    override fun isStateful(): Boolean {
        return tint != null
    }

    private fun updateScaledBitmap(bounds: Rect): Bitmap {
        scaledBitmap?.recycle()
        scaledBitmap = null
        // use the smaller dimension as the circle size
        val size = min(bounds.width(), bounds.height())
        val scaledBitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size)
        this.scaledBitmap = scaledBitmap
        return scaledBitmap
    }
}