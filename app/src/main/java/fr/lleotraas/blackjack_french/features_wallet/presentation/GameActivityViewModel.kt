package fr.lleotraas.blackjack_french.features_wallet.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_wallet.use_case.wallet.WalletUseCases
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Bet
import fr.lleotraas.blackjack_french.features_wallet.use_case.bet.BetUseCases
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Named("GameActivityViewModel")
class GameActivityViewModel @Inject constructor (
    private val walletUseCases: WalletUseCases,
    private val betUseCases: BetUseCases
): ViewModel() {

    suspend fun updateBank(wallet: Wallet) {
        walletUseCases.updateWallet(wallet)
    }

    fun getBank(id: Long): LiveData<Wallet> {
        return walletUseCases.getWallet(id)
    }

    fun setBet(bet: Bet) {
        betUseCases.setBet(bet)
    }

    fun getBet(): LiveData<Bet> {
        return betUseCases.getBet()
    }
}