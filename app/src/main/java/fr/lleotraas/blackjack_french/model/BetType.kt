package fr.lleotraas.blackjack_french.model

enum class BetType(type: String) {
    MAIN_HAND("main_hand"),
    FIRST_SPLIT_BET("first_split_bet"),
    SECOND_SPLIT_BET("second_split_bet"),
    INSURANCE_BET("insurance_bet"),
    TOTAL_BET("total_bet")
}