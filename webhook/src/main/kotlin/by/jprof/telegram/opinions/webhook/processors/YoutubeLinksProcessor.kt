package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.webhook.dao.VotesDAO
import by.jprof.telegram.opinions.webhook.dao.YoutubeDAO
import by.jprof.telegram.opinions.webhook.entity.Votes
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.boldMarkdownV2
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.TextLinkTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.fullEntitiesList
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.escapeMarkdownV2Common
import com.google.api.services.youtube.YouTube
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class YoutubeLinksProcessor(
        private val bot: RequestsExecutor,
        private val votesDAO: VotesDAO,
        private val youtubeDAO: YoutubeDAO,
        private val youTube: YouTube
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(YoutubeLinksProcessor::class.java)!!
        private const val ACCEPTED_DISPLAY_LEN = 500

        // TODO: https://twitter.com/shipilev/status/934133677445042176 passes this regex!
        @Suppress("RegExpRedundantEscape")
        private val youTubeLinkRegex = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/\\w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#\\&\\?]*).*".toRegex()

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
            sendVoteForVideoMessage(link, update)
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

    private fun sendVoteForVideoMessage(videoId: String, update: MessageUpdate) {
        logger.debug("Youtube video id is: $videoId")

        val response = youTube.videos().list("snippet,statistics").setId(videoId).execute()

        logger.debug("YouTube response: {}", response)

        val videoDetails = response.items.first()
        val snippet = videoDetails.snippet
        val channelId = snippet.channelId
        val views = videoDetails.statistics.viewCount
        val likes = videoDetails.statistics.likeCount
        val dislikes = videoDetails.statistics.dislikeCount
        val rawDescription = if (snippet.description.length > ACCEPTED_DISPLAY_LEN) {
            snippet.description.substring(IntRange(0, ACCEPTED_DISPLAY_LEN)) + "…"
        } else {
            snippet.description
        }
        val description = rawDescription.escapeMarkdownV2Common()

        runBlocking {
            logger.debug("checking if $channelId is in a white list")

            if (youtubeDAO.isInWhiteList(channelId)) {
                logger.debug("$channelId is in a white list")
                val videoText = "Cast your vote for: ${snippet.title}".boldMarkdownV2() +
                        "\n\n```\n$description\n\n```" +
                        "Views: $views / Likes: $likes / Dislikes: $dislikes".boldMarkdownV2() //trim indent have strange layout
                var votes = getVotesByYoutubeId(videoId)
                var canRepost = false
                if (null == votes.lastRepostedAt) {
                    votes = votes.copy(lastRepostedAt = Instant.now())
                    votesDAO.save(votes)
                    canRepost = true
                }
                val timePassed = Duration.between(votes.lastRepostedAt, Instant.now())
                val coolDown = Random.nextLong(
                    Duration.ofMinutes(30).toMillis(), Duration.ofHours(1).toMillis()
                )
                canRepost = if (canRepost) canRepost else timePassed.toMillis() > coolDown
                if (canRepost) {
                    logger.debug("Sending text {} for video {}", videoText, votes.id)
                    bot.sendMessage(
                        chatId = update.data.chat.id,
                        text = videoText,
                        parseMode = MarkdownV2ParseMode,
                        replyToMessageId = update.data.messageId,
                        replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(votes, votes.id))
                    )
                }
            } else {
                logger.debug("$channelId is not in a white list")
            }
        }
    }

    private suspend fun getVotesByYoutubeId(videoId: String, prefix: Boolean = true): Votes {
        val id = if (prefix) "YOUTUBE-$videoId" else videoId
        return votesDAO.get(id) ?: Votes(id)
    }

    private suspend fun processCallback(callbackUpdate: CallbackQueryUpdate) {
        val callbackQuery = callbackUpdate.data

        logger.debug("process callback: $callbackQuery")

        if (callbackQuery is MessageDataCallbackQuery) {
            val data = callbackQuery.data
            if (data.startsWith("YOUTUBE")) {
                val (youtubeVideoId, vote) = data.split(":")
                val votes = getVotesByYoutubeId(youtubeVideoId, prefix = false)
                val fromUserId = callbackQuery.user.id.chatId.toString()
                val updatedVotes = votes.copy(votes = votes.votes + (fromUserId to vote))

                votesDAO.save(updatedVotes)
                bot.answerCallbackQuery(callbackQuery = callbackQuery)
                bot.editMessageReplyMarkup(
                        message = callbackQuery.message,
                        replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(updatedVotes, youtubeVideoId))
                )
            } else {
                logger.debug("Unknown callback query. Skipping")
            }
        }
    }
}
