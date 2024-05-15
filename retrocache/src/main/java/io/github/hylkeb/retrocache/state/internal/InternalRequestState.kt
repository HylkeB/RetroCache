package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.susstatemachine.State
import io.github.hylkeb.susstatemachine.Transition
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select


internal sealed interface InternalRequestState<T> : State<InternalRequestState<T>> {
    interface Idle<T> : InternalRequestState<T> {
        suspend fun fetch(forceRefresh: Boolean)
    }
    interface Fetching<T> : InternalRequestState<T>, RequestState.Fetching<T>

    sealed interface Error<T> : InternalRequestState<T> {
        suspend fun retry()

        interface WithResponse<T> : Error<T>, RequestState.Error.WithResponse<T>
        interface WithException<T> : Error<T>, RequestState.Error.WithException<T>
    }

    sealed interface Data<T> : InternalRequestState<T> {
        interface Success<T> : Data<T>, RequestState.Data.Success<T> {
            suspend fun refresh()
        }
        interface Refreshing<T> : Data<T>, RequestState.Data.Refreshing<T>

        sealed interface RefreshFailed<T> : Data<T> {
            suspend fun retry()

            interface WithResponse<T> : RefreshFailed<T>, RequestState.Data.RefreshFailed.WithResponse<T>
            interface WithException<T> : RefreshFailed<T>, RequestState.Data.RefreshFailed.WithException<T>
        }
    }
}

/**
 * Bridge method to make getAge method accessible from the internal Data class.
 */
internal fun <R> InternalRequestState.Data<R>.getAge(time: Long): Duration {
    return when (this) {
        is InternalRequestState.Data.RefreshFailed.WithException -> getAge(time)
        is InternalRequestState.Data.RefreshFailed.WithResponse -> getAge(time)
        is InternalRequestState.Data.Refreshing -> getAge(time)
        is InternalRequestState.Data.Success -> getAge(time)
    }
}

/**
 * Helper method to automatically transition to fetching once the data expired
 */
internal suspend fun <T> InternalRequestState.Data<T>.withParentTransition(
    cacheableRequest: CacheableRequest<T>,
    childEnter: suspend CoroutineScope.() -> Transition<InternalRequestState<T>>
): Transition<InternalRequestState<T>> = coroutineScope {
    val age = getAge(cacheableRequest.dateTimeProvider.currentTimeMillis())
    val expiresIn = cacheableRequest.cacheConfiguration.expirationDuration - age
    val expirationTimer = launch { delay(expiresIn.inWholeMilliseconds) }

    val childTransition = async { childEnter() }

    return@coroutineScope select {
        childTransition.onAwait {
            expirationTimer.cancel()
            it
        }
        expirationTimer.onJoin {
            childTransition.cancel()
            Transition(FetchingImpl(true, cacheableRequest), "Data expired (${cacheableRequest.cacheConfiguration.expirationDuration})")
        }
    }
}