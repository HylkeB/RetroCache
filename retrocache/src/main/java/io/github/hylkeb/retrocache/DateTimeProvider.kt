package io.github.hylkeb.retrocache

fun interface DateTimeProvider {
    fun currentTimeMillis(): Long

    companion object {
        fun fromSystem(): DateTimeProvider {
            return DateTimeProvider { System.currentTimeMillis() }
        }
    }
}
