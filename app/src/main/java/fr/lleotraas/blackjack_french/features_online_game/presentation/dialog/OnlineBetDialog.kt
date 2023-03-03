package fr.lleotraas.blackjack_french.features_online_game.presentation.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogBetBinding
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User
import fr.lleotraas.blackjack_french.features_online_game.presentation.OnlineGameActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.MAIN_HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.getArrayOfBetString
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.getIndex
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.getInvertedTabIndex
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.makeBet
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.makeTotalBet

@AndroidEntryPoint
class OnlineBetDialog : BottomSheetDialogFragment() {
    private lateinit var mBinding: DialogBetBinding
    private val mViewModel: OnlineGameActivityViewModel by viewModels()
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogBetBinding.inflate(inflater, container, false)
        val currentUserId = requireArguments().get(CURRENT_USER_ID) as String
        updateUI(currentUserId)
        createPickerMinMaxValue(0)
        return mBinding.root
    }

    private fun updateUI(currentUserId: String) {
        getCurrentUser(currentUserId)
    }

    private fun configureListener(user: User) {
        mBinding.apply {

            dialogBetTenOfThousandsBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(user)
            }

            dialogBetThousandBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(user)
            }

            dialogBetHundredBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(user)
            }

            dialogBetDozensBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(user)
            }

            dialogBetUnityBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(user)
            }

            dialogBetNumberOfBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(user)
            }

            betDialogOkBtn.setOnClickListener {
                currentUser.bet!![MAIN_HAND] = makeBet(
                    dialogBetUnityBet.value,
                    dialogBetDozensBet.value,
                    dialogBetHundredBet.value,
                    dialogBetThousandBet.value,
                    dialogBetTenOfThousandsBet.value
                )
                if (user.wallet!! < user.bet!![MAIN_HAND]!!) {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
                } else {
                    mViewModel.updateOnlineUserBetAndWallet(currentUser)
                    dismiss()
                }
            }
        }
    }

    private fun updateTextView(user: User) {
        mBinding.betDialogBetBankTv.text = ((user.wallet ?: 0.0) - totalBet()).toString()
    }

    private fun getCurrentUser(currentUserId: String) {
        mViewModel.getOnlineUser(currentUserId).observe(viewLifecycleOwner, this::updateUser)
    }

    private fun updateUser(user: User?) {
        if (user != null) {
            currentUser = user
            mBinding.apply {
                betDialogBetBankTv.text = user.wallet.toString()
                if (user.bet!![MAIN_HAND]!! > 0.0) {
                    val betArray = getArrayOfBetString(user.bet!![MAIN_HAND].toString())
                    val betTabTv = createBetTabTv()
                    for (index in betArray.indices) {
                        betTabTv[getIndex(betArray.size, betTabTv.size, index)].value = betArray[index].toString().toInt()
                    }
                    betDialogBetBankTv.text = (user.wallet?.minus(bet())).toString()
                }

            }
            configureListener(user)
//            setPickerMaxValue()
        }
    }

    private fun bet() = makeBet(
        mBinding.dialogBetUnityBet.value,
        mBinding.dialogBetDozensBet.value,
        mBinding.dialogBetHundredBet.value,
        mBinding.dialogBetThousandBet.value,
        mBinding.dialogBetTenOfThousandsBet.value
    )

    private fun totalBet() = makeTotalBet(
        mBinding.dialogBetUnityBet.value,
        mBinding.dialogBetDozensBet.value,
        mBinding.dialogBetHundredBet.value,
        mBinding.dialogBetThousandBet.value,
        mBinding.dialogBetTenOfThousandsBet.value,
        mBinding.dialogBetNumberOfBet.value
    )

    private fun createBetTabTv() = listOf(
        mBinding.dialogBetTenOfThousandsBet,
        mBinding.dialogBetThousandBet,
        mBinding.dialogBetHundredBet,
        mBinding.dialogBetDozensBet,
        mBinding.dialogBetUnityBet
    )

    private fun createPickerMinMaxValue(startAtIndex: Int) {
        for (index in startAtIndex until createBetTabTv().size) {
            createBetTabTv()[index].minValue = 0
            createBetTabTv()[index].maxValue = 9
        }
    }

    private fun setPickerMaxValue() {
        val betArray = getArrayOfBetString(mBinding.betDialogBetBankTv.text.toString())
        for (index in betArray.indices) {
            createBetTabTv()[index].maxValue = betArray[index].toString().toInt()
            Log.e("OnlineDialogBet","createPickerMinMaxValue: index:$index, inverted index: ${betArray[getInvertedTabIndex(betArray.size, index)].toString().toInt()} picker max value:${createBetTabTv()[index].maxValue}" )
        }
    }
}