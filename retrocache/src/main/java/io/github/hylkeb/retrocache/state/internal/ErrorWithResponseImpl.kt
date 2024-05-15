package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import retrofit2.HttpException

internal class ErrorWithResponseImpl<T>(
    override val exception: HttpException,
    private val cacheableRequest: CacheableRequest<T>,
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Error.WithResponse<T> {

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