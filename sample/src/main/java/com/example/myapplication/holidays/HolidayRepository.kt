package com.example.myapplication.holidays

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject internal constructor(
    private val holidayService: HolidayService
) {

    private val cachedHolidayRequest = holidayService.getCachedHolidays("2024", "NL")

    suspend fun getHolidays(): List<Holiday> {
        return holidayService.getHolidays("2024", "NL")
    }

    fun refreshCachedHolidays() {
        cachedHolidayRequest.forceRefresh()
    }

    val holidayFlow by lazy { cachedHolidayRequest.observeData() }

}