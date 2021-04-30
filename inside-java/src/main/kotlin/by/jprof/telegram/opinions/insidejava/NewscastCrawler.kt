package by.jprof.telegram.opinions.insidejava

import by.jprof.telegram.opinions.news.entity.InsideJavaNewscastAttrs
import by.jprof.telegram.opinions.news.produce.Producer
import by.jprof.telegram.opinions.news.queue.Event
import by.jprof.telegram.opinions.news.queue.NewsQueue
import by.jprof.telegram.opinions.news.queue.QueueItem
import com.google.api.services.youtube.YouTube
import java.time.Instant

class NewscastCrawler(
    val youtube: YouTube,
    val queue: NewsQueue
) : Producer {
    companion object {
        // inside java news cast playlist id
        // https://www.youtube.com/watch?v=T7-4I_pUlpw&list=PLX8CzqL3ArzX8ZzPNjBgji7rznFFiOr58
        private const val ID = "PLX8CzqL3ArzX8ZzPNjBgji7rznFFiOr58"
    }

    override suspend fun produce() {
        val news = queue.findAll<InsideJavaNewscastAttrs>(Event.INSIDE_JAVA_NEWSCAST)
        val videoIds = news.map { it.payload.videoId }
        val playlist = youtube.playlistItems().list("contentDetails");
        playlist.playlistId = ID
        playlist.execute().items.filterNot {
            videoIds.contains(it.contentDetails.videoId)
        }.forEach {
            queue.push(
                QueueItem(
                    event = Event.INSIDE_JAVA_NEWSCAST,
                    payload = InsideJavaNewscastAttrs(
                        it.contentDetails.videoId
                    ),
                    Instant.parse(it.contentDetails.videoPublishedAt.toString())
                )
            )
        }
    }
}