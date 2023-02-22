package fr.lleotraas.blackjack_french.features_offline_game.data.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.data.data_source.BankDao
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.WalletRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val bankDao: BankDao
    ) : WalletRepository {

    override fun getWallet(id: Long): LiveData<Wallet> {
        return bankDao.getBankById(id)
    }

    override fun getAllWallet(): LiveData<List<Wallet>> {
        return bankDao.getAllBank()
    }

    override suspend fun updateWallet(wallet: Wallet) {
        bankDao.updateBank(wallet)
    }

    override suspend fun insertWallet(wallet: Wallet): Long {
        return bankDao.insertBank(wallet)
    }

    override suspend fun deleteWallet(id: Long): Int {
        return bankDao.deleteBank(id)
    }

}