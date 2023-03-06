package fr.lleotraas.blackjack_french.features_offline_game.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.ActivityGameBinding

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureToolbar()
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.activityGameToolbar.toolbar)
    }
}