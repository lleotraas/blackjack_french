package fr.lleotraas.blackjack_french.features_offline_game.domain.model

import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

data class CustomPlayer(
    var score: Int,
    val hand: ArrayList<Card>,
    var bet: Double,
    val playerNumber: PlayerNumberType,
    var resultScore: Int,
    var insuranceBet: Double,
    val playerHandType: HandType,
    var isCurrentPlayer: Boolean,
    var isDouble: Boolean,
    var isPlayerDrawAce: Boolean,
    var isPlayerScoreSoft: Boolean,
    var isPlayerFirstSplit: Boolean,
    var isPlayerSecondSplit: Boolean,
    var isInsuranceOpen: Boolean,
    var isHelpMode: Boolean = false,
)
