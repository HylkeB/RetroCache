package io.github.hylkeb.retrocache.state

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import retrofit2.HttpException

// todo documentation
// todo proper compose annotations?
sealed interface RequestState<T> {
    companion object {
        fun <T> initialState(): RequestState<T> = object : Fetching<T> { }
    }

    interface Fetching<T> : RequestState<T>

    sealed interface Error<T> : RequestState<T> {
        val exception: Throwable

        interface WithResponse<T> : Error<T> {
            override val exception: HttpException
        }

        interface WithException<T> : Error<T>
    }

    sealed interface Data<T> : RequestState<T> {
        val result: T
        val dateTimeMillis: Long
        val fromCache: Boolean
        fun getAge(currentTime: Long): Duration = (currentTime - dateTimeMillis).toDuration(DurationUnit.MILLISECONDS)

        interface Success<T> : Data<T>
        interface Refreshing<T> : Data<T>

        sealed interface RefreshFailed<T> : Data<T> {
            val exception: Throwable

            interface WithResponse<T> : RefreshFailed<T> {
                override val exception: HttpException
            }

            interface WithException<T> : RefreshFailed<T>
        }
    }
}
