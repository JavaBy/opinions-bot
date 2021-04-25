package by.jprof.telegram.opinions.insidejava.podcast

import by.jprof.telegram.opinions.insidejava.dao.InsideJavaDAO
import by.jprof.telegram.opinions.news.entity.InsideJavaAttrs
import by.jprof.telegram.opinions.news.produce.Producer
import by.jprof.telegram.opinions.news.queue.Kind
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
    private val dao: InsideJavaDAO,
    private val queue: NewsQueue
) : Producer {
    companion object {
        private val logger = LogManager.getLogger(Producer::class.java)!!

        // example: Mon, 12 Apr 2021 13:16:16 +0000
        private val pubDatePattern = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss xx")
    }

    override suspend fun produce() {
        val rss = parser.parse(URL(rssUrl).readText())
        val states = dao.list()

        states.forEach { state ->
            logger.info("Processing state for {}", state.chatId)

            val newItems = (rss.items ?: emptyList()).filter { item ->
                val guid = item.guid

                if (guid == null) {
                    logger.warn("Item {} doesn't have a GUID!", item)
                }

                (guid != null && !state.guids.contains(guid.value)).also {
                    if (!it) {
                        logger.debug("Filtered {}", item.guid?.value)
                    }
                }
            }

            logger.info("New items for {}: {}", state.chatId, newItems.map { it.guid?.value })

            newItems.forEach { item ->
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
                        .toString(StandardCharsets.UTF_8).take(16)
                }

                val queueItem = QueueItem(
                    kind = Kind.INSIDE_JAVA_PODCAST,
                    payload = InsideJavaAttrs(
                        chatId = state.chatId.toString(),
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

            val news = queue.news(Kind.INSIDE_JAVA_PODCAST)
            queue.markProcessed(news[0])

            val newState = state.copy(
                guids = state.guids + newItems.mapNotNull { it.guid?.value }
            )

            dao.save(newState)
        }
    }
}
