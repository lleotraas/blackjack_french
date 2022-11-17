package fr.lleotraas.blackjack_french.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogInvitationToPlayBinding
import fr.lleotraas.blackjack_french.model.OnlineStatusType
import fr.lleotraas.blackjack_french.ui.activity.OnlineGameActivityViewModel

@AndroidEntryPoint
class OpponentQuitGameDialog : DialogFragment() {

    private lateinit var mBinding: DialogInvitationToPlayBinding
    private val mViewModel: OnlineGameActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogInvitationToPlayBinding.inflate(inflater, container, false)
        updateUiText()
        configureListeners()
        return mBinding.root
    }

    private fun updateUiText() {
        mBinding.apply {
            dialogInvitationToPlayTitleTv.text = requireContext().resources.getString(R.string.opponent_quit_game_dialog_title)
            dialogInvitationToPlayMessageTv.text = requireContext().resources.getString(R.string.opponent_quit_game_dialog_message)
            dialogInvitationToPlayPositiveBtn.text = requireContext().resources.getString(R.string.opponent_quit_game_dialog_positive_btn)
            dialogInvitationToPlayNegativeBtn.visibility = View.INVISIBLE
        }
    }

    private fun configureListeners() {
        mBinding.dialogInvitationToPlayPositiveBtn.setOnClickListener {
            mViewModel.updateOnlineStatus(mViewModel.getCurrentUser()?.uid.toString(), OnlineStatusType.ONLINE)
            requireActivity().finish()
            dismiss()
        }
    }
}