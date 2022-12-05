package fr.lleotraas.blackjack_french.dependency

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.lleotraas.blackjack_french.data.repository.*
import fr.lleotraas.blackjack_french.domain.repository.*
import fr.lleotraas.blackjack_french.features_wallet.data.repository.BetRepositoryImpl
import fr.lleotraas.blackjack_french.features_wallet.data.repository.WalletRepositoryImpl
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.BetRepository
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.WalletRepository
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
}