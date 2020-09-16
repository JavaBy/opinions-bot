package by.jprof.telegram.opinions.entity

import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class Button(
        val text: String,
        val data: String
)

data class Keyboard(
        val id: String,
        val buttons: List<List<Button>>
)

fun Map<String, AttributeValue>.toButton(): Button = Button(
        text = this["text"]?.s() ?: throw IllegalStateException("Missing text property"),
        data = this["data"]?.s() ?: throw IllegalStateException("Missing data property")
)

fun Map<String, AttributeValue>.toKeyboard(): Keyboard = Keyboard(
        id = this["id"]?.s() ?: throw IllegalStateException("Missing id property"),
        buttons = this["buttons"]?.l()
                ?.map { row ->
                    row?.l()?.map { button ->
                        button?.m()?.toButton() ?: throw IllegalStateException("Invalid button")
                    } ?: throw IllegalStateException("Invalid row")
                }
                ?: emptyList()
)

fun Keyboard.toInlineKeyboardMarkup() = InlineKeyboardMarkup(
        keyboard = this.buttons.map { row ->
            row.map { button ->
                CallbackDataInlineKeyboardButton(button.text, button.data)
            }
        }
)
