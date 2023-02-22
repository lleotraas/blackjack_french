package fr.lleotraas.blackjack_french.features_offline_game.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet

interface WalletRepository {

    fun getWallet(id: Long): LiveData<Wallet>

    fun getAllWallet(): LiveData<List<Wallet>>

    suspend fun updateWallet(wallet: Wallet)

    suspend fun insertWallet(wallet: Wallet): Long

    suspend fun deleteWallet(id: Long): Int

}