package fr.lleotraas.blackjack_french.dependency

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.lleotraas.blackjack_french.database.BlackjackDatabase
import fr.lleotraas.blackjack_french.database.dao.BankDao
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.data.repository.FirebaseHelperImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providePlayerBankRepository(database: BlackjackDatabase): BankDao {
        return database.playerBankDao()
    }

    @Provides
    @Singleton
    fun provideBlackjackDatabase(@ApplicationContext appContext: Context): BlackjackDatabase {
        return Room.databaseBuilder(
            appContext.applicationContext,
            BlackjackDatabase::class.java,
            "blackjack_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFirebaseHelper() : FirebaseHelper {
        return FirebaseHelperImpl()
    }

}