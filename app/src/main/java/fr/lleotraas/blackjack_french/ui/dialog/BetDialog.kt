package fr.lleotraas.blackjack_french.ui.dialog

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
import fr.lleotraas.blackjack_french.model.Bank
import fr.lleotraas.blackjack_french.model.Bet
import fr.lleotraas.blackjack_french.ui.activity.GameActivityViewModel
import fr.lleotraas.blackjack_french.ui.fragment.GameFragment.Companion.PLAYER_SAVE_ID
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

    private fun updateUser(bank: Bank) {
        mBinding.apply {
            betDialogBetBankTv.text = bank.amount.toString()
            if (bet.playerBet > 0.0) {
                val betArray =
                    Utils.getArrayOfBetString(bet.playerBet.toString())
                val betTabTv = createBetTabTv()
                for (index in betArray.indices) {
                    betTabTv[Utils.getIndex(betArray.size, betTabTv.size, index)].value = betArray[index].toString().toInt()
                }
                betDialogBetBankTv.text = (bank.amount.minus(bet())).toString()
            }

        }
        configureListeners(bank)
//           setPickerMaxValue()

    }

    private fun configureListeners(bank: Bank) {
        mBinding.apply {
            dialogBetTenOfThousandsBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(bank)
            }

            dialogBetThousandBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(bank)
            }

            dialogBetHundredBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(bank)
            }

            dialogBetDozensBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(bank)
            }

            dialogBetUnityBet.setOnValueChangedListener { _, _, _ ->
                updateTextView(bank)
            }

            betDialogOkBtn.setOnClickListener {
                bet.playerBet = Utils.makeBet(
                    dialogBetUnityBet.value,
                    dialogBetDozensBet.value,
                    dialogBetHundredBet.value,
                    dialogBetThousandBet.value,
                    dialogBetTenOfThousandsBet.value
                )
                if (bank.amount < bet.playerBet) {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
                } else {
                    setBet(bet)
                    dismiss()
                }
            }
        }
    }

    private fun updateTextView(bank: Bank) {
        mBinding.betDialogBetBankTv.text = ((bank.amount) - bet()).toString()
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