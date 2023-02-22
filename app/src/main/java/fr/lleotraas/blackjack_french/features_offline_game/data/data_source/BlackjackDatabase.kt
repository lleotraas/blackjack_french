package fr.lleotraas.blackjack_french.features_offline_game.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet

@Database(
    entities = [Wallet::class],
    version = 1,
    exportSchema = false
)
abstract class BlackjackDatabase: RoomDatabase() {

    abstract fun playerBankDao(): BankDao
}