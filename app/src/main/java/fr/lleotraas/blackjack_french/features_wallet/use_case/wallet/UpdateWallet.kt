package fr.lleotraas.blackjack_french.features_wallet.use_case.wallet

import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.WalletRepository
import javax.inject.Inject

class UpdateWallet @Inject constructor(
    private val repository: WalletRepository
) {

    suspend operator fun invoke(wallet: Wallet) {
        repository.updateWallet(wallet)
    }
}
