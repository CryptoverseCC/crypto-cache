package io.userfeeds.cryptocache.cryptoverse

import io.userfeeds.cryptocache.FeedItem
import org.springframework.stereotype.Component

@Component
class FeedRepository {

    var cache = Cache(emptyList(), 0)
}

data class Cache(val allItems: List<FeedItem>, val version: Long)
