package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import fr.lleotraas.blackjack_french.features_offline_game.domain.model.CustomPlayer
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Wallet
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User

class InsuranceUtils {
    companion object {

        fun retrieveInsuranceBetInWallet(wallet: Double, bet: Double) =
            wallet - (bet / 2.0)

        fun updateInsurance(user: User, dealerHaveBlackjack: Boolean) =
            if (user.bet!![Utils.INSURANCE]!! > 0 && dealerHaveBlackjack)
                user.bet!![Utils.INSURANCE]!! * 3
            else
                0.0

        fun openInsurance(arrayOfCustomPlayer: ArrayList<CustomPlayer>) {
            for (player in arrayOfCustomPlayer) {
                player.isInsuranceOpen = true
            }
        }

        fun closeInsurance(arrayOfCustomPlayer: ArrayList<CustomPlayer>) {
            for (player in arrayOfCustomPlayer) {
                player.isInsuranceOpen = false
            }
        }

        fun playerHaveInsurance(playerList: ArrayList<CustomPlayer>): Boolean {
            for (player in playerList) {
                if (player.insuranceBet > 0.0) {
                    return true
                }
            }
            return false
        }

        fun insurancePay(playerToCompare: CustomPlayer): Double {
            return if (playerToCompare.insuranceBet > 0.0) playerToCompare.insuranceBet * 2 else 0.0
        }

        fun insuranceLoose(playerToCompare: CustomPlayer): Double {
            return if (playerToCompare.insuranceBet > 0.0) playerToCompare.insuranceBet else 0.0
        }

        fun payAllInsurance(
            wallet: Wallet,
            playerList: ArrayList<CustomPlayer>,
            dealerHaveBlackJack: Boolean
        ) {
            for (player in playerList) {
                if (player.insuranceBet > 0.0) {
                    if (dealerHaveBlackJack) {
                        wallet.amount+=insurancePay(player)
                    } else {
                        wallet.amount-=insuranceLoose(player)
                    }
                }
            }
        }

    }
}