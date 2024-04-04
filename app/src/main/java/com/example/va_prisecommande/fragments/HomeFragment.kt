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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.va_prisecommande.R
import com.example.va_prisecommande.adapter.SalespersonAdapter
import com.example.va_prisecommande.dao.CommercialDao
import com.example.va_prisecommande.model.Commercial
import com.example.va_prisecommande.singleton.DataRepository
import com.example.va_prisecommande.singleton.DataRepository.downloadXml
import com.example.va_prisecommande.singleton.DataRepository.parseXmlToCommerciaux
import com.example.va_prisecommande.viewmodel.SharedViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var salespersonAdapter: SalespersonAdapter
    private lateinit var viewModel: SharedViewModel
    private var commercial: Commercial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        viewModel.selectedCommercial.observe(this) { selectedCommercial ->
            commercial = selectedCommercial
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

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
                viewModel.selectCommercial(selectedCommercial)
                val fragment = ClientFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
    /*
    lifecycleScope.launch {
            try {
                val contextNonNull = requireContext()
                DataRepository.initializeDatabase(contextNonNull)
                val localCommerciaux = getAllCommerciaux()

                if (localCommerciaux.isNullOrEmpty()) {
                    updateDataInBackground()
                } else {
                    salespersonAdapter.updateData(localCommerciaux)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lors de l'initialisation de la base de données", Toast.LENGTH_SHORT).show()
            }
        }

        private suspend fun updateDataInBackground() {
            // Télécharger les données depuis le serveur FTP et les parser
            val commerciauxXml = downloadXml("/commerciaux.xml")
            val commerciaux = parseXmlToCommerciaux(commerciauxXml ?: "")

            // Mettre à jour la base de données Room
            DataRepository.updateCommerciaux(commerciaux)
        }
     */
}