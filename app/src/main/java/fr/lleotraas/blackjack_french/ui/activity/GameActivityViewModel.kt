package fr.lleotraas.blackjack_french.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.data.repository.PlayerBankRepositoryImpl
import fr.lleotraas.blackjack_french.domain.repository.BetRepository
import fr.lleotraas.blackjack_french.model.Bank
import fr.lleotraas.blackjack_french.model.Bet
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Named("GameActivityViewModel")
class GameActivityViewModel @Inject constructor (
    private val playerBankRepository: PlayerBankRepositoryImpl,
    private val betRepository: BetRepository
): ViewModel() {

    suspend fun insertBank(bank: Bank): Long {
        return playerBankRepository.insertBank(bank)
    }

    suspend fun updateBank(bank: Bank) {
        playerBankRepository.updateBank(bank)
    }

    suspend fun deleteBank(id: Long): Int {
        return playerBankRepository.deleteBank(id)
    }

    fun getBank(id: Long): LiveData<Bank> {
        return playerBankRepository.getBank(id)
    }

    fun getAllBank(): LiveData<List<Bank>> {
        return playerBankRepository.getAllBank()
    }

    fun setBet(bet: Bet) {
        betRepository.setPlayerBet(bet)
    }

    fun getBet(): LiveData<Bet> {
        return betRepository.getPlayerBet()
    }
}