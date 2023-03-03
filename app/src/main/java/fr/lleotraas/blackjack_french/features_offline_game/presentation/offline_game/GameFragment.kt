package fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType
import fr.lleotraas.blackjack_french.features_offline_game.domain.service.TimeService
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.FIRST_SPLIT
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.MAIN_HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.SECOND_SPLIT
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.createDeck
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.createPlayerHand
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.shuffleDeck
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter.GameAdapter
import fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog.BetDialog
import fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game.OfflineGameFragment.Companion.PLAYER_SAVE_ID
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class GameFragment : Fragment() {

    private lateinit var mBinding: FragmentOnlineGameBinding
    private lateinit var deck: Deck
    private var dealer = Dealer(0, ArrayList(), isDealerDrawAce = false, isDealerScoreSoft = false)
    private var player = createPlayerHand(0, 0.0)
    private var cardsDraw = 0
    private var isPlayerDrawAce = false
    private var isDealerDrawAce = false
    private var isPlayerScoreSoft = false
    private var isDealerScoreSoft = false
    private var isFirstSplit = false
    private var isSecondSplit = false
    private var handType = HandType.MainHand.toHandType(MAIN_HAND)
    private lateinit var playerHandAdapter: GameAdapter
    private lateinit var playerFirstSplitAdapter: GameAdapter
    private lateinit var playerSecondSplitAdapter: GameAdapter
    private lateinit var dealerHandAdapter: GameAdapter
    private var wallet: Wallet? = null
    private var bet = Bet(0.0,0.0,0.0,0.0,0.0, 0.0)
    private var serviceIntent: Intent? = null
    private var broadcastTimer: Intent? = null
    private var time = -1.0
    private var currentTime = 0.0
    private var isTimerStarted = false
    private val mViewModel: GameActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOnlineGameBinding.inflate(inflater, container, false)
        val bankId = requireActivity().intent.extras!!.get(PLAYER_SAVE_ID) as Long
        initAdapter()
        loadBank(bankId)
        loadBet()
        updateUI()
        initBroadcastTimer()
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

    private fun updateUI() {
        mBinding.fragmentOnlineGameOpponentBetBtn.visibility = View.GONE
    }

    private fun initAdapter() {
        playerHandAdapter = GameAdapter()
        playerFirstSplitAdapter = GameAdapter()
        playerSecondSplitAdapter = GameAdapter()
        dealerHandAdapter = GameAdapter()
    }

    private fun loadBank(bankId: Long) {
//        mViewModel.getBank(bankId).observe(viewLifecycleOwner) { currentBank ->
//            wallet = currentBank
//            mBinding.fragmentOnlineGameBankAmountTv.text = String.format("%s", currentBank.amount)
//
//            Log.e(TAG, "refreshTotalSpent: initial bet: ${this.bet.playerBet} \tmain hand bet: ${this.bet.mainHandBet} \tfirst split bet: ${this.bet.firstSplitBet} \tsecond split bet: ${this.bet.secondSplitBet} \ttotal bet: ${this.bet.totalBet}")
//        }
    }

    private fun showLoanDialog() = AlertDialog.Builder(requireContext()).apply {
        setTitle(requireContext().resources.getString(R.string.online_game_fragment_contract_loan))
        setMessage(requireContext().resources.getString(R.string.online_game_fragment_loan_offline_message))
        setCancelable(false)
        setPositiveButton(requireContext().resources.getString(R.string.dialog_invitation_to_play_positive_btn)) { dialogInterface, _ ->
            wallet?.amount = wallet?.amount?.plus(500.0)!!
            lifecycleScope.launch {
                mViewModel.updateBank(wallet!!)
            }
            mBinding.fragmentOnlineGameContractLoanBtn.visibility = View.GONE
            dialogInterface.dismiss()
        }
        setNegativeButton(requireContext().resources.getString(R.string.dialog_invitation_to_play_negative_btn)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
    }

    private fun initBroadcastTimer() {
        serviceIntent = Intent(requireContext(), TimeService::class.java)
        broadcastTimer = requireActivity().registerReceiver(updateTime, IntentFilter(TimeService.TIMER_UPDATED))
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimeService.TIME_EXTRA, 0.0)
            event(time)
            Log.e(javaClass.simpleName, "updateTime onReceive: ${time}sec")
        }
    }

    private fun resetTimer() {
        time = -1.0
        currentTime = 0.0
    }

    private fun event(time: Double) {
        if (time == currentTime && dealer.score < 17) {
            currentTime++
            dealerDrawCard()
        }

        if (time == currentTime && dealer.score > 16) {
            resetTimer()
            stopTimer()
            dealerSequence()
        }
    }

    private fun startTimer() {
        if (!isTimerStarted) {
            serviceIntent?.putExtra(TimeService.TIME_EXTRA, time)
            requireActivity().startService(serviceIntent)
            isTimerStarted = true
        }
    }

    private fun stopTimer() {
        if (isTimerStarted) {
            requireActivity().stopService(serviceIntent)
            isTimerStarted = false
        }
    }

    private fun stopServiceAndUnregisterReceiver() {
        requireActivity().unregisterReceiver(updateTime)
    }

    private fun loadBet() {
        mViewModel.getBet().observe(viewLifecycleOwner) { currentBet ->
//            bet = currentBet
//            bet.totalBet = currentBet.playerBet
//            bet.mainHandBet = currentBet.playerBet
            showBetInUI()
            if (bet.playerBet > 0) {
                mBinding.fragmentOnlineGameGameStart.isEnabled = true
            }
        }
    }

    private fun updatePlayerBankAmount() {
        lifecycleScope.launch {
            mViewModel.updateBank(wallet!!)
        }
    }

    private fun prepareDeck() {
        deck = createDeck()
        Log.e(TAG, "beginGame: deck size = ${deck.deckList.size}")
        shuffleDeck(deck.deckList)
    }

    private fun showScore() {
        mBinding.fragmentOnlineGamePlayerOneMainHandScoreTv.text = String.format("%s", player.score[MAIN_HAND])
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.text = String.format("%s", player.score[FIRST_SPLIT])
        mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.text = String.format("%s", player.score[SECOND_SPLIT])
        mBinding.fragmentOnlineGameDealerScoreTv.text = String.format("%s", dealer.score)
    }

    private fun configureListeners() {
        // Start Button
        mBinding.fragmentOnlineGameGameStart.setOnClickListener {
            if (wallet!!.amount >= bet.playerBet) {
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
                handType = HandType.MainHand.toHandType(MAIN_HAND)
                player.hand[MAIN_HAND]?.clear()
                player.hand[FIRST_SPLIT]?.clear()
                player.hand[SECOND_SPLIT]?.clear()
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
            if (player.hand[MAIN_HAND]?.get(0)?.value == player.hand[MAIN_HAND]?.get(1)?.value) {
                mBinding.fragmentOnlineGameSplitBtn.isEnabled = true
            }

            // Player start with blackjack
            if (player.score[MAIN_HAND] == 21) {
                mBinding.fragmentOnlineGamePlayerOneMainHandScoreTv.text = requireContext().resources.getString(R.string.fragment_main_game_blackjack)
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
            if (wallet!!.amount >= bet.playerBet) {
                playerDrawCard()
                decreaseBank()
                updatePlayerBankAmount()
                doubleBet()
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
                when {
                    player.score[MAIN_HAND]!! < 22 -> playerStop()
                    player.score[FIRST_SPLIT] in 1..21 -> playerStop()
                    player.score[SECOND_SPLIT] in 1..21 -> playerStop()
                }
            } else {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
            }
        }

        //Split
        mBinding.fragmentOnlineGameSplitBtn.setOnClickListener {
            if (wallet!!.amount >= bet.playerBet) {
                splitPlayerGame()
                decreaseBank()
                updatePlayerBankAmount()
                mBinding.fragmentOnlineGameSplitBtn.isEnabled = false
                if (player.hand[MAIN_HAND]?.get(0)?.value == player.hand[MAIN_HAND]?.get(1)?.value && !isSecondSplit && player.hand[MAIN_HAND]?.get(0)?.value != 1) {
                    mBinding.fragmentOnlineGameSplitBtn.isEnabled = true
                }
                if (player.hand[MAIN_HAND]?.get(0)?.value == 1) {
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
            bundle.putLong(PLAYER_SAVE_ID, wallet!!.id)
            betDialog.arguments = bundle
            betDialog.show(requireActivity().supportFragmentManager, betDialog.tag)
        }

        // LOAN DIALOG
        mBinding.fragmentOnlineGameContractLoanBtn.setOnClickListener {
            showLoanDialog().show()
        }
    }

    private fun showInsurance() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(requireContext().resources.getString(R.string.fragment_main_game_insurance_title))
        alertDialog.setMessage(requireContext().resources.getString(R.string.fragment_main_game_insurance))
        alertDialog.setPositiveButton(requireContext().resources.getString(R.string.bet_dialog_ok)) { dialog, _ ->
            if (wallet!!.amount >= bet.playerBet) {
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
        mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_blackjack_result)
        disableHitAndStopButtons()
    }

    private fun decreaseBank() {
        if (wallet!!.amount >= bet.playerBet) {
            wallet!!.amount -= bet.playerBet
        }
        Log.e(TAG, "refreshTotalSpent: initial bet: ${bet.playerBet} \tmain hand bet: ${bet.mainHandBet} \tfirst split bet: ${bet.firstSplitBet} \tsecond split bet: ${bet.secondSplitBet} \ttotal bet: ${bet.totalBet}")
    }

    private fun decreaseBankWithInsurance() {
        if (wallet!!.amount > bet.insuranceBet) {
            wallet!!.amount -= bet.insuranceBet
        }
    }

    private fun increaseBank(bet: Double) {
        wallet!!.amount += bet
        Log.e(TAG, "refreshTotalSpent: initial bet: ${this.bet.playerBet} \tmain hand bet: ${this.bet.mainHandBet} \tfirst split bet: ${this.bet.firstSplitBet} \tsecond split bet: ${this.bet.secondSplitBet} \ttotal bet: ${this.bet.totalBet}")
    }

    private fun increaseBankWithBlackjack() {
        wallet!!.amount += ((bet.playerBet * 2) + (bet.playerBet / 2))
    }

    private fun increaseBankWithInsurance() {
        wallet!!.amount += bet.insuranceBet * 3
    }

    private fun insuranceBet() {
        bet.insuranceBet += (bet.playerBet / 2)
        bet.totalBet += bet.insuranceBet
    }

    private fun doubleBet() {
        when(handType.valueOf) {
            MAIN_HAND -> {
                bet.mainHandBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
            FIRST_SPLIT -> {
                bet.firstSplitBet += bet.playerBet
                bet.totalBet += bet.playerBet
            }
            SECOND_SPLIT -> {
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
        if (score > dealer.score && score == 21 && player.hand[MAIN_HAND]?.size == 2) {
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
            player.hand[MAIN_HAND]?.size == 2 &&
            player.score[FIRST_SPLIT] == 0 &&
            player.score[SECOND_SPLIT] == 0
        ) {
            mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_draw)
            increaseBank(bet.mainHandBet)
        } else {
            mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_you_lose)
        }
        showGameOver()
    }

    private fun playerDrawCard() {
        var score: Int
        when (handType.valueOf) {
            // Card draw on the first split
            FIRST_SPLIT -> {
                player.score[FIRST_SPLIT] = player.score[FIRST_SPLIT]!!.plus(deck.deckList[cardsDraw].value!!)
                score = player.score[FIRST_SPLIT]!!
                player.hand[FIRST_SPLIT]?.add(deck.deckList[cardsDraw])
            }
            //Card draw on the second split
            SECOND_SPLIT -> {
                player.score[SECOND_SPLIT] = player.score[SECOND_SPLIT]!!.plus(deck.deckList[cardsDraw].value!!)
                score = player.score[SECOND_SPLIT]!!
                player.hand[SECOND_SPLIT]?.add(deck.deckList[cardsDraw])
            }
            // Card draw on the main hand
            else -> {
                player.score[MAIN_HAND] = player.score[MAIN_HAND]!!.plus(deck.deckList[cardsDraw].value!!)
                score = player.score[MAIN_HAND]!!
                player.hand[MAIN_HAND]!!.add(deck.deckList[cardsDraw])
            }
        }
        // If the player got 2 cards he can double, doesn't matter the hand.
        if (
            player.hand[MAIN_HAND]!!.size > 2 && handType.valueOf == MAIN_HAND ||
                    player.hand[FIRST_SPLIT]!!.size > 2 && handType.valueOf == FIRST_SPLIT ||
                    player.hand[SECOND_SPLIT]!!.size > 2 && handType.valueOf == SECOND_SPLIT
        ) {
            mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
        }
//        setupPlayerHandRecyclerView()
        setupPlayerFirstSplitRecyclerView()
        setupPlayerSecondSplitRecyclerView()
//        loadPlayerHandIntoRecyclerView(player.hand[MAIN_HAND]!!)
        loadPlayerFirstSplitIntoRecyclerView(player.hand[FIRST_SPLIT]!!)
        loadPlayerSecondSplitIntoRecyclerView(player.hand[SECOND_SPLIT]!!)

        Log.e(TAG, "playerDrawCard: ${deck.deckList[cardsDraw].number} ${deck.deckList[cardsDraw].color} ${deck.deckList[cardsDraw].value}")

        if (deck.deckList[cardsDraw].number == NumberType.ACE && !isPlayerDrawAce) {
            isPlayerDrawAce = true
        }

        if (deck.deckList[cardsDraw].number == NumberType.ACE && score > 11 && isPlayerDrawAce && !isPlayerScoreSoft) {
            isPlayerDrawAce = false
        }

        if (deck.deckList[cardsDraw].number == NumberType.ACE && score < 12 && isPlayerDrawAce) {
            score += 10
            when (handType.valueOf) {
                MAIN_HAND -> player.score[MAIN_HAND]?.plus(10)
                FIRST_SPLIT -> player.score[FIRST_SPLIT]?.plus(10)
                else -> player.score[SECOND_SPLIT]?.plus(10)
            }
            isPlayerScoreSoft = true
        }

        if (score > 21 && isPlayerDrawAce) {
            score -= 10
            when (handType.valueOf) {
                MAIN_HAND -> player.score[MAIN_HAND]?.minus(10)
                FIRST_SPLIT -> player.score[FIRST_SPLIT]?.minus(10)
                else -> player.score[SECOND_SPLIT]?.minus(10)
            }
            isPlayerDrawAce = false
            isPlayerScoreSoft = false
        }

        showScore()

        when {
            score > 21 && !isFirstSplit && !isSecondSplit -> playerBust()
            score > 21 && handType.valueOf == MAIN_HAND && isFirstSplit -> {
                playerStop()
                bet.totalBet -= bet.mainHandBet
            }
            score > 21 && handType.valueOf == FIRST_SPLIT -> {
                playerStop()
                bet.totalBet -= bet.firstSplitBet
            }
            score > 21 && handType.valueOf == SECOND_SPLIT -> {
                playerStop()
                bet.totalBet -= bet.secondSplitBet
            }
        }
        cardsDraw++
    }

    private fun playerBust() {
        mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text =
            requireContext().resources.getString(R.string.fragment_main_game_you_lose)
        showGameOver()
        disableHitAndStopButtons()
    }

    private fun playerStop() {
        when {
            // Player stop the first split and draw a card on the second split.
            handType.valueOf == FIRST_SPLIT && isSecondSplit -> {
                handType = HandType.MainHand.toHandType(SECOND_SPLIT)
                playerDrawCard()
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = true
                isFirstSplit = false
            }
            // Player stop the main hand et draw a card on the first split.
            handType.valueOf == MAIN_HAND && isFirstSplit -> {
                handType = HandType.MainHand.toHandType(FIRST_SPLIT)
                mBinding.fragmentOnlineGameDoubleBtn.isEnabled = true
                playerDrawCard()
                if (player.hand[FIRST_SPLIT]?.get(0)?.value == player.hand[FIRST_SPLIT]?.get(1)?.value && !isSecondSplit) {
                    mBinding.fragmentOnlineGameSplitBtn.isEnabled = true
                }
            }
            // Bank draw cards.
            else -> {
                startTimer()
                disableHitAndStopButtons()
            }
        }
    }

    private fun dealerSequence() {

        if (dealer.score < 22) {

            if (player.score[FIRST_SPLIT]!! > 0 ) {
                mBinding.fragmentOnlineGameFirstSplitResultTv.text = compareScore(player.score[FIRST_SPLIT]!!, bet.firstSplitBet)
                mBinding.fragmentOnlineGameFirstSplitResultTv.visibility = View.VISIBLE
            }

            if (player.score[SECOND_SPLIT]!! > 0) {
                mBinding.fragmentOnlineGameSecondSplitResultTv.text = compareScore(player.score[SECOND_SPLIT]!!, bet.secondSplitBet)
                mBinding.fragmentOnlineGameSecondSplitResultTv.visibility = View.VISIBLE
            }

            if (dealer.score == 21 && dealer.hand.size == 2) {
                if (bet.insuranceBet > 0) {
                    increaseBankWithInsurance()
                    Log.e(TAG, "refreshTotalSpent: initial bet: ${bet.playerBet} \tmain hand bet: ${bet.mainHandBet} \tfirst split bet: ${bet.firstSplitBet} \tsecond split bet: ${bet.secondSplitBet} \tinsurance bet: ${bet.insuranceBet} \ttotal bet: ${bet.totalBet}")
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_insurance_win), Toast.LENGTH_SHORT).show()
                }
                compareWithDealerBlackjack()
            } else if (player.score[MAIN_HAND] == 21 && player.hand[MAIN_HAND]?.size == 2 && player.hand[FIRST_SPLIT]?.size == 0) {
                increaseBankWithBlackjack()
                showGameOver()
                mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_you_win)
            } else {
                mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text = compareScore(player.score[MAIN_HAND]!!, bet.mainHandBet)
                if (bet.insuranceBet > 0) {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_insurance_lose), Toast.LENGTH_SHORT).show()
                }
            }

            updatePlayerBankAmount()
        } else {
            if (player.score[MAIN_HAND] == 21 && player.hand[MAIN_HAND]?.size == 2 && player.hand[FIRST_SPLIT]?.size == 0) {
                increaseBankWithBlackjack()
                mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_you_win)
            } else {
                mBinding.fragmentOnlineGameDealerResultTv.text =
                    requireContext().resources.getString(R.string.online_game_fragment_bust)
                increaseBank(bet.totalBet * 2)
            }
            showGameOver()
            updatePlayerBankAmount()
        }
        // Dealer does not have enough card to continue and shuffle the deck
        if (cardsDraw > 260) {
            Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_shuffle_deck), Toast.LENGTH_SHORT).show()
            cardsDraw = 0
            deck.deckList.clear()
            prepareDeck()
        }
    }

    private fun splitPlayerGame() {
        // Add card in second split from main hand or from first split.
        Log.e(TAG, "splitPlayerGame: hand type: $handType")
        //TODO When split game, main score isn't decremented and FirstSplit isn't incremented correctly
        if (!isSecondSplit && isFirstSplit && handType.valueOf == MAIN_HAND) {
            player.hand[SECOND_SPLIT]?.add(player.hand[MAIN_HAND]!![player.hand[MAIN_HAND]?.size?.minus(1)!!])
            player.score[SECOND_SPLIT]?.plus(player.hand[MAIN_HAND]!![player.hand[MAIN_HAND]?.size?.minus(1)!!].value!!)
            mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.VISIBLE
            isSecondSplit = true
        } else if (handType.valueOf == FIRST_SPLIT) {
            player.hand[SECOND_SPLIT]?.add(player.hand[FIRST_SPLIT]!![player.hand[FIRST_SPLIT]?.size?.minus(1)!!])
            player.score[SECOND_SPLIT]?.plus(player.hand[FIRST_SPLIT]!![player.hand[FIRST_SPLIT]?.size?.minus(1)!!].value!!)
            mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.VISIBLE
            isSecondSplit = true
        }
        // Add card in first split from main hand.
        if (!isFirstSplit) {
            player.hand[FIRST_SPLIT]?.add(player.hand[MAIN_HAND]!![player.hand[MAIN_HAND]?.size?.minus(1)!!])
            player.score[FIRST_SPLIT]?.plus(player.hand[MAIN_HAND]!![player.hand[MAIN_HAND]?.size?.minus(1)!!].value!!)
            mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.VISIBLE
            isFirstSplit = true
        }
        // Refresh main hand score or first split score.
        if (handType.valueOf == MAIN_HAND) {
            player.score[MAIN_HAND]?.minus(player.hand[MAIN_HAND]!![player.hand[MAIN_HAND]?.size?.minus(1)!!].value!!)
            player.hand[MAIN_HAND]?.remove(player.hand[MAIN_HAND]!![player.hand[MAIN_HAND]?.size?.minus(1)!!])
        } else {
            player.score[FIRST_SPLIT]?.minus(player.hand[FIRST_SPLIT]!![player.hand[FIRST_SPLIT]?.size?.minus(1)!!].value!!)
            player.hand[FIRST_SPLIT]?.remove(player.hand[FIRST_SPLIT]!![player.hand[FIRST_SPLIT]?.size?.minus(1)!!])
        }
        Log.e(TAG, "splitPlayerGame: hand type: ${handType.valueOf}")
        splitBet()
        playerDrawCard()
    }

    private fun splitAce() {
        playerStop()
        player.score[FIRST_SPLIT]?.plus(10)
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
        mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.visibility = View.VISIBLE
        showLoanBtn()
    }

    private fun showLoanBtn() {
        if (
            wallet != null &&
            wallet?.amount!! < 100
        ) {
            mBinding.fragmentOnlineGameContractLoanBtn.visibility = View.VISIBLE
        } else {
            mBinding.fragmentOnlineGameContractLoanBtn.visibility = View.GONE
        }
    }

    private fun showBetInUI() {
        val betToShow = if (bet.totalBet.toString().last() == '0') bet.totalBet.toInt() else bet.totalBet
        mBinding.fragmentOnlineGamePlayerBetBtn.text = String.format(" %s%s", requireContext().resources.getString(R.string.fragment_main_game_current_bet), betToShow)
    }

    private fun hideGameOver() {
        mBinding.fragmentOnlineGameGameStart.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerOneMainHandResultTv.visibility = View.GONE
        mBinding.fragmentOnlineGameFirstSplitResultTv.visibility = View.GONE
        mBinding.fragmentOnlineGameSecondSplitResultTv.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.GONE
    }

    private fun hideReadyPictures() {
        mBinding.apply {
            fragmentOnlineGameCurrentUserReadyImg.visibility = View.GONE
            fragmentOnlineGameOpponentReadyImg.visibility = View.GONE
        }
    }

//    private fun setupPlayerHandRecyclerView() = mBinding.fragmentOnlineGamePlayerRecyclerView.apply {
//        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//    }

    private fun setupPlayerFirstSplitRecyclerView() = mBinding.fragmentOnlineGamePlayerFirstSplitRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerSecondSplitRecyclerView() = mBinding.fragmentOnlineGamePlayerSecondSplitRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupDealerHandRecyclerView() = mBinding.fragmentOnlineGameDealerRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

//    private fun loadPlayerHandIntoRecyclerView(hand: ArrayList<Card>) {
//        playerHandAdapter.submitList(hand)
//        mBinding.fragmentOnlineGamePlayerRecyclerView.adapter = playerHandAdapter
//    }

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



    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        stopServiceAndUnregisterReceiver()
    }
}