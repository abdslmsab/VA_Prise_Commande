package com.example.va_prisecommande.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.adapter.BasketAdapter
import com.example.va_prisecommande.ftp.FtpDownloadTask
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.model.ArticlePourPanier
import com.example.va_prisecommande.utils.DataRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PanierCommandeFragment : Fragment() {
    private lateinit var basketAdapter: BasketAdapter
    private var articlesDansLePanier = mutableListOf<ArticlePourPanier>()
    private var articles: List<Article> = emptyList()
    private lateinit var eanEditText: EditText
    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basketAdapter = BasketAdapter(articlesDansLePanier)
        chargerArticles()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_panier_commande, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)

        basketAdapter = BasketAdapter(articlesDansLePanier)

        recyclerView.adapter = basketAdapter

        val eanInputLayout = view.findViewById<TextInputLayout>(R.id.ean_input)
        val eanEditText = eanInputLayout.editText as TextInputEditText
        val conditionnementSpinner = view.findViewById<Spinner>(R.id.packaging_input)
        val quantiteInputLayout = view.findViewById<TextInputLayout>(R.id.quantity_input)
        val quantiteEditText = quantiteInputLayout.editText as TextInputEditText

        eanEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length == 4) {
                    val eanComplet = eanInputLayout.prefixText.toString() + s.toString()
                    chargerArticleEtConditionnements(eanComplet, conditionnementSpinner)
                }
            }

        })

        view.findViewById<Button>(R.id.add_button).setOnClickListener {
            val ean = eanInputLayout.prefixText.toString() + eanEditText.text.toString()
            val selectedItem = conditionnementSpinner.selectedItem
            val conditionnementSelected = selectedItem?.toString() ?: "Valeur par défaut"
            val quantite = quantiteEditText.text.toString().toIntOrNull() ?: 0
            ajouterAuPanier(ean, conditionnementSelected, quantite)
        }

        return view
    }

    private fun chargerArticleEtConditionnements(ean: String, conditionnementSpinner: Spinner) {
        lifecycleScope.launch {
            val xml = withContext(Dispatchers.IO) {
                // Remplacez cette ligne par votre logique de téléchargement
                FtpDownloadTask().downloadXmlFile(
                    "server.nap-agency.com", "ftpVital", "Kz5Jkud6GG", "/articles.xml"
                )
            }

            withContext(Dispatchers.Main) {
                if (xml.isNotEmpty()) {
                    val articles = dataRepository.parseXmlToArticles(xml)
                    val articleTrouve = articles.firstOrNull { it.ean == ean }

                    articleTrouve?.let { article ->
                        // Mettez à jour le spinner avec les conditionnements de l'article
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, article.conditionnements)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        conditionnementSpinner.adapter = adapter

                        // Sélectionnez le conditionnement par défaut (s'il y en a un)
                        article.conditionnementDefaut?.let { defaultConditionnement ->
                            val position = adapter.getPosition(defaultConditionnement)
                            conditionnementSpinner.setSelection(position)
                        }
                    } ?: run {
                        Toast.makeText(context, "Article avec EAN: $ean non trouvé", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Impossible de charger les données.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()
        }

        val validerBouton = view.findViewById<Button>(R.id.right_button)
        validerBouton.setOnClickListener {
            val fragment = ConsignesCommandeFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun ajouterAuPanier(ean: String, conditionnement: String, quantite: Int) {
        val article = dataRepository.findArticleByEan(ean, articles)
        if (article != null) {
            val articlePourPanier = ArticlePourPanier(ean, article.nom, conditionnement, quantite)
            articlesDansLePanier.add(articlePourPanier)
            basketAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(context, "Article non trouvé", Toast.LENGTH_LONG).show()
        }
    }


    private fun chargerArticles() {
        lifecycleScope.launch {
            val xml = withContext(Dispatchers.IO) {
                FtpDownloadTask().downloadXmlFile(
                    "server.nap-agency.com", "ftpVital", "Kz5Jkud6GG", "/articles.xml"
                )
            }

            withContext(Dispatchers.Main) {
                if (xml.isEmpty()) {
                    Toast.makeText(context, "Impossible de charger les données.", Toast.LENGTH_LONG).show()
                } else {
                    articles = dataRepository.parseXmlToArticles(xml)
                }
            }
        }
    }

}
