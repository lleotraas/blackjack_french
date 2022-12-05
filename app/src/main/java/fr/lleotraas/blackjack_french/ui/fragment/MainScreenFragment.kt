package fr.lleotraas.blackjack_french.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentMainScreenBinding
import fr.lleotraas.blackjack_french.features_wallet.presentation.LoadGameDialog
import fr.lleotraas.blackjack_french.features_wallet.presentation.NewGameDialog
import fr.lleotraas.blackjack_french.ui.dialog.OnlineGameDialog

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
                val newGameDialog = NewGameDialog()
                newGameDialog.show(requireActivity().supportFragmentManager, newGameDialog.tag)
            }

            fragmentMainScreenLoadGameBtn.setOnClickListener {
                val loadGameDialog = LoadGameDialog()
                loadGameDialog.show(requireActivity().supportFragmentManager, loadGameDialog.tag)
            }

            fragmentMainScreenPlayOnlineBtn.setOnClickListener {
                val onlineGameDialog = OnlineGameDialog()
                onlineGameDialog.show(requireActivity().supportFragmentManager, onlineGameDialog.tag)
            }
        }
    }
}