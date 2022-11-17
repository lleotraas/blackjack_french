package fr.lleotraas.blackjack_french.ui.activity

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.model.User
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class ProfileActivityViewModel @Inject constructor (
    val firebaseHelper: FirebaseHelper,
    val userRepository: UserRepository
) : ViewModel() {

    fun getOnlineUser(userId: String): LiveData<User?> {
        return userRepository.getFakeUser()
//        return userRepository.getCurrentOnlineUser(userId)
    }

    fun updateUserPicture(user: User?) {
        userRepository.updateUserPicture(user)
    }

    fun updateUser(user: User?) {
        userRepository.updateUser(user)
    }

    fun uploadImage(userId: String, uri: Uri, imageResized: ByteArray) {
        userRepository.uploadImage(userId, uri, imageResized)
    }

    fun deleteImage(userId: String, uri: Uri) {
        userRepository.deleteImage(userId, uri)
    }

    fun getAllImage() = userRepository.getAllImage()

    fun getUserStats(userId: String) = userRepository.getUserStats(userId)
}