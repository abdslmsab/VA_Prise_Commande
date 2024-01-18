package com.example.va_prisecommande.utils

import com.example.va_prisecommande.model.Article
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class DataRepository {
    fun parseXmlToArticles(xml: String): List<Article> {
        val cleanXml = xml.trim().removePrefix("\uFEFF")

        val articles = mutableListOf<Article>()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlPullParser = xmlPullParserFactory.newPullParser()
        xmlPullParser.setInput(StringReader(cleanXml))

        var eventType = xmlPullParser.eventType
        var currentArticle: Article? = null
        var currentTag: String? = null
        var currentConditionnements: MutableList<String>? = null
        var currentConditionnementDefaut: String? = null
        var isDefault: Boolean = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = xmlPullParser.name
                    if (currentTag == "article") {
                        currentArticle = Article("", "", mutableListOf())
                        currentConditionnements = mutableListOf()
                        currentConditionnementDefaut = null
                    } else if (currentTag == "Conditionnement") {
                        isDefault = xmlPullParser.getAttributeValue(null, "default")?.toBoolean() ?: false
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = xmlPullParser.text.trim()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            "Ean" -> currentArticle?.ean = text
                            "Nom" -> currentArticle?.nom = text
                            "Conditionnement" -> {
                                if (isDefault) {
                                    currentConditionnementDefaut = text
                                }
                                currentConditionnements?.add(text)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == "article" && currentArticle != null) {
                        currentArticle.conditionnements = currentConditionnements ?: mutableListOf()
                        currentArticle.conditionnementDefaut = currentConditionnementDefaut
                        articles.add(currentArticle)
                    }
                    // Reset the default flag when the end of a Conditionnement tag is reached
                    if (xmlPullParser.name == "Conditionnement") {
                        isDefault = false
                    }
                    currentTag = null
                }
            }
            eventType = xmlPullParser.next()
        }
        return articles
    }


    fun findArticleByEan(ean: String, articles: List<Article>): Article? {
        return articles.firstOrNull { it.ean == ean }
    }
}