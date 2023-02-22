package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet

import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Bet
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.BetRepository
import javax.inject.Inject

data class SetBet @Inject constructor(
    private val repository: BetRepository
) {

    operator fun invoke(bet: HashMap<String, Double>) {
        repository.setPlayerBet(bet)
    }
}
