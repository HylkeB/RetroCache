package io.github.hylkeb.retrocache.di

import io.github.hylkeb.retrocache.CacheConfiguration
import io.github.hylkeb.retrocache.CacheProvider
import io.github.hylkeb.retrocache.DateTimeProvider
import io.github.hylkeb.retrocache.state.internal.Idle
import io.github.hylkeb.retrocache.state.internal.InternalRequestState
import io.github.hylkeb.susstatemachine.StateMachine
import io.github.hylkeb.susstatemachine.StateObserver
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter

internal class RealCacheableRequestDependencyContainer<R>(
    override val call: Call<R>,
    override val cacheConfiguration: CacheConfiguration,
    override val dateTimeProvider: DateTimeProvider,
    override val responseBodyConverter: Converter<ResponseBody, R>,
    override val cacheProvider: CacheProvider,
    override val requestStateObserver: StateObserver?,
) : CacheableRequestDependencyContainer<R> {
    override val requestStateMachine: StateMachine<InternalRequestState<R>> by lazy {
        StateMachine(Idle(this), "RequestStateMachine for ${call.request().url.encodedPath}", requestStateObserver)
    }
}