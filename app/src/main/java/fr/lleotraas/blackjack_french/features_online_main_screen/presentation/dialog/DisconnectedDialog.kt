package fr.lleotraas.blackjack_french.features_online_main_screen.presentation.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogInvitationToPlayBinding
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.MainScreenActivity
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.OnlineMainScreenActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.CURRENT_USER_ID

@AndroidEntryPoint
class DisconnectedDialog : DialogFragment(){

    private lateinit var mBinding: DialogInvitationToPlayBinding
    private val mViewModel: OnlineMainScreenActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogInvitationToPlayBinding.inflate(inflater, container, false)
        val currentUserId = requireArguments().get(CURRENT_USER_ID) as String
        updateUi(currentUserId)
        configureListeners(currentUserId)
        return mBinding.root
    }

    private fun updateUi(currentUserId: String) {
        mBinding.apply {
            dialogInvitationToPlayTitleTv.text = requireContext().resources.getString(R.string.disconnected_dialog_title)
            dialogInvitationToPlayMessageTv.text = requireContext().resources.getString(R.string.disconnected_dialog_message)
        }
    }

    private fun configureListeners(currentUserId: String) {
        mBinding.apply {
            dialogInvitationToPlayPositiveBtn.setOnClickListener {
                mViewModel.updateOnlineUserStatus(currentUserId, OnlineStatusType.ONLINE)
                dismiss()
            }
            dialogInvitationToPlayNegativeBtn.setOnClickListener {
                mViewModel.updateOnlineUserStatus(currentUserId, OnlineStatusType.OFFLINE)
                requireActivity().startActivity(Intent(requireContext(), MainScreenActivity::class.java))
                dismiss()
            }
        }
    }
}