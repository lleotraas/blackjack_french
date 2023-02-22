package fr.lleotraas.blackjack_french.features_setting_menu.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentSettingMenuBinding

@AndroidEntryPoint
class SettingMenuFragment : Fragment() {

    private lateinit var mBinding: FragmentSettingMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingMenuBinding.inflate(inflater, container, false)
        configureListeners()
        return mBinding.root
    }

    private fun configureListeners() {

    }
}