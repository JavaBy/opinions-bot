package by.jprof.telegram.opinions.commands

import by.jprof.telegram.opinions.dao.YoutubeDAO
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.google.api.services.youtube.YouTube
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class AddYoutubeChannelBotCommand(
        private val youtubeDAO: YoutubeDAO,
        private val bot: RequestsExecutor,
        private val youtubeApi: YouTube
) : BotCommand {
    companion object {
        val logger = LogManager.getLogger(AddYoutubeChannelBotCommand::class.java)!!
    }

    override fun execute(chatIdentifier: Long, vararg parameters: String) {
        logger.debug("executing $ADD_TO_WHITE_LIST command for chat $chatIdentifier")
        if (parameters.size != 1) {
            throw IllegalArgumentException("Add youtube command should have exactly 1 parameter")
        } else {
            val searchCriteria = parameters[0]
            val chatId = ChatId(chatIdentifier)

            val isAdded = runBlocking { return@runBlocking youtubeDAO.isInWhiteList(searchCriteria) }

            if (isAdded) {
                runBlocking {
                    bot.sendMessage(chatId = chatId, text = "Channel is already in the whitelist")
                }
            } else {
                val response = ChannelResponseData(resp = newChannelRequest().setId(searchCriteria).execute())
                val total = response.total
                if (total > 0) { //found
                    addChannelIfExistsInYouTube(response, chatId)
                } else {
                    val userNameSearchResponse =
                            ChannelResponseData(resp = newChannelRequest().setForUsername(searchCriteria).execute())
                    addChannelIfExistsInYouTube(userNameSearchResponse, chatId)
                }
            }

        }
    }

    private fun newChannelRequest() = youtubeApi.channels().list("statistics,snippet")

    private fun addChannelIfExistsInYouTube(userNameSearchResponse: ChannelResponseData, chatId: ChatId) {
        userNameSearchResponse.channelId?.let {
            runBlocking {
                logger.debug("channel $it was found in the YouTube API")
                youtubeDAO.addToWhiteList(channelId = it)
                val addedMsg = "Channel ${userNameSearchResponse.channelTitle} is added to the whitelist"
                bot.sendMessage(chatId = chatId, text = addedMsg)
            }
        }
    }

}



