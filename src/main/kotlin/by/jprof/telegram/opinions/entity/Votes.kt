package by.jprof.telegram.opinions.entity

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

const val DOWN_VOTE = "\uD83D\uDC4E"
const val UP_VOTE = "\uD83D\uDC4D"

data class Votes(
        val id: String,
        val votes: Map<String, String> = emptyMap()
)

fun Votes.toAttributeValues(): Map<String, AttributeValue> = mapOf(
        "id" to AttributeValue.builder().s(this.id).build(),
        "votes" to AttributeValue.builder().m(
                this
                        .votes
                        .mapValues { (_, value) -> AttributeValue.builder().s(value).build() }
        ).build()
)

fun Map<String, AttributeValue>.toVotes(): Votes = Votes(
        id = this["id"]?.s() ?: throw IllegalStateException("Missing id property"),
        votes = this["votes"]?.m()
                ?.mapValues { (_, value) -> value.s() ?: "" }
                ?: emptyMap()
)

val Votes.upVotes: Int
        get() = this.votes.count { (_, vote) -> vote == "+" }

val Votes.downVotes: Int
        get() = this.votes.count { (_, vote) -> vote == "-" }