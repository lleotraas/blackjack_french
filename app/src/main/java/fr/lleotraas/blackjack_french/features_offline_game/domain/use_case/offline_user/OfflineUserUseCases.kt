package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.offline_user

import javax.inject.Inject

data class OfflineUserUseCases @Inject constructor(
    val updateOfflineUser: UpdateOfflineUser,
    val getOfflineUser: GetOfflineUser
)