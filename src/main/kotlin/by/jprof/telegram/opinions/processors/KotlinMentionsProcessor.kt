package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import by.jprof.telegram.opinions.entity.KotlinMention
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendSticker
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendTextMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.toInputFile
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.media.StickerContent
import com.github.insanusmokrassar.TelegramBotAPI.types.toChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class KotlinMentionsProcessor(
        private val bot: RequestsExecutor,
        private val kotlinMentionsDAO: KotlinMentionsDAO,
        private val stickerFileId: String = zeroDaysWithoutKotlinStickerFileId,
        private val hasPassedEnoughTimeSincePreviousMention: (Duration) -> Boolean = ::hasPassedEnoughTimeSincePreviousMention,
        private val containsMatchIn: (String) -> Boolean = kotlinRegex::containsMatchIn,
        private val composeStickerMessage: (Duration) -> String = ::composeStickerMessage
) : UpdateProcessor {
    companion object {
        private const val zeroDaysWithoutKotlinStickerFileId = "CAACAgIAAxkBAAIBsF8V0dPb6EesBKSujFFOx_URfhSdAAJAAQACqSImBOs5DmSNtKlmGgQ"
        private val kotlinRegex = "([kк]+.{0,3}[оo0aа]+.{0,3}[тt]+.{0,3}[лl]+.{0,3}[ие1ie]+.{0,3}[нnH]+)".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        private val minRequiredDelayBetweenReplies: Duration = Duration.ofMinutes(30)!!
        private val maxRequiredDelayBetweenReplies: Duration = Duration.ofHours(1)!!

        private fun hasPassedEnoughTimeSincePreviousMention(duration: Duration): Boolean =
                duration.toMillis() > Random.nextLong(
                        minRequiredDelayBetweenReplies.toMillis(),
                        maxRequiredDelayBetweenReplies.toMillis())

        fun composeStickerMessage(duration: Duration): String =
                "Passed %02dd:%02dh:%02dm:%02ds without an incident".format(
                        duration.toDaysPart(), duration.toHoursPart(),
                        duration.toMinutesPart(), duration.toSecondsPart())
    }

    override suspend fun process(update: Update) {
        val message = (update as? MessageUpdate) ?: return
        val contentMessage = (message.data as? CommonMessageImpl<*>) ?: return
        val textContent = (contentMessage.content as? TextContent) ?: return
        if (!containsMatchIn(textContent.text)) return
        val chatId = contentMessage.chat.id.chatId
        val userId = contentMessage.user.id.chatId
        val mentions = kotlinMentionsDAO.find(chatId.toString())
                ?: return sendSticker(
                        KotlinMention(chatId, Instant.now(), mutableMapOf()),
                        contentMessage.messageId)
        val duration = computeDurationIfPassedEnoughTime(mentions.timestamp) ?: return
        val updatedMention = mentions.updateUserStats(userId)
        sendSticker(updatedMention, contentMessage.messageId) {
            bot.sendTextMessage(chatId.toChatId(),
                    composeStickerMessage(duration),
                    replyToMessageId = it.messageId)
        }
    }

    private fun computeDurationIfPassedEnoughTime(lastTime: Instant): Duration? {
        val duration = Duration.between(lastTime, Instant.now())
        if (hasPassedEnoughTimeSincePreviousMention(duration)) {
            return duration
        }
        return null
    }

    private suspend fun sendSticker(
            mentions: KotlinMention,
            messageId: MessageIdentifier,
            onSend: suspend (ContentMessage<StickerContent>) -> Unit = {}
    ) {
        val response = bot.sendSticker(
                ChatId(mentions.chatId),
                stickerFileId.toInputFile(),
                replyToMessageId = messageId
        )

        onSend(response)

        kotlinMentionsDAO.save(mentions)
    }
}
