package com.example.gameofbugs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Locale

class RegistrationFragment : Fragment(R.layout.registration_form) {

    private var day = 0
    private var month = 0
    private var year = 0

    private var users: List<UserEntity> = emptyList()
    private var selectedUser: UserEntity? = null
    private var suppressSelectionEvents = false

    // 1. ДОБАВЛЯЕМ ПЕРЕМЕННУЮ НА УРОВЕНЬ КЛАССА
    private var tvCurrentUser: TextView? = null

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
        val btnSaveUser = view.findViewById<Button>(R.id.btnSaveUser)
        val btnActivateUser = view.findViewById<Button>(R.id.btnActivateUser)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val spSavedUsers = view.findViewById<Spinner>(R.id.spSavedUsers)

        // 2. ИНИЦИАЛИЗИРУЕМ ЕЁ (удаляем слово 'val' перед именем)
        tvCurrentUser = view.findViewById<TextView>(R.id.tvCurrentUser)

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

        spSavedUsers.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                if (suppressSelectionEvents) {
                    return
                }

                if (position == 0) {
                    selectedUser = null
                    clearForm(
                        etLastName,
                        etFirstName,
                        etMiddleName,
                        tvBirthDate,
                        ivZodiac,
                        tvResult,
                        sbDifficulty,
                        tvDifficultyValue
                    )
                    return
                }

