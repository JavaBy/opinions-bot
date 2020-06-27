package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.YoutubeDAO
import by.jprof.telegram.opinions.model.Update
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.justTextSources
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.CallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import org.apache.logging.log4j.LogManager

class YoutubeLinksProcessor(val bot: RequestsExecutor, val youtubeDAO: YoutubeDAO) : UpdateProcessor {
    companion object {
        val logger = LogManager.getLogger(JEPLinksProcessor::class.java)!!
        val siteRegex = """
            http(?:s?):\/\/(?:www\.)?youtu(?:be\.com\/watch\?v=|\.be\/)([\w\-\_]*)(&(amp;)?‌​[\w\?‌​=]*)?
        """.trimIndent().toRegex()
    }

    override suspend fun process(update: Update) {
        when (update.newUpdate) {
            is ContentMessage<*> -> processMessage(update.newUpdate)
            is CallbackQuery -> processCallback(update.newUpdate)
        }
    }

    private fun processMessage(message: ContentMessage<*>) {
        logger.debug("Processing the message")
        val youtubeLinks = (message.content as? TextContent)?.let {
            it.entities.justTextSources().filter { textSource ->
                textSource is URLTextSource && siteRegex.matches(textSource.source)
            }
        }

        youtubeLinks?.forEach { _ ->
            logger.debug("YouTube Link found in message, processing...")
        }
    }

    private fun processCallback(callback: CallbackQuery) {
        TODO("Not yet implemented")
    }

}