package fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.main_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentMainScreenBinding
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.LoadGameDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.NewGameDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.OnlineGameDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.RulesHelpDialog

@AndroidEntryPoint
class MainScreenFragment : Fragment(){

    private var _binding: FragmentMainScreenBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        configureListeners()
        return mBinding.root
    }

    private fun configureListeners() {
        mBinding.apply {

            fragmentMainScreenNewGameBtn.setOnClickListener {
                NewGameDialog().show(requireActivity().supportFragmentManager, NewGameDialog().tag)
            }

            fragmentMainScreenLoadGameBtn.setOnClickListener {
                LoadGameDialog().show(requireActivity().supportFragmentManager, LoadGameDialog().tag)
            }

            fragmentMainScreenPlayOnlineBtn.setOnClickListener {
                OnlineGameDialog().show(requireActivity().supportFragmentManager, OnlineGameDialog().tag)
            }

            fragmentMainScreenRulesBtn.setOnClickListener {
                RulesHelpDialog().show(requireActivity().supportFragmentManager, RulesHelpDialog().tag)
            }
        }
    }
}