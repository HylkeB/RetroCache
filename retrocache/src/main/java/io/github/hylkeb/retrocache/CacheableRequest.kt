package io.github.hylkeb.retrocache

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.di.RealCacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.state.internal.ErrorWithException
import io.github.hylkeb.retrocache.state.internal.ErrorWithResponse
import io.github.hylkeb.retrocache.state.internal.Fetching
import io.github.hylkeb.retrocache.state.internal.Idle
import io.github.hylkeb.retrocache.state.internal.InternalRequestState
import io.github.hylkeb.retrocache.state.internal.RefreshFailedWithException
import io.github.hylkeb.retrocache.state.internal.RefreshFailedWithResponse
import io.github.hylkeb.retrocache.state.internal.Refreshing
import io.github.hylkeb.retrocache.state.internal.Success
import io.github.hylkeb.susstatemachine.StateMachine
import io.github.hylkeb.susstatemachine.StateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter

class CacheableRequest<R> internal constructor(
    private val coroutineScope: CoroutineScope,
    private val dependencyContainer: CacheableRequestDependencyContainer<R>
) {
    internal constructor(
        coroutineScope: CoroutineScope,
        call: Call<R>,
        cacheConfiguration: CacheConfiguration,
        dateTimeProvider: DateTimeProvider,
        responseBodyConverter: Converter<ResponseBody, R>,
        cacheProvider: CacheProvider,
        requestStateObserver: StateObserver?,
    ) : this(
        coroutineScope,
        RealCacheableRequestDependencyContainer(call, cacheConfiguration, dateTimeProvider, responseBodyConverter, cacheProvider, requestStateObserver)
    )

    private val requestStateMachine: StateMachine<InternalRequestState<R>> by lazy { dependencyContainer.requestStateMachine }

    init {
        coroutineScope.launch {
            requestStateMachine.run()
        }
    }

    private val requestStateFlow: Flow<RequestState<R>> by lazy {
        requestStateMachine.stateFlow
            .mapNotNull<InternalRequestState<R>, RequestState<R>> {
                when (it) {
                    is RefreshFailedWithException -> it
                    is RefreshFailedWithResponse -> it
                    is Refreshing -> it
                    is Success -> it
                    is ErrorWithException -> it
                    is ErrorWithResponse -> it
                    is Fetching -> it
                    is Idle -> null
                }
            }
    }

    /**
     * Observe request state. Automatically initiates the request if it hasn't been done yet.
     */
    fun observeData(): Flow<RequestState<R>> {
        if (requestStateMachine.stateFlow.replayCache[0] is Idle) {
            coroutineScope.launch {
                (requestStateMachine.stateFlow.first() as? Idle)?.fetch(false)
            }
        }
        return requestStateFlow
    }

    // TODO: figure out when to clean a cache entry

    /**
     * Returns the data, or a failure.
     * @param forceRefresh True if the data must come from the remote. False also allows cached data.
     * Cached data could either be cached in memory or in the CacheProvider.
     */
    suspend fun getData(forceRefresh: Boolean = false): Result<R> {
        realFetch(forceRefresh) // suspends until the state is fetching/refreshing or data, depending on the current state and the forceRefresh property
        val resultState = requestStateFlow
            .filter { resultState: RequestState<R> ->
                when (resultState) {
                    is RequestState.Fetching -> false // no data available, wait for data to become available
                    is RequestState.Data.Refreshing -> !forceRefresh // data available, but forceRefresh is requested so wait for new data
                    else -> true // an end state
                }
            }
            .first()

        return when (resultState) {
            is RequestState.Data.RefreshFailed -> if (forceRefresh) Result.failure(resultState.exception) else Result.success(resultState.result)
            is RequestState.Data.Refreshing -> if (forceRefresh) Result.failure(IllegalStateException("Should not be in the Refreshing state here")) else Result.success(resultState.result)
            is RequestState.Data.Success -> Result.success(resultState.result)
            is RequestState.Error -> Result.failure(resultState.exception)
            is RequestState.Fetching -> Result.failure(IllegalStateException("Should not be in the Fetching state here"))
        }
    }

    fun forceRefresh() {
        coroutineScope.launch {
            realFetch(true)
        }
    }

    // TODO rename this method
    private suspend fun realFetch(forceRefresh: Boolean) {
        when (val currentState = requestStateMachine.stateFlow.first()) {
            is Idle -> currentState.fetch(forceRefresh)
            is InternalRequestState.Error -> currentState.retry()
            is Fetching -> {
                // Fetch in progress, if force refresh is requested, wait for it to become ready to know if it fetched from cache or not
                if (forceRefresh) {
                    val fetchResult = requestStateMachine.stateFlow.filter { it !is Fetching }.first()
                    if (fetchResult is Success && fetchResult.fromCache) {
                        fetchResult.refresh()
                    }
                }
            }
            is InternalRequestState.Data -> {
                if (!forceRefresh) return
                when (currentState) {
                    is Refreshing -> { /* Nothing needed to do, refresh already running. */ }
                    is InternalRequestState.Data.RefreshFailed -> currentState.retry()
                    is Success -> currentState.refresh()
                }
            }
        }
    }
}