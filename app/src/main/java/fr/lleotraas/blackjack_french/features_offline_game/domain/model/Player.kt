package fr.lleotraas.blackjack_french.features_offline_game.domain.model

data class Player(
    var score: HashMap<String, Int>,
    var hand: HashMap<String, ArrayList<Card>>,
    var bet: HashMap<String, Double>,
    val playerNumberType: PlayerNumberType,
    var resultScore: HashMap<String, Int>,
    var isPlayerDrawAce: HashMap<String, Boolean>,
    var isPlayerScoreSoft: HashMap<String, Boolean>,
    var isPlayerFirstSplit: Boolean,
    var isPlayerSecondSplit: Boolean
)