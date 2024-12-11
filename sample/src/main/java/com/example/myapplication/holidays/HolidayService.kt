package com.example.myapplication.holidays

import io.github.hylkeb.retrocache.CacheConfiguration
import io.github.hylkeb.retrocache.CacheableRequest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Tag

interface HolidayService {
    @GET("/api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getHolidays(
        @Path("year") year: String,
        @Path("countryCode") countryCode: String,
    ): List<Holiday>


    @GET("/api/v3/PublicHolidays/{year}/{countryCode}")
    fun getCachedHolidays(
        @Path("year") year: String,
        @Path("countryCode") countryCode: String,
        @Tag cacheConfiguration: CacheConfiguration = CacheConfiguration(
            staleDuration = 3.days,
            expirationDuration = 7.days,
            persistedCache = true
        )
    ): CacheableRequest<List<Holiday>>
}
