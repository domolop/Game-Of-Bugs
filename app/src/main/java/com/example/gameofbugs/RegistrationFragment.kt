package com.example.gameofbugs

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Locale

class RegistrationFragment : Fragment(R.layout.registration_form) {

    private var day = 0
    private var month = 0
    private var year = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etLastName = view.findViewById<EditText>(R.id.etLastName)
        val etFirstName = view.findViewById<EditText>(R.id.etFirstName)
        val etMiddleName = view.findViewById<EditText>(R.id.etMiddleName)
        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        val spCourse = view.findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = view.findViewById<SeekBar>(R.id.sbDifficulty)
        val tvDifficultyValue = view.findViewById<TextView>(R.id.tvDifficultyValue)
        val btnBirthDate = view.findViewById<Button>(R.id.btnBirthDate)
        val tvBirthDate = view.findViewById<TextView>(R.id.tvBirthDate)
        val btnShowResult = view.findViewById<Button>(R.id.btnShowResult)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        sbDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficultyValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        btnBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    year = selectedYear
                    month = selectedMonth + 1
                    day = selectedDay
                    tvBirthDate.text = String.format(
                        Locale.getDefault(),
                        "%02d.%02d.%04d",
                        day,
                        month,
                        year
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }

        btnShowResult.setOnClickListener {
            val lastName = etLastName.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()

            if (lastName.isEmpty() || firstName.isEmpty()) {
                tvResult.text = "Введите фамилию и имя!"
                return@setOnClickListener
            }

            if (day == 0) {
                tvResult.text = "Выберите дату рождения!"
                return@setOnClickListener
            }

            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbFemale -> "Женский"
                else -> "Мужской"
            }

            val birthDate = String.format(
                Locale.getDefault(),
                "%02d.%02d.%04d",
                day,
                month,
                year
            )

            val zodiac = getZodiacSign(day, month)
            val zodiacRes = getZodiacImageRes(zodiac)

            val player = Player(
                firstName = firstName,
                lastName = lastName,
                middleName = middleName,
                gender = gender,
                birthDate = birthDate,
                zodiac = zodiac,
                course = spCourse.selectedItem.toString(),
                difficulty = sbDifficulty.progress
            )

            if (zodiacRes != 0) {
                ivZodiac.setImageResource(zodiacRes)
                ivZodiac.visibility = View.VISIBLE
            } else {
                ivZodiac.visibility = View.GONE
            }

            tvResult.text = """
                Фамилия: ${player.lastName}
                Имя: ${player.firstName}
                Отчество: ${player.middleName.ifEmpty { "—" }}
                Пол: ${player.gender}
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiac}
                Курс: ${player.course}
                Сложность: ${player.difficulty}
            """.trimIndent()
        }
    }
}
