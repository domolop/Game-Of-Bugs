package com.example.gameofbugs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameofbugs.data.GameRepository
import com.example.gameofbugs.data.UserEntity
import com.example.gameofbugs.model.Player
import com.example.gameofbugs.model.getZodiacSign
import kotlinx.coroutines.launch
import java.util.Locale

class RegistrationViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>(emptyList())
    val users: LiveData<List<UserEntity>> = _users

    private val _selectedUser = MutableLiveData<UserEntity?>()
    val selectedUser: LiveData<UserEntity?> = _selectedUser

    private val _activeUser = MutableLiveData<UserEntity?>()
    val activeUser: LiveData<UserEntity?> = _activeUser

    private val _birthDate = MutableLiveData(NO_BIRTH_DATE)
    val birthDate: LiveData<String> = _birthDate

    private val _result = MutableLiveData("")
    val result: LiveData<String> = _result

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private var day = 0
    private var month = 0
    private var year = 0

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = gameRepository.loadUsers()
        }
    }

    fun loadActiveUser() {
        viewModelScope.launch {
            _activeUser.value = gameRepository.loadActiveUser()
        }
    }

    fun messageShown() {
        _message.value = null
    }

    fun selectedIndex(): Int {
        val id = _selectedUser.value?.id ?: return 0
        val list = _users.value ?: return 0
        return list.indexOfFirst { it.id == id } + 1
    }

    fun setBirthDate(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day

        _birthDate.value = if (day == 0) {
            NO_BIRTH_DATE
        } else {
            String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month, year)
        }
    }

    fun selectUser(position: Int) {
        val user = if (position == 0) null else _users.value?.getOrNull(position - 1)
        _selectedUser.value = user

        if (user == null) {
            setBirthDate(0, 0, 0)
            _result.value = ""
            return
        }

        val parts = user.birthDate.split(".")
        if (parts.size == 3) {
            setBirthDate(
                parts[2].toIntOrNull() ?: 0,
                parts[1].toIntOrNull() ?: 0,
                parts[0].toIntOrNull() ?: 0
            )
        }

        gameRepository.setActiveUser(user.id)
        _activeUser.value = user
        _result.value = "Выбран пользователь: ${user.displayName}"
    }

    fun saveUser(
        lastName: String,
        firstName: String,
        middleName: String,
        gender: String,
        course: String,
        difficulty: Int
    ) {
        if (lastName.isEmpty() || firstName.isEmpty()) {
            _result.value = "Введите фамилию и имя!"
            return
        }

        if (day == 0) {
            _result.value = "Выберите дату рождения!"
            return
        }

        val player = Player(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            gender = gender,
            birthDate = _birthDate.value.orEmpty(),
            zodiac = getZodiacSign(day, month),
            course = course,
            difficulty = difficulty
        )

        viewModelScope.launch {
            val user = gameRepository.saveUser(player, _selectedUser.value?.id)

            _selectedUser.value = user
            _activeUser.value = user
            _result.value = buildString {
                appendLine("Пользователь сохранён")
                appendLine("Имя: ${user.displayName}")
                appendLine("Пол: ${user.gender}")
                appendLine("Дата рождения: ${user.birthDate}")
                appendLine("Знак зодиака: ${user.zodiac}")
                appendLine("Курс: ${user.course}")
                appendLine("Сложность: ${user.difficulty}")
            }.trimEnd()
            _message.value = "Пользователь сохранён"
            _users.value = gameRepository.loadUsers()
        }
    }

    fun activateUser() {
        val user = _selectedUser.value

        if (user == null) {
            _message.value = "Выберите сохранённого пользователя"
            return
        }

        gameRepository.setActiveUser(user.id)
        _activeUser.value = user
        _result.value = "Пользователь активирован: ${user.displayName}"
        _message.value = "Пользователь активирован"
    }

    companion object {
        private const val NO_BIRTH_DATE = "Дата не выбрана"
    }
}
