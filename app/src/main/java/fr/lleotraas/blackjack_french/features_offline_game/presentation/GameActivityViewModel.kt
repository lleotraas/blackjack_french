package fr.lleotraas.blackjack_french.features_offline_game.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.wallet.WalletUseCases
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet.BetUseCases
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user.OfflineUserUseCases
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Named("GameActivityViewModel")
class GameActivityViewModel @Inject constructor (
    private val walletUseCases: WalletUseCases,
    private val betUseCases: BetUseCases,
    private val offlineUserUseCases: OfflineUserUseCases
): ViewModel() {

    // WALLET
    suspend fun updateBank(wallet: Wallet) {
        walletUseCases.updateWallet(wallet)
    }

    fun getBank(id: Long): LiveData<Wallet> = walletUseCases.getWallet(id)

    // BET
    fun setBet(bet: HashMap<String, Double>) {
        betUseCases.setBet(bet)
    }

    fun getBet(): LiveData<HashMap<String, Double>> {
        return betUseCases.getBet()
    }

    // OFFLINE USER
    fun updateOfflineUser(newOfflineUser: OfflineUser) {
        offlineUserUseCases.updateOfflineUser(newOfflineUser)
    }

    fun getOfflineUser(): LiveData<OfflineUser> = offlineUserUseCases.getOfflineUser()
}