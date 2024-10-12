package io.github.janmalch.pocpic.core

import kotlin.random.Random

/**
 * @author https://stackoverflow.com/a/36230607
 */
fun <T> Random.prepareNextWeighted(items: List<T>, getWeight: (T) -> Int): () -> T {
    require(items.isNotEmpty()) { "List must not be empty." }
    if (items.size == 1) {
        val first = items[0]
        return { first }
    }
    var totalWeight = 0
    val map = mutableMapOf<T, Int>()
    for (item in items) {
        val itemWeight = getWeight(item)
        map[item] = itemWeight
        totalWeight += itemWeight
    }
    require(totalWeight > 0) { "Total weight must be positive." }
    return fun(): T {
        val random = nextInt(totalWeight)
        var search = 0
        for ((item, itemWeight) in map) {
            search += itemWeight
            if (random < search) return item
        }
        // should be unreachable
        return map.keys.first()
    }
}
