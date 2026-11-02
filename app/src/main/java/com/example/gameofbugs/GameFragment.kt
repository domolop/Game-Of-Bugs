package com.example.gameofbugs

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class GameFragment : Fragment(R.layout.tab_game) {

    private var gameView: GameView? = null
    private var currentState: GameState? = null
    private var currentUser: UserEntity? = null
    private var currentSettings: GameSettings = GameSettings().sanitized()
    private var tvCurrentUser: TextView? = null
    private var btnStart: Button? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameViewLocal = view.findViewById<GameView>(R.id.gameView)
        val tvScore = view.findViewById<TextView>(R.id.tvGameScore)
        val tvMisses = view.findViewById<TextView>(R.id.tvGameMisses)
        val tvTime = view.findViewById<TextView>(R.id.tvGameTime)
        btnStart = view.findViewById(R.id.btnStartGame)
        val btnGameMenu = view.findViewById<Button>(R.id.btnGameMenu)
        tvCurrentUser = view.findViewById(R.id.tvCurrentUser)

        gameView = gameViewLocal

        gameViewLocal.onStateChanged = { state ->
            currentState = state
            tvScore.text = "Очки: ${state.score}"
            tvMisses.text = "Промахи: ${state.misses}"
            tvTime.text = if (state.paused) {
                "Пауза: ${state.remainingSeconds} сек."
            } else {
                "Время: ${state.remainingSeconds} сек."
            }
            renderStartButton()
        }

        gameViewLocal.onRoundFinished = { result ->
            val user = currentUser
            if (user == null) {
                Toast.makeText(
                    requireContext(),
                    "Сначала выберите пользователя",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Вместо return@onRoundFinished мы просто выполняем код в блоке else
                GameRepository.getInstance(requireContext())
                    .saveBestRecord(user, result, currentSettings) { saved ->
                        Toast.makeText(
                            requireContext(),
                            if (saved) {
                                "Результат сохранён в рекорды"
                            } else {
                                "Рекорд не улучшен"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        btnStart?.setOnClickListener {
            when {
                currentState?.paused == true -> {
                    gameViewLocal.resumeGame()
                }

                currentUser == null -> {
                    (activity as? PageNavigationHost)?.navigateToPage(Pages.REGISTRATION)
                }

                else -> {
                    currentSettings = GamePreferences(requireContext()).load()
                    gameViewLocal.startGame(currentSettings)
                }
            }
        }

        btnGameMenu.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(Menu.NONE, Pages.REGISTRATION, Menu.NONE, "Регистрация")
            popup.menu.add(Menu.NONE, Pages.RULES, Menu.NONE, "Правила")
            popup.menu.add(Menu.NONE, Pages.AUTHORS, Menu.NONE, "Авторы")
            popup.menu.add(Menu.NONE, Pages.RECORDS, Menu.NONE, "Рекорды")
            popup.menu.add(Menu.NONE, Pages.SETTINGS, Menu.NONE, "Настройки")
            popup.setOnMenuItemClickListener { item ->
                (activity as? PageNavigationHost)?.navigateToPage(item.itemId)
                true
            }
            popup.show()
        }

        loadActiveUser()
    }

    override fun onResume() {
        super.onResume()
        gameView?.resumeGame()
        loadActiveUser()
    }

    override fun onPause() {
        gameView?.pauseGame()
        super.onPause()
    }

    override fun onDestroyView() {
        gameView?.onStateChanged = null
        gameView?.onRoundFinished = null
        gameView = null
        currentState = null
        super.onDestroyView()
    }

    private fun loadActiveUser() {
        GameRepository.getInstance(requireContext()).loadActiveUser { user ->
            currentUser = user
            tvCurrentUser?.text = if (user == null) {
                "Игрок: не выбран"
            } else {
                "Игрок: ${user.displayName}"
            }
            renderStartButton()
        }
    }

    private fun renderStartButton() {
        val button = btnStart ?: return
        val user = currentUser
        val state = currentState

        button.isEnabled = state?.running != true
        button.text = when {
            state?.running == true -> "Игра идёт..."
            state?.paused == true -> "Продолжить"
            user == null -> "Выберите пользователя"
            else -> "Начать игру"
        }
    }
}
