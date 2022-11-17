package fr.lleotraas.blackjack_french.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.lleotraas.blackjack_french.domain.repository.BetRepository
import fr.lleotraas.blackjack_french.model.Bet
import javax.inject.Inject

class BetRepositoryImpl @Inject constructor(): BetRepository{
    private var playerBet = MutableLiveData<Bet>()
    private var opponentBet = MutableLiveData<Bet>()

    override fun setPlayerBet(bet: Bet) {
        playerBet.postValue(bet)
    }

    override fun getPlayerBet(): LiveData<Bet> {
        return playerBet
    }

    override fun setOpponentBet(bet: Bet) {
        opponentBet.postValue(bet)
    }

    override fun getOpponentBet(): LiveData<Bet> {
        return opponentBet
    }
}