package fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.DialogBetBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser

@AndroidEntryPoint
class BetDialog : BottomSheetDialogFragment() {

    private lateinit var mBinding: DialogBetBinding
    private val viewModel: GameActivityViewModel by viewModels()
    private var playerBankAmountMax: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogBetBinding.inflate(inflater, container, false)
        getPlayerBank()
        createPickerMinMaxValue()
        createNumberOfBetPickerValue()
        return mBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    private fun getPlayerBank() {
        viewModel.getOfflineUser().observe(viewLifecycleOwner) { offlineUser ->
            mBinding.betDialogBetBankTv.text = String.format("%s", offlineUser.wallet?.amount)
            playerBankAmountMax = offlineUser.wallet?.amount
            updateUser(offlineUser)
        }
    }

    private fun updateUser(offlineUser: OfflineUser) {
        mBinding.apply {
            betDialogBetBankTv.text = offlineUser.wallet?.amount.toString()
            if ((offlineUser.bet[offlineUser.playerIndex][Utils.MAIN_HAND])!! > 0.0) {
                val betArray =
                    Utils.getArrayOfBetString(offlineUser.bet[offlineUser.playerIndex][Utils.MAIN_HAND].toString())
                val betTabTv = createBetTabTv()
                for (index in betArray.indices) {
                    betTabTv[Utils.getIndex(betArray.size, betTabTv.size, index)].value = betArray[index].toString().toInt()
                }
                betDialogBetBankTv.text = (offlineUser.wallet?.amount?.minus(bet())).toString()
            }
        }
        configureListeners(offlineUser)
//           setPickerMaxValue()

    }

    private fun configureListeners(offlineUser: OfflineUser) {
        mBinding.apply {
            dialogBetTenOfThousandsBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(offlineUser.wallet)
            }

            dialogBetThousandBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(offlineUser.wallet)
            }

            dialogBetHundredBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(offlineUser.wallet)
            }

            dialogBetDozensBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(offlineUser.wallet)
            }

            dialogBetUnityBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(offlineUser.wallet)
            }

            betDialogOkBtn.setOnClickListener {
                offlineUser.player = Utils.createArrayListOfPlayerHand(dialogBetNumberOfBet.value)
                offlineUser.bet = Utils.createArrayListOfBet(dialogBetNumberOfBet.value)
                for (i in 0 until dialogBetNumberOfBet.value) {
                    offlineUser.bet[i][Utils.MAIN_HAND] = Utils.makeBet(
                        dialogBetUnityBet.value,
                        dialogBetDozensBet.value,
                        dialogBetHundredBet.value,
                        dialogBetThousandBet.value,
                        dialogBetTenOfThousandsBet.value
                    )
                }
                if(offlineUser.wallet?.amount != null) {
                    if (offlineUser.wallet.amount < (offlineUser.bet[offlineUser.playerIndex][Utils.MAIN_HAND] ?: 0.0)) {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.bet_dialog_not_enough_money),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        setBet(offlineUser)
                        dismiss()
                    }
                }
            }
        }
    }

    private fun updateTextView(wallet: Wallet?) {
        mBinding.betDialogBetBankTv.text = ((wallet?.amount)?.minus(bet()).toString())
    }

    private fun bet() = Utils.makeBet(
        mBinding.dialogBetUnityBet.value,
        mBinding.dialogBetDozensBet.value,
        mBinding.dialogBetHundredBet.value,
        mBinding.dialogBetThousandBet.value,
        mBinding.dialogBetTenOfThousandsBet.value
    )

    private fun createBetTabTv() = listOf(
        mBinding.dialogBetTenOfThousandsBet,
        mBinding.dialogBetThousandBet,
        mBinding.dialogBetHundredBet,
        mBinding.dialogBetDozensBet,
        mBinding.dialogBetUnityBet
    )

    private fun createPickerMinMaxValue() {
        for (index in 0 until createBetTabTv().size) {
            createBetTabTv()[index].minValue = 0
            createBetTabTv()[index].maxValue = 9
        }
    }

    private fun createNumberOfBetPickerValue() {
        mBinding.apply {
            dialogBetNumberOfBet.minValue = 1
            dialogBetNumberOfBet.maxValue = 7
        }
    }

//    private fun showMoneyAmount(betTotal: Int) {
//        mBinding.betDialogBetBankTv.text = String.format("%s", playerBank!!.amount)
//    }

    private fun setBet(offlineUser: OfflineUser) {
        viewModel.updateOfflineUser(offlineUser)
    }
}