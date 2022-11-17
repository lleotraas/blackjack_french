package fr.lleotraas.blackjack_french.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogNewGameBinding
import fr.lleotraas.blackjack_french.model.Bank
import fr.lleotraas.blackjack_french.ui.activity.GameActivity
import fr.lleotraas.blackjack_french.ui.activity.MainScreenActivityViewModel
import fr.lleotraas.blackjack_french.ui.fragment.GameFragment.Companion.PLAYER_SAVE_ID
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewGameDialog : DialogFragment() {

    private lateinit var mBinding: DialogNewGameBinding
    private val mViewModelScreen: MainScreenActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogNewGameBinding.inflate(inflater, container, false)
        isSaveSlotFree()
        configureListeners()
        return mBinding.root
    }

    private fun isSaveSlotFree() {
        mViewModelScreen.getAllBank().observe(viewLifecycleOwner) { listOfBank ->
            if (listOfBank.size == 3) {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.new_game_dialog_delete_save), Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun configureListeners() {
        mBinding.apply {
            newGameDialogPositiveBtn.setOnClickListener {
                if (newGameDialogEnterName.text!!.isNotEmpty()) {
                    lifecycleScope.launch {
                        val playerId = mViewModelScreen.insertBank(Bank(0, 500.0, newGameDialogEnterName.text.toString()))
                        beginNewGame(playerId)
                        dismiss()
                    }
                }
            }

            newGameDialogNegativeBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun beginNewGame(playerId: Long) {
        val intent = Intent(requireContext(), GameActivity::class.java)
        intent.putExtra(PLAYER_SAVE_ID, playerId)
        startActivity(intent)
    }
}