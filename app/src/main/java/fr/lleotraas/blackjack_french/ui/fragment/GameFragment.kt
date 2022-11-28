package fr.lleotraas.blackjack_french.ui.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineGameBinding
import fr.lleotraas.blackjack_french.model.*
import fr.lleotraas.blackjack_french.ui.activity.GameActivityViewModel
import fr.lleotraas.blackjack_french.ui.dialog.BetDialog
import fr.lleotraas.blackjack_french.utils.Utils.Companion.FIRST_SPLIT
import fr.lleotraas.blackjack_french.utils.Utils.Companion.MAIN_HAND
import fr.lleotraas.blackjack_french.utils.Utils.Companion.SECOND_SPLIT
import fr.lleotraas.blackjack_french.utils.Utils.Companion.createDeck
import fr.lleotraas.blackjack_french.utils.Utils.Companion.shuffleDeck
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class GameFragment : Fragment() {

    private lateinit var mBinding: FragmentOnlineGameBinding
    private lateinit var deck: Deck
    private var dealer = Dealer(0, ArrayList(), isDealerDrawAce = false, isDealerScoreSoft = false)
    private var player = Player(Array(3){0}, Array(3){ ArrayList() }, PlayerNumberType.PLAYER_ONE, isPlayerDrawAce = Array(3){false}, isPlayerScoreSoft = Array(3){false}, isPlayerFirstSplit = false, isPlayerSecondSplit = false)
    private var cardsDraw = 0
    private var isPlayerDrawAce = false
    private var isDealerDrawAce = false
    private var isPlayerScoreSoft = false
    private var isDealerScoreSoft = false
    private var isFirstSplit = false
    private var isSecondSplit = false
    private var handType = HandType.MAIN_HAND
    private lateinit var playerHandAdapter: GameAdapter
    private lateinit var playerFirstSplitAdapter: GameAdapter
    private lateinit var playerSecondSplitAdapter: GameAdapter
    private lateinit var dealerHandAdapter: GameAdapter
    private var bank: Bank? = null
    private var bet = Bet(0.0,0.0,0.0,0.0,0.0, 0.0)
    private val mViewModel: GameActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOnlineGameBinding.inflate(inflater, container, false)
        playerHandAdapter = GameAdapter()
        playerFirstSplitAdapter = GameAdapter()
        playerSecondSplitAdapter = GameAdapter()
        dealerHandAdapter = GameAdapter()
        val bankId = requireActivity().intent.extras!!.get(PLAYER_SAVE_ID) as Long
        loadBank(bankId)
        loadBet()
        configureListeners()
        prepareDeck()
        disableHitAndStopButtons()
        hideReadyPictures()
        mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
        mBinding.fragmentOnlineGameSplitBtn.isEnabled = false
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.GONE
        return mBinding.root
    }

    private fun loadBank(bankId: Long) {
        mViewModel.getBank(bankId).observe(viewLifecycleOwner) { currentBank ->
            bank = currentBank
            mBinding.fragmentOnlineGameBankAmountTv.text = String.format("%s", currentBank.amount)
//            mBinding.fragmentMainScreenPlayerInformation.text = String.format(" %s %s %s", bank!!.pseudo, requireContext().resources.getString(R.string.fragment_main_game_current_bet), bet.playerBet)
        }
    }

    private fun loadBet() {
        mViewModel.getBet().observe(viewLifecycleOwner) { currentBet ->
            bet = currentBet
            bet.totalBet = currentBet.playerBet
            bet.mainHandBet = currentBet.playerBet
            if (bank != null) {
//                mBinding.fragmentMainScreenPlayerInformation.text = String.format(" %s %s %s", bank!!.pseudo, requireContext().resources.getString(R.string.fragment_main_game_current_bet), currentBet.playerBet)
            }
            if (bet.playerBet > 0) {
                mBinding.fragmentOnlineGameGameStart.isEnabled = true
            }
        }
    }

    private fun updatePlayerBankAmount() {
        lifecycleScope.launch {
            mViewModel.updateBank(bank!!)
        }
    }

    private fun prepareDeck() {
        deck = createDeck()
        Log.e(TAG, "beginGame: deck size = ${deck.deckList.size}")
        shuffleDeck(deck.deckList)
    }

    private fun showScore() {
        mBinding.fragmentOnlineGamePlayerScoreTv.text = String.format("%s", player.score[MAIN_HAND])
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.text = String.format("%s", player.score[FIRST_SPLIT])
        mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.text = String.format("%s", player.score[SECOND_SPLIT])
        mBinding.fragmentOnlineGameDealerScoreTv.text = String.format("%s", dealer.score)
    }

    private fun configureListeners() {
        // Start Button
        mBinding.fragmentOnlineGameGameStart.setOnClickListener {
            if (bank!!.amount >= bet.playerBet) {
                player.score[MAIN_HAND] = 0
                player.score[FIRST_SPLIT] = 0
                player.score[SECOND_SPLIT] = 0
                dealer.score = 0
                bet.firstSplitBet = 0.0
                bet.secondSplitBet = 0.0
                bet.insuranceBet = 0.0
                bet.mainHandBet = bet.playerBet
                bet.totalBet = bet.playerBet
                isPlayerDrawAce = false
                isDealerDrawAce = false
                isPlayerScoreSoft = false
                isDealerScoreSoft = false
                isFirstSplit = false
                isSecondSplit = false
                handType = HandType.MAIN_HAND
                player.hand[MAIN_HAND].clear()
                player.hand[FIRST_SPLIT].clear()
                player.hand[SECOND_SPLIT].clear()
                dealer.hand.clear()
                playerDrawCard()
                dealerDrawCard()
                playerDrawCard()
                hideGameOver()
                disableHitAndStopButtons()
                decreaseBank()
                updatePlayerBankAmount()
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = true
            } else {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
            }

            // Player start with 2 cards with same values.
            if (player.hand[MAIN_HAND][0].value == player.hand[MAIN_HAND][1].value) {
                mBinding.fragmentOnlineGameSplitBtn.isEnabled = true
            }

            // Player start with blackjack
            if (player.score[MAIN_HAND] == 21) {
                mBinding.fragmentOnlineGamePlayerScoreTv.text = requireContext().resources.getString(R.string.fragment_main_game_blackjack)
            }

            // Dealer have an ace
            if (dealer.score == 11) {
                showInsurance()
            }

            // Player got black jack at the distribution and dealer don't have value equals to 10 or 11.
            if (player.score[MAIN_HAND] == 21 && dealer.score < 10) {
                blackjackGiven()
            }
        }

        //TODO when player split ace, if he draw value of ten, it's considering like a blackjack.

        //Draw a Card Button
        mBinding.fragmentOnlineGameHitBtn.setOnClickListener {
            playerDrawCard()
        }

        //Double
        mBinding.fragmentOnlineGameDoubleBtn.setOnClickListener {
            if (bank!!.amount >= bet.playerBet) {
                playerDrawCard()
                decreaseBank()
                updatePlayerBankAmount()
                doubleBet()
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
                when {
                    player.score[MAIN_HAND] < 22 -> playerStop()
                    player.score[FIRST_SPLIT] in 1..21 -> playerStop()
                    player.score[SECOND_SPLIT] in 1..21 -> playerStop()
                }
            } else {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
            }
        }

        //Split
        mBinding.fragmentOnlineGameSplitBtn.setOnClickListener {
            if (bank!!.amount >= bet.playerBet) {
                splitPlayerGame()
                decreaseBank()
                updatePlayerBankAmount()
                mBinding.fragmentOnlineGameSplitBtn.isEnabled = false
                if (player.hand[MAIN_HAND][0].value == player.hand[MAIN_HAND][1].value && !isSecondSplit && player.hand[MAIN_HAND][0].value != 1) {
                    mBinding.fragmentOnlineGameSplitBtn.isEnabled = true
                }
                if (player.hand[MAIN_HAND][0].value == 1) {
                    splitAce()
                    mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
                }
            } else {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
            }
        }

        //Player stop & Dealer draw cards
        mBinding.fragmentOnlineGameStopBtn.setOnClickListener {
            playerStop()
        }

        // Open dialog and choose a bet
        mBinding.fragmentOnlineGameFab.setOnClickListener {
            val betDialog = BetDialog()
            val bundle = Bundle()
            bundle.putLong(PLAYER_SAVE_ID, bank!!.id)
            betDialog.arguments = bundle
            betDialog.show(requireActivity().supportFragmentManager, betDialog.tag)
        }
    }

    private fun showInsurance() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(requireContext().resources.getString(R.string.fragment_main_game_insurance_title))
        alertDialog.setMessage(requireContext().resources.getString(R.string.fragment_main_game_insurance))
        alertDialog.setPositiveButton(requireContext().resources.getString(R.string.bet_dialog_ok)) { dialog, _ ->
            if (bank!!.amount >= bet.playerBet) {
                insuranceBet()
                decreaseBankWithInsurance()
                updatePlayerBankAmount()
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.fragment_main_game_assured),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
            }
        }
        alertDialog.setNegativeButton(requireContext().resources.getString(R.string.bet_dialog_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun blackjackGiven() {
        showGameOver()
        increaseBankWithBlackjack()
        updatePlayerBankAmount()
        mBinding.fragmentOnlineGameResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_blackjack_result)
        disableHitAndStopButtons()
    }

    private fun decreaseBank() {
        if (bank!!.amount >= bet.playerBet) {
            bank!!.amount -= bet.playerBet
        }
        Log.e(TAG, "refreshTotalSpent: initial bet: ${bet.playerBet} \tmain hand bet: ${bet.mainHandBet} \tfirst split bet: ${bet.firstSplitBet} \tsecond split bet: ${bet.secondSplitBet} \ttotal bet: ${bet.totalBet}")
    }

    private fun decreaseBankWithInsurance() {
        if (bank!!.amount > bet.insuranceBet) {
            bank!!.amount -= bet.insuranceBet
        }
    }

    private fun increaseBank(bet: Double) {
        bank!!.amount += bet
        Log.e(TAG, "refreshTotalSpent: initial bet: ${this.bet.playerBet} \tmain hand bet: ${this.bet.mainHandBet} \tfirst split bet: ${this.bet.firstSplitBet} \tsecond split bet: ${this.bet.secondSplitBet} \ttotal bet: ${this.bet.totalBet}")
    }

    private fun increaseBankWithBlackjack() {
        bank!!.amount += ((bet.playerBet * 2) + (bet.playerBet / 2))
    }

    private fun increaseBankWithInsurance() {
        bank!!.amount += bet.insuranceBet * 3
    }

    private fun insuranceBet() {
        bet.insuranceBet += (bet.playerBet / 2)
        bet.totalBet += bet.insuranceBet
    }

    private fun doubleBet() {
        when(handType) {
            HandType.MAIN_HAND -> {
                bet.mainHandBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
            HandType.FIRST_SPLIT -> {
                bet.firstSplitBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
            HandType.SECOND_SPLIT -> {
                bet.secondSplitBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
        }
        Log.e(TAG, "refreshTotalSpent: initial bet: ${bet.playerBet} \tmain hand bet: ${bet.mainHandBet} \tfirst split bet: ${bet.firstSplitBet} \tsecond split bet: ${bet.secondSplitBet} \ttotal bet: ${bet.totalBet}")
    }

    private fun splitBet() {
        when {
            isFirstSplit && !isSecondSplit -> {
                bet.firstSplitBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
            isSecondSplit && isFirstSplit -> {
                bet.secondSplitBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
        }
        Log.e(TAG, "refreshTotalSpent: initial bet: ${bet.playerBet} \tmain hand bet: ${bet.mainHandBet} \tfirst split bet: ${bet.firstSplitBet} \tsecond split bet: ${bet.secondSplitBet} \ttotal bet: ${bet.totalBet}")
    }

    private fun compareScore(score: Int, bet: Double): CharSequence? {
        var text: String? = null
        Log.e(TAG, "compareScore: player score: ${player.score[MAIN_HAND]} \tdealer score: ${dealer.score}")
        if (score > dealer.score && score == 21 && player.hand[MAIN_HAND].size == 2) {
            increaseBankWithBlackjack()
            text = requireContext().resources.getString(R.string.fragment_main_game_you_win)
        } else {
            when {
                score > 21 -> {
                    text = requireContext().resources.getString(R.string.fragment_main_game_you_lose)
                }
                score > dealer.score -> {
                    increaseBank(bet * 2)
                    text = requireContext().resources.getString(R.string.fragment_main_game_you_win)
                }
                score < dealer.score -> {
                    text = requireContext().resources.getString(R.string.fragment_main_game_you_lose)
                }
                score == dealer.score -> {
                    increaseBank(bet)
                    text = requireContext().resources.getString(R.string.fragment_main_game_draw)
                }
            }
        }
        showGameOver()
        return text
    }

    private fun compareWithDealerBlackjack() {
        if (
            player.score[MAIN_HAND] == 21 &&
            player.hand[MAIN_HAND].size == 2 &&
            player.score[FIRST_SPLIT] == 0 &&
            player.score[SECOND_SPLIT] == 0
        ) {
            mBinding.fragmentOnlineGameResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_draw)
            increaseBank(bet.mainHandBet)
        } else {
            mBinding.fragmentOnlineGameResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_you_lose)
        }
        showGameOver()
    }

    private fun playerDrawCard() {
        var score: Int
        when (handType) {
            // Card draw on the first split
            HandType.FIRST_SPLIT -> {
                player.score[FIRST_SPLIT] += deck.deckList[cardsDraw].value!!
                score = player.score[FIRST_SPLIT]
                player.hand[FIRST_SPLIT].add(deck.deckList[cardsDraw])
            }
            //Card draw on the second split
            HandType.SECOND_SPLIT -> {
                player.score[SECOND_SPLIT] += deck.deckList[cardsDraw].value!!
                score = player.score[SECOND_SPLIT]
                player.hand[SECOND_SPLIT].add(deck.deckList[cardsDraw])
            }
            // Card draw on the main hand
            else -> {
                player.score[MAIN_HAND] += deck.deckList[cardsDraw].value!!
                score = player.score[MAIN_HAND]
                player.hand[MAIN_HAND].add(deck.deckList[cardsDraw])
            }
        }
        // If the player got 2 cards he can double, doesn't matter the hand.
        if (
            player.hand[MAIN_HAND].size > 2 && handType == HandType.MAIN_HAND ||
                    player.hand[FIRST_SPLIT].size > 2 && handType == HandType.FIRST_SPLIT ||
                    player.hand[SECOND_SPLIT].size > 2 && handType == HandType.SECOND_SPLIT
        ) {
            mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
        }
        setupPlayerHandRecyclerView()
        setupPlayerFirstSplitRecyclerView()
        setupPlayerSecondSplitRecyclerView()
        loadPlayerHandIntoRecyclerView(player.hand[MAIN_HAND])
        loadPlayerFirstSplitIntoRecyclerView(player.hand[FIRST_SPLIT])
        loadPlayerSecondSplitIntoRecyclerView(player.hand[SECOND_SPLIT])

        Log.e(TAG, "playerDrawCard: ${deck.deckList[cardsDraw].number} ${deck.deckList[cardsDraw].color} ${deck.deckList[cardsDraw].value}")

        if (deck.deckList[cardsDraw].number == NumberType.ACE && !isPlayerDrawAce) {
            isPlayerDrawAce = true
        }

        if (deck.deckList[cardsDraw].number == NumberType.ACE && score > 11 && isPlayerDrawAce && !isPlayerScoreSoft) {
            isPlayerDrawAce = false
        }

        if (deck.deckList[cardsDraw].number == NumberType.ACE && score < 12 && isPlayerDrawAce) {
            score += 10
            when (handType) {
                HandType.MAIN_HAND -> player.score[MAIN_HAND] += 10
                HandType.FIRST_SPLIT -> player.score[FIRST_SPLIT] += 10
                HandType.SECOND_SPLIT -> player.score[SECOND_SPLIT] += 10
            }
            isPlayerScoreSoft = true
        }

        if (score > 21 && isPlayerDrawAce) {
            score -= 10
            when (handType) {
                HandType.MAIN_HAND -> player.score[MAIN_HAND] -= 10
                HandType.FIRST_SPLIT -> player.score[FIRST_SPLIT] -= 10
                HandType.SECOND_SPLIT -> player.score[SECOND_SPLIT] -= 10
            }
            isPlayerDrawAce = false
            isPlayerScoreSoft = false
        }

        showScore()

        when {
            score > 21 && !isFirstSplit && !isSecondSplit -> playerBust()
            score > 21 && handType == HandType.MAIN_HAND && isFirstSplit -> {
                playerStop()
                bet.totalBet -= bet.mainHandBet
            }
            score > 21 && handType == HandType.FIRST_SPLIT -> {
                playerStop()
                bet.totalBet -= bet.firstSplitBet
            }
            score > 21 && handType == HandType.SECOND_SPLIT -> {
                playerStop()
                bet.totalBet -= bet.secondSplitBet
            }
        }
        cardsDraw++
    }

    private fun playerBust() {
        mBinding.fragmentOnlineGameResultTv.text =
            requireContext().resources.getString(R.string.fragment_main_game_you_lose)
        showGameOver()
        disableHitAndStopButtons()
    }

    private fun playerStop() {
        when {
            // Player stop the first split and draw a card on the second split.
            handType == HandType.FIRST_SPLIT && isSecondSplit -> {
                handType = HandType.SECOND_SPLIT
                playerDrawCard()
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = true
                isFirstSplit = false
            }
            // Player stop the main hand et draw a card on the first split.
            handType == HandType.MAIN_HAND && isFirstSplit -> {
                handType = HandType.FIRST_SPLIT
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = true
                playerDrawCard()
                if (player.hand[FIRST_SPLIT][0].value == player.hand[FIRST_SPLIT][1].value && !isSecondSplit) {
                    mBinding.fragmentOnlineGameSplitBtn.isEnabled = true
                }
            }
            // Bank draw cards.
            else -> {
                while (dealer.score < 17) {
                    dealerDrawCard()
                }
                if (dealer.score < 22) {

                    if (player.score[FIRST_SPLIT] > 0 ) {
                        mBinding.fragmentOnlineGameFirstSplitResultTv.text = compareScore(player.score[FIRST_SPLIT], bet.firstSplitBet)
                            mBinding.fragmentOnlineGameFirstSplitResultTv.visibility = View.VISIBLE
                    }

                    if (player.score[SECOND_SPLIT] > 0) {
                        mBinding.fragmentOnlineGameSecondSplitResultTv.text = compareScore(player.score[SECOND_SPLIT], bet.secondSplitBet)
                            mBinding.fragmentOnlineGameSecondSplitResultTv.visibility = View.VISIBLE
                    }

                    if (dealer.score == 21 && dealer.hand.size == 2) {
                        if (bet.insuranceBet > 0) {
                            increaseBankWithInsurance()
                            Log.e(TAG, "refreshTotalSpent: initial bet: ${bet.playerBet} \tmain hand bet: ${bet.mainHandBet} \tfirst split bet: ${bet.firstSplitBet} \tsecond split bet: ${bet.secondSplitBet} \tinsurance bet: ${bet.insuranceBet} \ttotal bet: ${bet.totalBet}")
                            Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_insurance_win), Toast.LENGTH_SHORT).show()
                        }
                        compareWithDealerBlackjack()
                    } else if (player.score[MAIN_HAND] == 21 && player.hand[MAIN_HAND].size == 2 && player.hand[FIRST_SPLIT].size == 0) {
                        increaseBankWithBlackjack()
                        showGameOver()
                        mBinding.fragmentOnlineGameResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_you_win)
                    } else {
                        mBinding.fragmentOnlineGameResultTv.text = compareScore(player.score[MAIN_HAND], bet.mainHandBet)
                        if (bet.insuranceBet > 0) {
                            Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_insurance_lose), Toast.LENGTH_SHORT).show()
                        }
                    }

                    updatePlayerBankAmount()
                    disableHitAndStopButtons()
                } else {
                    if (player.score[MAIN_HAND] == 21 && player.hand[MAIN_HAND].size == 2 && player.hand[FIRST_SPLIT].size == 0) {
                        increaseBankWithBlackjack()
                        mBinding.fragmentOnlineGameResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_you_win)
                    } else {
                        mBinding.fragmentOnlineGameResultTv.text =
                            requireContext().resources.getString(R.string.online_game_fragment_bust)
                        increaseBank(bet.totalBet * 2)
                    }
                    showGameOver()
                    updatePlayerBankAmount()
                    disableHitAndStopButtons()
                }
                // Dealer does not have enough card to continue and shuffle the deck
                if (cardsDraw > 260) {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_shuffle_deck), Toast.LENGTH_SHORT).show()
                    cardsDraw = 0
                    deck.deckList.clear()
                    prepareDeck()
                }
            }
        }
    }

    private fun splitPlayerGame() {
        // Add card in second split from main hand or from first split.
        Log.e(TAG, "splitPlayerGame: hand type: $handType")
        if (!isSecondSplit && isFirstSplit && handType == HandType.MAIN_HAND) {
            player.hand[SECOND_SPLIT].add(player.hand[MAIN_HAND][player.hand[MAIN_HAND].size - 1])
            player.score[SECOND_SPLIT] += player.hand[MAIN_HAND][player.hand[MAIN_HAND].size - 1].value!!
            mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.VISIBLE
            isSecondSplit = true
        } else if (handType == HandType.FIRST_SPLIT) {
            player.hand[SECOND_SPLIT].add(player.hand[FIRST_SPLIT][player.hand[FIRST_SPLIT].size - 1])
            player.score[SECOND_SPLIT] += player.hand[FIRST_SPLIT][player.hand[FIRST_SPLIT].size - 1].value!!
            mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.VISIBLE
            isSecondSplit = true
        }
        // Add card in first split from main hand.
        if (!isFirstSplit) {
            player.hand[FIRST_SPLIT].add(player.hand[MAIN_HAND][player.hand[MAIN_HAND].size - 1])
            player.score[FIRST_SPLIT] += player.hand[MAIN_HAND][player.hand[MAIN_HAND].size - 1].value!!
            mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.VISIBLE
            isFirstSplit = true
        }
        // Refresh main hand score or first split score.
        if (handType == HandType.MAIN_HAND) {
            player.score[MAIN_HAND] -= player.hand[MAIN_HAND][player.hand[MAIN_HAND].size - 1].value!!
            player.hand[MAIN_HAND].remove(player.hand[MAIN_HAND][player.hand[MAIN_HAND].size - 1])
        } else {
            player.score[FIRST_SPLIT] -= player.hand[FIRST_SPLIT][player.hand[FIRST_SPLIT].size - 1].value!!
            player.hand[FIRST_SPLIT].remove(player.hand[FIRST_SPLIT][player.hand[FIRST_SPLIT].size - 1])
        }
        Log.e(TAG, "splitPlayerGame: hand type: $handType")
        splitBet()
        playerDrawCard()
    }

    private fun splitAce() {
        playerStop()
        player.score[FIRST_SPLIT] += 10
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.VISIBLE
        playerStop()
    }

    private fun dealerDrawCard() {
        dealer.score += deck.deckList[cardsDraw].value!!
        dealer.hand.add(deck.deckList[cardsDraw])
        setupDealerHandRecyclerView()
        loadDealerHandIntoRecyclerView(dealer.hand)

        if (deck.deckList[cardsDraw].number == NumberType.ACE && !isDealerDrawAce) {
            isDealerDrawAce = true
        }

        if (deck.deckList[cardsDraw].number == NumberType.ACE && dealer.score > 11 && isDealerDrawAce && !isDealerScoreSoft) {
            isDealerDrawAce = false
        }

        if (deck.deckList[cardsDraw].number == NumberType.ACE && dealer.score < 12 && isDealerDrawAce) {
            dealer.score += 10

            isDealerScoreSoft = true
        }

        if (dealer.score > 21 && isDealerDrawAce) {
            dealer.score -= 10
            isDealerDrawAce = false
            isDealerScoreSoft = false
        }

        showScore()
        Log.e(TAG, "dealerDrawCard: ${deck.deckList[cardsDraw].number} ${deck.deckList[cardsDraw].color} ${deck.deckList[cardsDraw].value}")
        cardsDraw++
    }

    private fun disableHitAndStopButtons() {
        mBinding.fragmentOnlineGameHitBtn.isEnabled = !mBinding.fragmentOnlineGameHitBtn.isEnabled
        mBinding.fragmentOnlineGameStopBtn.isEnabled = !mBinding.fragmentOnlineGameStopBtn.isEnabled
        mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
        mBinding.fragmentOnlineGameSplitBtn.isEnabled = false
        mBinding.fragmentOnlineGameGameStart.isEnabled = bet.playerBet != 0.0
    }

    private fun showGameOver() {
        mBinding.fragmentOnlineGameGameStart.visibility = View.VISIBLE
        mBinding.fragmentOnlineGameResultTv.visibility = View.VISIBLE
    }

    private fun hideGameOver() {
        mBinding.fragmentOnlineGameGameStart.visibility = View.GONE
        mBinding.fragmentOnlineGameResultTv.visibility = View.GONE
        mBinding.fragmentOnlineGameFirstSplitResultTv.visibility = View.GONE
        mBinding.fragmentOnlineGameSecondSplitResultTv.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.GONE
    }

    private fun hideReadyPictures() {
        mBinding.apply {
            fragmentOnlineGameCurrentUserReadyImg.visibility = View.GONE
            fragmentOnlineGameOpponentReadyImg.visibility = View.GONE
        }
    }

    private fun setupPlayerHandRecyclerView() = mBinding.fragmentOnlineGamePlayerRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerFirstSplitRecyclerView() = mBinding.fragmentOnlineGamePlayerFirstSplitRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerSecondSplitRecyclerView() = mBinding.fragmentOnlineGamePlayerSecondSplitRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupDealerHandRecyclerView() = mBinding.fragmentOnlineGameDealerRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadPlayerHandIntoRecyclerView(hand: ArrayList<Card>) {
        playerHandAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerRecyclerView.adapter = playerHandAdapter
    }

    private fun loadPlayerFirstSplitIntoRecyclerView(hand: ArrayList<Card>) {
        playerFirstSplitAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerFirstSplitRecyclerView.adapter = playerFirstSplitAdapter
    }

    private fun loadPlayerSecondSplitIntoRecyclerView(hand: ArrayList<Card>) {
        playerSecondSplitAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerSecondSplitRecyclerView.adapter = playerSecondSplitAdapter
    }

    private fun loadDealerHandIntoRecyclerView(hand: ArrayList<Card>) {
        dealerHandAdapter.submitList(hand)
        mBinding.fragmentOnlineGameDealerRecyclerView.adapter = dealerHandAdapter
    }

    companion object {
        const val PLAYER_SAVE_ID = "player_save_id"
    }
}