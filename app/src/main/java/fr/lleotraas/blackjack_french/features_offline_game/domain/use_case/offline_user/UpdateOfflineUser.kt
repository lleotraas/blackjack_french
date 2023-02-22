package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user

import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.OfflineUserRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser

class UpdateOfflineUser(
    private val repository: OfflineUserRepository
) {

    operator fun invoke(offlineUser: OfflineUser) {
        repository.updateOfflineUser(offlineUser)
    }

}