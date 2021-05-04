package by.jprof.telegram.opinions.rssparser

sealed class ParseException(reason: String) : Exception(reason)
class ElementNotFoundException(xpath: String) : ParseException("Element $xpath not found")
class DateParseException(date: String) : ParseException("Could not parse date $date")