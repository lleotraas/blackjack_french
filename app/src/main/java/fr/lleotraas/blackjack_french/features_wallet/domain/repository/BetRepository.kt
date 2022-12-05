package fr.lleotraas.blackjack_french.features_wallet.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_wallet.domain.model.Bet

interface BetRepository {

    fun setPlayerBet(bet: Bet)

    fun getPlayerBet(): LiveData<Bet>

    fun setOpponentBet(bet: Bet)

    fun getOpponentBet(): LiveData<Bet>
}