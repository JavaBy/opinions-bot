package by.jprof.telegram.opinions.commands

interface BotCommand {
    fun execute(chatIdentifier: Long, vararg parameters: String)
}
