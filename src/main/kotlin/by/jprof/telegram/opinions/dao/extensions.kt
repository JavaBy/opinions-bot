package by.jprof.telegram.opinions.dao

import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

fun <T : Number> T.toAttributeValue(): AttributeValue = AttributeValue.builder().n(this.toString()).build()

fun Map<String, AttributeValue>.toAttributeValue() = AttributeValue.builder().m(this).build()

fun Map<String, AttributeValue>.require(attr: String, message: String? = null): AttributeValue {
    return this[attr] ?: throw IllegalStateException(message ?: "Missing '$attr' attribute!")
}

fun Update.asText(): TextContent? {
    val message = (this as? MessageUpdate) ?: return null
    val content = (message.data as? CommonMessageImpl<*>) ?: return null
    return content.content as? TextContent
}