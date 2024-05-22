package com.example.va_prisecommande.fragments

import AppDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.va_prisecommande.R
import com.example.va_prisecommande.adapter.SalespersonAdapter
import com.example.va_prisecommande.dao.CommercialDao
import com.example.va_prisecommande.model.Commercial
import com.example.va_prisecommande.singleton.DataRepository
import com.example.va_prisecommande.viewmodel.SharedViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var salespersonAdapter: SalespersonAdapter
    private lateinit var viewModel: SharedViewModel
    private var commercial: Commercial? = null
    private lateinit var database: AppDatabase
    private lateinit var commercialDao: CommercialDao

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
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val verticalRecyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                DataRepository.loadAllData()
                val commerciaux = DataRepository.commerciaux

                if (commerciaux != null) {
                    salespersonAdapter = SalespersonAdapter(commerciaux)
                    verticalRecyclerView.adapter = salespersonAdapter
                } else {
                    Toast.makeText(context, "Impossible de charger les données des commerciaux", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erreur lors du chargement des données", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }

        if (!initializeDatabase()) {
            Toast.makeText(context, "Erreur lors de l'initialisation de la base de données", Toast.LENGTH_LONG).show()
        }

        lifecycleScope.launch {
            try {
                val commerciaux: List<Commercial> = commercialDao.getAll()

                if (commerciaux.isEmpty()) {
                    DataRepository.loadAllData()
                    salespersonAdapter = DataRepository.commerciaux?.let { SalespersonAdapter(it) }!!
                    verticalRecyclerView.adapter = salespersonAdapter
                    progressBar.visibility = View.GONE
                } else {
                    salespersonAdapter = SalespersonAdapter(commerciaux)
                    verticalRecyclerView.adapter = salespersonAdapter
                    progressBar.visibility = View.GONE
                }

                DataRepository.loadAllData()

                DataRepository.commerciaux?.let { commerciauxList ->
                    salespersonAdapter = SalespersonAdapter(commerciauxList)
                    verticalRecyclerView.adapter = salespersonAdapter
                } ?: run {
                    Toast.makeText(context, "Impossible de charger la liste mise à jour", Toast.LENGTH_LONG).show()
                }

                DataRepository.commerciaux?.let { commercialDao.insertAll(it)}
            } catch (e : Exception) {
                Log.e("Data update", "Failed to load data from the database", e)

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
                        progressBar.visibility = View.GONE
                    } ?: run {
                        Toast.makeText(context, "Impossible de charger les données.", Toast.LENGTH_LONG).show()
                    }
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

    private fun initializeDatabase(): Boolean {
        val context = activity ?: return false
        Log.d("Database Init", "Starting database initialization...")
        try {
            AppDatabase.initialiser(context)
            database = AppDatabase.getInstance()
            commercialDao = database.commercialDao()
            Log.d("Database Init", "Database initialized successfully")
            return true
        } catch (e: Exception) {
            Log.e("Database Initialization Error", "Error initializing database", e)
            return false
        }
    }
}