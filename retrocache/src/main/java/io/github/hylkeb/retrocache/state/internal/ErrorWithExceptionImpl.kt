package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job

internal class ErrorWithExceptionImpl<T>(
    override val exception: Throwable,
    private val cacheableRequest: CacheableRequest<T>,
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Error.WithException<T> {

    private val retryRequested: CompletableJob = Job()

    override suspend fun enter(): Transition<InternalRequestState<T>> {
        retryRequested.join()
        return Transition(FetchingImpl(true, cacheableRequest))
    }

    override suspend fun retry() {
        retryRequested.complete()
        awaitTransition()
    }
}