package com.jeffmcknight.magneticmontecarlo.ktExtension

/**
 * Returns index of the first element matching the given [predicate], or null if the collection does
 * not contain such element.
*/
fun <T> Iterable<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    val indexOfFirst = indexOfFirst { predicate(it) }
    return if (indexOfFirst > -1) indexOfFirst else null
}