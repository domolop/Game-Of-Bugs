package com.example.gameofbugs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameofbugs.data.GameRepository
import com.example.gameofbugs.data.RecordItem
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _records = MutableLiveData<List<RecordItem>>(emptyList())
    val records: LiveData<List<RecordItem>> = _records

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = gameRepository.loadRecords()
        }
    }
}
