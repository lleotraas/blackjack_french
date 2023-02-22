package fr.lleotraas.blackjack_french.features_offline_game.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Bet

interface BetRepository {

    fun setPlayerBet(bet: HashMap<String, Double>)

    fun getPlayerBet(): LiveData<HashMap<String, Double>>

    fun setOpponentBet(bet: HashMap<String, Double>)

    fun getOpponentBet(): LiveData<HashMap<String, Double>>
}