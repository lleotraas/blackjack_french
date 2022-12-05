package fr.lleotraas.blackjack_french.features_wallet.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.lleotraas.blackjack_french.features_wallet.data.data_source.BankDao
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.WalletRepository
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
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

    @Update
    override suspend fun updateWallet(wallet: Wallet) {
        bankDao.updateBank(wallet)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insertWallet(wallet: Wallet): Long {
        return bankDao.insertBank(wallet)
    }

    @Query("DELETE FROM player_bank WHERE player_id = :id")
    override suspend fun deleteWallet(id: Long): Int {
        return bankDao.deleteBank(id)
    }

}