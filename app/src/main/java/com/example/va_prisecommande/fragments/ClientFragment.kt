package com.example.va_prisecommande.fragments

import Client
import DocumentTypeDialogFragment
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
import com.example.va_prisecommande.adapter.ClientAdapter
import com.example.va_prisecommande.adapter.SalespersonAdapter
import com.example.va_prisecommande.ftp.FtpDownloadTask
import com.example.va_prisecommande.model.Commercial
import com.example.va_prisecommande.singleton.DataRepository
import com.example.va_prisecommande.viewmodel.ClientViewModel
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

private const val PARAM_COMMERCIAL = "commercial"

class ClientFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(commercial: Commercial) =
            ClientFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_COMMERCIAL, commercial);
                }
            }
    }

    private lateinit var commercial: Commercial

    private lateinit var clientAdapter: ClientAdapter
    private lateinit var viewModel: ClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ClientViewModel::class.java)
        arguments?.let {
            commercial = it.getParcelable(PARAM_COMMERCIAL)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.fragment_client, container, false)

        // Initialisation de l'adaptateur avec une liste vide
        clientAdapter = ClientAdapter(emptyList())

        //Récupération du RecyclerView
        val verticalRecyclerView = view?.findViewById<RecyclerView>(R.id.vertical_recycler_view)
        if (verticalRecyclerView != null) {
            verticalRecyclerView.adapter = clientAdapter
        }

        lifecycleScope.launch {
            if (DataRepository.clients == null) {
                DataRepository.loadAllData()
            }

            DataRepository.clients?.let { clientsList ->
                clientAdapter = ClientAdapter(clientsList)
                verticalRecyclerView?.adapter = clientAdapter
            } ?: run {
                Toast.makeText(
                    context,
                    "Impossible de charger les données des clients.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val searchInput =
            view?.findViewById<TextInputLayout>(R.id.search_input)?.editText as TextInputEditText

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                clientAdapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Pas besoin d'implémentation ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pas besoin d'implémentation ici
            }
        })

        // Ajout de l'écouteur de clic au bouton
        view?.findViewById<Button>(R.id.right_button)?.setOnClickListener {
            // Vérifiez si un commercial est sélectionné
            var isSelected = false
            for (i in 0 until clientAdapter.itemCount) {
                val viewHolder =
                    verticalRecyclerView?.findViewHolderForAdapterPosition(i) as? ClientAdapter.ViewHolder
                if (viewHolder?.radioButton?.isChecked == true) {
                    isSelected = true
                    break
                }
            }

            // S'il n'y a pas de commercial sélectionné, affichez un message d'erreur
            if (clientAdapter.selectedClient == null) {
                Toast.makeText(activity, "Veuillez sélectionner un client", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Sinon, continuez avec l'affichage de la popup
                showDocumentTypeDialog()
            }
        }

        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun showDocumentTypeDialog() {
        val dialog = DocumentTypeDialogFragment()
        dialog.show(parentFragmentManager, "DocumentTypeDialogFragment")
    }
}