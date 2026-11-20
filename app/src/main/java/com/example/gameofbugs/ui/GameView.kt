package com.example.gameofbugs.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import com.example.gameofbugs.R
import com.example.gameofbugs.model.BugType
import com.example.gameofbugs.model.GameBug
import com.example.gameofbugs.viewmodel.GameViewModel
import kotlin.math.atan2

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var game: GameViewModel? = null
    private val density = resources.displayMetrics.density

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        textSize = dp(16f)
    }

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var sensorOn = false
    private var player: MediaPlayer? = null

    private val bonusRadius = dp(24f)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val game = game ?: return
            val x = event.values[0]
            val y = event.values[1]

            when (display?.rotation) {
                Surface.ROTATION_90 -> {
                    game.tiltX = y
                    game.tiltY = x
                }

                Surface.ROTATION_180 -> {
                    game.tiltX = x
                    game.tiltY = -y
                }

                Surface.ROTATION_270 -> {
                    game.tiltX = -y
                    game.tiltY = -x
                }

                else -> {
                    game.tiltX = -x
                    game.tiltY = y
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    fun setGame(game: GameViewModel) {
        this.game = game
    }

    fun setTilting(tilting: Boolean) {
        if (tilting) {
            registerSensor()
        } else {
            unregisterSensor()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val game = game ?: return true

        if (event.actionMasked == MotionEvent.ACTION_UP && game.running) {
            if (game.bonusVisible && isBonusHit(game, event.x, event.y)) {
                game.takeBonus()
                playScream()
            } else {
                val hitIndex = game.bugs.indexOfLast { bug ->
                    val dx = event.x - bug.x * width
                    val dy = event.y - bug.y * height
                    val hitRadius = radius(bug) * HIT_RADIUS_FACTOR

                    dx * dx + dy * dy <= hitRadius * hitRadius
                }

                if (hitIndex >= 0) {
                    game.hitBug(hitIndex)
                } else {
                    game.miss()
                }
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val game = game ?: return

        game.bugs.forEach { bug ->
            drawBug(canvas, bug)
        }

        if (game.bonusVisible) {
            drawBonus(canvas, game.bonusX * width, game.bonusY * height)
        }

        if (game.tiltLeft > 0f) {
            canvas.drawText(
                "Наклоняйте телефон! ${game.tiltLeft.toInt() + 1}",
                width / 2f,
                dp(28f),
                textPaint
            )
        }

        if (game.running) {
            postInvalidateOnAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        unregisterSensor()
        player?.release()
        player = null
        super.onDetachedFromWindow()
    }

    private fun isBonusHit(game: GameViewModel, x: Float, y: Float): Boolean {
        val dx = x - game.bonusX * width
        val dy = y - game.bonusY * height
        val hitRadius = bonusRadius * HIT_RADIUS_FACTOR

        return dx * dx + dy * dy <= hitRadius * hitRadius
    }

    private fun playScream() {
        player?.release()
        player = MediaPlayer.create(context, R.raw.bug_scream)
        player?.start()
    }

    private fun registerSensor() {
        if (!sensorOn && accelerometer != null) {
            sensorManager.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            sensorOn = true
        }
    }

    private fun unregisterSensor() {
        if (sensorOn) {
            sensorManager.unregisterListener(sensorListener)
            sensorOn = false
        }
    }

    private fun radius(bug: GameBug): Float {
        return dp(bug.type.sizeDp) / 2f
    }

    private fun drawBonus(canvas: Canvas, x: Float, y: Float) {
        fillPaint.color = Color.rgb(255, 200, 40)
        canvas.drawCircle(x, y, bonusRadius, fillPaint)

        strokePaint.color = Color.rgb(230, 130, 0)
        strokePaint.strokeWidth = 5f
        canvas.drawCircle(x, y, bonusRadius, strokePaint)

        canvas.drawText("★", x, y + dp(6f), textPaint)
    }

    private fun drawBug(canvas: Canvas, bug: GameBug) {
        canvas.save()

        canvas.translate(bug.x * width, bug.y * height)

        canvas.rotate(
            Math.toDegrees(
                atan2(
                    (bug.vy * height).toDouble(),
                    (bug.vx * width).toDouble()
                )
            ).toFloat()
        )

        val scale = radius(bug) / dp(24f)
        canvas.scale(scale, scale)

        strokePaint.strokeWidth = 2.5f

        when (bug.type) {
            BugType.COCKROACH ->
                drawCockroach(
                    canvas,
                    Color.rgb(105, 67, 41),
                    Color.rgb(70, 44, 28),
                    Color.rgb(55, 35, 22)
                )

            BugType.BEETLE ->
                drawBeetle(canvas)

            BugType.SPIDER ->
                drawSpider(canvas)

            BugType.BUTTERFLY ->
                drawButterfly(canvas)

            BugType.GOLD ->
                drawCockroach(
                    canvas,
                    Color.rgb(255, 200, 40),
                    Color.rgb(212, 160, 20),
                    Color.rgb(140, 100, 0)
                )
        }

        canvas.restore()
    }

    private fun drawCockroach(
        canvas: Canvas,
        bodyColor: Int,
        headColor: Int,
        legColor: Int
    ) {
        fillPaint.color = bodyColor
        canvas.drawOval(RectF(-14f, -22f, 16f, 22f), fillPaint)

        fillPaint.color = headColor
        canvas.drawCircle(0f, -22f, 9f, fillPaint)

        strokePaint.color = legColor

        for (side in listOf(-1f, 1f)) {
            canvas.drawLine(side * 8f, -10f, side * 25f, -17f, strokePaint)
            canvas.drawLine(side * 10f, 0f, side * 28f, 0f, strokePaint)
            canvas.drawLine(side * 8f, 10f, side * 25f, 17f, strokePaint)
        }

        canvas.drawLine(-5f, -29f, -18f, -39f, strokePaint)
        canvas.drawLine(5f, -29f, 18f, -39f, strokePaint)
    }

    private fun drawBeetle(canvas: Canvas) {
        fillPaint.color = Color.rgb(42, 122, 66)
        canvas.drawOval(RectF(-16f, -22f, 16f, 22f), fillPaint)

        fillPaint.color = Color.rgb(25, 82, 45)
        canvas.drawCircle(0f, -22f, 8f, fillPaint)

        strokePaint.color = Color.rgb(16, 55, 30)
        canvas.drawLine(0f, -14f, 0f, 20f, strokePaint)
        canvas.drawLine(-7f, -22f, -20f, -31f, strokePaint)
        canvas.drawLine(7f, -22f, 20f, -31f, strokePaint)
    }

    private fun drawSpider(canvas: Canvas) {
        fillPaint.color = Color.rgb(45, 45, 50)
        canvas.drawCircle(0f, 3f, 14f, fillPaint)
        canvas.drawCircle(0f, -14f, 10f, fillPaint)

        strokePaint.color = Color.rgb(25, 25, 28)

        for (i in -1..1) {
            val y = i * 11f
            canvas.drawLine(-9f, y, -27f, y - 9f, strokePaint)
            canvas.drawLine(9f, y, 27f, y - 9f, strokePaint)
        }

        canvas.drawCircle(-3f, -16f, 2.2f, whitePaint)
        canvas.drawCircle(3f, -16f, 2.2f, whitePaint)
        canvas.drawCircle(-3f, -16f, 1f, blackPaint)
        canvas.drawCircle(3f, -16f, 1f, blackPaint)
    }

    private fun drawButterfly(canvas: Canvas) {
        fillPaint.color = Color.rgb(154, 78, 176)
        canvas.drawOval(RectF(-27f, -22f, -2f, 5f), fillPaint)
        canvas.drawOval(RectF(2f, -22f, 27f, 5f), fillPaint)

        fillPaint.color = Color.rgb(229, 153, 205)
        canvas.drawOval(RectF(-24f, 0f, -3f, 20f), fillPaint)
        canvas.drawOval(RectF(3f, 0f, 24f, 20f), fillPaint)

        fillPaint.color = Color.rgb(65, 42, 35)
        canvas.drawRoundRect(RectF(-3f, -13f, 3f, 16f), 3f, 3f, fillPaint)
    }

    private fun dp(value: Float): Float {
        return value * density
    }

    companion object {
        private const val HIT_RADIUS_FACTOR = 1.35f
    }
}
