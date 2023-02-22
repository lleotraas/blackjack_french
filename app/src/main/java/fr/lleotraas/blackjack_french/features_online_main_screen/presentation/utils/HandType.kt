package fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils

import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.FIRST_SPLIT
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.FIRST_SPLIT_HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.MAIN_HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.SECOND_SPLIT_HAND

sealed class HandType(
    val valueOf: String
    ) {
    object MainHand: HandType(MAIN_HAND)
    object FirstSplit : HandType(FIRST_SPLIT_HAND)
    object SecondSplit : HandType(SECOND_SPLIT_HAND)

    fun toHandType(string: String): HandType {
        return when (string) {
             MAIN_HAND -> { MainHand }
             FIRST_SPLIT -> { FirstSplit }
             else -> { SecondSplit }
        }
    }
}