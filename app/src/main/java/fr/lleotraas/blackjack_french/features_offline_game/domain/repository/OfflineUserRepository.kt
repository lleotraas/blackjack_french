package fr.lleotraas.blackjack_french.features_offline_game.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser

interface OfflineUserRepository {

    fun updateOfflineUser(offlineUser: OfflineUser)

    fun getOfflineUser(): LiveData<OfflineUser>

}