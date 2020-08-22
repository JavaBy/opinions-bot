package by.jprof.telegram.opinions.dao

import com.github.insanusmokrassar.TelegramBotAPI.types.files.PathedFile
import com.github.insanusmokrassar.TelegramBotAPI.types.files.filename
import com.github.insanusmokrassar.TelegramBotAPI.types.files.fullUrl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import com.github.insanusmokrassar.TelegramBotAPI.utils.TelegramAPIUrlsKeeper
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.io.File
import java.io.FileOutputStream
import java.net.URL

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

fun <T : Number> T.toAttributeValue(): AttributeValue = AttributeValue.builder().n(this.toString()).build()

fun Map<String, AttributeValue>.toAttributeValue() = AttributeValue.builder().m(this).build()

fun Map<String, AttributeValue>.require(attr: String, message: String? = null): AttributeValue {
    return this[attr] ?: throw IllegalStateException(message ?: "Missing '$attr' attribute!")
}

fun PathedFile.download(
        telegramAPIUrlsKeeper: TelegramAPIUrlsKeeper,
        dest: File = File.createTempFile(this.fileUniqueId, this.filename)
): File {
    URL(this.fullUrl(telegramAPIUrlsKeeper)).openStream().use {
        it.transferTo(FileOutputStream(dest))
    }
    return dest
}

fun Update.asText(): TextContent? {
    val message = (this as? MessageUpdate) ?: return null
    val content = (message.data as? CommonMessageImpl<*>) ?: return null
    return content.content as? TextContent
}