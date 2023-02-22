package fr.lleotraas.blackjack_french.features_online_main_screen.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.ChatRepository
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class OnlineMainScreenActivityViewModel @Inject constructor (
    val firebaseHelper: FirebaseHelper,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
): ViewModel() {

    fun getCurrentUser(): FirebaseUser? {
        return firebaseHelper.getCurrentUser()
    }

    fun isCurrentUserLogged(): Boolean {
        return firebaseHelper.isCurrentUserLogged()
    }

    fun signOut(currentUserId: String) = userRepository.signOut(currentUserId)

    fun getOnlineUser(userId: String): LiveData<User?> {
        return userRepository.getCurrentOnlineUser(userId)
    }

    fun updateOnlineUserStatus(currentUserId: String, userOnlineStatus: OnlineStatusType) {
        userRepository.updateOnlineStatusAskForPlay(currentUserId, userOnlineStatus)
    }

    fun updateUserIsGameHost(currentUserId: String, userIsGameHost: Boolean) {
        userRepository.updateIsGameHost(currentUserId, userIsGameHost)
    }

    fun getAllOnlineUser() = userRepository.getAllOnlineUsers()

    fun allUserUpdated() = userRepository.allUserUpdated()

    fun sendMessage(message: Message) {
        chatRepository.sendMessage(message)
    }

    fun getAllMessage() = chatRepository.getAllMessage()

    fun listenToMessage() = chatRepository.listenToMessage()

    fun getAllImage() = userRepository.getAllImage()

    fun updateImageList() {
        userRepository.updateImageList()
    }

    fun compareImageList() {
        userRepository.compareImageList()
    }
}