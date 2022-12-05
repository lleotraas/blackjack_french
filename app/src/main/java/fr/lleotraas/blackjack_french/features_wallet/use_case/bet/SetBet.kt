package fr.lleotraas.blackjack_french.features_wallet.use_case.bet

import fr.lleotraas.blackjack_french.features_wallet.domain.model.Bet
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.BetRepository
import javax.inject.Inject

data class SetBet @Inject constructor(
    private val repository: BetRepository
) {

    operator fun invoke(bet: Bet) {
        repository.setPlayerBet(bet)
    }
}
