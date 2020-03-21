package com.mmathieu.yelpsearch.common

sealed class Lce<T> {
    class Loading<T> : Lce<T>()
    data class Content<T>(val content: T) : Lce<T>()
    data class Error<T>(val message: String) : Lce<T>()
}