                val user = users.getOrNull(position - 1)
                if (user != null) {
                    selectedUser = user
                    fillForm(
                        user,
                        etLastName,
                        etFirstName,
                        etMiddleName,
                        rgGender,
                        tvBirthDate,
                        sbDifficulty,
                        tvDifficultyValue,
                        // 3. Используем безопасный вызов !! или передаем напрямую, так как мы внутри onViewCreated
                        tvCurrentUser!!,
                        ivZodiac,
                        tvResult
                    )
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        btnSaveUser.setOnClickListener {
            saveUser(
                etLastName,
                etFirstName,
                etMiddleName,
                rgGender,
                spCourse,
                sbDifficulty,
                tvDifficultyValue,
                tvBirthDate,
                ivZodiac,
                tvResult,
                spSavedUsers
            )
        }

        btnActivateUser.setOnClickListener {
            val user = selectedUser
            if (user == null) {
                Toast.makeText(requireContext(), "Выберите сохранённого пользователя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GameRepository.getInstance(requireContext()).setActiveUser(user.id)
            // 4. Добавляем оператор безопасного вызова ?. к свойству класса
            tvCurrentUser?.text = "Текущий игрок: ${user.displayName}"
            tvResult.text = "Пользователь активирован: ${user.displayName}"
            Toast.makeText(requireContext(), "Пользователь активирован", Toast.LENGTH_SHORT).show()
        }

        // 5. Передаем в методы актуальную переменную (через !!)
        loadCurrentUser(tvCurrentUser!!)
        loadUsers(
            spSavedUsers,
            tvCurrentUser!!,
            etLastName,
            etFirstName,
            etMiddleName,
            rgGender,
            tvBirthDate,
            sbDifficulty,
            tvDifficultyValue,
            ivZodiac,
            tvResult
        )
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<TextView>(R.id.tvCurrentUser)?.let { loadCurrentUser(it) }
    }

    private fun saveUser(
        etLastName: EditText,
        etFirstName: EditText,
        etMiddleName: EditText,
        rgGender: RadioGroup,
        spCourse: Spinner,
        sbDifficulty: SeekBar,
        tvDifficultyValue: TextView,
        tvBirthDate: TextView,
        ivZodiac: ImageView,
        tvResult: TextView,
        spSavedUsers: Spinner
    ) {
        val lastName = etLastName.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val middleName = etMiddleName.text.toString().trim()

        if (lastName.isEmpty() || firstName.isEmpty()) {
            tvResult.text = "Введите фамилию и имя!"
            return
        }

        if (day == 0) {
            tvResult.text = "Выберите дату рождения!"
            return
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

        GameRepository.getInstance(requireContext())
            .saveUser(player, selectedUser?.id) { user ->
                selectedUser = user
                // active user label is refreshed separately
                tvResult.text = buildString {
                    appendLine("Пользователь сохранён")
                    appendLine("Имя: ${user.displayName}")
                    appendLine("Пол: ${user.gender}")
                    appendLine("Дата рождения: ${user.birthDate}")
                    appendLine("Знак зодиака: ${user.zodiac}")
                    appendLine("Курс: ${user.course}")
                    appendLine("Сложность: ${user.difficulty}")
                }.trimEnd()
                Toast.makeText(requireContext(), "Пользователь сохранён", Toast.LENGTH_SHORT).show()
                loadUsers(
                    spSavedUsers,
                    tvCurrentUser!!,
                    etLastName,
                    etFirstName,
                    etMiddleName,
                    rgGender,
                    tvBirthDate,
                    sbDifficulty,
                    tvDifficultyValue,
                    ivZodiac,
                    tvResult
                )
            }
    }

    private fun loadUsers(
        spSavedUsers: Spinner,
        tvCurrentUser: TextView,
        etLastName: EditText,
        etFirstName: EditText,
        etMiddleName: EditText,
        rgGender: RadioGroup,
        tvBirthDate: TextView,
        sbDifficulty: SeekBar,
        tvDifficultyValue: TextView,
        ivZodiac: ImageView,
        tvResult: TextView
    ) {
        GameRepository.getInstance(requireContext()).loadUsers { loadedUsers ->
            users = loadedUsers
            val labels = mutableListOf("— новый пользователь —")
            labels += loadedUsers.map { it.displayName }

            suppressSelectionEvents = true
            spSavedUsers.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            val selectedIndex = selectedUser?.let { user ->
                loadedUsers.indexOfFirst { it.id == user.id }
                    .takeIf { it >= 0 }
                    ?.plus(1)
            } ?: 0

            spSavedUsers.setSelection(selectedIndex, false)
            suppressSelectionEvents = false

            loadCurrentUser(tvCurrentUser)
        }
    }

    private fun loadCurrentUser(tvCurrentUser: TextView) {
        GameRepository.getInstance(requireContext()).loadActiveUser { user ->
            tvCurrentUser.text = if (user == null) {
                "Текущий игрок: не выбран"
            } else {
                "Текущий игрок: ${user.displayName}"
            }
        }
    }

    private fun fillForm(
        user: UserEntity,
        etLastName: EditText,
        etFirstName: EditText,
        etMiddleName: EditText,
        rgGender: RadioGroup,
        tvBirthDate: TextView,
        sbDifficulty: SeekBar,
        tvDifficultyValue: TextView,
        tvCurrentUser: TextView,
        ivZodiac: ImageView,
        tvResult: TextView
    ) {
        etLastName.setText(user.lastName)
        etFirstName.setText(user.firstName)
        etMiddleName.setText(user.middleName)
        tvBirthDate.text = user.birthDate
        tvDifficultyValue.text = user.difficulty.toString()
        sbDifficulty.progress = user.difficulty
        rgGender.check(
            when (user.gender) {
                "Женский" -> R.id.rbFemale
                else -> R.id.rbMale
            }
        )

        val parts = user.birthDate.split(".")
        if (parts.size == 3) {
            day = parts[0].toIntOrNull() ?: 0
            month = parts[1].toIntOrNull() ?: 0
            year = parts[2].toIntOrNull() ?: 0
        }

        val zodiacRes = getZodiacImageRes(user.zodiac)
        if (zodiacRes != 0) {
            ivZodiac.setImageResource(zodiacRes)
            ivZodiac.visibility = View.VISIBLE
        }

        GameRepository.getInstance(requireContext()).setActiveUser(user.id)
        loadCurrentUser(tvCurrentUser)
        tvResult.text = "Выбран пользователь: ${user.displayName}"
    }

    private fun clearForm(
        etLastName: EditText,
        etFirstName: EditText,
        etMiddleName: EditText,
        tvBirthDate: TextView,
        ivZodiac: ImageView,
        tvResult: TextView,
        sbDifficulty: SeekBar,
        tvDifficultyValue: TextView
    ) {
        etLastName.text.clear()
        etFirstName.text.clear()
        etMiddleName.text.clear()
        day = 0
        month = 0
        year = 0
        tvBirthDate.text = "Дата не выбрана"
        ivZodiac.visibility = View.GONE
        tvResult.text = ""
        sbDifficulty.progress = 5
        tvDifficultyValue.text = "5"
    }
}
