package com.ecozym.wastemanagement.utils

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun failure(exception: Throwable): Result<Nothing> = Failure(exception)
        fun loading(): Result<Nothing> = Loading
    }

    // Add fold method for handling success/failure cases
    inline fun <R> fold(
        onSuccess: (value: T) -> R,
        onFailure: (exception: Throwable) -> R,
        onLoading: () -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Failure -> onFailure(exception)
            is Loading -> onLoading()
        }
    }

    // Additional utility methods
    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
        is Loading -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> exception
        is Loading -> null
    }
}