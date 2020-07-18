package by.jprof.telegram.opinions.dao

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

fun Long.toAttributeValue(): AttributeValue = AttributeValue.builder().n(this.toString()).build()
