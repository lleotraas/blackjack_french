package fr.lleotraas.blackjack_french.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fr.lleotraas.blackjack_french.model.Bank
import kotlinx.coroutines.flow.Flow


@Dao
interface BankDao{

    @Query("SELECT * FROM player_bank WHERE player_id = :id")
    fun getBankById(id: Long): LiveData<Bank>

    @Query("SELECT * FROM player_bank")
    fun getAllBank(): LiveData<List<Bank>>

    @Update
    suspend fun updateBank(bank: Bank)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBank(bank: Bank): Long

    @Query("DELETE FROM player_bank WHERE player_id = :id")
    suspend fun deleteBank(id: Long): Int
}