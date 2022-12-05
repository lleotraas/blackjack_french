package fr.lleotraas.blackjack_french.features_wallet.use_case.wallet

import javax.inject.Inject


data class WalletUseCases @Inject constructor(
    val insertWallet: InsertWallet,
    val getWallet: GetWallet,
    val getWallets: GetWallets,
    val updateWallet: UpdateWallet,
    val deleteWallet: DeleteWallet
)
