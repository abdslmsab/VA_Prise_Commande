package com.example.va_prisecommande.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.va_prisecommande.R
import com.example.va_prisecommande.databinding.ActivityMainBinding
import com.example.va_prisecommande.fragments.HomeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this

        setContentView(binding.root)

        //Injection du fragment dans la boîte (fragment_container)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, HomeFragment())
        transaction.commit()
    }
}