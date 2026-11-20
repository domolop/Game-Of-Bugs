package com.example.gameofbugs.viewmodel

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameofbugs.data.GamePreferences
import com.example.gameofbugs.data.GameRepository
import com.example.gameofbugs.data.GoldRepository
import com.example.gameofbugs.data.UserEntity
import com.example.gameofbugs.model.BugType
import com.example.gameofbugs.model.GameBug
import com.example.gameofbugs.model.GameResult
import com.example.gameofbugs.model.GameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(
    private val gameRepository: GameRepository,
    private val goldRepository: GoldRepository,
    private val gamePreferences: GamePreferences
) : ViewModel() {

    val bugs = mutableListOf<GameBug>()

    var tiltX = 0f
    var tiltY = 0f

    var tiltLeft = 0f
        private set

    var bonusVisible = false
        private set

    var bonusX = 0f
        private set

    var bonusY = 0f
        private set

    var running = false
        private set

    private val _state = MutableLiveData(GameState())
    val state: LiveData<GameState> = _state

    private val _user = MutableLiveData<UserEntity?>()
    val user: LiveData<UserEntity?> = _user

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val random = Random(SystemClock.uptimeMillis())
    private var loopJob: Job? = null
    private var settings = gamePreferences.load()
    private var goldRate: Double? = null

    private var score = 0
    private var misses = 0
    private var activeRound = false

    private var pausedRemainingMs = 0L
    private var roundEndAt = 0L
    private var lastFrameAt = 0L
    private var lastSpawnAt = 0L
    private var lastReportedSecond = -1

    private var goldTimer = 0f
    private var bonusLeft = 0f
    private var bonusTimer = 0f

    fun loadActiveUser() {
        viewModelScope.launch {
            _user.value = gameRepository.loadActiveUser()
        }
    }

    fun loadGoldRate() {
        viewModelScope.launch {
            goldRate = goldRepository.loadRate()
        }
    }

    fun messageShown() {
        _message.value = null
    }

    fun startGame() {
        stopRound()

        settings = gamePreferences.load()
        score = 0
        misses = 0
        goldTimer = 0f

        val now = SystemClock.elapsedRealtime()
        roundEndAt = now + settings.roundDurationSeconds * 1000L
        lastFrameAt = now
        lastSpawnAt = now - SPAWN_INTERVAL_MS
        lastReportedSecond = -1

        activeRound = true
        running = true

        spawnBug()
        publishState()
        startLoop()
    }

    fun pauseGame() {
        if (!activeRound || !running) {
            return
        }

        pausedRemainingMs = (roundEndAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        running = false
        loopJob?.cancel()
        publishState()
    }

    fun resumeGame() {
        if (!activeRound || running) {
            return
        }

        val now = SystemClock.elapsedRealtime()
        roundEndAt = now + pausedRemainingMs
        pausedRemainingMs = 0L
        lastFrameAt = now
        lastReportedSecond = -1
        running = true

        publishState()
        startLoop()
    }

    fun hitBug(index: Int) {
        if (index !in bugs.indices) {
            return
        }
        score += bugs[index].points
        bugs.removeAt(index)
        publishState()
    }

    fun miss() {
        score -= MISS_PENALTY
        misses++
        publishState()
    }

    fun takeBonus() {
        bonusVisible = false
        score += BONUS_POINTS
        tiltLeft = TILT_SECONDS
        tiltX = 0f
        tiltY = 0f
        publishState()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (running) {
                delay(FRAME_DELAY_MS)
                updateFrame()
            }
        }
    }

    private fun updateFrame() {
        val now = SystemClock.elapsedRealtime()

        if (now >= roundEndAt) {
            finishRound()
            return
        }

        val dt = ((now - lastFrameAt) / 1000f).coerceIn(0f, 0.05f)
        lastFrameAt = now

        if (bugs.size < settings.maxBugs && now - lastSpawnAt >= SPAWN_INTERVAL_MS) {
            spawnBug()
        }

        updateBonus(dt)
        updateGold(dt)
        updateBugs(dt)

        val seconds = remainingSeconds()
        if (seconds != lastReportedSecond) {
            lastReportedSecond = seconds
            publishState()
        }
    }

    private fun spawnBug(
        type: BugType = randomType(),
        points: Int = type.points
    ) {
        val angle = random.nextDouble(0.0, 2.0 * PI)
        val speed = (BASE_SPEED + settings.gameSpeed * SPEED_STEP) * type.speedMultiplier

        bugs += GameBug(
            type = type,
            points = points,
            x = MARGIN + random.nextFloat() * (1f - 2 * MARGIN),
            y = MARGIN + random.nextFloat() * (1f - 2 * MARGIN),
            vx = cos(angle).toFloat() * speed,
            vy = sin(angle).toFloat() * speed,
            speed = speed
        )

        lastSpawnAt = SystemClock.elapsedRealtime()
    }

    private fun randomType(): BugType {
        val types = BugType.values().filter { it != BugType.GOLD }
        return types[random.nextInt(types.size)]
    }

    private fun updateGold(dt: Float) {
        goldTimer += dt

        if (goldTimer >= GOLD_INTERVAL_SECONDS) {
            goldTimer = 0f
            spawnBug(BugType.GOLD, goldPoints())
        }
    }

    private fun goldPoints(): Int {
        val rate = goldRate ?: return GOLD_DEFAULT_POINTS
        return (rate / GOLD_RATE_DIVIDER).toInt().coerceAtLeast(1)
    }

    private fun updateBugs(dt: Float) {
        if (dt <= 0f) {
            return
        }

        val tilt = tiltLeft > 0f
        val bounce = if (tilt) TILT_BOUNCE else 1f

        bugs.forEach { bug ->
            if (tilt) {
                bug.vx += tiltX * TILT_ACCEL * dt
                bug.vy += tiltY * TILT_ACCEL * dt
                bug.vx *= 1f - TILT_FRICTION * dt
                bug.vy *= 1f - TILT_FRICTION * dt
            }

            bug.x += bug.vx * dt
            bug.y += bug.vy * dt

            if (bug.x < MARGIN) {
                bug.x = MARGIN
                bug.vx = abs(bug.vx) * bounce
            } else if (bug.x > 1f - MARGIN) {
                bug.x = 1f - MARGIN
                bug.vx = -abs(bug.vx) * bounce
            }

            if (bug.y < MARGIN) {
                bug.y = MARGIN
                bug.vy = abs(bug.vy) * bounce
            } else if (bug.y > 1f - MARGIN) {
                bug.y = 1f - MARGIN
                bug.vy = -abs(bug.vy) * bounce
            }
        }
    }

    private fun updateBonus(dt: Float) {
        if (tiltLeft > 0f) {
            tiltLeft -= dt

            if (tiltLeft <= 0f) {
                stopTilt()
            }
        } else if (bonusVisible) {
            bonusLeft -= dt

            if (bonusLeft <= 0f) {
                bonusVisible = false
                bonusTimer = 0f
            }
        } else {
            bonusTimer += dt

            if (bonusTimer >= settings.bonusIntervalSeconds) {
                spawnBonus()
            }
        }
    }

    private fun spawnBonus() {
        bonusX = BONUS_MARGIN + random.nextFloat() * (1f - 2 * BONUS_MARGIN)
        bonusY = BONUS_MARGIN + random.nextFloat() * (1f - 2 * BONUS_MARGIN)
        bonusLeft = BONUS_LIFETIME_SECONDS
        bonusVisible = true
    }

    private fun stopTilt() {
        tiltLeft = 0f
        bonusTimer = 0f

        bugs.forEach { bug ->
            val length = hypot(bug.vx, bug.vy)
            val angle = if (length < 0.01f) {
                random.nextDouble(0.0, 2.0 * PI)
            } else {
                atan2(bug.vy.toDouble(), bug.vx.toDouble())
            }

            bug.vx = cos(angle).toFloat() * bug.speed
            bug.vy = sin(angle).toFloat() * bug.speed
        }

        publishState()
    }

    private fun finishRound() {
        val result = GameResult(score = score, misses = misses)

        stopRound()
        saveResult(result)
        publishState()
    }

    private fun stopRound() {
        running = false
        activeRound = false
        pausedRemainingMs = 0L
        loopJob?.cancel()

        bonusVisible = false
        bonusTimer = 0f
        tiltLeft = 0f

        bugs.clear()
    }

    private fun saveResult(result: GameResult) {
        val player = _user.value

        if (player == null) {
            _message.value = "Сначала выберите пользователя"
            return
        }

        viewModelScope.launch {
            val saved = gameRepository.saveBestRecord(player, result, settings)
            _message.value = if (saved) {
                "Результат сохранён в рекорды"
            } else {
                "Рекорд не улучшен"
            }
        }
    }

    private fun publishState() {
        _state.value = GameState(
            score = score,
            misses = misses,
            remainingSeconds = remainingSeconds(),
            running = running,
            paused = activeRound && !running,
            tilting = running && tiltLeft > 0f
        )
    }

    private fun remainingSeconds(): Int {
        if (!activeRound) {
            return 0
        }

        val remainingMs = if (running) {
            (roundEndAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        } else {
            pausedRemainingMs
        }

        return ((remainingMs + 999L) / 1000L).toInt()
    }

    companion object {
        private const val FRAME_DELAY_MS = 16L
        private const val SPAWN_INTERVAL_MS = 700L
        private const val MISS_PENALTY = 1
        private const val MARGIN = 0.06f
        private const val BASE_SPEED = 0.15f
        private const val SPEED_STEP = 0.05f
        private const val BONUS_POINTS = 5
        private const val BONUS_MARGIN = 0.1f
        private const val BONUS_LIFETIME_SECONDS = 5f
        private const val TILT_SECONDS = 10f
        private const val TILT_ACCEL = 0.25f
        private const val TILT_FRICTION = 1.5f
        private const val TILT_BOUNCE = 0.3f
        private const val GOLD_INTERVAL_SECONDS = 20f
        private const val GOLD_RATE_DIVIDER = 1000
        private const val GOLD_DEFAULT_POINTS = 10
    }
}
