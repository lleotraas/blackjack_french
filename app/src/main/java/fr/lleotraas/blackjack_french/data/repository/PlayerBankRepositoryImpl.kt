package fr.lleotraas.blackjack_french.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.lleotraas.blackjack_french.database.dao.BankDao
import fr.lleotraas.blackjack_french.model.Bank
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerBankRepositoryImpl @Inject constructor(
    private val bankDao: BankDao
) {

    fun getBank(id: Long): LiveData<Bank> {
        return bankDao.getBankById(id)
    }

    fun getAllBank(): LiveData<List<Bank>> {
        return bankDao.getAllBank()
    }

    @Update
    suspend fun updateBank(bank: Bank) {
        bankDao.updateBank(bank)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBank(bank: Bank): Long {
        return bankDao.insertBank(bank)
    }

    @Query("DELETE FROM player_bank WHERE player_id = :id")
    suspend fun deleteBank(id: Long): Int {
        return bankDao.deleteBank(id)
    }

}