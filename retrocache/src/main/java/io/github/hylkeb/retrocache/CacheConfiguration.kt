package io.github.hylkeb.retrocache

import kotlin.time.Duration

data class CacheConfiguration(
    val staleDuration: Duration,
    val expirationDuration: Duration,
    val persistedCache: Boolean,
) {
    init {
        require(expirationDuration >= staleDuration) { "Expiration duration must be greater than or equal to the stale duration. ($expirationDuration < $staleDuration)" }
    }

    companion object {
        val inMemoryCache: CacheConfiguration = CacheConfiguration(Duration.INFINITE, Duration.INFINITE, false)
    }
}
