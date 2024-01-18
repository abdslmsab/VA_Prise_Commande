package com.example.va_prisecommande.model

data class ArticlePourPanier(
    val ean: String,
    val nom: String,
    val conditionnement: String,
    val quantite: Int
)
