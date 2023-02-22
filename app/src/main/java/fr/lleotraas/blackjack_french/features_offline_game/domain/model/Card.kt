package fr.lleotraas.blackjack_french.features_offline_game.domain.model

class Card(
    val number: NumberType? = null,
    val color: ColorType? = null,
    val value: Int? = 0,
    var isAnimate: Boolean = true
) {
}