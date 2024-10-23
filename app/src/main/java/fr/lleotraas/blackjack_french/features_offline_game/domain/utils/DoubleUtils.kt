package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Dealer
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Player
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

class DoubleUtils {
    companion object {

        fun getHandTypeForDoubledBox(handType: String) = when(handType) {
            Utils.SECOND_SPLIT -> Utils.FIRST_SPLIT
            else -> Utils.MAIN_HAND
        }

        fun disableDoubleBtn(playerHand: Player, user: User, dealer: Dealer, time: Double): Boolean {
            if (playerHand.hand[Utils.MAIN_HAND]?.size!! > 2 && user.splitType == HandType.MainHand) {
                return false
            }
            if (playerHand.hand[Utils.FIRST_SPLIT]?.size!! > 2 && user.splitType == HandType.FirstSplit) {
                return false
            }
            if (playerHand.hand[Utils.SECOND_SPLIT]?.size!! > 2 && user.splitType == HandType.SecondSplit) {
                return false
            }
            if (playerHand.hand[Utils.FIRST_SPLIT]!!.isNotEmpty()) {
                if (playerHand.hand[Utils.FIRST_SPLIT]?.get(0)?.value == 1 && playerHand.hand[Utils.MAIN_HAND]?.get(0)?.value == 1) {
                    return false
                }
            }
            if (
                time >= 1.0 &&
                playerHand.hand[user.splitType?.valueOf]?.size == 2 &&
                playerHand.playerNumberType == user.playerTurn &&
                dealer.score < 12
            ) {
                return true
            }
            return false
        }

    }
}