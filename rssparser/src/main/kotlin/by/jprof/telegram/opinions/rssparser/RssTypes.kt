package by.jprof.telegram.opinions.rssparser

import java.util.*

/**
 * Parsed feed. Available fields are basic subset of those defined in Atom specification.
 * See: https://tools.ietf.org/html/rfc4287.
 * Please note that some of the fields are not available by RSS specification.
 *
 * @see AtomFeed
 * @see RssFeed
 */
sealed class Feed {
    /**
     * Feed ID.
     */
    abstract val id: String?

    /**
     * Feed title.
     */
    abstract val title: String
    /**
     * Date of last feed update.
     */
    abstract val updated: Date?
    /**
     * Date of last publication of content in feed.
     */
    abstract val published: Date?
    /**
     * List of links found in feed description.
     */
    abstract val links: List<Link>

    /**
     * List of entries found in feed description.
     */
    abstract val entries: List<FeedEntry>
}

/**
 * Concrete type of Atom feed.
 * @see Feed
 */
data class AtomFeed(
    override val id: String,
    override val title: String,
    override val updated: Date,
    override val links: List<Link>,
    override val entries: List<AtomEntry>
) : Feed() {
    override val published: Date? = null
}

/**
 * Concrete type of RSS feed.
 * @see Feed
 */
data class RssFeed(
    override val title: String,
    override val updated: Date?,
    override val published: Date?,
    override val links: List<Link>,
    override val entries: List<RssEntry>
) : Feed() {
    override val id: String? = null
}

/**
 * Parsed feed entry. Available fields are basic subset of those defined in Atom specification.
 * See: https://tools.ietf.org/html/rfc4287
 * @see AtomEntry
 * @see RssEntry
 */
sealed class FeedEntry {
    /**
     * ID of entry.
     */
    abstract val id: String?

    /**
     * List of links found in entry.
     */
    abstract val links: List<Link>

    /**
     * Title of entry.
     */
    abstract val title: String?

    /**
     * Entry summary/synopsis.
     */
    abstract val summary: String?

    /**
     * Entry content. Note that it is always null in RSS fields, as RSS provides only summary.
     */
    abstract val content: String?

    /**
     * Date of last update of entry.
     */
    abstract val updated: Date?

    /**
     * Date of entry publication.
     */
    abstract val published: Date?

    /**
     * List of authors of entry.
     */
    abstract val authors: List<Author>
}

/**
 * Concrete type of Atom feed entry.
 * @see FeedEntry
 */
data class AtomEntry(
    override val id: String,
    override val links: List<Link>,
    override val title: String,
    override val summary: String?,
    override val content: String?,
    override val updated: Date,
    override val published: Date?,
    override val authors: List<Author>
) : FeedEntry()

/**
 * Concrete type of Rss feed entry.
 * @see FeedEntry
 */
data class RssEntry(
    override val id: String?,
    override val links: List<Link>,
    override val title: String?,
    override val summary: String?,
    override val published: Date?,
    override val authors: List<Author>
) : FeedEntry() {
    override val content: String? = null
    override val updated: Date? = null
}

/**
 * Author of entry.
 */
data class Author(
        val name: String,
        val uri: String?,
        val email: String?
)

/**
 * Link found in entry metadata. Note that the contents of links are not specified and can vary among different
 * RSS/Atom implementations.
 */
data class Link(
        val relationType: String?,
        val mimeType: String?,
        val href: String,
        val hrefLanguage: String?
)
