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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import java.util.Calendar
import java.util.Locale
import android.view.Menu
import android.widget.PopupMenu
class MainActivity : AppCompatActivity() {

    private var day = 0
    private var month = 0
    private var year = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTabs()
        setupRegistration()
        setupRules()
        setupAuthors()
        setupSettings()
        setupGame()
        setupGameMenu()
    }

    private fun setupGameMenu() {
        val btnGameMenu =
            findViewById<Button>(R.id.btnGameMenu)

        btnGameMenu.setOnClickListener { anchor ->

            val popup =
                PopupMenu(this, anchor)

            popup.menu.add(
                Menu.NONE,
                TAB_RULES,
                Menu.NONE,
                "Правила"
            )

            popup.menu.add(
                Menu.NONE,
                TAB_AUTHORS,
                Menu.NONE,
                "Авторы"
            )

            popup.menu.add(
                Menu.NONE,
                TAB_SETTINGS,
                Menu.NONE,
                "Настройки"
            )

            popup.setOnMenuItemClickListener { item ->
                selectTab(item.itemId)
                true
            }

            popup.show()
        }
    }

    private fun selectTab(position: Int) {
        findViewById<TabLayout>(
            R.id.tabLayout
        ).getTabAt(position)?.select()
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val tabs = listOf(
            findViewById<View>(R.id.gameTab),
            findViewById<View>(R.id.registrationTab),
            findViewById<View>(R.id.rulesTab),
            findViewById<View>(R.id.authorsTab),
            findViewById<View>(R.id.settingsTab)
        )

        listOf(
            "Игра",
            "Регистрация",
            "Правила",
            "Авторы",
            "Настройки"
        ).forEach { title ->
            tabLayout.addTab(
                tabLayout.newTab().setText(title)
            )
        }

        fun showTab(position: Int) {
            tabs.forEachIndexed { index, view ->
                view.visibility =
                    if (index == position) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(
                    tab: TabLayout.Tab
                ) {
                    showTab(tab.position)
                }

                override fun onTabUnselected(
                    tab: TabLayout.Tab
                ) = Unit

                override fun onTabReselected(
                    tab: TabLayout.Tab
                ) = Unit
            }
        )

        showTab(TAB_GAME)
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

        val btnShowResult = findViewById<Button>(R.id.btnShowResult)
        val ivZodiac = findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = findViewById<TextView>(R.id.tvResult)

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

    private fun setupAuthors() {

        val lvAuthors = findViewById<android.widget.ListView>(R.id.lvAuthors)

        val authors = listOf(
            Author(
                "Скуртяин Д.Е.",
                R.drawable.author_skuryatin
            ),
            Author(
                "Кумов Д.В.",
                R.drawable.author_kumov
            )
        )

        lvAuthors.adapter = AuthorAdapter(
            this,
            authors
        )
    }

    private fun setupSettings() {
        val preferences =
            GamePreferences(this)

        val current =
            preferences.load()

        val sbGameSpeed =
            findViewById<SeekBar>(
                R.id.sbGameSpeed
            )

        val sbMaxBugs =
            findViewById<SeekBar>(
                R.id.sbMaxBugs
            )

        val sbBonusInterval =
            findViewById<SeekBar>(
                R.id.sbBonusInterval
            )

        val sbRoundDuration =
            findViewById<SeekBar>(
                R.id.sbRoundDuration
            )

        val tvGameSpeedValue =
            findViewById<TextView>(
                R.id.tvGameSpeedValue
            )

        val tvMaxBugsValue =
            findViewById<TextView>(
                R.id.tvMaxBugsValue
            )

        val tvBonusIntervalValue =
            findViewById<TextView>(
                R.id.tvBonusIntervalValue
            )

        val tvRoundDurationValue =
            findViewById<TextView>(
                R.id.tvRoundDurationValue
            )

        sbGameSpeed.progress =
            current.gameSpeed - 1

        sbMaxBugs.progress =
            current.maxBugs - 1

        sbBonusInterval.progress =
            current.bonusIntervalSeconds - 5

        sbRoundDuration.progress =
            current.roundDurationSeconds - 30

        tvGameSpeedValue.text =
            current.gameSpeed.toString()

        tvMaxBugsValue.text =
            current.maxBugs.toString()

        tvBonusIntervalValue.text =
            "${current.bonusIntervalSeconds} сек."

        tvRoundDurationValue.text =
            "${current.roundDurationSeconds} сек."

        sbGameSpeed.setOnSeekBarChangeListener(
            simpleListener {
                tvGameSpeedValue.text =
                    (it + 1).toString()
            }
        )

        sbMaxBugs.setOnSeekBarChangeListener(
            simpleListener {
                tvMaxBugsValue.text =
                    (it + 1).toString()
            }
        )

        sbBonusInterval.setOnSeekBarChangeListener(
            simpleListener {
                tvBonusIntervalValue.text =
                    "${it + 5} сек."
            }
        )

        sbRoundDuration.setOnSeekBarChangeListener(
            simpleListener {
                tvRoundDurationValue.text =
                    "${it + 30} сек."
            }
        )

        findViewById<Button>(
            R.id.btnSaveSettings
        ).setOnClickListener {

            preferences.save(
                GameSettings(
                    gameSpeed =
                        sbGameSpeed.progress + 1,

                    maxBugs =
                        sbMaxBugs.progress + 1,

                    bonusIntervalSeconds =
                        sbBonusInterval.progress + 5,

                    roundDurationSeconds =
                        sbRoundDuration.progress + 30
                )
            )

            Toast.makeText(
                this,
                "Настройки сохранены",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupGame() {
        val gameView =
            findViewById<GameView>(R.id.gameView)

        val tvScore =
            findViewById<TextView>(R.id.tvGameScore)

        val tvMisses =
            findViewById<TextView>(R.id.tvGameMisses)

        val tvTime =
            findViewById<TextView>(R.id.tvGameTime)

        val btnStart =
            findViewById<Button>(R.id.btnStartGame)

        gameView.onStateChanged = { state ->

            tvScore.text =
                "Очки: ${state.score}"

            tvMisses.text =
                "Промахи: ${state.misses}"

            tvTime.text =
                if (state.paused) {
                    "Пауза: ${state.remainingSeconds} сек."
                } else {
                    "Время: ${state.remainingSeconds} сек."
                }

            btnStart.isEnabled =
                !state.running

            btnStart.text =
                when {
                    state.running ->
                        "Игра идёт..."

                    state.paused ->
                        "Продолжить"

                    else ->
                        "Начать игру"
                }
        }

        btnStart.setOnClickListener {
            gameView.startGame(
                GamePreferences(this).load()
            )
        }
    }

    private fun simpleListener(
        onProgress: (Int) -> Unit
    ) = object : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(
            seekBar: SeekBar?,
            progress: Int,
            fromUser: Boolean
        ) {
            onProgress(progress)
        }

        override fun onStartTrackingTouch(
            seekBar: SeekBar?
        ) {
        }

        override fun onStopTrackingTouch(
            seekBar: SeekBar?
        ) {
        }
    }

    private fun getZodiacSign(
        day: Int,
        month: Int
    ): String {

        return when {

            (month == 3 && day >= 21) ||
                    (month == 4 && day <= 19) ->
                "Овен"

            (month == 4 && day >= 20) ||
                    (month == 5 && day <= 20) ->
                "Телец"

            (month == 5 && day >= 21) ||
                    (month == 6 && day <= 20) ->
                "Близнецы"

            (month == 6 && day >= 21) ||
                    (month == 7 && day <= 22) ->
                "Рак"

            (month == 7 && day >= 23) ||
                    (month == 8 && day <= 22) ->
                "Лев"

            (month == 8 && day >= 23) ||
                    (month == 9 && day <= 22) ->
                "Дева"

            (month == 9 && day >= 23) ||
                    (month == 10 && day <= 22) ->
                "Весы"

            (month == 10 && day >= 23) ||
                    (month == 11 && day <= 21) ->
                "Скорпион"

            (month == 11 && day >= 22) ||
                    (month == 12 && day <= 21) ->
                "Стрелец"

            (month == 12 && day >= 22) ||
                    (month == 1 && day <= 19) ->
                "Козерог"

            (month == 1 && day >= 20) ||
                    (month == 2 && day <= 18) ->
                "Водолей"

            else ->
                "Рыбы"
        }
    }

    private fun getZodiacImageRes(
        zodiac: String
    ): Int {

        return when (zodiac) {

            "Овен" ->
                R.drawable.zodiac_aries

            "Телец" ->
                R.drawable.zodiac_taurus

            "Близнецы" ->
                R.drawable.zodiac_gemini

            "Рак" ->
                R.drawable.zodiac_cancer

            "Лев" ->
                R.drawable.zodiac_leo

            "Дева" ->
                R.drawable.zodiac_virgo

            "Весы" ->
                R.drawable.zodiac_libra

            "Скорпион" ->
                R.drawable.zodiac_scorpio

            "Стрелец" ->
                R.drawable.zodiac_sagittarius

            "Козерог" ->
                R.drawable.zodiac_capricorn

            "Водолей" ->
                R.drawable.zodiac_aquarius

            "Рыбы" ->
                R.drawable.zodiac_pisces

            else ->
                0
        }

    }
    companion object {
        const val TAB_GAME = 0
        const val TAB_RULES = 2
        const val TAB_AUTHORS = 3
        const val TAB_SETTINGS = 4
    }
}