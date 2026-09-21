package com.example.gameofbugs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GameState(
    val score: Int,
    val misses: Int,
    val remainingSeconds: Int,
    val running: Boolean,
    val paused: Boolean
)

private enum class BugType(
    val points: Int,
    val sizeDp: Float,
    val speedMultiplier: Float
) {
    COCKROACH(1, 44f, 1.0f),
    BEETLE(2, 42f, 0.85f),
    SPIDER(3, 38f, 1.15f),
    BUTTERFLY(4, 46f, 1.3f)
}

private data class GameBug(
    val type: BugType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float
)

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val handler = Handler(Looper.getMainLooper())
    private val random = Random(SystemClock.uptimeMillis())
    private val bugs = mutableListOf<GameBug>()
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

    var onStateChanged: ((GameState) -> Unit)? = null

    private var settings = GameSettings().sanitized()

    private var score = 0
    private var misses = 0

    private var running = false
    private var activeRound = false

    private var pausedRemainingMs = 0L
    private var roundEndAt = 0L

    private var lastFrameAt = 0L
    private var lastSpawnAt = 0L
    private var lastReportedSecond = -1

    private var viewVisible = true
    private var windowVisible = true

    private val frameRunnable = object : Runnable {
        override fun run() {
            if (!running) {
                return
            }

            val now = SystemClock.elapsedRealtime()

            if (now >= roundEndAt) {
                finishRound()
                return
            }

            val dt = if (lastFrameAt == 0L) {
                0f
            } else {
                ((now - lastFrameAt) / 1000f).coerceIn(0f, 0.05f)
            }

            lastFrameAt = now

            if (
                bugs.size < settings.maxBugs &&
                now - lastSpawnAt >= SPAWN_INTERVAL_MS
            ) {
                spawnBug()
            }

            updateBugs(dt)
            reportStateIfNeeded(now)

            invalidate()
            handler.postDelayed(this, FRAME_DELAY_MS)
        }
    }

    fun startGame(newSettings: GameSettings = GameSettings()) {
        stopRound(notify = false)

        settings = newSettings.sanitized()

        score = 0
        misses = 0

        pausedRemainingMs = 0L
        roundEndAt =
            SystemClock.elapsedRealtime() +
                    settings.roundDurationSeconds * 1000L

        lastFrameAt = SystemClock.elapsedRealtime()
        lastSpawnAt = lastFrameAt - SPAWN_INTERVAL_MS
        lastReportedSecond = -1

        activeRound = true
        running = true

        spawnBug()

        reportState(force = true)

        handler.post(frameRunnable)
        invalidate()
    }

    fun pauseGame() {
        if (!activeRound || !running) {
            return
        }

        val now = SystemClock.elapsedRealtime()

        pausedRemainingMs =
            (roundEndAt - now).coerceAtLeast(0L)

        running = false
        lastFrameAt = 0L

        handler.removeCallbacks(frameRunnable)

        reportState(force = true)
    }

    fun resumeGame() {
        if (
            !activeRound ||
            running ||
            pausedRemainingMs <= 0L
        ) {
            return
        }

        val now = SystemClock.elapsedRealtime()

        roundEndAt = now + pausedRemainingMs
        pausedRemainingMs = 0L

        lastFrameAt = now
        lastReportedSecond = -1

        running = true

        reportState(force = true)

        handler.post(frameRunnable)
    }

    val isPaused: Boolean
        get() =
            activeRound &&
                    !running &&
                    pausedRemainingMs > 0L

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (
            event.actionMasked == MotionEvent.ACTION_UP &&
            running
        ) {
            val hitIndex = bugs.indexOfLast { bug ->
                val dx = event.x - bug.x
                val dy = event.y - bug.y

                val hitRadius =
                    bug.radius * HIT_RADIUS_FACTOR

                dx * dx + dy * dy <=
                        hitRadius * hitRadius
            }

            if (hitIndex >= 0) {
                score += bugs[hitIndex].type.points
                bugs.removeAt(hitIndex)
            } else {
                score -= MISS_PENALTY
                misses++
            }

            reportState(force = true)
            invalidate()

            return true
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bugs.forEach { bug ->
            drawBug(canvas, bug)
        }
    }

    override fun onVisibilityChanged(
        changedView: View,
        visibility: Int
    ) {
        super.onVisibilityChanged(
            changedView,
            visibility
        )

        if (changedView === this) {
            viewVisible = visibility == VISIBLE
            updatePauseState()
        }
    }

    override fun onWindowVisibilityChanged(
        visibility: Int
    ) {
        super.onWindowVisibilityChanged(visibility)

        windowVisible = visibility == VISIBLE
        updatePauseState()
    }

    override fun onDetachedFromWindow() {
        stopRound(notify = false)
        super.onDetachedFromWindow()
    }

    private fun updatePauseState() {
        if (viewVisible && windowVisible) {
            resumeGame()
        } else {
            pauseGame()
        }
    }

    private fun spawnBug() {
        if (
            width <= 0 ||
            height <= 0 ||
            bugs.size >= settings.maxBugs
        ) {
            return
        }

        val types = BugType.values()
        val type = types[random.nextInt(types.size)]

        val radius = dp(type.sizeDp) / 2f

        val minX = radius
        val maxX = (width - radius)
            .coerceAtLeast(radius)

        val minY = radius
        val maxY = (height - radius)
            .coerceAtLeast(radius)

        val angle =
            random.nextDouble(0.0, 2.0 * PI)

        val baseSpeed =
            dp(55f + settings.gameSpeed * 18f)

        val speed =
            baseSpeed * type.speedMultiplier

        bugs += GameBug(
            type = type,
            x = random.nextFloat() *
                    (maxX - minX) + minX,
            y = random.nextFloat() *
                    (maxY - minY) + minY,
            vx = cos(angle).toFloat() * speed,
            vy = sin(angle).toFloat() * speed,
            radius = radius
        )

        lastSpawnAt =
            SystemClock.elapsedRealtime()
    }

    private fun updateBugs(dt: Float) {
        if (dt <= 0f) {
            return
        }

        bugs.forEach { bug ->
            bug.x += bug.vx * dt
            bug.y += bug.vy * dt

            if (bug.x - bug.radius < 0f) {
                bug.x = bug.radius
                bug.vx = abs(bug.vx)
            } else if (bug.x + bug.radius > width) {
                bug.x =
                    (width - bug.radius)
                        .coerceAtLeast(bug.radius)

                bug.vx = -abs(bug.vx)
            }

            if (bug.y - bug.radius < 0f) {
                bug.y = bug.radius
                bug.vy = abs(bug.vy)
            } else if (bug.y + bug.radius > height) {
                bug.y =
                    (height - bug.radius)
                        .coerceAtLeast(bug.radius)

                bug.vy = -abs(bug.vy)
            }
        }
    }

    private fun drawBug(
        canvas: Canvas,
        bug: GameBug
    ) {
        canvas.save()

        canvas.translate(bug.x, bug.y)

        canvas.rotate(
            Math.toDegrees(
                atan2(
                    bug.vy.toDouble(),
                    bug.vx.toDouble()
                )
            ).toFloat()
        )

        val scale =
            bug.radius / dp(24f)

        canvas.scale(scale, scale)

        strokePaint.strokeWidth = 2.5f

        when (bug.type) {
            BugType.COCKROACH ->
                drawCockroach(canvas)

            BugType.BEETLE ->
                drawBeetle(canvas)

            BugType.SPIDER ->
                drawSpider(canvas)

            BugType.BUTTERFLY ->
                drawButterfly(canvas)
        }

        canvas.restore()
    }

    private fun drawCockroach(canvas: Canvas) {
        fillPaint.color = Color.rgb(105, 67, 41)

        canvas.drawOval(
            RectF(-14f, -22f, 16f, 22f),
            fillPaint
        )

        fillPaint.color =
            Color.rgb(70, 44, 28)

        canvas.drawCircle(
            0f,
            -22f,
            9f,
            fillPaint
        )

        strokePaint.color =
            Color.rgb(55, 35, 22)

        for (side in listOf(-1f, 1f)) {
            canvas.drawLine(
                side * 8f,
                -10f,
                side * 25f,
                -17f,
                strokePaint
            )

            canvas.drawLine(
                side * 10f,
                0f,
                side * 28f,
                0f,
                strokePaint
            )

            canvas.drawLine(
                side * 8f,
                10f,
                side * 25f,
                17f,
                strokePaint
            )
        }

        canvas.drawLine(
            -5f,
            -29f,
            -18f,
            -39f,
            strokePaint
        )

        canvas.drawLine(
            5f,
            -29f,
            18f,
            -39f,
            strokePaint
        )
    }

    private fun drawBeetle(canvas: Canvas) {
        fillPaint.color =
            Color.rgb(42, 122, 66)

        canvas.drawOval(
            RectF(-16f, -22f, 16f, 22f),
            fillPaint
        )

        fillPaint.color =
            Color.rgb(25, 82, 45)

        canvas.drawCircle(
            0f,
            -22f,
            8f,
            fillPaint
        )

        strokePaint.color =
            Color.rgb(16, 55, 30)

        canvas.drawLine(
            0f,
            -14f,
            0f,
            20f,
            strokePaint
        )

        canvas.drawLine(
            -7f,
            -22f,
            -20f,
            -31f,
            strokePaint
        )

        canvas.drawLine(
            7f,
            -22f,
            20f,
            -31f,
            strokePaint
        )
    }

    private fun drawSpider(canvas: Canvas) {
        fillPaint.color =
            Color.rgb(45, 45, 50)

        canvas.drawCircle(
            0f,
            3f,
            14f,
            fillPaint
        )

        canvas.drawCircle(
            0f,
            -14f,
            10f,
            fillPaint
        )

        strokePaint.color =
            Color.rgb(25, 25, 28)

        for (i in -1..1) {
            val y = i * 11f

            canvas.drawLine(
                -9f,
                y,
                -27f,
                y - 9f,
                strokePaint
            )

            canvas.drawLine(
                9f,
                y,
                27f,
                y - 9f,
                strokePaint
            )
        }

        canvas.drawCircle(
            -3f,
            -16f,
            2.2f,
            whitePaint
        )

        canvas.drawCircle(
            3f,
            -16f,
            2.2f,
            whitePaint
        )

        canvas.drawCircle(
            -3f,
            -16f,
            1f,
            blackPaint
        )

        canvas.drawCircle(
            3f,
            -16f,
            1f,
            blackPaint
        )
    }

    private fun drawButterfly(canvas: Canvas) {
        fillPaint.color =
            Color.rgb(154, 78, 176)

        canvas.drawOval(
            RectF(-27f, -22f, -2f, 5f),
            fillPaint
        )

        canvas.drawOval(
            RectF(2f, -22f, 27f, 5f),
            fillPaint
        )

        fillPaint.color =
            Color.rgb(229, 153, 205)

        canvas.drawOval(
            RectF(-24f, 0f, -3f, 20f),
            fillPaint
        )

        canvas.drawOval(
            RectF(3f, 0f, 24f, 20f),
            fillPaint
        )

        fillPaint.color =
            Color.rgb(65, 42, 35)

        canvas.drawRoundRect(
            RectF(-3f, -13f, 3f, 16f),
            3f,
            3f,
            fillPaint
        )
    }

    private fun finishRound() {
        running = false
        activeRound = false
        pausedRemainingMs = 0L

        handler.removeCallbacks(frameRunnable)

        bugs.clear()

        reportState(force = true)
        invalidate()
    }

    private fun stopRound(notify: Boolean) {
        running = false
        activeRound = false
        pausedRemainingMs = 0L

        handler.removeCallbacks(frameRunnable)

        bugs.clear()

        if (notify) {
            reportState(force = true)
        }
    }

    private fun reportStateIfNeeded(
        now: Long
    ) {
        val remaining =
            remainingSeconds(now)

        if (remaining != lastReportedSecond) {
            lastReportedSecond = remaining
            reportState(
                force = true,
                now = now
            )
        }
    }

    private fun reportState(
        force: Boolean,
        now: Long =
            SystemClock.elapsedRealtime()
    ) {
        if (!force) {
            return
        }

        onStateChanged?.invoke(
            GameState(
                score = score,
                misses = misses,
                remainingSeconds =
                    remainingSeconds(now),
                running = running,
                paused =
                    activeRound &&
                            !running &&
                            pausedRemainingMs > 0L
            )
        )
    }

    private fun remainingSeconds(
        now: Long
    ): Int {
        if (!activeRound) {
            return 0
        }

        val remainingMs =
            if (running) {
                (roundEndAt - now)
                    .coerceAtLeast(0L)
            } else {
                pausedRemainingMs
            }

        return ((remainingMs + 999L) / 1000L)
            .toInt()
            .coerceAtLeast(0)
    }

    private fun dp(value: Float): Float {
        return value * density
    }

    companion object {
        private const val FRAME_DELAY_MS = 16L
        private const val SPAWN_INTERVAL_MS = 700L
        private const val MISS_PENALTY = 1
        private const val HIT_RADIUS_FACTOR = 1.35f
    }
}