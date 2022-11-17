package fr.lleotraas.blackjack_french.model

class OnlineDeck(
    var deckList: ArrayList<Card>? = null,
    var index: Int? = 0,
    var playerTurn: PlayerNumberType? = null
) {
}