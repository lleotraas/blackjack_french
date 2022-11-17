package fr.lleotraas.blackjack_french.model

class Dealer(
    var score: Int,
    val hand: ArrayList<Card>,
    var isDealerDrawAce: Boolean,
    var isDealerScoreSoft: Boolean,
)