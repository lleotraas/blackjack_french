package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user

import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.OfflineUserRepository

class GetOfflineUser(
    private val repository: OfflineUserRepository
) {

    operator fun invoke() = repository.getOfflineUser()


}