package fr.lleotraas.blackjack_french.features_offline_game.domain.model

import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils

data class OfflineUser(
    val wallet: Wallet? = null,
    var bet: ArrayList<HashMap<String, Double>> = Utils.createArrayListOfBet(1),
    var player: ArrayList<Player> = Utils.createArrayListOfPlayerHand(1),
    var playerIndex: Int = 0
)
