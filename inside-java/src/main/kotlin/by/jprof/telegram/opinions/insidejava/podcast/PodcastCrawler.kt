package by.jprof.telegram.opinions.insidejava.podcast

import by.jprof.telegram.opinions.news.entity.InsideJavaPodcastAttrs
import by.jprof.telegram.opinions.news.produce.Producer
import by.jprof.telegram.opinions.news.queue.Event
import by.jprof.telegram.opinions.news.queue.NewsQueue
import by.jprof.telegram.opinions.news.queue.QueueItem
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.boldMarkdownV2
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.escapeMarkdownV2Common
import org.apache.logging.log4j.LogManager
import tw.ktrssreader.kotlin.parser.ITunesParser
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class PodcastCrawler(
    private val rssUrl: String,
    private val parser: ITunesParser,
    private val queue: NewsQueue
) : Producer {
    companion object {
        private val logger = LogManager.getLogger(Producer::class.java)!!

        // example: Mon, 12 Apr 2021 13:16:16 +0000
        private val pubDatePattern = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss xx")
    }

    override suspend fun produce() {
        val rss = parser.parse(URL(rssUrl).readText())
        val podcasts: List<QueueItem<InsideJavaPodcastAttrs>> =
            queue.findAll(Event.INSIDE_JAVA_PODCAST)
        val guids = podcasts.map { it.payload.guid }
        rss.items?.filterNot {
            guids.contains(it.guid?.value ?: "")
        }?.forEach { item ->
            val image = item.image
            val title = item.title ?: "New Inside Java episode!"
            val summary = item.summary
            val linkedTitle = if (item.link != null) {
                "[${title.boldMarkdownV2()}](${item.link})"
            } else {
                title
            }
            val text = linkedTitle +
                    if (summary != null) {
                        "\n\n```\n${summary.escapeMarkdownV2Common()}\n\n```"
                    } else {
                        ""
                    }

            val guid = item.guid?.value ?: run {
                logger.warn("Item {} doesn't have a GUID!. Infer GUID from text {}", item, text)
                Base64.getEncoder().encode(text.encodeToByteArray())
                    .toString(StandardCharsets.UTF_8).take(32)
            }

            val queueItem = QueueItem(
                event = Event.INSIDE_JAVA_PODCAST,
                payload = InsideJavaPodcastAttrs(
                    caption = text,
                    guid = guid,
                    fileId = image
                ),
                createdAt = item.pubDate?.let {
                    ZonedDateTime.parse(
                        it, pubDatePattern
                    ).toInstant()
                }
            )
            queue.push(queueItem)

            logger.info("Queued item {}", queueItem)
        }
    }
}
