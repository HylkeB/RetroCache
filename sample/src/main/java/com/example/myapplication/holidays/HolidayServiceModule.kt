package com.example.myapplication.holidays

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.hylkeb.retrocache.CacheProvider
import io.github.hylkeb.retrocache.CacheableRequestCallAdapterFactory
import io.github.hylkeb.retrocache.CachingInterceptor
import io.github.hylkeb.retrocache.FolderCacheProvider
import io.github.hylkeb.susstatemachine.StateObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object HolidayServiceModule {
    @Provides
    fun provideHolidayService(
        cacheProvider: CacheProvider,
        okHttpClient: OkHttpClient,
        stateObserver: StateObserver
    ): HolidayService {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://date.nager.at")
            .client(okHttpClient)
            .addCallAdapterFactory(CacheableRequestCallAdapterFactory(SupervisorJob(), cacheProvider, requestStateObserver = stateObserver))
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()
            .create(HolidayService::class.java)
    }

    @Provides
    fun provideCacheProvider(@ApplicationContext context: Context): CacheProvider {
        return FolderCacheProvider(context.cacheDir)
    }

    @Provides
    fun provideOkHttpClient(cacheProvider: CacheProvider): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(CachingInterceptor(cacheProvider))
            .build()
    }

    @Provides
    fun provideRequestStateObserver(): StateObserver {
        return StateObserver { stateMachine, fromState, toState, reason, cause -> Log.i("REQUEST_STATE", "$stateMachine: $fromState -> $toState ($reason, ${cause?.message})") }
    }
}
