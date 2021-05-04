package by.jprof.telegram.opinions.rssparser

/**
 * Contains configuration for field resolution when parsing RSS/Atom document.
 * Properties define paths for corresponding properties in Feed class.
 * Default values for Atom and RSS are provided in companion object.
 * Paths are defined in XPath relative notation, for example: "rss/channel".
 * Empty string in values marks makes field skipped while parsing.
 *
 * @property feedRoot path to feed tag
 * @property id path to feed ID relative to feed root
 * @property title path to feed title relative to feed root
 * @property updated path to feed update date relative to feed root
 * @property published path to feed publish date relative to feed root
 * @property link path to feed link relative to feed root
 * @property entryRoot path to entry root tag relative to feed root
 * @property entryPathConfig configuration of paths for entry
 *
 * @see Feed
 */
data class FeedXmlPathConfig(
    val feedRoot: String,
    val id: String,
    val title: String,
    val updated: String,
    val published: String,
    val link: String,
    val entryRoot: String,
    val entryPathConfig: EntryXmlPathConfig
) {
    companion object {
        /**
         * Default Atom feed configuration.
         */
        val atomDefault = FeedXmlPathConfig(
            feedRoot = "feed",
            id = "id",
            title = "title",
            updated = "updated",
            published = "",
            link = "link",
            entryRoot = "entry",
            entryPathConfig = EntryXmlPathConfig.atomDefault
        )

        /**
         * Default RSS feed configuration.
         */
        val rssDefault = FeedXmlPathConfig(
            feedRoot = "rss/channel",
            id = "",
            title = "title",
            updated = "lastBuildDate",
            published = "pubDate",
            link = "link",
            entryRoot = "item",
            entryPathConfig = EntryXmlPathConfig.rssDefault
        )
    }
}

/**
 * Contains configuration for field resolution when parsing RSS/Atom document.
 * Properties define paths for corresponding properties in FeedEntry class.
 * Default values for Atom and RSS are provided in companion object.
 * Paths are defined in path notation, for example: "rss/channel".
 * Empty string in values marks makes field skipped while parsing.
 *
 * @property id path to entry ID relative to entry root
 * @property title path to entry title relative to entry root
 * @property updated path to entry update date relative to entry root
 * @property published path to entry publish date relative to entry root
 * @property link path to entry link relative to entry root
 * @property summary path to entry summary relative to entry root
 * @property content path to entry content relative to entry root
 * @property author path to entry author relative to entry root
 *
 * @see FeedXmlPathConfig
 * @see FeedEntry
 */
data class EntryXmlPathConfig(
    val id: String,
    val link: String,
    val title: String,
    val summary: String,
    val content: String,
    val updated: String,
    val published: String,
    val author: String
) {
    companion object {
        /**
         * Default Atom entry configuration.
         */
        val atomDefault = EntryXmlPathConfig(
            id = "id",
            link = "link",
            title = "title",
            summary = "summary",
            content = "content",
            updated = "updated",
            published = "published",
            author = "author"
        )

        /**
         * Default RSS entry configuration.
         */
        val rssDefault = EntryXmlPathConfig(
            id = "guid",
            link = "link",
            title = "title",
            summary = "description",
            content = "",
            updated = "",
            published = "pubDate",
            author = "author"
        )
    }
}