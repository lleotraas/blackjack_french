package fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogHelpBinding

class RulesHelpDialog : DialogFragment() {

    private lateinit var binding: DialogHelpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogHelpBinding.inflate(inflater, container, false)
        updateUi()
        configureListener()
        return binding.root
    }

    private fun updateUi() {
        binding.apply {
            helpDialogTitleTv.text = requireContext().resources.getString(R.string.activity_main_screen_rules)
            helpDialogMessageTv.text = requireContext().resources.getString(R.string.game_rules)
//            helpDialogContextualImg.background = ContextCompat.getDrawable(requireContext(), R.drawable.insurance)
        }
    }

    private fun configureListener() {
        binding.helpDialogPositiveBtn.setOnClickListener {
            dismiss()
        }
    }
}