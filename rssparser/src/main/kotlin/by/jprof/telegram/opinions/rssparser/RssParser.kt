package by.jprof.telegram.opinions.rssparser

import org.dom4j.*
import org.dom4j.io.SAXReader
import org.dom4j.tree.DefaultElement
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class FeedParser(private val config: FeedXmlPathConfig, private val feedType: FeedType) {

    fun parse(source: String) = parse(source.byteInputStream())

    @Throws(ParseException::class)
    fun parse(source: InputStream): List<Feed> {
        val document = SAXReader().read(source)

        // clear namespaces: https://stackoverflow.com/a/7107122
        document.accept(object : VisitorSupport() {
            override fun visit(document: Document) {
                (document.rootElement as DefaultElement).namespace = Namespace.NO_NAMESPACE
                document.rootElement.additionalNamespaces().clear()
            }

            override fun visit(namespace: Namespace) {
                namespace.detach()
            }

            override fun visit(node: Attribute) {
                if (node.toString().contains("xmlns") || node.toString().contains("xsi:"))
                    node.detach()
            }

            override fun visit(node: Element) {
                if (node is DefaultElement)
                    node.namespace = Namespace.NO_NAMESPACE
            }
        })

        val root = document.rootElement
        return root.selectNodes("/${config.feedRoot}").map { feedNode ->
            val id = feedNode.getElement(config.id, requiredInAtom = true)?.text?.trim()
            val title = feedNode.getElement(config.title, requiredInAtom = true, requiredInRss = true)!!.text.trim()
            val updated = feedNode.getElement(config.updated, requiredInAtom = true)?.text?.trim()?.toDate()
            val published = feedNode.getElement(config.published)?.text?.trim()?.toDate()
            val links = feedNode.selectNodes(config.link).mapNotNull(::parseLink)
            val entries = parseEntries(feedNode)

            @Suppress("UNCHECKED_CAST")
            if (feedType == FeedType.ATOM) {
                AtomFeed(
                    id = id!!,
                    title = title,
                    updated = updated!!,
                    links = links,
                    entries = entries as List<AtomEntry>
                )
            } else {
                RssFeed(
                    title = title,
                    updated = updated,
                    published = published,
                    links = links,
                    entries = entries as List<RssEntry>
                )
            }
        }
    }

    private fun parseEntries(feedNode: Node): List<FeedEntry> {
        val cfg = config.entryPathConfig
        return feedNode.selectNodes(config.entryRoot).mapNotNull { entry ->
            val id = entry.getElement(cfg.id, requiredInAtom = true)?.text?.trim()
            val title = entry.getElement(cfg.title, requiredInAtom = true)?.text?.trim()
            val summary = entry.getElement(cfg.summary)?.text?.trim()
            val content = entry.getElement(cfg.content)?.text?.trim()
            val updated = entry.getElement(cfg.updated, requiredInAtom = true)?.text?.trim()?.toDate()
            val published = entry.getElement(cfg.published)?.text?.trim()?.toDate()
            val links = entry.selectNodes(cfg.link).mapNotNull(::parseLink)
            val authors = entry.selectNodes(cfg.author).mapNotNull(::parseAuthor)
            if (feedType == FeedType.ATOM)
                AtomEntry(
                    id = id!!,
                    links = links,
                    title = title!!,
                    summary = summary,
                    content = content,
                    updated = updated!!,
                    published = published,
                    authors = authors
                )
            else
                RssEntry(
                    id = id,
                    links = links,
                    title = title,
                    summary = summary,
                    published = published,
                    authors = authors
                )
        }
    }

    private fun Node.getElement(xpath: String, requiredInAtom: Boolean = false, requiredInRss: Boolean = false): Node? {
        val isRequired = if (feedType == FeedType.ATOM) requiredInAtom else requiredInRss
        return if (isRequired) requireNode(xpath) else optionalNode(xpath)
    }

    companion object {
        private fun parseLink(link: Node): Link? {
            return if (link !is Element)
                null
            else {
                val relationType = link.attribute("rel")?.value?.trim()
                val mimeType = link.attribute("type")?.value?.trim()
                val hrefLanguage = link.attribute("hreflang")?.value?.trim()
                val href = link.attribute("href")?.value?.trim() ?: link.text.trim()
                Link(relationType, mimeType, href, hrefLanguage)
            }
        }

        private fun parseAuthor(author: Node): Author? {
            return if (author !is Element)
                null
            else {
                val uri = author.attribute("uri")?.value?.trim()
                val email = author.attribute("email")?.value?.trim()
                val name = author.attribute("name")?.value?.trim() ?: author.text.trim()
                Author(name, uri, email)
            }
        }

        private fun Node.requireNode(xpath: String): Node {
            val node = selectSingleNode(xpath)
            if (node == null)
                throw ElementNotFoundException("Node $xpath not found")
            else
                return node
        }

        private fun Node.optionalNode(xpath: String): Node? {
            return xpath.takeIf { it.isNotEmpty() }?.let { selectSingleNode(it) }
        }

        private fun String.toDate(): Date {
            listOf(DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.RFC_1123_DATE_TIME).forEach {
                val date = kotlin.runCatching { ZonedDateTime.parse(this, it) }.getOrNull()
                if (date != null)
                    return Date.from(date.toInstant())
            }
            throw DateParseException(this)
        }

        /**
         * Get Atom parser.
         * @param config configuration of parser
         * @return FeedParser instance
         */
        fun atom(config: FeedXmlPathConfig = FeedXmlPathConfig.atomDefault) = FeedParser(config, FeedType.ATOM)


        /**
         * Get RSS parser.
         * @param config configuration of parser
         * @return FeedParser instance
         */
        fun rss(config: FeedXmlPathConfig = FeedXmlPathConfig.rssDefault) = FeedParser(config, FeedType.RSS)
    }
}






/**
 * Supported feed types
 */
enum class FeedType {
    ATOM, RSS
}