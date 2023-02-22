package fr.lleotraas.blackjack_french.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.lleotraas.blackjack_french.features_offline_game.data.data_source.BlackjackDatabase
import fr.lleotraas.blackjack_french.features_offline_game.data.data_source.BankDao
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.BetRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.OfflineUserRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.WalletRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet.BetUseCases
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet.GetBet
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet.SetBet
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user.GetOfflineUser
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user.OfflineUserUseCases
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user.UpdateOfflineUser
import fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.wallet.*
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_main_screen.data.repository.FirebaseHelperImpl
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

    @Provides
    fun provideBetUseCases(repository: BetRepository) =
        BetUseCases(
            setBet = SetBet(repository),
            getBet = GetBet(repository)
        )

    @Provides
    fun provideWalletUseCases(repository: WalletRepository) =
        WalletUseCases(
            insertWallet = InsertWallet(repository),
            getWallet = GetWallet(repository),
            getWallets = GetWallets(repository),
            updateWallet = UpdateWallet(repository),
            deleteWallet = DeleteWallet(repository)
        )

    @Provides
    fun provideOfflineUserUseCases(repository: OfflineUserRepository) =
        OfflineUserUseCases(
            updateOfflineUser = UpdateOfflineUser(repository),
            getOfflineUser = GetOfflineUser(repository)
        )

}