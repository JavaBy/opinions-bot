package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import by.jprof.telegram.opinions.entity.MentionStats
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.getChatMember
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.RegularTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.fullEntitiesList
import com.github.insanusmokrassar.TelegramBotAPI.types.toChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import java.util.Date

class KotlinStatsCommandProcessor(
        private val bot: RequestsExecutor,
        private val kotlinMentionsDAO: KotlinMentionsDAO
) : CommandProcessor("kotlinstats") {
    override suspend fun doProcess(update: Update) {
        val message = (update as? MessageUpdate) ?: return
        val content = (message.data as? CommonMessageImpl<*>) ?: return
        val text = (content.content as? TextContent) ?: return
        val chatId = content.chat.id
        val mention = kotlinMentionsDAO.find(chatId.chatId) ?: return
        val topUsers = takeTopKotlinFans(chatId, mention.users, extractLimit(text))
        bot.sendMessage(chatId,
                composeStatsMessage(topUsers),
                parseMode = MarkdownV2,
                replyToMessageId = content.messageId)
    }

    private suspend fun takeTopKotlinFans(
            chatId: ChatId,
            users: Map<Long, MentionStats>,
            limit: Int
    ): List<Pair<User, MentionStats>> {
        return users.entries
                .sortedByDescending { it.value.count }
                .take(limit)
                .map { it.toPair() }
                .map { (userId, stats) ->
                    val chatMember = bot.getChatMember(chatId, userId.toChatId())
                    chatMember.user to stats
                }
    }

}

fun extractLimit(text: TextContent): Int {
    val source = text.fullEntitiesList().firstOrNull { it is RegularTextSource }
    val limit = source as? RegularTextSource
    return limit?.source?.trim()?.toIntOrNull()?.coerceIn(1, 20) ?: 3
}

fun composeStatsMessage(topUsers: List<Pair<User, MentionStats>>): String {
    val header = "Top %d kotlin fans%n%-35s%-15s%s%n".format(
            topUsers.size, "__Username__", "__Mentions__", "__Last mention at__")
    return topUsers.joinToString(separator = "\n", prefix = header) { (user, stats) ->
        "%-35s%-15s%3\$tb %3\$td'%3\$ty at %3\$tR".format(
                "${user.firstName} ${user.lastName}",
                stats.count,
                Date.from(stats.lastUpdatedAt))
    }
}