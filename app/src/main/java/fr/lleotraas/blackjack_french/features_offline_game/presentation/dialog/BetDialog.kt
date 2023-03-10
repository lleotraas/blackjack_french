package fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog

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
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivityViewModel

@AndroidEntryPoint
class BetDialog : BottomSheetDialogFragment() {

    private lateinit var mBinding: DialogBetBinding
    private val viewModel: GameActivityViewModel by viewModels()

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

    private fun getPlayerBank() {
        viewModel.getOfflineUser().observe(viewLifecycleOwner) { offlineUser ->
            mBinding.betDialogBetBankTv.text = String.format("%s", offlineUser.wallet?.amount)
            updateUser(offlineUser)
        }
    }

    private fun updateUser(offlineUser: OfflineUser) {
        mBinding.apply {
            betDialogBetBankTv.text = offlineUser.wallet?.amount.toString()
            val currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
            if (offlineUser.defaultBet > 0.0) {
                val betArray = Utils.getArrayOfBetString(currentPlayer.bet.toString())
                val betTabTv = createBetTabTv()
                for (index in betArray.indices) {
                    betTabTv[Utils.getIndex(betArray.size, betTabTv.size, index)].value = betArray[index].toString().toInt()
                }
                dialogBetNumberOfBet.value = offlineUser.playerCount
                betDialogBetBankTv.text = (offlineUser.wallet!!.amount - bet() * offlineUser.playerCount).toString()
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

            dialogBetNumberOfBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(offlineUser.wallet)
            }

            betDialogOkBtn.setOnClickListener {
                    offlineUser.defaultBet = Utils.makeBet(
                        dialogBetUnityBet.value,
                        dialogBetDozensBet.value,
                        dialogBetHundredBet.value,
                        dialogBetThousandBet.value,
                        dialogBetTenOfThousandsBet.value

                    )
                offlineUser.player = Utils.createCustomPlayerList(dialogBetNumberOfBet.value, offlineUser.defaultBet)
                offlineUser.playerCount = dialogBetNumberOfBet.value
                if(offlineUser.wallet?.amount != null) {
                    if (offlineUser.wallet.amount < offlineUser.defaultBet) {
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
        mBinding.betDialogBetBankTv.text = ((wallet?.amount)?.minus(totalBet()).toString())
    }

    private fun bet() = Utils.makeBet(
        mBinding.dialogBetUnityBet.value,
        mBinding.dialogBetDozensBet.value,
        mBinding.dialogBetHundredBet.value,
        mBinding.dialogBetThousandBet.value,
        mBinding.dialogBetTenOfThousandsBet.value
    )

    private fun totalBet() = Utils.makeTotalBet(
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