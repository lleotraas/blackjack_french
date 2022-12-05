package fr.lleotraas.blackjack_french.features_wallet.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogLoadGameBinding
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
import fr.lleotraas.blackjack_french.ui.activity.MainScreenActivityViewModel
import fr.lleotraas.blackjack_french.features_wallet.presentation.GameFragment.Companion.PLAYER_SAVE_ID
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoadGameDialog() : BottomSheetDialogFragment() {

    private lateinit var mBinding: DialogLoadGameBinding
    private val mViewModelScreen: MainScreenActivityViewModel by viewModels()
    private val mListOfWallet = ArrayList<Wallet>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogLoadGameBinding.inflate(inflater, container, false)
        setSaveName()
        configureListeners()
        return mBinding.root
    }

    private fun setSaveName() {
        mViewModelScreen.getAllBank().observe(viewLifecycleOwner) { listOfBank ->
            mListOfWallet.addAll(listOfBank)
            if (listOfBank.isNotEmpty()) {
                if (listOfBank.size == 1 || listOfBank.size > 1) {
                        mBinding.loadGameDialogFirstBtn.text = String.format("%s %s. %s%s", listOfBank[0].pseudo, requireContext().resources.getString(R.string.load_game_dialog_game_number), requireContext().resources.getString(R.string.load_game_dialog_bank_amout), listOfBank[0].amount)
                }
                if (listOfBank.size == 2 || listOfBank.size > 2) {
                        mBinding.loadGameDialogSecondBtn.text = String.format("%s %s. %s%s", listOfBank[1].pseudo, requireContext().resources.getString(R.string.load_game_dialog_game_number), requireContext().resources.getString(R.string.load_game_dialog_bank_amout), listOfBank[1].amount)
                } else {
                    mBinding.loadGameDialogDeleteSecondBtn.isEnabled = false
                }
                if (listOfBank.size == 3) {
                        mBinding.loadGameDialogThirdBtn.text = String.format("%s %s. %s%s", listOfBank[2].pseudo, requireContext().resources.getString(R.string.load_game_dialog_game_number), requireContext().resources.getString(R.string.load_game_dialog_bank_amout), listOfBank[2].amount)
                } else {
                    mBinding.loadGameDialogDeleteThirdBtn.isEnabled = false
                }
            } else {
                mBinding.loadGameDialogDeleteFirstBtn.isEnabled = false
                mBinding.loadGameDialogDeleteSecondBtn.isEnabled = false
                mBinding.loadGameDialogDeleteThirdBtn.isEnabled = false
            }
        }
    }

    private fun configureListeners() {
        mBinding.apply {
            loadGameDialogFirstBtn.setOnClickListener {
                if (loadGameDialogFirstBtn.text != requireContext().resources.getString(R.string.load_game_dialog_empty_slot)) {
                    loadGameSave(mListOfWallet[0].id)
                }
            }
            loadGameDialogSecondBtn.setOnClickListener {
                if (loadGameDialogSecondBtn.text != requireContext().resources.getString(R.string.load_game_dialog_empty_slot)) {
                    loadGameSave(mListOfWallet[1].id)
                }
            }
            loadGameDialogThirdBtn.setOnClickListener {
                if (loadGameDialogThirdBtn.text != requireContext().resources.getString(R.string.load_game_dialog_empty_slot)) {
                    loadGameSave(mListOfWallet[2].id)
                }
            }
            loadGameDialogDeleteFirstBtn.setOnClickListener {
                deleteGame(mListOfWallet[0].id)
                loadGameDialogFirstBtn.text = requireContext().resources.getString(R.string.load_game_dialog_empty_slot)
            }
            loadGameDialogDeleteSecondBtn.setOnClickListener {
                deleteGame(mListOfWallet[1].id)
                loadGameDialogSecondBtn.text = requireContext().resources.getString(R.string.load_game_dialog_empty_slot)
            }
            loadGameDialogDeleteThirdBtn.setOnClickListener {
                deleteGame(mListOfWallet[2].id)
                loadGameDialogThirdBtn.text = requireContext().resources.getString(R.string.load_game_dialog_empty_slot)
            }
        }
    }

    private fun loadGameSave(playerId: Long) {
        val intent = Intent(requireContext(), GameActivity::class.java)
        intent.putExtra(PLAYER_SAVE_ID, playerId)
        startActivity(intent)
        dismiss()
    }

    private fun deleteGame(id: Long) {
        lifecycleScope.launch {
            mViewModelScreen.deleteBank(id)
        }
    }
}