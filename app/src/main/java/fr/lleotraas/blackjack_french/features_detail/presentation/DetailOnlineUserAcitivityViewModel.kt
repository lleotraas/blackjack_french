package fr.lleotraas.blackjack_french.features_detail.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_game.domain.repository.OnlineGameChatRepository
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.UserRepository
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message
import javax.inject.Inject

@HiltViewModel
class DetailOnlineUserActivityViewModel @Inject constructor(
    private val firebaseHelper: FirebaseHelper,
    private val userRepository: UserRepository,
    private val onlineGameChatRepository: OnlineGameChatRepository
) : ViewModel() {

    fun getCurrentUser(): FirebaseUser? {
        return firebaseHelper.getCurrentUser()
    }

    fun isCurrentUserLogged(): Boolean {
        return firebaseHelper.isCurrentUserLogged()
    }

    fun signOut() {
        firebaseHelper.signOut()
    }

    fun createOnlineUser() {
        userRepository.createOnlineUser()
    }

    fun getOnlineUser(userId: String): LiveData<User?> {
        return userRepository.getCurrentOnlineUser(userId)
    }

    fun getSearchedUser(userId: String): LiveData<User?> {
        return userRepository.getSearchedUser(userId)
    }

    fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        userOnlineStatus: OnlineStatusType
    ) {
        userRepository.updateOnlineStatusAskForPlay(currentUserId, userOnlineStatus)
    }

    fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        searchedUserId: String,
        userOnlineStatus: OnlineStatusType
    ) {
        userRepository.updateOnlineStatusAskForPlay(currentUserId, searchedUserId, userOnlineStatus)
    }

    fun updateOnlineStatusPlaying(currentUserId: String,
                                  searchedUserId: String,) {
        userRepository.updateOnlineStatusPlaying(currentUserId, searchedUserId)
    }

    fun updateOnlineUserStatus(userId: String, userStatus: OnlineStatusType) {
        userRepository.updateOnlineStatusAskForPlay(userId, userStatus)
    }

    fun updateUserIsGameHost(currentUserId: String, userIsGameHost: Boolean) {
        userRepository.updateIsGameHost(currentUserId, userIsGameHost)
    }

    fun getAllOnlineUser() = userRepository.getAllOnlineUsers()

    fun getAllProfileImage() = userRepository.getAllImage()

    fun getAllUserStats(userId: String) = userRepository.getUserStats(userId)

    fun sendMessage(userId: String, message: Message) {
        onlineGameChatRepository.sendMessage(userId, message)
    }
}