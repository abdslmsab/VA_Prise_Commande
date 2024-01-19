package com.example.va_prisecommande.singleton

import Client
import com.example.va_prisecommande.ftp.FtpDownloadTask
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.model.Commercial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object DataRepository {
    var commerciaux: List<Commercial>? = null
    var clients: List<Client>? = null
    var articles: List<Article>? = null
    var commerciauxXml: String? = null
    var clientsXml: String? = null
    var articlesXml: String? = null


    suspend fun loadAllData() {
        commerciauxXml = downloadXml("/commerciaux.xml")
        commerciaux = parseXmlToCommerciaux(commerciauxXml ?: "")

        clientsXml = downloadXml("/clients.xml")
        clients = parseXmlToClients(clientsXml ?: "")

        articlesXml = downloadXml("/articles.xml")
        articles = parseXmlToArticles(articlesXml ?: "")
    }

    suspend fun downloadXml(path: String): String {
        return withContext(Dispatchers.IO) {
            FtpDownloadTask().downloadXmlFile("server.nap-agency.com", "ftpVital", "Kz5Jkud6GG", path)
        }
    }

    fun parseXmlToCommerciaux(xml: String): List<Commercial> {
        val cleanXml = xml.trim().removePrefix("\uFEFF")

        val commerciaux = mutableListOf<Commercial>()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlPullParser = xmlPullParserFactory.newPullParser()
        xmlPullParser.setInput(StringReader(cleanXml))

        var eventType = xmlPullParser.eventType
        var currentCommercial: Commercial? = null
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (xmlPullParser.name == "row") {
                        currentCommercial = Commercial("", "")
                    }
                    currentTag = xmlPullParser.name
                }
                XmlPullParser.TEXT -> {
                    val text = xmlPullParser.text
                    currentCommercial?.let {
                        when (currentTag) {
                            "Nom" -> it.nom = text
                            "Prenom" -> it.prenom = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == "row" && currentCommercial != null) {
                        commerciaux.add(currentCommercial)
                        currentCommercial = null
                    }
                    currentTag = null
                }
            }
            eventType = xmlPullParser.next()
        }
        return commerciaux
    }

    fun parseXmlToClients(xml: String): List<Client> {
        val cleanXml = xml.trim().removePrefix("\uFEFF")

        val clients = mutableListOf<Client>()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlPullParser = xmlPullParserFactory.newPullParser()
        xmlPullParser.setInput(StringReader(cleanXml))

        var eventType = xmlPullParser.eventType
        var currentClient: Client? = null
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (xmlPullParser.name == "row") {
                        currentClient = Client("", "", "", "")
                    }
                    currentTag = xmlPullParser.name
                }

                XmlPullParser.TEXT -> {
                    val text = xmlPullParser.text
                    currentClient?.let {
                        when (currentTag) {
                            "Code" -> it.code = text
                            "Nom" -> it.nom = text
                            "Adresse" -> it.adresse = text
                            "CodePostal" -> it.codepostal = text
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == "row" && currentClient != null) {
                        clients.add(currentClient)
                        currentClient = null
                    }
                    currentTag = null
                }
            }
            eventType = xmlPullParser.next()
        }
        return clients
    }

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
}