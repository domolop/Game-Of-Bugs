package com.example.gameofbugs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.PopupMenu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class GameFragment : Fragment(R.layout.tab_game) {

    private var gameView: GameView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameViewLocal = view.findViewById<GameView>(R.id.gameView)
        val tvScore = view.findViewById<TextView>(R.id.tvGameScore)
        val tvMisses = view.findViewById<TextView>(R.id.tvGameMisses)
        val tvTime = view.findViewById<TextView>(R.id.tvGameTime)
        val btnStart = view.findViewById<Button>(R.id.btnStartGame)
        val btnGameMenu = view.findViewById<Button>(R.id.btnGameMenu)

        gameView = gameViewLocal

        gameViewLocal.onStateChanged = { state ->
            tvScore.text = "Очки: ${state.score}"
            tvMisses.text = "Промахи: ${state.misses}"
            tvTime.text = if (state.paused) {
                "Пауза: ${state.remainingSeconds} сек."
            } else {
                "Время: ${state.remainingSeconds} сек."
            }

            btnStart.isEnabled = !state.running
            btnStart.text = when {
                state.running -> "Игра идёт..."
                state.paused -> "Продолжить"
                else -> "Начать игру"
            }
        }

        btnStart.setOnClickListener {
            gameViewLocal.startGame(GamePreferences(requireContext()).load())
        }

        btnGameMenu.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(Menu.NONE, Pages.RULES, Menu.NONE, "Правила")
            popup.menu.add(Menu.NONE, Pages.AUTHORS, Menu.NONE, "Авторы")
            popup.menu.add(Menu.NONE, Pages.SETTINGS, Menu.NONE, "Настройки")
            popup.setOnMenuItemClickListener { item ->
                (activity as? PageNavigationHost)?.navigateToPage(item.itemId)
                true
            }
            popup.show()
        }
    }

    override fun onPause() {
        gameView?.pauseGame()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        gameView?.resumeGame()
    }

    override fun onDestroyView() {
        gameView?.onStateChanged = null
        gameView = null
        super.onDestroyView()
    }
}
