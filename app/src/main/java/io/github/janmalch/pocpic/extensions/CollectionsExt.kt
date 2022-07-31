package io.github.janmalch.pocpic.extensions

fun <T> Collection<T>.randomUnlikeOrNull(other: T? = null): T? {
    var next: T? = randomOrNull() ?: return null
    if (this.size > 1) {
        while (next == other) {
            next = randomOrNull()
        }
    }
    return next
}
