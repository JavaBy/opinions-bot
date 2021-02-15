package by.jprof.telegram.opinions.insidejava

import by.jprof.telegram.opinions.insidejava.config.RSS_URL
import by.jprof.telegram.opinions.insidejava.config.dynamoModule
import by.jprof.telegram.opinions.insidejava.config.envModule
import by.jprof.telegram.opinions.insidejava.config.rssModule
import by.jprof.telegram.opinions.insidejava.config.telegramModule
import by.jprof.telegram.opinions.insidejava.dao.InsideJavaDAO
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendAudio
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendPhoto
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.boldMarkdownV2
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.FileId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.core.qualifier.named
import tw.ktrssreader.kotlin.parser.ITunesParser
import java.net.URI
import java.net.URL

class Handler : RequestHandler<ScheduledEvent, Unit>, KoinComponent {
    companion object {
        val logger = LogManager.getLogger(Handler::class.java)!!
    }

    init {
        startKoin {
            modules(envModule, rssModule, dynamoModule, telegramModule)
        }
    }

    private val rssUrl: String by inject(named(RSS_URL))
    private val parser: ITunesParser by inject()
    private val dao: InsideJavaDAO by inject()
    private val bot: RequestsExecutor by inject()

    override fun handleRequest(event: ScheduledEvent, context: Context?) = runBlocking {
        logger.debug("Incoming event: {}", event)

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
                val linkedTitle = if (item.link != null) {
                    "[${title.boldMarkdownV2()}](${item.link})"
                } else {
                    title
                }
                val text = linkedTitle
                // + if (item.description != null) {
                //     "\n\n```\n${item.description}\n\n```"
                // } else {
                //     ""
                // }

                if (image != null) {
                    logger.info("Sending photo {} with caption {} to {}", image, text, state.chatId)

                    bot.sendPhoto(
                            chatId = ChatId(state.chatId),
                            fileId = FileId(image),
                            caption = text,
                            parseMode = MarkdownV2ParseMode,
                    )
                } else {
                    logger.info("Sending text {} to {}", text, state.chatId)

                    bot.sendMessage(
                            chatId = ChatId(state.chatId),
                            text = text,
                            parseMode = MarkdownV2ParseMode,
                    )
                }
            }

            val newState = state.copy(
                    guids = state.guids + newItems.mapNotNull { it.guid?.value }
            )

            dao.save(newState)
        }
    }
}

fun main() {
    val handler = Handler()

    handler.handleRequest(ScheduledEvent(), null)
}
