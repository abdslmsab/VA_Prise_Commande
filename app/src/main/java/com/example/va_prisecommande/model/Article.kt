package com.example.va_prisecommande.model

data class Article(
    var ean: String,
    var nom: String,
    var conditionnements: List<String>,
    var conditionnementDefaut: String? = null
) {
    // Initialise le conditionnement par défaut lors de la création de l'objet
    init {
        conditionnementDefaut = conditionnements.firstOrNull { it.contains("default") }
    }
}
