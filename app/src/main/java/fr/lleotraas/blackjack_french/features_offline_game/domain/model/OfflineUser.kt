package fr.lleotraas.blackjack_french.features_offline_game.domain.model

import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

data class OfflineUser(
    val wallet: Wallet? = null,
    var player: ArrayList<CustomPlayer> = Utils.createCustomPlayerList(1, 0.0),
    var currentPlayerNumber: PlayerNumberType = PlayerNumberType.PLAYER_ONE,
    var currentHandType: HandType = HandType.MainHand,
    var defaultBet: Double = 0.0,
    var playerCount: Int = 1,
    var totalBet: Double = 0.0,
    var isPlaying: Boolean = false,
    var isHelpMode: Boolean = false
)
