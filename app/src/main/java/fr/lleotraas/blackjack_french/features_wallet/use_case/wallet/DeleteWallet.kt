package fr.lleotraas.blackjack_french.features_wallet.use_case.wallet

import fr.lleotraas.blackjack_french.features_wallet.domain.repository.WalletRepository
import javax.inject.Inject

class DeleteWallet @Inject constructor(
    private val repository: WalletRepository
) {

    suspend operator fun invoke(id: Long): Int {
        return repository.deleteWallet(id)
    }
}
