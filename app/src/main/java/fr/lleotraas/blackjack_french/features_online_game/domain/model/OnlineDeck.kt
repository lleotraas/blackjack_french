package fr.lleotraas.blackjack_french.features_online_game.domain.model

import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.PlayerNumberType

class OnlineDeck(
    var deckList: ArrayList<Card> = ArrayList(),
    var index: Int = 0,
    var playerTurn: PlayerNumberType = PlayerNumberType.PLAYER_ONE
) {
}