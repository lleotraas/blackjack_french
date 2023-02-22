package fr.lleotraas.blackjack_french.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.lleotraas.blackjack_french.features_online_main_screen.data.repository.UserRepositoryImpl
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.UserRepository
import fr.lleotraas.blackjack_french.features_offline_game.data.repository.BetRepositoryImpl
import fr.lleotraas.blackjack_french.features_offline_game.data.repository.OfflineUserRepositoryImpl
import fr.lleotraas.blackjack_french.features_offline_game.data.repository.WalletRepositoryImpl
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.BetRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.OfflineUserRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.WalletRepository
import fr.lleotraas.blackjack_french.features_online_game.data.repository.OnlineDeckRepositoryImpl
import fr.lleotraas.blackjack_french.features_online_game.data.repository.OnlineGameChatRepositoryImpl
import fr.lleotraas.blackjack_french.features_online_game.domain.repository.OnlineDeckRepository
import fr.lleotraas.blackjack_french.features_online_game.domain.repository.OnlineGameChatRepository
import fr.lleotraas.blackjack_french.features_online_main_screen.data.repository.ChatRepositoryImpl
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.ChatRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindOnlineDeckRepository(
        onlineDeckRepositoryImpl: OnlineDeckRepositoryImpl
    ): OnlineDeckRepository

    @Binds
    @Singleton
    abstract fun bindBetRepository(
        betRepositoryImpl: BetRepositoryImpl
    ): BetRepository

    @Binds
    @Singleton
    abstract fun bindOnlineGameChatRepository(
        onlineGameChatRepositoryImpl: OnlineGameChatRepositoryImpl
    ): OnlineGameChatRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindOfflineUserRepository(
        offlineUserRepositoryImpl: OfflineUserRepositoryImpl
    ): OfflineUserRepository
}