package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.holidays.Holiday
import com.example.myapplication.holidays.HolidayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.hylkeb.retrocache.state.RequestState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    var holidays by mutableStateOf<List<Holiday>>(emptyList())
        private set

    val holidayState = holidayRepository.holidayFlow.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = RequestState.initialState())

    fun fetchHolidays() {
        viewModelScope.launch {
            holidays = holidayRepository.getHolidays()
        }
    }

    fun refreshCachedHolidays() {
        holidayRepository.refreshCachedHolidays()
    }
}