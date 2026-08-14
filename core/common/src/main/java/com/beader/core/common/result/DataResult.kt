package com.beader.core.common.result

/**
 * Generic wrapper for an operation outcome, used at every architectural
 * boundary (repository -> domain -> presentation) instead of throwing
 * exceptions across layers. Presentation layers exhaustively `when` over
 * this to render success/error/loading UI state.
 */
sealed interface DataResult<out T> {
    data class Success<T>(
        val data: T,
    ) : DataResult<T>

    data class Error(
        val throwable: Throwable,
        val message: String? = throwable.message,
    ) : DataResult<Nothing>

    data object Loading : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> this
        is DataResult.Loading -> this
    }

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (Throwable) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(throwable)
    return this
}
