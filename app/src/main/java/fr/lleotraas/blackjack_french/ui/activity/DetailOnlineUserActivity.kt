package fr.lleotraas.blackjack_french.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.ActivityDetailOnlineUserBinding
import fr.lleotraas.blackjack_french.databinding.ActivityOnlineMainScreenBinding

@AndroidEntryPoint
class DetailOnlineUserActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailOnlineUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailOnlineUserBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }
}