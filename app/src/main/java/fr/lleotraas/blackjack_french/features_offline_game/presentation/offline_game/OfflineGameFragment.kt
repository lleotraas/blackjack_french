package fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineGameCopyBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_offline_game.domain.service.TimeService
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter.GameAdapter
import fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter.PlayerBoardAdapter
import fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog.BetDialog
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OfflineGameFragment :Fragment() {

    private lateinit var binding: FragmentOnlineGameCopyBinding
    private val viewModel: GameActivityViewModel by viewModels()
    private var offlineUser: OfflineUser? = null
    private lateinit var dealer: Dealer
    private lateinit var deck: OnlineDeck
    private lateinit var playerBoardAdapter: PlayerBoardAdapter
    private lateinit var playerBoardRv: RecyclerView
    private lateinit var dealerCardRv: RecyclerView
    private lateinit var dealerHandAdapter: GameAdapter
    private var serviceIntent: Intent? = null
    private var broadcastTimer: Intent? = null
    private var time = -1.0
    private var currentTime = 0.0
    private var isTimerStarted = false
    private var isShuffleTime = false
    private lateinit var smoothScroller: LinearSmoothScroller
    private var boardIndex = 0
    private lateinit var recyclerViewState: Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlineGameCopyBinding.inflate(inflater, container, false)
        val bankId = requireActivity().intent.extras?.getLong(PLAYER_SAVE_ID)
        initDeck()
        initWallet(bankId)
        createAdapterList()
        initBroadcastTimer()
        hideUnnecessaryView()
        prepareDealerProfileImg()
        disableBtn()
        configureListeners()
        return binding.root
    }

    private fun initDeck() {
        deck = prepareDeck()
    }

    private fun createAdapterList() {
        playerBoardAdapter = PlayerBoardAdapter()
        playerBoardRv = binding.fragmentOnlineGameBoardRv
        setupVerticalRecyclerView(playerBoardRv)

        recyclerViewState = playerBoardRv.layoutManager!!.onSaveInstanceState()!!

        dealerHandAdapter = GameAdapter()
        dealerCardRv = binding.fragmentOnlineGameDealerRecyclerView
        setupHorizontalRecyclerView(dealerCardRv)

        smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initWallet(bankId: Long?) {

        if (bankId != null) {
            viewModel.getBank(bankId).observe(viewLifecycleOwner) { wallet ->
                if (offlineUser == null) {
                    Log.d(javaClass.simpleName, "initWallet: wallet pseudo:${wallet.pseudo}")
                    offlineUser = OfflineUser(
                        wallet,
                    )

                    dealer = Utils.createDealer()
                } else {
                    offlineUser!!.wallet?.amount = wallet.amount
                }
                viewModel.updateOfflineUser(offlineUser!!)
                binding.apply {
                    fragmentOnlineGameBankAmountTv.text = wallet?.amount.toString()
                    fragmentOnlineGamePlayerNameBet.text = wallet?.pseudo
                }

            }

        }
    }

//    private fun initPlayer(bankId: Long?) {
//
//        if (bankId != null) {
//
//            viewModel.getOfflineUser().observe(viewLifecycleOwner) { user ->
//                binding.apply {
//                    Log.d(javaClass.simpleName, "initPlayer: user pseudo: ${user.wallet?.pseudo}")
//                }
//            }
//
//        }
//    }

    private fun initBroadcastTimer() {
        serviceIntent = Intent(requireContext(), TimeService::class.java)
        broadcastTimer = requireActivity().registerReceiver(updateTime, IntentFilter(TimeService.TIMER_UPDATED))
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimeService.TIME_EXTRA, 0.0)
            event(offlineUser!!)
            Log.e(javaClass.simpleName, "updateTime onReceive: ${time}sec")
        }
    }

    private fun event(offlineUser: OfflineUser) {
        if (
            time == currentTime &&
            dealer.score in 2..16 &&
            time in 0.0 .. 10.0
        ) {
            currentTime++
            dealerDrawCard(offlineUser, dealer, deck)
        }

        if (
            time == currentTime &&
            dealer.score > 16 &&
            time in 0.0 .. 10.0
                ) {
            resetTimer()
            stopTimer()
            offlineUser.currentHandType = HandType.MainHand
            offlineUser.currentPlayerNumber = PlayerNumberType.PLAYER_ONE
            compareScoreWithDealer(offlineUser)
            showStartBtn()
            if (isShuffleTime) {
                isShuffleTime = false
                Utils.shuffleDeck(deck.deckList!!)
                deck.index = 0
                makeSnackBar(requireContext().resources.getString(R.string.fragment_main_game_shuffle_deck))
            }
            Log.e(javaClass.simpleName, "event: reset and stop time")
        }
    }

    private fun compareScoreWithDealer(offlineUser: OfflineUser) {
        var winBet = 0.0
        for (indexOfPlayer in 0 until offlineUser.player.size) {

            val playerToCompare = offlineUser.player[indexOfPlayer]
            val score = playerToCompare.score
            val bet = playerToCompare.bet
            val handSize = playerToCompare.hand.size
            val handType = offlineUser.player[indexOfPlayer].playerHandType.valueOf
            var resultScore: Int
            val isFirstSplit = offlineUser.player[indexOfPlayer].isPlayerFirstSplit
            if (score in 1..21) {
                resultScore = if (dealer.score < 22) {
                    Utils.compareScore(score, handSize, handType, isFirstSplit, dealer)
                } else {
                    Utils.compareScore(score, handSize)
                }
                offlineUser.player[indexOfPlayer].resultScore = resultScore
                winBet += Utils.paymentForPlayer(resultScore, bet)
            }

            if (Utils.isDealerHaveBlackJack(dealer)) {
                binding.fragmentOnlineGameDealerScoreTv.text = requireContext().resources.getString(R.string.online_game_fragment_blackjack)
                winBet += insurancePay(playerToCompare)
            }
        }
        offlineUser.wallet!!.amount += winBet
        showWinBetResult(winBet)
        addCardToView(offlineUser)
        lifecycleScope.launch {
            viewModel.updateBank(offlineUser.wallet!!)
        }
    }

    private fun showWinBetResult(winBet: Double) {
        var bet = if (winBet.toString().last() == '0') winBet.toString().replaceAfter(".","").replace(".","") else winBet.toString()
        val result =
            if (winBet < 0.0)
                requireContext().resources.getString(R.string.fragment_main_game_you_lose) + " $bet"
            else
                requireContext().resources.getString(R.string.fragment_main_game_you_win) + " $bet"

        binding.fragmentOnlineGamePlayerBetBtn.text = result
    }

    private fun insurancePay(playerToCompare: CustomPlayer): Double {
        Log.e(javaClass.simpleName, "insurancePay: insurance bet = ${playerToCompare.insuranceBet}")
        return if (playerToCompare.insuranceBet > 0.0) playerToCompare.insuranceBet * 2 else 0.0
    }

    private fun startTimer() {
        if (!isTimerStarted) {
            serviceIntent?.putExtra(TimeService.TIME_EXTRA, time)
            requireActivity().startService(serviceIntent)
            isTimerStarted = true
            Log.e(javaClass.simpleName, "startTimer: time started")
        }
    }

    private fun stopTimer() {
        if (isTimerStarted) {
            requireActivity().stopService(serviceIntent)
            isTimerStarted = false
            Log.e(javaClass.simpleName, "stopTimer: time stopped")
        }
    }

    private fun resetTimer() {
        time = -1.0
        currentTime = 0.0
    }

    private fun hideUnnecessaryView() {
        binding.apply {
            fragmentOnlineGameOpponentBetBtn.visibility = View.GONE
            fragmentOnlineGameCurrentUserReadyImg.visibility = View.GONE
            fragmentOnlineGameOpponentReadyImg.visibility = View.GONE
        }
    }

    private fun prepareDealerProfileImg() {
        binding.apply {
            Glide.with(root)
                .load(ContextCompat.getDrawable(requireContext(), R.drawable.dealer))
                .circleCrop()
                .into(fragmentOnlineGameDealerImg)
        }
    }

    private fun disableBtn() {
        binding.apply {
            fragmentOnlineGameHitBtn.isEnabled = false
            fragmentOnlineGameStopBtn.isEnabled = false
            fragmentOnlineGameDoubleBtn.isEnabled = false
            fragmentOnlineGameSplitBtn.isEnabled = false
        }
    }


    private fun configureListeners() {
        binding.apply {
            val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            fragmentOnlineGameFab.setOnClickListener {
                fragmentOnlineGameFab.startAnimation(animUp)
                fragmentOnlineGameFabBorderImg.startAnimation(animUp)
                val betDialog = BetDialog()
                betDialog.show(requireActivity().supportFragmentManager, betDialog.tag)
            }

            fragmentOnlineGameGameStart.setOnClickListener {
                if (offlineUser!!.defaultBet > 0.0) {
                    fragmentOnlineGamePlayerBetBtn.text = ""
                    boardIndex = 0
                    fragmentOnlineGameGameStart.startAnimation(animUp)
                    offlineUser!!.player = Utils.createCustomPlayerList(
                        offlineUser!!.playerCount,
                        offlineUser!!.defaultBet
                    )
                    dealer = Utils.createDealer()
                    resetRecyclerView(dealer)
                    distributionSequence(deck)
                    enableHitAndStopBtn()
                    hideStartBtn()
                } else {
                    makeSnackBar(requireContext().resources.getString(R.string.offline_game_fragment_bet_must_not_be_0))
                }
            }

            fragmentOnlineGameHitBtn.setOnClickListener {
                Utils.closeInsurance(offlineUser!!.player)
                val currentPlayer = Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
                fragmentOnlineGameHitBtn.startAnimation(animUp)
                disableDoubleAndSplitBtn()
                playerDrawCard(offlineUser!!, deck, currentPlayer)
                playerBust(offlineUser!!, deck)
                addCardToView(offlineUser!!)
            }

            fragmentOnlineGameStopBtn.setOnClickListener {
                Utils.closeInsurance(offlineUser!!.player)
                fragmentOnlineGameStopBtn.startAnimation(animUp)
                nextHandOrDealerTurn(offlineUser!!, deck)
            }

            fragmentOnlineGameDoubleBtn.setOnClickListener {
                Utils.closeInsurance(offlineUser!!.player)
                val currentPlayer = Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
                fragmentOnlineGameDoubleBtn.startAnimation(animUp)
                val doubleBet = currentPlayer.bet * 2.0
                currentPlayer.isDouble = true
                currentPlayer.bet = doubleBet
                playerDrawCard(offlineUser!!, deck, currentPlayer)
                playerBust(offlineUser!!, deck)
                addCardToView(offlineUser!!)
                nextHandOrDealerTurn(offlineUser!!, deck)

            }

            fragmentOnlineGameSplitBtn.setOnClickListener {
                Utils.closeInsurance(offlineUser!!.player)
                val currentPlayer = Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
                val bet = currentPlayer.bet
                if (bet <= offlineUser!!.wallet!!.amount) {
                    fragmentOnlineGameSplitBtn.startAnimation(animUp)
                    Utils.splitPlayerGame(
                        offlineUser!!.player,
                        offlineUser!!.currentHandType,
                        currentPlayer
                    )
                    if (Utils.playerHave2Aces(offlineUser!!)) {
                        playerDrawCard(offlineUser!!, deck, currentPlayer)
                        nextHandOrDealerTurn(offlineUser!!, deck)
                        currentPlayer.score =
                            currentPlayer.score + 10
                        nextHandOrDealerTurn(offlineUser!!, deck)
                    } else {
                        playerDrawCard(offlineUser!!, deck, currentPlayer)
                        playerCanSplit(currentPlayer)
                        addCardToView(offlineUser!!)
                    }
                } else {
                    makeSnackBar(requireContext().resources.getString(R.string.bet_dialog_not_enough_money))
                }
            }
        }
    }

    private fun nextHandOrDealerTurn(offlineUser: OfflineUser, deck: OnlineDeck) {
        var currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
        when{
            currentPlayer.isPlayerFirstSplit && offlineUser.currentHandType == HandType.MainHand -> {
                offlineUser.currentHandType = HandType.FirstSplit
                Utils.updateCurrentPlayerInArrayListOfCustomPlayer(offlineUser.player, offlineUser.currentPlayerNumber, offlineUser.currentHandType)
                currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
                playerDrawCard(offlineUser, deck, currentPlayer)
                boardIndex++
                addCardToView(offlineUser)
                playerCanDouble(currentPlayer)
                playerCanSplit(currentPlayer)
            }
            currentPlayer.isPlayerSecondSplit && offlineUser.currentHandType == HandType.FirstSplit -> {
                offlineUser.currentHandType = HandType.SecondSplit
                Utils.updateCurrentPlayerInArrayListOfCustomPlayer(offlineUser.player, offlineUser.currentPlayerNumber, offlineUser.currentHandType)
                currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
                playerDrawCard(offlineUser, deck, currentPlayer)
                boardIndex++
                addCardToView(offlineUser)
                playerCanDouble(currentPlayer)
                playerCanSplit(currentPlayer)
            }
            Utils.getIndexOfPlayerNumberType(offlineUser.currentPlayerNumber)  != offlineUser.playerCount -> {
                offlineUser.currentPlayerNumber = Utils.nextPlayerTurn(offlineUser)
                offlineUser.currentHandType = HandType.MainHand
                Utils.updateCurrentPlayerInArrayListOfCustomPlayer(offlineUser.player, offlineUser.currentPlayerNumber, offlineUser.currentHandType)
                boardIndex++
                offlineUser.currentHandType = HandType.MainHand
                currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
                playerCanDouble(currentPlayer)
                playerCanSplit(currentPlayer)
                addCardToView(offlineUser)
            }
            else -> {
                startTimer()
                disableBtn()
            }
        }
    }

    private fun playerDrawCard(offlineUser: OfflineUser, deck: OnlineDeck, player: CustomPlayer) {
        redCardEndOfTheDeck(offlineUser, deck)
        Utils.addCardToPlayerHandList(player, deck, offlineUser.currentHandType)
        player.isPlayerDrawAce =
            Utils.playerDrawAnAceOrNot(deck, player)
        player.isPlayerScoreSoft =
            Utils.playerHaveASoftScoreOrNot(player, deck)
        if (!player.isPlayerScoreSoft) {
            setPlayerDrawAceAndScoreSoftToFalse(player)
        }
        deck.index = Utils.incrementDeckIndex(deck)
    }

    private fun redCardEndOfTheDeck(offlineUser: OfflineUser, deck: OnlineDeck) {
        if (deck.index == 260) {
//            val currentPlayerHand = Utils.currentPlayerHand(offlineUser)
//            currentPlayerHand.add(
//                Card(NumberType.RED, ColorType.RED, 0)
//            )
            isShuffleTime = true
        }
    }

    private fun dealerDrawCard(offlineUser: OfflineUser, dealer: Dealer, deck: OnlineDeck) {
        Utils.addCardToDealerList(dealer, deck)
        dealer.isDealerDrawAce = Utils.dealerDrawAnAceOrNot(deck, dealer)
        dealer.isDealerScoreSoft = Utils.modifyDealerScoreWithAce(dealer, deck)
        if (!dealer.isDealerScoreSoft) {
            dealer.isDealerDrawAce = false
        }
        addDealerCardToView(dealer)
        showScoreView(dealer)
        deck.index = Utils.incrementDeckIndex(deck)
    }

    private fun playerBust(offlineUser: OfflineUser, deck: OnlineDeck) {
        val indexOfPlayerHand = offlineUser.currentHandType
        val currentPlayer = Utils.getCurrentPlayer(offlineUser, indexOfPlayerHand)
        if (currentPlayer.score > 21) {
            currentPlayer.resultScore = R.string.fragment_main_game_you_lose
            nextHandOrDealerTurn(offlineUser, deck)
            offlineUser.wallet?.amount = offlineUser.wallet?.amount?.minus(currentPlayer.bet)!!
            binding.fragmentOnlineGameBankAmountTv.text = offlineUser.wallet.amount.toString()
        }
    }

    private fun prepareDeck(): OnlineDeck {
        val deck = OnlineDeck()
        deck.deckList = Utils.createDeck().deckList
        Utils.shuffleDeck(deck.deckList!!)
        return deck
    }

    private fun distributionSequence(deck: OnlineDeck) {
        val currentPlayer = Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
        giveOneCardToAllBox(offlineUser!!, deck)
        dealerDrawCard(offlineUser!!, dealer, deck)
        giveOneCardToAllBox(offlineUser!!, deck)
        addCardToView(offlineUser!!)
        addDealerCardToView(dealer)
        showScoreView(dealer)
        playerCanSplit(currentPlayer)
        playerCanDouble(currentPlayer)
        dealerHaveAnAce(offlineUser!!, dealer)
    }

    private fun dealerHaveAnAce(offlineUser: OfflineUser, dealer: Dealer) {
        if (dealer.score == 11) {
            makeSnackBar(requireContext().resources.getString(R.string.message_for_insurance))
            Utils.openInsurance(offlineUser.player)
        }
    }

    private fun giveOneCardToAllBox(offlineUser: OfflineUser, deck: OnlineDeck) {
        for (playerIndex in 0 until offlineUser.player.size) {
            playerDrawCard(offlineUser, deck, offlineUser.player[playerIndex])
        }
    }

    private fun setPlayerDrawAceAndScoreSoftToFalse(playerHand: CustomPlayer) {
        playerHand.isPlayerDrawAce = false
        playerHand.isPlayerScoreSoft = false
    }

    private fun playerCanSplit(currentPlayer: CustomPlayer) {
        val firstCard = currentPlayer.hand[0]
        val secondCard = currentPlayer.hand[1]
        val isSecondSplit = currentPlayer.isPlayerSecondSplit
        binding.fragmentOnlineGameSplitBtn.isEnabled = firstCard.value == secondCard.value && !isSecondSplit && currentPlayer.playerHandType != HandType.SecondSplit
    }

    private fun playerCanDouble(currentPlayer: CustomPlayer) {
        val isPlayerHaveTwoCards = currentPlayer.hand.size == 2
        binding.fragmentOnlineGameDoubleBtn.isEnabled = isPlayerHaveTwoCards
    }

    private fun addCardToView(offlineUser: OfflineUser) {
        recyclerViewState = playerBoardRv.layoutManager!!.onSaveInstanceState()!!
        loadPlayerBoardIntoRecyclerView(offlineUser.player, playerBoardAdapter, playerBoardRv)
    }

    private fun addDealerCardToView(dealer: Dealer?) {
        loadHandIntoRecyclerView(
            dealer?.hand,
            dealerHandAdapter,
            binding.fragmentOnlineGameDealerRecyclerView
        )
    }

    private fun resetRecyclerView(dealer: Dealer?) {
        loadHandIntoRecyclerView(
            dealer?.hand,
            dealerHandAdapter,
            binding.fragmentOnlineGameDealerRecyclerView
        )
    }

    private fun showScoreView(dealer: Dealer) {
        binding.apply {
            fragmentOnlineGameDealerScoreTv.text = dealer.score.toString()
//            dealerHaveBlackjack(dealer)
        }
    }

    private fun enableHitAndStopBtn() {
        binding.apply {
            fragmentOnlineGameHitBtn.isEnabled = true
            fragmentOnlineGameStopBtn.isEnabled = true
        }
    }

    private fun disableDoubleAndSplitBtn() {
        binding.apply {
            fragmentOnlineGameDoubleBtn.isEnabled = false
            fragmentOnlineGameSplitBtn.isEnabled = false
        }
    }

    private fun hideStartBtn() {
        binding.fragmentOnlineGameGameStart.visibility = View.GONE
        binding.fragmentOnlineGameDealerImg.visibility = View.VISIBLE
    }

    private fun showStartBtn() {
        binding.fragmentOnlineGameGameStart.visibility = View.VISIBLE
        binding.fragmentOnlineGameDealerImg.visibility = View.INVISIBLE
    }

    private fun loadHandIntoRecyclerView(hand: ArrayList<Card>?, adapter: GameAdapter, recyclerView: RecyclerView) {
        adapter.submitList(hand)
        recyclerView.adapter = adapter
    }

    private fun setupHorizontalRecyclerView(recyclerView: RecyclerView) = recyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupVerticalRecyclerView(recyclerView: RecyclerView) = recyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
    }

    private fun loadPlayerBoardIntoRecyclerView(playerBoardList: ArrayList<CustomPlayer>, adapter: PlayerBoardAdapter, recyclerView: RecyclerView) {
        adapter.submitList(playerBoardList)
        recyclerView.adapter = adapter
//        recyclerView.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_down)
        recyclerView.layoutManager!!.onRestoreInstanceState(recyclerViewState)
    }

    private fun makeSnackBar(message: String) =
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        requireActivity().unregisterReceiver(updateTime)
    }

    companion object {
        const val PLAYER_SAVE_ID = "player_save_id"
    }

}