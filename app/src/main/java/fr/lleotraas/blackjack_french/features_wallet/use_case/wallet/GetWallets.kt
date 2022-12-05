package fr.lleotraas.blackjack_french.features_wallet.use_case.wallet

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetWallets @Inject constructor(
    private val repository: WalletRepository
) {

    operator fun invoke(): LiveData<List<Wallet>> {
        return repository.getAllWallet()
    }
}
