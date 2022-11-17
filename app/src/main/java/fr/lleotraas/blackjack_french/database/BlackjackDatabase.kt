package fr.lleotraas.blackjack_french.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.lleotraas.blackjack_french.database.dao.BankDao
import fr.lleotraas.blackjack_french.model.Bank

@Database(
    entities = [Bank::class],
    version = 1,
    exportSchema = false
)
abstract class BlackjackDatabase: RoomDatabase() {

    abstract fun playerBankDao(): BankDao
}