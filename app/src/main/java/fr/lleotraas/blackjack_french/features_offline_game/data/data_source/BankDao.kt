package fr.lleotraas.blackjack_french.features_offline_game.data.data_source

import androidx.lifecycle.LiveData
import androidx.room.*
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet


@Dao
interface BankDao{

    @Query("SELECT * FROM player_bank WHERE player_id = :id")
    fun getBankById(id: Long): LiveData<Wallet>

    @Query("SELECT * FROM player_bank")
    fun getAllBank(): LiveData<List<Wallet>>

    @Update
    suspend fun updateBank(wallet: Wallet)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBank(wallet: Wallet): Long

    @Query("DELETE FROM player_bank WHERE player_id = :id")
    suspend fun deleteBank(id: Long): Int
}