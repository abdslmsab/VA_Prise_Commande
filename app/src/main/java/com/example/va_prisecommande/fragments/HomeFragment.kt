package com.example.va_prisecommande.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.adapter.SalespersonAdapter
import com.example.va_prisecommande.ftp.FtpDownloadTask
import com.example.va_prisecommande.model.Commercial
import com.example.va_prisecommande.viewmodel.MainViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class HomeFragment : Fragment() {

    private lateinit var salespersonAdapter: SalespersonAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Récupération du RecyclerView
        val verticalRecyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)

        salespersonAdapter = SalespersonAdapter(emptyList())
        verticalRecyclerView.adapter = salespersonAdapter

        lifecycleScope.launch {
            val xml = withContext(Dispatchers.IO) {
                // Logique de téléchargement
                FtpDownloadTask().downloadXmlFile(
                    "server.nap-agency.com", "ftpVital", "Kz5Jkud6GG", "/commerciaux.xml"
                )
            }

            withContext(Dispatchers.Main) {
                if (xml.isEmpty()) {
                    Toast.makeText(context, "Impossible de charger les données.", Toast.LENGTH_LONG).show()
                } else {
                    val commerciauxList = parseXmlToCommerciaux(xml)
                    salespersonAdapter = SalespersonAdapter(commerciauxList)
                    verticalRecyclerView.adapter = salespersonAdapter
                }
            }
        }

        val searchInput = view?.findViewById<TextInputLayout>(R.id.search_input)?.editText as TextInputEditText

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                salespersonAdapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Pas besoin d'implémentation ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pas besoin d'implémentation ici
            }
        })


        view.findViewById<Button>(R.id.right_button)?.setOnClickListener {
            val selectedCommercial = salespersonAdapter.selectedCommercial
            if (selectedCommercial == null) {
                Toast.makeText(activity, "Veuillez sélectionner un commercial", Toast.LENGTH_SHORT).show()
            } else {
                val fragment = ClientFragment.newInstance(selectedCommercial)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
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
}