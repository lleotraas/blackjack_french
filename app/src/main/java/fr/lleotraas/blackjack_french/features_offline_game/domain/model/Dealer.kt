package fr.lleotraas.blackjack_french.features_offline_game.domain.model

class Dealer(
    var score: Int,
    var hand: ArrayList<Card>,
    var isDealerDrawAce: Boolean,
    var isDealerScoreSoft: Boolean,
)