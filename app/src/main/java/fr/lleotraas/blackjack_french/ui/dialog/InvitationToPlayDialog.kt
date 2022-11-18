package fr.lleotraas.blackjack_french.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogInvitationToPlayBinding
import fr.lleotraas.blackjack_french.model.Message
import fr.lleotraas.blackjack_french.model.OnlineStatusType
import fr.lleotraas.blackjack_french.ui.activity.DetailOnlineUserActivityViewModel
import fr.lleotraas.blackjack_french.ui.activity.OnlineGameActivity
import fr.lleotraas.blackjack_french.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.ONLINE_STATUS
import fr.lleotraas.blackjack_french.utils.Utils.Companion.PSEUDO
import fr.lleotraas.blackjack_french.utils.Utils.Companion.SEARCHED_USER_ID
import java.util.*

@AndroidEntryPoint
class InvitationToPlayDialog : DialogFragment() {

    private lateinit var mBinding: DialogInvitationToPlayBinding
    private val mViewModel: DetailOnlineUserActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogInvitationToPlayBinding.inflate(inflater, container, false)
        val currentUserId = requireArguments().get(CURRENT_USER_ID) as String
        val searchedUserId = requireArguments().get(SEARCHED_USER_ID) as String
        updateUI(searchedUserId)
        configureListeners(currentUserId, searchedUserId)
        return mBinding.root
    }

    private fun updateUI(searchedUserId: String) {
        mViewModel.getSearchedUser(searchedUserId).observe(viewLifecycleOwner) { searchedUserOnline ->
            mBinding.dialogInvitationToPlayMessageTv.text = String.format("%s %s", searchedUserOnline!!.pseudo, requireContext().resources.getString(R.string.invitation_to_play_dialog_message))

            mBinding.apply {
                dialogInvitationToPlayTitleTv.text = requireContext().resources.getString(R.string.dialog_invitation_to_play_title)
                dialogInvitationToPlayMessageTv.visibility = View.VISIBLE
                dialogInvitationToPlayPositiveBtn.visibility = View.VISIBLE
                dialogInvitationToPlayNegativeBtn.text = requireContext().resources.getString(R.string.bet_dialog_cancel)
            }

            if (searchedUserOnline.onlineStatus == OnlineStatusType.ONLINE) {
                mBinding.apply {
                    dialogInvitationToPlayTitleTv.text = requireContext().resources.getString(R.string.waiting_for_answer_dialog_opponent_cancel_request)
                    dialogInvitationToPlayMessageTv.visibility = View.GONE
                    dialogInvitationToPlayPositiveBtn.visibility = View.GONE
                    dialogInvitationToPlayNegativeBtn.text = requireContext().resources.getString(R.string.bet_dialog_ok)
                }
            }
        }
    }

    private fun configureListeners(currentUserId: String, searchedUserId: String) {

        mBinding.apply {
            dialogInvitationToPlayPositiveBtn.setOnClickListener {
                mViewModel.updateOnlineStatusPlaying(currentUserId, searchedUserId)
                mViewModel.sendMessage(searchedUserId, Message(
                    "123456",
                    "bot",
                    Calendar.getInstance().timeInMillis.toString(),
                    requireContext().resources.getString(R.string.online_game_fragment_welcome),
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_person_24).toString()
                ))
                val intent = Intent(requireActivity(), OnlineGameActivity::class.java)
                intent.putExtra(CURRENT_USER_ID, currentUserId)
                intent.putExtra(SEARCHED_USER_ID, searchedUserId)
                startActivity(intent)
                dismiss()
            }

            dialogInvitationToPlayNegativeBtn.setOnClickListener {
                mViewModel.updateOnlineStatusAskForPlay(currentUserId, OnlineStatusType.ONLINE)
                mViewModel.updateOnlineStatusAskForPlay(searchedUserId, OnlineStatusType.ONLINE)
                dismiss()
            }
        }

    }
}