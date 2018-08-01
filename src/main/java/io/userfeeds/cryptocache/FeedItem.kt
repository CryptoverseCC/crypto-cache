package io.userfeeds.cryptocache

import io.userfeeds.cryptocache.opensea.ContextInfoApiModel
import io.userfeeds.cryptocache.opensea.ItemIdExtractor
import io.userfeeds.cryptocache.opensea.OpenSeaData
import io.userfeeds.cryptocache.opensea.OpenSeaDataAddingVisitor

typealias FeedItem = MutableMap<String, Any>

val FeedItem.replies
    @Suppress("UNCHECKED_CAST")
    get() = this["replies"] as List<FeedItem>

val FeedItem.likes
    @Suppress("UNCHECKED_CAST")
    get() = this["likes"] as List<FeedItem>

class FeedItemIdExtractor : ItemIdExtractor<FeedItem> {
    override fun extractContextsFromItem(item: FeedItem): List<String> {
        return (listOf(item.context)
                + item.replies.flatMap { it.likes.map(FeedItem::context) + it.context }
                + item.likes.map(FeedItem::context))
                .filterNotNull()
    }
}

class OpenSeaToFeedAddingVisitor(private val openSeaDataByContext: Map<String, OpenSeaData>) : OpenSeaDataAddingVisitor<FeedItem> {
    override fun visitItem(item: FeedItem) {
        openSeaDataByContext[item.context]?.let { it ->
            item["context_info"] = ContextInfoApiModel(it)
            addReplies(item, openSeaDataByContext)
            addLikes(item, openSeaDataByContext)
        }
    }

    private fun addReplies(item: FeedItem, openSeaDataByContext: Map<String, OpenSeaData>) {
        item.replies.forEach { replay ->
            openSeaDataByContext[replay.context]?.let { data ->
                replay["context_info"] = ContextInfoApiModel(data)
                addLikes(replay, openSeaDataByContext)
            }
        }
    }

    private fun addLikes(item: FeedItem, openSeaDataByContext: Map<String, OpenSeaData>) {
        item.likes.forEach { like ->
            openSeaDataByContext[like.context]?.let { data ->
                like["context_info"] = ContextInfoApiModel(data)
            }
        }
    }
}