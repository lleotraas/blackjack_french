package fr.lleotraas.blackjack_french.features_offline_game.domain.model

import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

data class Player(
    var score: HashMap<String, Int>,
    var hand: HashMap<String, ArrayList<Card>>,
    var currentHandType: HandType,
    val playerNumberType: PlayerNumberType,
    var isPlayerDrawAce: HashMap<String, Boolean>,
    var isPlayerScoreSoft: HashMap<String, Boolean>,
    var isPlayerFirstSplit: Boolean,
    var isPlayerSecondSplit: Boolean
) {
}