package fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.HelpDialogBinding

class DealerHelpDialog : DialogFragment() {

    private lateinit var binding: HelpDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HelpDialogBinding.inflate(inflater, container, false)
        updateUi()
        configureListener()
        return binding.root
    }

    private fun updateUi() {
        binding.apply {
            helpDialogTitleTv.text = requireContext().resources.getString(R.string.dealer_help_dialog_title)
            helpDialogMessageTv.text = requireContext().resources.getString(R.string.dealer_help_dialog_message)
            helpDialogContextualImg.background = ContextCompat.getDrawable(requireContext(), R.drawable.dealer_score)
        }
    }

    private fun configureListener() {
        binding.helpDialogPositiveBtn.setOnClickListener {
            dismiss()
        }
    }
}