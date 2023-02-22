package fr.lleotraas.blackjack_french.features_offline_game.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.repository.BetRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Bet
import javax.inject.Inject

class BetRepositoryImpl @Inject constructor(): BetRepository {
    private var playerBet = MutableLiveData<HashMap<String, Double>>()
    private var opponentBet = MutableLiveData<HashMap<String, Double>>()

    override fun setPlayerBet(bet: HashMap<String, Double>) {
        playerBet.postValue(bet)
    }

    override fun getPlayerBet(): LiveData<HashMap<String, Double>> {
        return playerBet
    }

    override fun setOpponentBet(bet: HashMap<String, Double>) {
        opponentBet.postValue(bet)
    }

    override fun getOpponentBet(): LiveData<HashMap<String, Double>> {
        return opponentBet
    }
}