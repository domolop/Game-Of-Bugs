package com.example.gameofbugs

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(R.layout.tab_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = GamePreferences(requireContext())
        val current = preferences.load()

        val sbGameSpeed = view.findViewById<SeekBar>(R.id.sbGameSpeed)
        val sbMaxBugs = view.findViewById<SeekBar>(R.id.sbMaxBugs)
        val sbBonusInterval = view.findViewById<SeekBar>(R.id.sbBonusInterval)
        val sbRoundDuration = view.findViewById<SeekBar>(R.id.sbRoundDuration)

        val tvGameSpeedValue = view.findViewById<TextView>(R.id.tvGameSpeedValue)
        val tvMaxBugsValue = view.findViewById<TextView>(R.id.tvMaxBugsValue)
        val tvBonusIntervalValue = view.findViewById<TextView>(R.id.tvBonusIntervalValue)
        val tvRoundDurationValue = view.findViewById<TextView>(R.id.tvRoundDurationValue)

        sbGameSpeed.progress = current.gameSpeed - 1
        sbMaxBugs.progress = current.maxBugs - 1
        sbBonusInterval.progress = current.bonusIntervalSeconds - 5
        sbRoundDuration.progress = current.roundDurationSeconds - 30

        tvGameSpeedValue.text = current.gameSpeed.toString()
        tvMaxBugsValue.text = current.maxBugs.toString()
        tvBonusIntervalValue.text = "${current.bonusIntervalSeconds} сек."
        tvRoundDurationValue.text = "${current.roundDurationSeconds} сек."

        sbGameSpeed.setOnSeekBarChangeListener(simpleListener { tvGameSpeedValue.text = (it + 1).toString() })
        sbMaxBugs.setOnSeekBarChangeListener(simpleListener { tvMaxBugsValue.text = (it + 1).toString() })
        sbBonusInterval.setOnSeekBarChangeListener(simpleListener { tvBonusIntervalValue.text = "${it + 5} сек." })
        sbRoundDuration.setOnSeekBarChangeListener(simpleListener { tvRoundDurationValue.text = "${it + 30} сек." })

        view.findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            preferences.save(
                GameSettings(
                    gameSpeed = sbGameSpeed.progress + 1,
                    maxBugs = sbMaxBugs.progress + 1,
                    bonusIntervalSeconds = sbBonusInterval.progress + 5,
                    roundDurationSeconds = sbRoundDuration.progress + 30
                )
            )

            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun simpleListener(onProgress: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgress(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    }
}
