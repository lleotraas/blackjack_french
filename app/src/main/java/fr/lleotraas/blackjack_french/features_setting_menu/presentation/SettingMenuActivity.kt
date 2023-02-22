package fr.lleotraas.blackjack_french.features_setting_menu.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.ActivitySettingMenuBinding

@AndroidEntryPoint
class SettingMenuActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySettingMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySettingMenuBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.activitySettingMenuToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = resources.getString(R.string.setting_menu_activity_toolbar_title)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}