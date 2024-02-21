package com.example.va_prisecommande.viewmodel

import Client
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.va_prisecommande.model.Commercial

class SharedViewModel : ViewModel() {
    val selectedCommercial = MutableLiveData<Commercial?>()
    val selectedClient = MutableLiveData<Client?>()

    // Fonctions pour mettre à jour les données
    fun selectCommercial(commercial: Commercial) {
        selectedCommercial.value = commercial
    }

    fun selectClient(client: Client) {
        selectedClient.value = client
    }
}
