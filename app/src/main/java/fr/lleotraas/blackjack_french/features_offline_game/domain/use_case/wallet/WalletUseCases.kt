package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.wallet

import javax.inject.Inject


data class WalletUseCases @Inject constructor(
    val insertWallet: InsertWallet,
    val getWallet: GetWallet,
    val getWallets: GetWallets,
    val updateWallet: UpdateWallet,
    val deleteWallet: DeleteWallet
)
