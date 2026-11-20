package com.example.gameofbugs.ui

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gameofbugs.R
import com.example.gameofbugs.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameFragment : Fragment(R.layout.tab_game) {

    private val game: GameViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameView = view.findViewById<GameView>(R.id.gameView)
        val tvScore = view.findViewById<TextView>(R.id.tvGameScore)
        val tvMisses = view.findViewById<TextView>(R.id.tvGameMisses)
        val tvTime = view.findViewById<TextView>(R.id.tvGameTime)
        val btnStart = view.findViewById<Button>(R.id.btnStartGame)
        val btnGameMenu = view.findViewById<Button>(R.id.btnGameMenu)
        val tvCurrentUser = view.findViewById<TextView>(R.id.tvCurrentUser)

        gameView.setGame(game)

        game.state.observe(viewLifecycleOwner) { state ->
            tvScore.text = "Очки: ${state.score}"
            tvMisses.text = "Промахи: ${state.misses}"
            tvTime.text = if (state.paused) {
                "Пауза: ${state.remainingSeconds} сек."
            } else {
                "Время: ${state.remainingSeconds} сек."
            }
            gameView.setTilting(state.tilting)
            gameView.invalidate()
            renderStartButton(btnStart)
        }

        game.user.observe(viewLifecycleOwner) { user ->
            tvCurrentUser.text = if (user == null) {
                "Игрок: не выбран"
            } else {
                "Игрок: ${user.displayName}"
            }
            renderStartButton(btnStart)
        }

        game.message.observe(viewLifecycleOwner) { text ->
            if (text != null) {
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                game.messageShown()
            }
        }

        btnStart.setOnClickListener {
            when {
                game.state.value?.paused == true -> {
                    game.resumeGame()
                }

                game.user.value == null -> {
                    (activity as? PageNavigationHost)?.navigateToPage(Pages.REGISTRATION)
                }

                else -> {
                    game.startGame()
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
    }

    override fun onResume() {
        super.onResume()
        game.resumeGame()
        game.loadActiveUser()
        game.loadGoldRate()
    }

    override fun onPause() {
        game.pauseGame()
        super.onPause()
    }

    private fun renderStartButton(button: Button) {
        val state = game.state.value
        val user = game.user.value

        button.isEnabled = state?.running != true
        button.text = when {
            state?.running == true -> "Игра идёт..."
            state?.paused == true -> "Продолжить"
            user == null -> "Выберите пользователя"
            else -> "Начать игру"
        }
    }
}
