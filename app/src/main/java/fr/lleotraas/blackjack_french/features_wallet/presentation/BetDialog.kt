package fr.lleotraas.blackjack_french.features_wallet.presentation

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
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Bet
import fr.lleotraas.blackjack_french.features_wallet.presentation.GameFragment.Companion.PLAYER_SAVE_ID
import fr.lleotraas.blackjack_french.utils.Utils

@AndroidEntryPoint
class BetDialog : BottomSheetDialogFragment() {

    private lateinit var mBinding: DialogBetBinding
    private val mViewModel: GameActivityViewModel by viewModels()
    private var playerBankId: Long? = null
    private var playerBankAmountMax: Double? = null
    private val bet = Bet(0.0,0.0,0.0,0.0,0.0, 0.0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogBetBinding.inflate(inflater, container, false)
        playerBankId = arguments?.get(PLAYER_SAVE_ID) as Long
        getPlayerBank()
        createPickerMinMaxValue(0)
        return mBinding.root
    }

    private fun getPlayerBank() {
        mViewModel.getBank(playerBankId!!).observe(viewLifecycleOwner) { currentBank ->
            mBinding.betDialogBetBankTv.text = String.format("%s", currentBank.amount)
            playerBankAmountMax = currentBank.amount
            updateUser(currentBank)
//            configureListeners(currentBank)
        }
    }

    private fun updateUser(wallet: Wallet) {
        mBinding.apply {
            betDialogBetBankTv.text = wallet.amount.toString()
            if (bet.playerBet > 0.0) {
                val betArray =
                    Utils.getArrayOfBetString(bet.playerBet.toString())
                val betTabTv = createBetTabTv()
                for (index in betArray.indices) {
                    betTabTv[Utils.getIndex(betArray.size, betTabTv.size, index)].value = betArray[index].toString().toInt()
                }
                betDialogBetBankTv.text = (wallet.amount.minus(bet())).toString()
            }

        }
        configureListeners(wallet)
//           setPickerMaxValue()

    }

    private fun configureListeners(wallet: Wallet) {
        mBinding.apply {
            dialogBetTenOfThousandsBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(wallet)
            }

            dialogBetThousandBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(wallet)
            }

            dialogBetHundredBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(wallet)
            }

            dialogBetDozensBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(wallet)
            }

            dialogBetUnityBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(wallet)
            }

            betDialogOkBtn.setOnClickListener {
                bet.playerBet = Utils.makeBet(
                    dialogBetUnityBet.value,
                    dialogBetDozensBet.value,
                    dialogBetHundredBet.value,
                    dialogBetThousandBet.value,
                    dialogBetTenOfThousandsBet.value
                )
                if (wallet.amount < bet.playerBet) {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
                } else {
                    setBet(bet)
                    dismiss()
                }
            }
        }
    }

    private fun updateTextView(wallet: Wallet) {
        mBinding.betDialogBetBankTv.text = ((wallet.amount) - bet()).toString()
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

    private fun createPickerMinMaxValue(startAtIndex: Int) {
        for (index in startAtIndex until createBetTabTv().size) {
            createBetTabTv()[index].minValue = 0
            createBetTabTv()[index].maxValue = 9
        }
    }

//    private fun showMoneyAmount(betTotal: Int) {
//        mBinding.betDialogBetBankTv.text = String.format("%s", playerBank!!.amount)
//    }

    private fun setBet(bet: Bet) {
        mViewModel.setBet(bet)
    }
}