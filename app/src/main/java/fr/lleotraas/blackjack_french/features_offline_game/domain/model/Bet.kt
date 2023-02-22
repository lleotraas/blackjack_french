package fr.lleotraas.blackjack_french.features_offline_game.domain.model

data class Bet(
    var playerBet: Double,
    var mainHandBet: Double,
    var firstSplitBet: Double,
    var secondSplitBet: Double,
    var insuranceBet: Double,
    var totalBet: Double
)