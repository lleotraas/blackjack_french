package fr.lleotraas.blackjack_french.features_wallet.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet

interface WalletRepository {

    fun getWallet(id: Long): LiveData<Wallet>

    fun getAllWallet(): LiveData<List<Wallet>>

    suspend fun updateWallet(wallet: Wallet)

    suspend fun insertWallet(wallet: Wallet): Long

    suspend fun deleteWallet(id: Long): Int

}