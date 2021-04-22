package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.components.youtube.Voting
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.TextLinkTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.fullEntitiesList
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import org.apache.logging.log4j.LogManager

class YoutubeLinksProcessor(
    private val voting: Voting,
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(YoutubeLinksProcessor::class.java)!!

        // TODO: https://twitter.com/shipilev/status/934133677445042176 passes this regex!
        @Suppress("RegExpRedundantEscape")
        private val youTubeLinkRegex =
            "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/\\w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#\\&\\?]*).*".toRegex()

        internal val String.youTubeVideoId: String?
            get() {
                val (_, _, _, _, _, _, id) = youTubeLinkRegex.matchEntire(this)?.destructured ?: return null

                return id
            }
    }

    override suspend fun process(update: Update) {
        logger.debug("Processing update {}", update)

        when (update) {
            is MessageUpdate -> processMessage(update)
            is CallbackQueryUpdate -> processCallback(update)
        }
    }

    private fun processMessage(update: MessageUpdate) {
        logger.debug("Processing the message {}", update)

        val videoIds = extractYoutubeVideoIds(update)

        logger.debug("Found YouTube videos: $videoIds")

        videoIds?.forEach { link ->
            voting.sendVoteForVideoMessage(link, update.data.chat.id, update.data.messageId)
        }
    }

    private fun extractYoutubeVideoIds(update: MessageUpdate): List<String>? {
        return (update.data as ContentMessage<*>).let { msg ->
            (msg.content as? TextContent)?.let { textContent ->
                textContent
                    .fullEntitiesList()
                    .let { entities ->
                        entities
                            .mapNotNull {
                                when (it) {
                                    is TextLinkTextSource -> it.url
                                    is URLTextSource -> it.source
                                    else -> null
                                }
                            }
                            .mapNotNull {
                                it.youTubeVideoId
                            }
                    }
            }
        }
    }

    private suspend fun processCallback(callbackUpdate: CallbackQueryUpdate) {
        voting.processCallback(callbackUpdate.data)
    }
}
