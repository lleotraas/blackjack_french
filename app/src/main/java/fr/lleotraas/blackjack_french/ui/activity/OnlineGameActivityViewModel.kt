package fr.lleotraas.blackjack_french.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.domain.repository.*
import fr.lleotraas.blackjack_french.model.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineGameActivityViewModel @Inject constructor (
    private val firebaseHelper: FirebaseHelper,
    private val userRepository: UserRepository,
    private val onlineDeckRepository: OnlineDeckRepository,
    private val onlineGameChatRepository: OnlineGameChatRepository
) : ViewModel() {

    fun isNumberOfGamePlayedUpdated() {
        userRepository.isNumberOfGamePlayedUpdated()
    }

    fun updateNumberOfGamePlayed(user: User) = userRepository.updateNumberOfGamePlayed(user)

    fun getCurrentUser(): FirebaseUser? {
        return firebaseHelper.getCurrentUser()
    }

    fun getOnlineUser(userId: String) = userRepository.getCurrentOnlineUser(userId)


    fun getSearchedUser(userId: String) = userRepository.getSearchedUser(userId)


    fun updateUserIsReady(currentUserId: String, userIsReady: Boolean) {
        userRepository.updateUserIsReady(currentUserId, userIsReady)
    }

    fun updateOnlineUserWalletAndIsSplitting(user: User?,isSplitting: Boolean) {
        userRepository.updateOnlineUserWalletAndIsSplitting(user, isSplitting)
    }

    fun updateOnlineUserBetAndWallet(user: User) {
        userRepository.updateOnlineUserBetAndWallet(user)
        userRepository.updateUserStats(user)
    }

    fun updateOnlineUserWalletAndLoan(user: User) {
        userRepository.updateOnlineUserWalletAndLoan(user)
    }

    fun updateUserForNewGame(user: User, isUserReady: Boolean?) {
        userRepository.updateUserForNewGame(user, isUserReady)
    }

    fun updateSplitType(user: User) {
        userRepository.updateSplitType(user)
    }

    fun updateIsSplitting(userId: String, isSplitting: Boolean) {
        userRepository.updateIsSplitting(userId, isSplitting)
    }

    fun isDeckExist(currentUserId: String): Boolean {
        return onlineDeckRepository.isDeckExist(currentUserId)
    }

    fun createOnlineDeck(currentUserId: String, deckLocal: OnlineDeck) {
        onlineDeckRepository.createOnlineDeck(currentUserId, deckLocal)
    }

    fun getOnlineDeck(currentUserId: String): LiveData<OnlineDeck?> {
        return onlineDeckRepository.getOnlineDeck(currentUserId)
    }

    fun updateOnlineStatus(currentUserId: String, onlineStatus: OnlineStatusType) {
        userRepository.updateOnlineStatusAskForPlay(currentUserId, onlineStatus)
    }

    fun updateOnlineDeckIndex(currentUserId: String, index: Int) {
        onlineDeckRepository.updateOnlineDeckIndex(currentUserId, index)
    }

    fun updateOnlineDeckPlayerTurn(currentUserId: String, playerNumberType: PlayerNumberType) {
        viewModelScope.launch {
            onlineDeckRepository.updateOnlineDeckPlayerTurn(currentUserId, playerNumberType)
        }
    }

    fun deleteOnlineDeck(currentUserId: String) {
        onlineDeckRepository.deleteOnlineDeck(currentUserId)
    }

    fun resetCurrentUserAndHandType(user: User?, handType: HandType, isSplitting: Boolean) {
        userRepository.resetCurrentUserAndHandType(user, handType, isSplitting)
    }

    fun getAllImage() = userRepository.getAllImage()

    fun sendMessage(userId: String, message: Message) {
        onlineGameChatRepository.sendMessage(userId, message)
    }

    fun getAllMessage(userId: String) = onlineGameChatRepository.getAllMessage(userId)

    fun listenToMessage(userId: String) = onlineGameChatRepository.listenToMessage(userId)

    fun deleteAllMessage(userId: String) {
        onlineGameChatRepository.deleteAllMessage(userId)
    }
}