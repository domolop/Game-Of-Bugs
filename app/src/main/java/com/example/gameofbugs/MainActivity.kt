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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var day = 0
    private var month = 0
    private var year = 0

    private lateinit var tabLayout: TabLayout

    private lateinit var registrationTab: View
    private lateinit var rulesTab: View
    private lateinit var authorsTab: View
    private lateinit var settingsTab: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)

        registrationTab = findViewById(R.id.registrationTab)
        rulesTab = findViewById(R.id.rulesTab)
        authorsTab = findViewById(R.id.authorsTab)
        settingsTab = findViewById(R.id.settingsTab)

        setupTabs()
        setupRegistration()
        setupRules()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Регистрация"))
        tabLayout.addTab(tabLayout.newTab().setText("Правила"))
        tabLayout.addTab(tabLayout.newTab().setText("Авторы"))
        tabLayout.addTab(tabLayout.newTab().setText("Настройки"))

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab) {
                    showTab(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            }
        )

        showTab(0)
    }

    private fun showTab(position: Int) {
        registrationTab.visibility = View.GONE
        rulesTab.visibility = View.GONE
        authorsTab.visibility = View.GONE
        settingsTab.visibility = View.GONE

        when (position) {
            0 -> registrationTab.visibility = View.VISIBLE
            1 -> rulesTab.visibility = View.VISIBLE
            2 -> authorsTab.visibility = View.VISIBLE
            3 -> settingsTab.visibility = View.VISIBLE
        }
    }

    private fun setupRegistration() {

        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etMiddleName = findViewById<EditText>(R.id.etMiddleName)

        val rgGender = findViewById<RadioGroup>(R.id.rgGender)

        val spCourse = findViewById<Spinner>(R.id.spCourse)

        val sbDifficulty = findViewById<SeekBar>(R.id.sbDifficulty)
        val tvDifficultyValue = findViewById<TextView>(R.id.tvDifficultyValue)

        sbDifficulty.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    tvDifficultyValue.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        val btnBirthDate = findViewById<Button>(R.id.btnBirthDate)
        val tvBirthDate = findViewById<TextView>(R.id.tvBirthDate)

        val btnShowResult = findViewById<Button>(R.id.btnShowResult)
        val ivZodiac = findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnBirthDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val dialog = DatePickerDialog(
                this,
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
                ivZodiac.visibility = ImageView.VISIBLE
            } else {
                ivZodiac.visibility = ImageView.GONE
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

    private fun setupRules() {

        val tvRules = findViewById<TextView>(R.id.tvRules)

        val rulesHtml = resources
            .openRawResource(R.raw.rules)
            .bufferedReader()
            .use { it.readText() }

        tvRules.text = Html.fromHtml(
            rulesHtml,
            Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun getZodiacSign(day: Int, month: Int): String {
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козерог"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
            else -> "Рыбы"
        }
    }

    private fun getZodiacImageRes(zodiac: String): Int {
        return when (zodiac) {
            "Овен" -> R.drawable.zodiac_aries
            "Телец" -> R.drawable.zodiac_taurus
            "Близнецы" -> R.drawable.zodiac_gemini
            "Рак" -> R.drawable.zodiac_cancer
            "Лев" -> R.drawable.zodiac_leo
            "Дева" -> R.drawable.zodiac_virgo
            "Весы" -> R.drawable.zodiac_libra
            "Скорпион" -> R.drawable.zodiac_scorpio
            "Стрелец" -> R.drawable.zodiac_sagittarius
            "Козерог" -> R.drawable.zodiac_capricorn
            "Водолей" -> R.drawable.zodiac_aquarius
            "Рыбы" -> R.drawable.zodiac_pisces
            else -> 0
        }
    }
}
