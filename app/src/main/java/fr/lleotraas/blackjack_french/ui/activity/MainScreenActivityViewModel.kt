package fr.lleotraas.blackjack_french.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.features_wallet.data.repository.WalletRepositoryImpl
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.domain.repository.UserRepository
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Wallet
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Named("MainScreenActivityViewModel")
class MainScreenActivityViewModel @Inject constructor (
    private val playerBankRepository: WalletRepositoryImpl,
    private val firebaseHelper: FirebaseHelper,
    private val userRepository: UserRepository
): ViewModel() {

    suspend fun insertBank(wallet: Wallet): Long {
        return playerBankRepository.insertWallet(wallet)
    }

    suspend fun updateBank(wallet: Wallet) {
        playerBankRepository.updateWallet(wallet)
    }

    suspend fun deleteBank(id: Long): Int {
        return playerBankRepository.deleteWallet(id)
    }

    fun getBank(id: Long): LiveData<Wallet> {
        return playerBankRepository.getWallet(id)
    }

    fun getAllBank(): LiveData<List<Wallet>> {
        return playerBankRepository.getAllWallet()
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

    fun getOnlineUser(userId: String) = userRepository.getCurrentOnlineUser(userId)

    fun getAllImage() = userRepository.getAllImage()

    fun updateImageList() {
        userRepository.updateImageList()
    }

    fun compareImageList() {
        userRepository.compareImageList()
    }

    fun getAllOnlineUsers() = userRepository.getAllOnlineUsers()
}