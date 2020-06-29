package by.jprof.telegram.opinions.commands

import by.jprof.telegram.opinions.dao.YoutubeDAO
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.google.api.services.youtube.YouTube
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class RemoveYoutubeChannelBotCommand(
        private val youtubeDAO: YoutubeDAO,
        private val bot: RequestsExecutor,
        private val youtubeApi: YouTube
) : BotCommand {

    companion object {
        val logger = LogManager.getLogger(RemoveYoutubeChannelBotCommand::class.java)!!
    }

    override fun execute(chatIdentifier: Long, vararg parameters: String) {
        logger.debug("executing $REMOVE_FROM_WHITE_LIST command for chat $chatIdentifier")
        if (parameters.size != 1) {
            throw IllegalArgumentException("Remove youtube command should have exactly 1 parameter")
        } else {
            val chatId = ChatId(chatIdentifier)
            val searchCriteria = parameters[0]

            val channelDataRequest = youtubeApi.channels().list("statistics, snippet")
            val response = ChannelResponseData(resp = channelDataRequest.setId(searchCriteria).execute())
            val total = response.total
            if (total > 0) { //found
                removeChannel(response, chatId)
            } else {
                val userNameSearchResponse =
                        ChannelResponseData(resp = channelDataRequest.setForUsername(searchCriteria).execute())
                removeChannel(userNameSearchResponse, chatId)
            }

        }
    }

    private fun removeChannel(userNameSearchResponse: ChannelResponseData, chatId: ChatId) {
        userNameSearchResponse.channelId?.let {
            runBlocking {
                logger.debug("channel $it was found in the YouTube API")
                youtubeDAO.removeFromWhiteList(channelId = it)
                val removed = "Channel ${userNameSearchResponse.channelTitle} is removed from the whitelist"
                bot.sendMessage(chatId = chatId, text = removed)
            }
        }
    }

}