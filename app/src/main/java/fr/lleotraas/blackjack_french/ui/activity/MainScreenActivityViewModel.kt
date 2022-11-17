package fr.lleotraas.blackjack_french.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.data.repository.PlayerBankRepositoryImpl
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.domain.repository.UserRepository
import fr.lleotraas.blackjack_french.model.Bank
import fr.lleotraas.blackjack_french.model.User
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Named("MainScreenActivityViewModel")
class MainScreenActivityViewModel @Inject constructor (
    private val playerBankRepository: PlayerBankRepositoryImpl,
    private val firebaseHelper: FirebaseHelper,
    private val userRepository: UserRepository
): ViewModel() {

    suspend fun insertBank(bank: Bank): Long {
        return playerBankRepository.insertBank(bank)
    }

    suspend fun updateBank(bank: Bank) {
        playerBankRepository.updateBank(bank)
    }

    suspend fun deleteBank(id: Long): Int {
        return playerBankRepository.deleteBank(id)
    }

    fun getBank(id: Long): LiveData<Bank> {
        return playerBankRepository.getBank(id)
    }

    fun getAllBank(): LiveData<List<Bank>> {
        return playerBankRepository.getAllBank()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseHelper.getCurrentUser()
    }

    fun isCurrentUserLogged(): Boolean {
        return firebaseHelper.isCurrentUserLogged()
    }

    fun signOut() {
         firebaseHelper.signOut()
    }

    fun getOnlineUser(userId: String): LiveData<User?> {
        return userRepository.getFakeUser()
//        userRepository.getCurrentOnlineUser(userId)
    }

    fun getAllImage() = userRepository.getAllImage()

    fun updateImageList() {
        userRepository.updateImageList()
    }

    fun compareImageList() {
        userRepository.compareImageList()
    }

    fun getAllOnlineUsers() = userRepository.getAllOnlineUsers()
}