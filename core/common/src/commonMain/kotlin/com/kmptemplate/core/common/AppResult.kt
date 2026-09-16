package com.kmptemplate.core.common

/**
 * Typed success/failure. Named [AppResult] rather than `Result` to avoid
 * colliding with kotlin.Result, which is easy to import by accident and
 * behaves differently (it carries a Throwable and is not exhaustive over
 * error types).
 */
sealed interface AppResult<out D, out E : AppError> {
    data class Success<out D>(val data: D) : AppResult<D, Nothing>
    data class Failure<out E : AppError>(val error: E) : AppResult<Nothing, E>
}

/** An operation that can fail but returns nothing on success. */
typealias EmptyResult<E> = AppResult<Unit, E>

/** Marker for every error type in the app, so errors stay exhaustive and mappable. */
interface AppError

inline fun <T, E : AppError, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> this
    }

inline fun <T, E : AppError, F : AppError> AppResult<T, E>.mapError(
    transform: (E) -> F,
): AppResult<T, F> = when (this) {
    is AppResult.Success -> this
    is AppResult.Failure -> AppResult.Failure(transform(error))
}

inline fun <T, E : AppError> AppResult<T, E>.onSuccess(action: (T) -> Unit): AppResult<T, E> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T, E : AppError> AppResult<T, E>.onFailure(action: (E) -> Unit): AppResult<T, E> {
    if (this is AppResult.Failure) action(error)
    return this
}

fun <T, E : AppError> AppResult<T, E>.getOrNull(): T? =
    (this as? AppResult.Success)?.data

fun <T, E : AppError> AppResult<T, E>.getOrDefault(default: T): T =
    (this as? AppResult.Success)?.data ?: default

fun <T, E : AppError> AppResult<T, E>.errorOrNull(): E? =
    (this as? AppResult.Failure)?.error

val AppResult<*, *>.isSuccess: Boolean get() = this is AppResult.Success

/**
 * Failures reaching or reading data, whether over the network or from local
 * storage. Extend this rather than throwing -- a ViewModel switches
 * exhaustively over these, and the UI layer is the only place that turns one
 * into a localized string (see CLAUDE.md's "state classes carry no UI types" rule).
 */
sealed interface DataError : AppError {
    enum class Remote : DataError {
        NO_INTERNET,
        REQUEST_TIMEOUT,
        SERVER_ERROR,
        SERIALIZATION,
        UNAUTHORIZED,
        NOT_FOUND,
        UNKNOWN,
    }

    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN,
    }
}
