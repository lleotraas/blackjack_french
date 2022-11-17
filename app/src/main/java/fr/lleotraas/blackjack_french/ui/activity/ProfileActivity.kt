package fr.lleotraas.blackjack_french.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.ActivityProfileBinding

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.activityProfileToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = resources.getString(R.string.profile_activity_toolbar_title)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}