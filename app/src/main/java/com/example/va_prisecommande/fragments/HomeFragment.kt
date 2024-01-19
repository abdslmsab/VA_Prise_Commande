package com.example.va_prisecommande.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.va_prisecommande.singleton.DataRepository
import com.example.va_prisecommande.viewmodel.MainViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var salespersonAdapter: SalespersonAdapter
    private lateinit var viewModel: MainViewModel
    // private lateinit var database: AppDatabase

    /*
    companion object {
        private const val COMMERCIAUX_KEY = "commerciaux_xml"
    }
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        /* database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "app_database"
        ).build()

         */
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialisation et chargement des données
        // initDataLoading()

        // Récupération du RecyclerView
        val verticalRecyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)

        salespersonAdapter = SalespersonAdapter(emptyList())
        verticalRecyclerView.adapter = salespersonAdapter

        lifecycleScope.launch {
            // Charger les données si elles ne sont pas déjà chargées
            if (DataRepository.commerciaux == null) {
                DataRepository.loadAllData()
            }

            // Mettre à jour l'adaptateur avec les données des commerciaux
            DataRepository.commerciaux?.let { commerciauxList ->
                salespersonAdapter = SalespersonAdapter(commerciauxList)
                verticalRecyclerView.adapter = salespersonAdapter
            } ?: run {
                Toast.makeText(context, "Impossible de charger les données.", Toast.LENGTH_LONG).show()
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

    /*
    private fun initDataLoading() {
        database.dataCacheDao().getData(COMMERCIAUX_KEY).observe(viewLifecycleOwner) { dataCache ->
            if (dataCache != null) {
                // Les données sont chargées du cache
                DataRepository.commerciaux = DataRepository.parseXmlToCommerciaux(dataCache.xmlData)
                updateUIWithCommerciauxData()
            } else {
                // Les données ne sont pas dans le cache, télécharger les données
                downloadAndSaveCommerciauxData()
            }
        }
    }


    private fun updateUIWithCommerciauxData() {
        DataRepository.commerciaux?.let { commerciauxList ->
            salespersonAdapter = SalespersonAdapter(commerciauxList)
            view?.findViewById<RecyclerView>(R.id.vertical_recycler_view)?.adapter = salespersonAdapter
        } ?: run {
            Toast.makeText(context, "Impossible de charger les données.", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadAndSaveCommerciauxData() {
        lifecycleScope.launch {
            val xmlData = withContext(Dispatchers.IO) {
                // Votre logique de téléchargement ici
                FtpDownloadTask().downloadXmlFile(
                    "server.nap-agency.com", "ftpVital", "Kz5Jkud6GG", "/commerciaux.xml"
                )
            }
            DataRepository.commerciaux = DataRepository.parseXmlToCommerciaux(xmlData)
            database.dataCacheDao().insertData(DataCache(COMMERCIAUX_KEY, xmlData))
            updateUIWithCommerciauxData()
        }
    }
    */

}