package com.example.va_prisecommande.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.model.Commercial
import com.example.va_prisecommande.viewmodel.MainViewModel

class SalespersonAdapter(
    private val commerciaux: List<Commercial>
) : RecyclerView.Adapter<SalespersonAdapter.ViewHolder>() {
    var filteredSalespersons: List<Commercial> = commerciaux
    var selectedCommercial: Commercial? = null
        private set

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.person_item)
        val radioButton: RadioButton = view.findViewById(R.id.radio_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_vertical_salesperson, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredSalespersons.size

    fun filter(query: String) {
        filteredSalespersons = if (query.isEmpty()) {
            commerciaux
        } else {
            val lowerCaseQuery = query.lowercase()
            commerciaux.filter {
                it.nom.lowercase().contains(lowerCaseQuery) ||
                        it.prenom.lowercase().contains(lowerCaseQuery)
            }
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val commercial = filteredSalespersons[position]
        holder.textView.text = "${commercial.prenom} ${commercial.nom}"

        holder.radioButton.isChecked = commercial == selectedCommercial

        holder.radioButton.setOnClickListener {
            selectedCommercial = filteredSalespersons[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }
}
