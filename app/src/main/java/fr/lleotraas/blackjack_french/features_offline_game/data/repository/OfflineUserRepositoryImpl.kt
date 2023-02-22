package fr.lleotraas.blackjack_french.features_offline_game.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.OfflineUserRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser
import javax.inject.Inject

class OfflineUserRepositoryImpl @Inject constructor() : OfflineUserRepository {

    private val offlineUser = MutableLiveData<OfflineUser>()

    override fun updateOfflineUser(offlineUser: OfflineUser) {
        this.offlineUser.postValue(offlineUser)
    }

    override fun getOfflineUser(): LiveData<OfflineUser> = offlineUser
}