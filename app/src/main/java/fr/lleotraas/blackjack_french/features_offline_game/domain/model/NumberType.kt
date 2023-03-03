package fr.lleotraas.blackjack_french.features_offline_game.domain.model

enum class NumberType(val index: Int, val value: Int) {
    RED(0,0),
    ACE(1, 1),
    TWO(2, 2),
    THREE(3, 3),
    FOUR(4, 4),
    FIVE(5, 5),
    SIX(6, 6),
    SEVEN(7, 7),
    HEIGHT(8, 8),
    NINE(9, 9),
    TEN(10, 10),
    JACK(11, 10),
    QUEEN(12, 10),
    KING(13, 10)
}