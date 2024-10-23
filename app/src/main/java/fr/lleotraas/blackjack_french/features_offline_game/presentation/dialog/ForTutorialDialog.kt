package fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import fr.lleotraas.blackjack_french.databinding.DialogForTutorialBinding

class ForTutorialDialog(private val title: String, private val message: String) : DialogFragment() {

    private lateinit var binding: DialogForTutorialBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogForTutorialBinding.inflate(inflater, container, false)
        updateUi()
        return binding.root
    }

    private fun updateUi() {
        binding.apply {
            dialogForTutorialTitleTv.text = title
            dialogForTutorialMessageTv.text = message
        }
    }

    private fun configureListener() {
        binding.dialogForTutorialPositiveBtn.setOnClickListener {
            dismiss()
        }
    }

}