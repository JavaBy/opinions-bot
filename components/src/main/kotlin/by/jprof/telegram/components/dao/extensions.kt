package by.jprof.telegram.components.dao

import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

fun <T : Number> T.toAttributeValue(): AttributeValue = AttributeValue.builder().n(this.toString()).build()

fun <T : Collection<String>> T.toAttributeValue(): AttributeValue = AttributeValue.builder().ss(this).build()

fun Map<String, AttributeValue>.toAttributeValue() = AttributeValue.builder().m(this).build()

fun Map<String, AttributeValue>.require(attr: String, message: String? = null): AttributeValue {
    return this[attr] ?: throw IllegalStateException(message ?: "Missing '$attr' attribute!. Attrs: $this")
}

fun Update.asText(): TextContent? {
    val message = (this as? MessageUpdate) ?: return null
    val content = (message.data as? CommonMessageImpl<*>) ?: return null
    return content.content as? TextContent
}