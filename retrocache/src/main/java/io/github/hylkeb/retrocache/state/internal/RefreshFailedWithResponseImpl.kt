package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import retrofit2.HttpException

internal class RefreshFailedWithResponseImpl<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    override val exception: HttpException,
    private val cacheableRequest: CacheableRequest<T>,
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Data.RefreshFailed.WithResponse<T> {

    private val retryRequested: CompletableJob = Job()

    override suspend fun enter(): Transition<InternalRequestState<T>> = withParentTransition(cacheableRequest) {
        retryRequested.join()
        return@withParentTransition Transition(RefreshingImpl(result, dateTimeMillis, fromCache, cacheableRequest))
    }

    override suspend fun retry() {
        retryRequested.complete()
        awaitTransition()
    }
}