package io.github.hylkeb.retrocache

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.internal.InternalRequestState
import io.github.hylkeb.susstatemachine.StateMachine
import io.github.hylkeb.susstatemachine.StateObserver
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter

internal class MockCacheableRequestDependencyContainer<R>(
    private val _call: Call<R>? = null,
    private val _cacheConfiguration: CacheConfiguration? = null,
    private val _dateTimeProvider: DateTimeProvider? = null,
    private val _responseBodyConverter: Converter<ResponseBody, R>? = null,
    private val _cacheProvider: CacheProvider? = null,
    private val _requestStateObserver: StateObserver? = null,
    private val _requestStateMachine: StateMachine<InternalRequestState<R>>? = null
) : CacheableRequestDependencyContainer<R> {
    override val call: Call<R>
        get() = requireNotNull(_call) { "No mocked value provided for call" }
    override val cacheConfiguration: CacheConfiguration
        get() = requireNotNull(_cacheConfiguration) { "No mocked value provided for cacheConfiguration" }
    override val dateTimeProvider: DateTimeProvider
        get() = requireNotNull(_dateTimeProvider) { "No mocked value provided for dateTimeFormatter" }
    override val responseBodyConverter: Converter<ResponseBody, R>
        get() = requireNotNull(_responseBodyConverter) { "No mocked value provided for responseBodyConverter" }
    override val cacheProvider: CacheProvider
        get() = requireNotNull(_cacheProvider) { "No mocked value provided for cacheProvider" }
    override val requestStateObserver: StateObserver?
        get() = requireNotNull(_requestStateObserver) { "No mocked value provided for requestStateObserver" }
    override val requestStateMachine: StateMachine<InternalRequestState<R>>
        get() = requireNotNull(_requestStateMachine) { "No mocked value provided for requestStateMachine" }
}
