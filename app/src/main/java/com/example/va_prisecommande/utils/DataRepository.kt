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
        var currentConditionnements = mutableListOf<String>()
        var currentConditionnementDefaut: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (xmlPullParser.name == "article") {
                        currentArticle = Article("", "", mutableListOf())
                        currentConditionnements.clear()
                        currentConditionnementDefaut = null
                    }
                    currentTag = xmlPullParser.name
                }
                XmlPullParser.TEXT -> {
                    val text = xmlPullParser.text.trim()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            "Ean" -> currentArticle?.ean = text
                            "Nom" -> currentArticle?.nom = text
                            "Conditionnement" -> {
                                val isDefault = xmlPullParser.getAttributeValue(null, "default")?.toBoolean() ?: false
                                if (isDefault) {
                                    currentConditionnementDefaut = text
                                }
                                currentConditionnements.add(text)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == "article" && currentArticle != null) {
                        currentArticle.conditionnements = currentConditionnements
                        currentArticle.conditionnementDefaut = currentConditionnementDefaut
                        articles.add(currentArticle)
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