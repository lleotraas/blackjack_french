package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet

import javax.inject.Inject

class BetUseCases @Inject constructor(
    val setBet: SetBet,
    val getBet: GetBet
) {
}