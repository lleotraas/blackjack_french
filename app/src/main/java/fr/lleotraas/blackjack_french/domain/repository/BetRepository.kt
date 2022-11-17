package fr.lleotraas.blackjack_french.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.model.Bet

interface BetRepository {

    fun setPlayerBet(bet: Bet)

    fun getPlayerBet(): LiveData<Bet>

    fun setOpponentBet(bet: Bet)

    fun getOpponentBet(): LiveData<Bet>
}