package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.webhook.entity.MentionStats
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.CommonUser
import com.github.insanusmokrassar.TelegramBotAPI.types.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

internal class KotlinStatsCommandProcessorKtTest {

    @Test
    fun composeStatsMessage() {
        assertEquals(
                """
                    Top 2 Kotlin fans:
                    ```
                    Username       Mentions       Last mention
                    Tony Soprano   1              1 студзеня @ 23:15
                    Silvio Dante   2              1 лютага @ 13:35```
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