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
class QuitOnlineGameDialog : DialogFragment() {

    private lateinit var mBinding: DialogInvitationToPlayBinding
    private val mViewModel: OnlineGameActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogInvitationToPlayBinding.inflate(inflater, container, false)
        updateUiText()
        configureListener()
        return mBinding.root
    }

    private fun updateUiText() {
        mBinding.apply {
            dialogInvitationToPlayTitleTv.text = requireContext().resources.getString(R.string.online_game_activity_quit)
            dialogInvitationToPlayMessageTv.text = requireContext().resources.getString(R.string.online_game_activity_quit_message)
            dialogInvitationToPlayPositiveBtn.text = requireContext().resources.getString(R.string.online_game_activity_yes)
            dialogInvitationToPlayNegativeBtn.text = requireContext().resources.getString(R.string.online_game_activity_no)
        }
    }

    private fun configureListener() {
        mBinding.apply {
            dialogInvitationToPlayPositiveBtn.setOnClickListener {
                mViewModel.updateOnlineStatus(mViewModel.getCurrentUser()?.uid.toString(), OnlineStatusType.OFFLINE)
                requireActivity().finish()
                dismiss()
            }

            dialogInvitationToPlayNegativeBtn.setOnClickListener {
                dismiss()
            }
        }
    }

}