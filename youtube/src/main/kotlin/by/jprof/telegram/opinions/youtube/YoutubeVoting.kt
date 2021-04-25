package by.jprof.telegram.opinions.youtube

import by.jprof.telegram.opinions.voting.Votes
import by.jprof.telegram.opinions.voting.VotesDAO
import by.jprof.telegram.opinions.voting.votingKeyBoard
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.boldMarkdownV2
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.CallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.escapeMarkdownV2Common
import com.google.api.services.youtube.YouTube
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class YoutubeVoting(
    private val bot: RequestsExecutor,
    private val votesDAO: VotesDAO,
    private val youtubeDAO: YoutubeDAO,
    private val youTube: YouTube
) {
    companion object {
        private val logger = LogManager.getLogger(YoutubeVoting::class.java)!!

        private const val ACCEPTED_DISPLAY_LEN = 500
    }

    suspend fun sendVoteForVideoMessage(chatId: ChatId, videoId: String, messageId: MessageIdentifier? = null) {
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
                        chatId = chatId,
                        text = videoText,
                        parseMode = MarkdownV2ParseMode,
                        replyToMessageId = messageId,
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

    suspend fun processCallback(callbackQuery: CallbackQuery) {
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