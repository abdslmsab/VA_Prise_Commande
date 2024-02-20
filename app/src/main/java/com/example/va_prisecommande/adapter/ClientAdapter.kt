package com.example.va_prisecommande.adapter

import Client
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R

class ClientAdapter(private var clients: List<Client>) : RecyclerView.Adapter<ClientAdapter.ViewHolder>() {
    var filteredClients: List<Client> = clients
    var selectedClient: Client? = null
        private set

    //Boîte pour ranger tous les composants à contrôler
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val radioButton = view.findViewById<RadioButton>(R.id.radio_button)
        val codeClientItem: TextView = view.findViewById(R.id.code_client_item)
        val nameClientItem: TextView = view.findViewById(R.id.name_client_item)
        val addressClientItem: TextView = view.findViewById(R.id.address_client_item)
        val zipClientItem: TextView = view.findViewById(R.id.zip_client_item)
        val cityClientItem: TextView = view.findViewById(R.id.city_client_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_vertical_client, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredClients.size
    }

    fun filter(query: String) {
        filteredClients = if (query.isEmpty()) {
            clients
        } else {
            val lowerCaseQuery = query.lowercase()
            clients.filter {
                it.code.lowercase().contains(lowerCaseQuery) ||
                        it.nom.lowercase().contains(lowerCaseQuery) ||
                        it.adresse.lowercase().contains(lowerCaseQuery) ||
                        it.code_postal.lowercase().contains(lowerCaseQuery) ||
                        it.ville.lowercase().contains(lowerCaseQuery)
            }
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val client = filteredClients[position]
        holder.codeClientItem.text = "${client.code}"
        holder.nameClientItem.text = "${client.nom}"
        holder.addressClientItem.text = "${client.adresse}"
        holder.zipClientItem.text = "${client.code_postal}"
        holder.cityClientItem.text = "${client.ville}"

        holder.radioButton.isChecked = client == selectedClient

        holder.radioButton.setOnClickListener {
            selectedClient = filteredClients[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }
}