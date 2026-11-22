package com.parallax.wallpaper

import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.sin

class ParallaxWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ParallaxEngine()

    inner class ParallaxEngine : Engine(), SensorEventListener {
        private val sensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
        private val rotationSensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

        private var background: Bitmap? = null
        private var glow: Bitmap? = null
        private var foreground: Bitmap? = null

        private var smoothedX = 0f
        private var smoothedY = 0f
        private val alpha = 0.15f // low-pass filter

        private var depthStrength = 1.0f
        private var visible = false
        private var glowPhase = 0f

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 60
            maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.NORMAL)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            loadBitmaps()
        }

        private fun loadBitmaps() {
            try {
                background = BitmapFactory.decodeResource(resources, R.drawable.background)
                glow = BitmapFactory.decodeResource(resources, R.drawable.glow)
                foreground = BitmapFactory.decodeResource(resources, R.drawable.foreground)
            } catch (e: Exception) {
                // Placeholders if missing
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                rotationSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                }
            } else {
                sensorManager.unregisterListener(this)
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val x = event.values[0]
                val y = event.values[1]
                smoothedX = smoothedX * (1 - alpha) + x * alpha
                smoothedY = smoothedY * (1 - alpha) + y * alpha
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK)

                    val w = canvas.width.toFloat()
                    val h = canvas.height.toFloat()

                    val strength = depthStrength

                    // Background (least movement)
                    background?.let { bmp ->
                        val dx = smoothedX * w * 0.05f * strength
                        val dy = smoothedY * h * 0.05f * strength
                        val src = Rect(0, 0, bmp.width, bmp.height)
                        val dst = RectF(-dx, -dy, w - dx, h - dy)
                        canvas.drawBitmap(bmp, src, dst, paint)
                    }

                    // Glow (medium, with pulse)
                    glow?.let { bmp ->
                        glowPhase += 0.016f // ~60fps
                        val pulse = 0.7f + 0.3f * ((sin(glowPhase * Math.PI / 2.0) + 1.0) / 2.0).toFloat()
                        paint.alpha = (pulse * 255).toInt()

                        val dx = smoothedX * w * 0.12f * strength
                        val dy = smoothedY * h * 0.12f * strength
                        val src = Rect(0, 0, bmp.width, bmp.height)
                        val dst = RectF(-dx, -dy, w - dx, h - dy)
                        canvas.drawBitmap(bmp, src, dst, paint)
                        paint.alpha = 255
                    }

                    // Foreground (most movement, opposite direction) + shadow
                    foreground?.let { bmp ->
                        val dx = -smoothedX * w * 0.25f * strength
                        val dy = -smoothedY * h * 0.25f * strength

                        // Shadow
                        val shadowDx = dx + 15f
                        val shadowDy = dy + 25f
                        val shadowSrc = Rect(0, 0, bmp.width, bmp.height)
                        val shadowDst = RectF(shadowDx, shadowDy, w + shadowDx, h + shadowDy)
                        canvas.drawBitmap(bmp.extractAlpha(), shadowSrc, shadowDst, shadowPaint)

                        // Foreground
                        val src = Rect(0, 0, bmp.width, bmp.height)
                        val dst = RectF(dx, dy, w + dx, h + dy)
                        canvas.drawBitmap(bmp, src, dst, paint)
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            if (visible) {
                surfaceHolder.surface?.let {
                    // Schedule next frame ~60fps
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        drawFrame()
                    }, 16)
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            sensorManager.unregisterListener(this)
            background?.recycle()
            glow?.recycle()
            foreground?.recycle()
        }
    }
}
