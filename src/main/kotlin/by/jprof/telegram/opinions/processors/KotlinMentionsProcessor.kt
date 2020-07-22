package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendSticker
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendTextMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.toInputFile
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.media.StickerContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import java.time.Duration
import java.time.Instant

class KotlinMentionsProcessor(
        private val bot: RequestsExecutor,
        private val kotlinMentionsDAO: KotlinMentionsDAO
) : UpdateProcessor {
    companion object {
        const val zeroDaysWithoutKotlinStickerFileId = "CAACAgIAAxkBAAIBsF8V0dPb6EesBKSujFFOx_URfhSdAAJAAQACqSImBOs5DmSNtKlmGgQ"
        val kotlinRegex = "(котлин|kotlin)".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        val requiredDelayBetweenReplies: Duration? = Duration.ofHours(1)

        fun composeWithoutMentionDurationMessage(duration: Duration): String {
            return "We have been existing %02dd:%02dh:%02dm:%02ds without mentioning"
                    .format(duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
        }
    }

    override suspend fun process(update: Update) {
        val message = (update as? MessageUpdate) ?: return
        val contentMessage = (message.data as? ContentMessage<*>) ?: return
        val textContent = (contentMessage.content as? TextContent) ?: return

        if (!kotlinRegex.containsMatchIn(textContent.text)) return

        val id = contentMessage.chat.id.chatId.toString()

        val lastTime = kotlinMentionsDAO.getKotlinLastMentionAt(id)
        if (lastTime == null) {
            sendSticker(contentMessage.chat.id, contentMessage.messageId)
            kotlinMentionsDAO.updateKotlinLastMentionAt(id, Instant.now())
            return
        }

        val duration = Duration.between(lastTime, Instant.now())
        if (duration < requiredDelayBetweenReplies) {
            return
        }

        val stickerMsg = sendSticker(contentMessage.chat.id, contentMessage.messageId)
        bot.sendTextMessage(contentMessage.chat.id,
                composeWithoutMentionDurationMessage(duration),
                replyToMessageId = stickerMsg.messageId)

        kotlinMentionsDAO.updateKotlinLastMentionAt(id, Instant.now())
    }

    private suspend fun sendSticker(
            chatId: ChatId,
            messageId: MessageIdentifier
    ): ContentMessage<StickerContent> = bot.sendSticker(
            chatId,
            zeroDaysWithoutKotlinStickerFileId.toInputFile(),
            replyToMessageId = messageId
    )
}
