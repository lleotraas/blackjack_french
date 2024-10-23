package fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.DialogTutorialBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivity
import fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game.OfflineGameFragment
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.MainScreenActivityViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BaseRuleDialog : DialogFragment() {

    private lateinit var binding : DialogTutorialBinding
    private val mViewModel: MainScreenActivityViewModel by viewModels()
    private var idToDelete: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTutorialBinding.inflate(inflater, container, false)
        mViewModel.getAllBank().observe(viewLifecycleOwner) { allWallet ->
            idToDelete = Utils.getTutorialNameId(allWallet)
        }
        configureListener()
        return binding.root
    }

    private fun configureListener() {
        binding.dialogTutorialBaseRuleBtn.setOnClickListener {
            lifecycleScope.launch {
                if (idToDelete != -1L) {
                    lifecycleScope.launch {
                        idToDelete?.let { id -> mViewModel.deleteBank(id) }
                        Log.i(BaseRuleDialog::class.simpleName, "configureListener: idToDelete N°$idToDelete deleted")
                    }
                }
                val playerId = mViewModel.insertBank(Wallet(0, 500.0001, OfflineGameFragment.TUTORIAL_NAME))
                beginTutorial(playerId)
                Log.i(BaseRuleDialog::class.simpleName, "configureListener: Tutorial player created with id N°$playerId")

            }

        }

        binding.dialogTutorialDoubleBtn.setOnClickListener {

        }

        binding.dialogTutorialSplitBtn.setOnClickListener {

        }

        binding.dialogTutorialInsuranceBtn.setOnClickListener {

        }
    }

    private fun beginTutorial(playerId: Long) {
        val intent = Intent(requireContext(), GameActivity::class.java)
        intent.putExtra(OfflineGameFragment.PLAYER_SAVE_ID, playerId)
        startActivity(intent)
        Log.i(BaseRuleDialog::class.simpleName, "beginTutorial: start GameActivity with playerId=$playerId")
    }

}