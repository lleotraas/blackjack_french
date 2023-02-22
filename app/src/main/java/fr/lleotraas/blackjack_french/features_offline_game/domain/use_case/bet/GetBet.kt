package fr.lleotraas.blackjack_french.features_offline_game.domain.use_case.bet

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Bet
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.BetRepository
import javax.inject.Inject

data class GetBet @Inject constructor(
    private val repository: BetRepository
) {

    operator fun invoke(): LiveData<HashMap<String, Double>> {
        return repository.getPlayerBet()
    }
}
