package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.YoutubeDAO
import by.jprof.telegram.opinions.model.Update
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.justTextSources
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.CallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import org.slf4j.LoggerFactory

class YoutubeLinksProcessor(val bot: RequestsExecutor, val youtubeDAO: YoutubeDAO) : UpdateProcessor {
    companion object {
        val logger = LoggerFactory.getLogger(YoutubeLinksProcessor::class.java)!!
        val siteRegex = """
            http(?:s?):\/\/(?:www\.)?youtu(?:be\.com\/watch\?v=|\.be\/)([\w\-\_]*)(&(amp;)?‌​[\w\?‌​=]*)?
        """.trimIndent().toRegex()
    }

    override suspend fun process(update: Update) {
        logger.debug("Processing update {}", update.newUpdate)
        when (update.newUpdate) {
            is MessageUpdate -> processMessage(update.newUpdate)
            is CallbackQuery -> processCallback(update.newUpdate)
        }
    }

    private suspend fun processMessage(update: MessageUpdate) {
        logger.debug("Processing the message {}", update)
        val youtubeLinks = (update.data as? ContentMessage<*>)?.let { msg ->
            (msg.content as? TextContent).let {
                it?.entities?.justTextSources()?.filter { textSource ->
                    textSource is URLTextSource && siteRegex.matches(textSource.source)
                }
            }
        }

        youtubeLinks?.forEach { link ->
            logger.debug("YouTube Link found in message, processing... ${link.source}")
        }
    }

    private suspend fun processCallback(callback: CallbackQuery) {
        TODO("Not yet implemented")
    }

}