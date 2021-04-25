package by.jprof.telegram.opinions.publication

import by.jprof.telegram.opinions.news.entity.InsideJavaPodcastAttrs
import by.jprof.telegram.opinions.news.queue.Kind
import by.jprof.telegram.opinions.news.queue.NewsQueue
import by.jprof.telegram.opinions.news.queue.QueueItem
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendPhoto
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.FileId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import org.apache.logging.log4j.LogManager

class TelegramPublisher(
    val queue: NewsQueue,
    val chats: ChatDao,
    val bot: RequestsExecutor
) : Publisher {
    companion object {
        private val logger = LogManager.getLogger(TelegramPublisher::class.java)!!
    }

    override suspend fun publish() {
        val eligibleChats = chats.findAll(Kind.INSIDE_JAVA_PODCAST)
        val news = queue.news<InsideJavaPodcastAttrs>(Kind.INSIDE_JAVA_PODCAST)
        news.sortedByDescending { it.createdAt ?: it.queuedAt }.forEach { anews ->
            eligibleChats.forEach { chat ->
                logger.info("Publisher {} to {}", anews, chat)
                send(chat, anews)
            }
            queue.markProcessed(anews)
            return@forEach
        }
    }

    private suspend fun send(chat: ChatAttrs, item: QueueItem<InsideJavaPodcastAttrs>) {
        if (item.payload.fileId != null) {
            logger.info(
                "Sending photo {} with caption {} to {}",
                item.payload.fileId, item.payload.caption
            )

            bot.sendPhoto(
                chatId = ChatId(chat.chatId.toLong()),
                fileId = FileId(item.payload.fileId!!),
                caption = item.payload.caption,
                parseMode = MarkdownV2ParseMode,
            )
        } else {
            logger.info(
                "Sending text {} to {}",
                item.payload.caption, chat.chatId
            )

            bot.sendMessage(
                chatId = ChatId(chat.chatId.toLong()),
                text = item.payload.caption,
                parseMode = MarkdownV2ParseMode,
            )
        }
    }
}