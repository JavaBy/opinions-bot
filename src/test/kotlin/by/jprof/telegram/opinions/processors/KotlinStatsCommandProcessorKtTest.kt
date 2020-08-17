package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.entity.MentionStats
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.CommonUser
import com.github.insanusmokrassar.TelegramBotAPI.types.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

internal class KotlinStatsCommandProcessorKtTest {
    @Test
    fun testParseLimit() {
        assertEquals(3, parseLimit("/cmd@bot"))
        assertEquals(20, parseLimit("/cmd@bot 30"))
        assertEquals(10, parseLimit("/cmd@bot  10"))
        assertEquals(10, parseLimit("/cmd@bot  10 xxx"))
    }

    @Test
    fun composeStatsMessage() {
        assertEquals(
                """
                    __Username__                       __Mentions__   __Last mention at__
                    Tony Soprano                       1              Jan 01'20 at 23:15
                    Silvio Dante                       2              Feb 01'20 at 13:35
                """.trimIndent(),
                composeStatsMessage(listOf(
                        CommonUser(
                                ChatId(1L),
                                "Tony",
                                lastName = "Soprano",
                                username = Username("@soprano")
                        ) to MentionStats(1, Instant.parse("2020-01-01T20:15:00.0Z")),
                        CommonUser(
                                ChatId(2L),
                                "Silvio",
                                lastName = "Dante",
                                username = Username("@dante")
                        ) to MentionStats(2, Instant.parse("2020-02-01T10:35:00.0Z"))
                ))
        )
    }
}