package by.jprof.telegram.opinions.dao

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

fun <T : Number> T.toAttributeValue(): AttributeValue = AttributeValue.builder().n(this.toString()).build()

fun Map<String, AttributeValue>.toAttributeValue() = AttributeValue.builder().m(this).build()

fun Map<String, AttributeValue>.require(attr: String, message: String? = null): AttributeValue {
    return this[attr] ?: throw IllegalStateException(message ?: "Missing '$attr' attribute in $this!")
}