package io.github.hylkeb.retrocache.di

import io.github.hylkeb.retrocache.CacheConfiguration
import io.github.hylkeb.retrocache.CacheProvider
import io.github.hylkeb.retrocache.DateTimeProvider
import io.github.hylkeb.retrocache.state.internal.InternalRequestState
import io.github.hylkeb.susstatemachine.StateMachine
import io.github.hylkeb.susstatemachine.StateObserver
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter

internal interface CacheableRequestDependencyContainer<R> {
    val call: Call<R>
    val cacheConfiguration: CacheConfiguration
    val dateTimeProvider: DateTimeProvider
    val responseBodyConverter: Converter<ResponseBody, R>
    val cacheProvider: CacheProvider
    val requestStateObserver: StateObserver?
    val requestStateMachine: StateMachine<InternalRequestState<R>>
}