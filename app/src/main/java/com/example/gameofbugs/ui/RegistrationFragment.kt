package com.example.gameofbugs.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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
import com.example.gameofbugs.R
import com.example.gameofbugs.data.UserEntity
import com.example.gameofbugs.model.getZodiacImageRes
import com.example.gameofbugs.viewmodel.RegistrationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class RegistrationFragment : Fragment(R.layout.registration_form) {

    private val registration: RegistrationViewModel by viewModel()

    private lateinit var etLastName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var sbDifficulty: SeekBar
    private lateinit var tvDifficultyValue: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etLastName = view.findViewById(R.id.etLastName)
        etFirstName = view.findViewById(R.id.etFirstName)
        etMiddleName = view.findViewById(R.id.etMiddleName)
        rgGender = view.findViewById(R.id.rgGender)
        sbDifficulty = view.findViewById(R.id.sbDifficulty)
        tvDifficultyValue = view.findViewById(R.id.tvDifficultyValue)

        val spCourse = view.findViewById<Spinner>(R.id.spCourse)
        val btnBirthDate = view.findViewById<Button>(R.id.btnBirthDate)
        val tvBirthDate = view.findViewById<TextView>(R.id.tvBirthDate)
        val btnSaveUser = view.findViewById<Button>(R.id.btnSaveUser)
        val btnActivateUser = view.findViewById<Button>(R.id.btnActivateUser)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val spSavedUsers = view.findViewById<Spinner>(R.id.spSavedUsers)
        val tvCurrentUser = view.findViewById<TextView>(R.id.tvCurrentUser)

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
                    registration.setBirthDate(selectedYear, selectedMonth + 1, selectedDay)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }

        spSavedUsers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                if (position == registration.selectedIndex()) {
                    return
                }

                registration.selectUser(position)

                val user = registration.selectedUser.value
                if (user == null) {
                    clearForm()
                } else {
                    fillForm(user)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        btnSaveUser.setOnClickListener {
            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbFemale -> "Женский"
                else -> "Мужской"
            }

            registration.saveUser(
                etLastName.text.toString().trim(),
                etFirstName.text.toString().trim(),
                etMiddleName.text.toString().trim(),
                gender,
                spCourse.selectedItem.toString(),
                sbDifficulty.progress
            )
        }

        btnActivateUser.setOnClickListener {
            registration.activateUser()
        }

        registration.birthDate.observe(viewLifecycleOwner) { text ->
            tvBirthDate.text = text
        }

        registration.result.observe(viewLifecycleOwner) { text ->
            tvResult.text = text
        }

        registration.activeUser.observe(viewLifecycleOwner) { user ->
            tvCurrentUser.text = if (user == null) {
                "Текущий игрок: не выбран"
            } else {
                "Текущий игрок: ${user.displayName}"
            }
        }

        registration.selectedUser.observe(viewLifecycleOwner) { user ->
            val zodiacRes = if (user == null) 0 else getZodiacImageRes(user.zodiac)
            if (zodiacRes != 0) {
                ivZodiac.setImageResource(zodiacRes)
                ivZodiac.visibility = View.VISIBLE
            } else {
                ivZodiac.visibility = View.GONE
            }
        }

        registration.users.observe(viewLifecycleOwner) { users ->
            val labels = mutableListOf("— новый пользователь —")
            labels += users.map { it.displayName }

            spSavedUsers.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spSavedUsers.setSelection(registration.selectedIndex(), false)
        }

        registration.message.observe(viewLifecycleOwner) { text ->
            if (text != null) {
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                registration.messageShown()
            }
        }

        registration.loadUsers()
    }

    override fun onResume() {
        super.onResume()
        registration.loadActiveUser()
    }

    private fun fillForm(user: UserEntity) {
        etLastName.setText(user.lastName)
        etFirstName.setText(user.firstName)
        etMiddleName.setText(user.middleName)
        sbDifficulty.progress = user.difficulty
        tvDifficultyValue.text = user.difficulty.toString()
        rgGender.check(
            when (user.gender) {
                "Женский" -> R.id.rbFemale
                else -> R.id.rbMale
            }
        )
    }

    private fun clearForm() {
        etLastName.text.clear()
        etFirstName.text.clear()
        etMiddleName.text.clear()
        sbDifficulty.progress = 5
        tvDifficultyValue.text = "5"
    }
}
