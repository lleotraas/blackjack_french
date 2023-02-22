package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.wallet

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.WalletRepository
import javax.inject.Inject

class GetWallet @Inject constructor(
    private val repository: WalletRepository
) {

    operator fun invoke(id: Long): LiveData<Wallet> {
        return repository.getWallet(id)
    }
}
