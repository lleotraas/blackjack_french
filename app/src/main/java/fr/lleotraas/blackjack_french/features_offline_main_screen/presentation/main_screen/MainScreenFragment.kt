package fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.main_screen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentMainScreenBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.MainScreenActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.BaseRuleDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.LoadGameDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.NewGameDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.OnlineGameDialog
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog.RulesHelpDialog
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@AndroidEntryPoint
class MainScreenFragment : Fragment(){

    private var _binding: FragmentMainScreenBinding? = null
    private val mBinding get() = _binding!!
    private val mViewmodel: MainScreenActivityViewModel by viewModels()
    private var idToDelete: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        mViewmodel.getAllBank().observe(viewLifecycleOwner) { wallets ->
            idToDelete = Utils.getTutorialNameId(wallets)
        }
        configureListeners()
        return mBinding.root
    }

    private fun configureListeners() {
        mBinding.apply {

            fragmentMainScreenNewGameBtn.setOnClickListener {
                NewGameDialog().show(requireActivity().supportFragmentManager, NewGameDialog().tag)
            }

            fragmentMainScreenLoadGameBtn.setOnClickListener {
                    if (idToDelete != -1L) {
                        lifecycleScope.launch {
                            idToDelete?.let { id -> mViewmodel.deleteBank(id) }
                            Log.i(MainScreenFragment::class.simpleName, "onCreateView: idToDelete N°$idToDelete deleted")
                        }
                    }
                    LoadGameDialog().show(requireActivity().supportFragmentManager, LoadGameDialog().tag)

            }

            fragmentMainScreenPlayOnlineBtn.setOnClickListener {
                OnlineGameDialog().show(requireActivity().supportFragmentManager, OnlineGameDialog().tag)
            }

            fragmentMainScreenTutorialBtn.setOnClickListener {
//                RulesHelpDialog().show(requireActivity().supportFragmentManager, RulesHelpDialog().tag)
                BaseRuleDialog().show(requireActivity().supportFragmentManager, BaseRuleDialog().tag)
            }
        }
    }
}