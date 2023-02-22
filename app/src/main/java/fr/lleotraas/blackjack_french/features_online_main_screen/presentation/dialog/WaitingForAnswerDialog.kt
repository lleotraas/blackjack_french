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
import fr.lleotraas.blackjack_french.databinding.DialogWaitingForAnswerBinding
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_detail.presentation.DetailOnlineUserActivityViewModel
import fr.lleotraas.blackjack_french.features_online_game.presentation.OnlineGameActivity
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.SEARCHED_USER_ID

@AndroidEntryPoint
class WaitingForAnswerDialog : DialogFragment() {

    private lateinit var mBinding: DialogWaitingForAnswerBinding
    private val mViewModel: DetailOnlineUserActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogWaitingForAnswerBinding.inflate(inflater, container, false)
        val currentUserId = requireArguments().get(CURRENT_USER_ID) as String
        val searchedUserId = requireArguments().get(SEARCHED_USER_ID) as String
        configureListeners(currentUserId)
        waitingOpponentAnswer(currentUserId, searchedUserId)
        return mBinding.root
    }

    private fun waitingOpponentAnswer(currentUserid: String, searchedUserId: String) {
        val intent = Intent(requireActivity(), OnlineGameActivity::class.java)
        mViewModel.getOnlineUser(currentUserid).observe(viewLifecycleOwner) { currentUserOnline ->
            if (currentUserOnline?.onlineStatus == OnlineStatusType.PLAYING) {
                intent.putExtra(CURRENT_USER_ID, currentUserid)
                intent.putExtra(SEARCHED_USER_ID, searchedUserId)
                startActivity(intent)
                dismiss()
            }

            if (currentUserOnline?.onlineStatus == OnlineStatusType.ONLINE) {
                mBinding.apply {
                    dialogWaitingForAnswerTitle.text = requireContext().resources.getString(R.string.waiting_for_answer_dialog_opponent_cancel_request)
                    dialogWaitingForAnswerNegativeBtn.text = requireContext().resources.getString(R.string.bet_dialog_ok)
                    dialogWaitingForAnswerProgressBar.visibility = View.GONE
                }
            }

            if (currentUserOnline?.onlineStatus == OnlineStatusType.WAITING_ANSWER) {
                mBinding.apply {
                    dialogWaitingForAnswerTitle.text = requireContext().resources.getString(R.string.dialog_waiting_for_answer_title)
                    dialogWaitingForAnswerNegativeBtn.text = requireContext().resources.getString(R.string.bet_dialog_cancel)
                    dialogWaitingForAnswerProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun configureListeners(currentUserid: String) {
        mBinding.dialogWaitingForAnswerNegativeBtn.setOnClickListener {
            mViewModel.updateOnlineStatusAskForPlay(currentUserid, OnlineStatusType.ONLINE)
            mViewModel.updateUserIsGameHost(currentUserid, false)
            dismiss()
        }
    }
}