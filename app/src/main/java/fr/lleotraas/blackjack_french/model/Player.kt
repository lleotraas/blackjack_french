package fr.lleotraas.blackjack_french.model

class Player(
    var score: Array<Int>,
    var hand: Array<ArrayList<Card>>,
    val playerNumberType: PlayerNumberType,
    var isPlayerDrawAce: Array<Boolean>,
    var isPlayerScoreSoft: Array<Boolean>,
    var isPlayerFirstSplit: Boolean,
    var isPlayerSecondSplit: Boolean
) {
}