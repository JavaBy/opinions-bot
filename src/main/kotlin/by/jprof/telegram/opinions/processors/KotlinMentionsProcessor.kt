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
import kotlin.random.Random

class KotlinMentionsProcessor(
    private val bot: RequestsExecutor,
    private val kotlinMentionsDAO: KotlinMentionsDAO,
    private val stickerFileId: String = zeroDaysWithoutKotlinStickerFileId
) : UpdateProcessor {
    companion object {
        const val zeroDaysWithoutKotlinStickerFileId = "CAACAgIAAxkBAAIBsF8V0dPb6EesBKSujFFOx_URfhSdAAJAAQACqSImBOs5DmSNtKlmGgQ"
        private val kotlinRegex = "([kк]+.{0,5}[оo0aа]+.{0,5}[тt]+.{0,5}[лl]+.{0,5}[ие1ie]+.{0,5}[нnH]+)".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        private val minRequiredDelayBetweenReplies: Duration = Duration.ofMinutes(30)
        private val maxRequiredDelayBetweenReplies: Duration = Duration.ofHours(1)
    }

    override suspend fun process(update: Update): Unit = update.let {
        when (it) {
            is MessageUpdate -> it
            else -> return
        }
    }.let {
        val contentMessage = (it.data as? ContentMessage<*>) ?: return
        val text = (contentMessage.content as? TextContent)?.text ?: return

        Pair(contentMessage, text)
    }.also { (_, text) ->
        if (text.isNotKotlin()) return
    }.let { (contentMessage, _) ->
        val id = contentMessage.chat.id.chatId.toString()

        Pair(contentMessage, id)
    }.let { (contentMessage, id) ->
        when (val lastTime = kotlinMentionsDAO.getKotlinLastMentionAt(id)) {
            null -> {
                sendSticker(contentMessage.chat.id, contentMessage.messageId)
                kotlinMentionsDAO.updateKotlinLastMentionAt(id, Instant.now())
                return
            }
            else -> Triple(contentMessage, id, lastTime)
        }
    }.let { (contentMessage, id, lastTime) ->
        val duration = Duration.between(lastTime, Instant.now())
        when (hasPassedEnoughTimeSincePreviousMention(duration)) {
            false -> return
            true -> Triple(contentMessage, id, duration)
        }
    }.let { (contentMessage, id, duration) ->
        val stickerMsg = sendSticker(contentMessage.chat.id, contentMessage.messageId)
        bot.sendTextMessage(contentMessage.chat.id, duration.toComposeStickerMessage(), replyToMessageId = stickerMsg.messageId)

        kotlinMentionsDAO.updateKotlinLastMentionAt(id, Instant.now())
    }

    private suspend fun sendSticker(
        chatId: ChatId,
        messageId: MessageIdentifier
    ): ContentMessage<StickerContent> = bot.sendSticker(
        chatId,
        stickerFileId.toInputFile(),
        replyToMessageId = messageId
    )

    private fun String.isNotKotlin(): Boolean {
        return !kotlinRegex.containsMatchIn(this)
    }

    private fun hasPassedEnoughTimeSincePreviousMention(duration: Duration): Boolean =
        duration.toMillis() > Random.nextLong(
            minRequiredDelayBetweenReplies.toMillis(),
            maxRequiredDelayBetweenReplies.toMillis())

}

fun Duration.toComposeStickerMessage(): String =
    "Passed %02dd:%02dh:%02dm:%02ds without an incident".format(
        this.toDaysPart(), this.toHoursPart(),
        this.toMinutesPart(), this.toSecondsPart())


