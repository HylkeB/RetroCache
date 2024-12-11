package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.susstatemachine.State
import io.github.hylkeb.susstatemachine.Transition
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

internal sealed class InternalRequestState<T> : State<InternalRequestState<T>>() {
    sealed class Error<T> : InternalRequestState<T>() {
        abstract suspend fun retry()
    }

    sealed class Data<T> : InternalRequestState<T>() {
        sealed class RefreshFailed<T> : Data<T>() {
            abstract suspend fun retry()
        }
    }
}

/**
 * Bridge method to make getAge method accessible from the internal Data class.
 */
internal fun <R> InternalRequestState.Data<R>.getAge(time: Long): Duration {
    return when (this) {
        is RefreshFailedWithException -> getAge(time)
        is RefreshFailedWithResponse -> getAge(time)
        is Refreshing -> getAge(time)
        is Success -> getAge(time)
    }
}

/**
 * Helper method to automatically transition to fetching once the data expired
 */
internal suspend fun <T> InternalRequestState.Data<T>.withParentTransition(
    dependencyContainer: CacheableRequestDependencyContainer<T>,
    childEnter: suspend CoroutineScope.() -> Transition<InternalRequestState<T>>
): Transition<InternalRequestState<T>> = coroutineScope {
    val age = getAge(dependencyContainer.dateTimeProvider.currentTimeMillis())
    val expiresIn = dependencyContainer.cacheConfiguration.expirationDuration - age
    val expirationTimer = launch { delay(expiresIn.inWholeMilliseconds) }

    val childTransition = async { childEnter() }

    return@coroutineScope select {
        childTransition.onAwait {
            expirationTimer.cancel()
            it
        }
        expirationTimer.onJoin {
            childTransition.cancel()
            Transition(Fetching(true, dependencyContainer), "Data expired (${dependencyContainer.cacheConfiguration.expirationDuration})")
        }
    }
}
