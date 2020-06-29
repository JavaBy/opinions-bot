package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.commands.ADD_TO_WHITE_LIST
import by.jprof.telegram.opinions.commands.BotCommand
import by.jprof.telegram.opinions.commands.REMOVE_FROM_WHITE_LIST
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.BotCommandTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class WhitelistCommandProcessor(
        private val bot: RequestsExecutor,
        private val commands: Map<String, BotCommand>
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(WhitelistCommandProcessor::class.java)!!
        private val siteRegex = """
            (?:https|http)\:\/\/(?:[\w]+\.)?youtube\.com\/(?:c\/|channel\/|user\/)?([a-zA-Z0-9\-]{1,})
        """.trimIndent().toRegex()
        private val COMMANDS = listOf(ADD_TO_WHITE_LIST, REMOVE_FROM_WHITE_LIST)
        private const val RESOURCE_GROUP_IDX = 1
    }

    override suspend fun process(update: Update) {
        logger.debug("Checking if update is command update: {}", update.updateId)
        if (isCommandUpdate(update)) {
            logger.debug("The update {} is command update", update.updateId)
            val botCommandMessage = extractFullCommandText(update as MessageUpdate)
            botCommandMessage?.let { executeCommand(it, update) }
        }
    }

    private fun isCommandUpdate(update: Update): Boolean {
        return (update as? MessageUpdate)?.let { messageUpdate ->
            (messageUpdate.data as? CommonMessageImpl<*>)?.let { message ->
                (message.content as? TextContent)?.entities
                        ?.map { entry -> entry.source }
                        ?.filterIsInstance<BotCommandTextSource>()
                        ?.isNotEmpty()
            }
        } ?: false
    }

    private fun extractFullCommandText(update: MessageUpdate): TextContent? {
        (update.data as CommonMessageImpl<*>).let { message ->
            val content = message.content as TextContent
            return if (content.entities.any { entry -> entry.source is BotCommandTextSource }) {
                content
            } else {
                null
            }
        }
    }

    private fun executeCommand(botCommandMessage: TextContent, update: MessageUpdate) {
        val command = botCommandMessage.entities.first { it.source is BotCommandTextSource }.source.source
        logger.debug("command is {}", command)
        val textWithoutCommand = botCommandMessage.text.replace(command, "").trim()
        val matchEntire = siteRegex.matchEntire(textWithoutCommand)
        if (null != matchEntire) {
            matchEntire.groups[RESOURCE_GROUP_IDX]?.value?.let { searchCriteria ->
                val parameters = searchCriteria.split("\\s+".toRegex()).toTypedArray()
                logger.debug("calling command: $command")
                logger.debug("available commands $commands")
                commands[command]?.execute(update.data.chat.id.chatId, *parameters)
            }
        } else {
            runBlocking { // commands using youtube blocking http api
                bot.sendMessage(
                        chatId = update.data.chat.id,
                        text = "Usage: $command https://link.to.youtube.channel"
                )
            }
        }
    }


}




