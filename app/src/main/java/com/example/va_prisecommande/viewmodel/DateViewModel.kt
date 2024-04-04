package com.example.va_prisecommande.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateViewModel : ViewModel() {

    private val _dateActuelle = MutableLiveData<String>()
    val dateActuelle: LiveData<String> = _dateActuelle

    fun setDateActuelle(date: String) {
        _dateActuelle.value = date
    }

    // Méthode de vérification de la date de durabilité minimale
    fun dateValide(dateString: String): Boolean {
        if (TextUtils.isEmpty(dateString)) {
            return false
        }

        // On divise la date en 3 parties dès qu'il y a un "/"
        val partiesDate = dateString.split("/")
        if (partiesDate.size != 3) {
            return false
        }

        try {
            val jour = partiesDate[0].toInt()
            val mois = partiesDate[1].toInt()
            val annee = partiesDate[2].toInt()

            // Vérification des limites pour jour, mois et année
            if (jour < 1 || jour > 31 || mois < 1 || mois > 12 || annee < 0 || annee > 99) {
                return false
            }

            // Vérification si la date est valide
            val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            dateFormatter.isLenient = false
            dateFormatter.parse(dateString)
        } catch (e: NumberFormatException) {
            return false
        } catch (e: ParseException) {
            return false
        }

        return true
    }
}
