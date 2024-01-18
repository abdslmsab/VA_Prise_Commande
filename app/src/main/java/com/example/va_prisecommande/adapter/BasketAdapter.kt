package com.example.va_prisecommande.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.model.ArticlePourPanier

class BasketAdapter(private val articlesDansLePanier: List<ArticlePourPanier>) :
    RecyclerView.Adapter<BasketAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eanTextView: TextView = view.findViewById(R.id.ean)
        val productNameTextView: TextView = view.findViewById(R.id.product_name)
        val packagingTextView: TextView = view.findViewById(R.id.packaging_input)
        val quantityTextView: TextView = view.findViewById(R.id.quantity_input)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_vertical_panier, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = articlesDansLePanier.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articlesDansLePanier[position]
        holder.eanTextView.text = article.ean
        holder.productNameTextView.text = article.nom
        holder.packagingTextView.text = article.conditionnement
        holder.quantityTextView.text = article.quantite.toString()
    }
}
