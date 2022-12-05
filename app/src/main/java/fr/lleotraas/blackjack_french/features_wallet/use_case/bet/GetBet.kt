package fr.lleotraas.blackjack_french.features_wallet.use_case.bet

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Bet
import fr.lleotraas.blackjack_french.features_wallet.domain.repository.BetRepository
import javax.inject.Inject

data class GetBet @Inject constructor(
    private val repository: BetRepository
) {

    operator fun invoke(): LiveData<Bet> {
        return repository.getPlayerBet()
    }
}
