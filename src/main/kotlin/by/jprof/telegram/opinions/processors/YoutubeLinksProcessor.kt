package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.VotesDAO
import by.jprof.telegram.opinions.dao.YoutubeDAO
import by.jprof.telegram.opinions.entity.Votes
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.TextSource
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.justTextSources
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.boldMarkdownV2
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.escapeMarkdownV2Common
import com.google.api.services.youtube.YouTube
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager


class YoutubeLinksProcessor(
        private val bot: RequestsExecutor,
        private val votesDAO: VotesDAO,
        private val youtubeDAO: YoutubeDAO,
        private val youTube: YouTube
) : UpdateProcessor {

    companion object {
        private val logger = LogManager.getLogger(YoutubeLinksProcessor::class.java)!!
        private const val VIDEO_ID_GROUP_INDEX = 1
        private const val ACCEPTED_DISPLAY_LEN = 500
        private val siteRegex = """
            http(?:s?):\/\/(?:www\.)?youtu(?:be\.com\/watch\?v=|\.be\/)([\w\-\_]*)(&(amp;)?‌​[\w\?‌​=]*)?
        """.trimIndent().toRegex()
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
        val youtubeLinks = extractYoutubeLinks(update)
        youtubeLinks?.let {
            logger.debug("Youtube links extracted: ${youtubeLinks.size}")
        }
        youtubeLinks?.forEach { msgText ->
            sendVoteForVideoMessage(msgText.source, update)
        }
    }

    private fun extractYoutubeLinks(update: MessageUpdate): List<TextSource>? {
        return (update.data as ContentMessage<*>).let { msg ->
            (msg.content as? TextContent).let {
                it?.entities?.justTextSources()?.filter { textSource ->
                    textSource is URLTextSource && siteRegex.matches(textSource.source)
                }
            }
        }
    }

    private fun sendVoteForVideoMessage(youtubeLink: String, update: MessageUpdate) {
        val regexGroups = siteRegex.matchEntire(youtubeLink)?.groupValues
        regexGroups?.get(VIDEO_ID_GROUP_INDEX)?.let { videoId ->
            logger.debug("Youtube video id is: $videoId")
            val response = youTube.videos().list("snippet,statistics").setId(videoId).execute()

            val videoDetails = response.items.first()
            val snippet = videoDetails.snippet
            val channelId = snippet.channelId
            val title = snippet.title
            val likes = videoDetails.statistics.likeCount
            val dislikes = videoDetails.statistics.dislikeCount
            val rawDescription = if (snippet.description.length > ACCEPTED_DISPLAY_LEN) {
                snippet.description.substring(IntRange(0, ACCEPTED_DISPLAY_LEN)) + "..."
            } else {
                snippet.description
            }

            val description = rawDescription.escapeMarkdownV2Common()



            runBlocking {
                logger.debug("checking if $channelId is in a white list")
                if (youtubeDAO.isInWhiteList(channelId)) {
                    logger.debug("$channelId is in a white list")
                    val videoText = "${"Vote for video: $youtubeLink".boldMarkdownV2()} \n$description " +
                            "\nLikes: $likes Dislikes: $dislikes".boldMarkdownV2() //trim indent have strange layout
                    val votes = getVotesByYoutubeId(videoId)
                    bot.sendMessage(
                            chatId = update.data.chat.id,
                            text = videoText,
                            parseMode = MarkdownV2ParseMode,
                            replyToMessageId = update.data.messageId,
                            replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(votes, videoId))
                    )
                } else {
                    logger.debug("$channelId is not in a white list")
                    bot.sendMessage(
                            chatId = update.data.chat.id,
                            text = "Channel of ${title.escapeMarkdownV2Common()} video is not in the group whitelist",
                            parseMode = MarkdownV2ParseMode,
                            replyToMessageId = update.data.messageId
                    )
                }
            }
        }

    }

    private suspend fun getVotesByYoutubeId(videoId: String): Votes {
        val id = "YOUTUBE-$videoId"
        return votesDAO.get(id) ?: Votes(id)
    }

    private suspend fun processCallback(callbackUpdate: CallbackQueryUpdate) {
        val callbackQuery = callbackUpdate.data
        logger.debug("process callback: $callbackQuery")
        if (callbackQuery is MessageDataCallbackQuery) {

            val (youtubeVideoId, vote) = callbackQuery.data.split(":")
            val votes = getVotesByYoutubeId(youtubeVideoId)
            val fromUserId = callbackQuery.user.id.chatId.toString()
            val updatedVotes = votes.copy(votes = votes.votes + (fromUserId to vote))

            votesDAO.save(updatedVotes)
            bot.answerCallbackQuery(callbackQuery = callbackQuery)

            bot.editMessageReplyMarkup(
                    message = callbackQuery.message,
                    replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(votes, youtubeVideoId))
            )

        }
    }


}
