package fr.lleotraas.blackjack_french.features_offline_game.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.ActivityGameBinding

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }
}