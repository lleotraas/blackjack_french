package fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineGameCopyBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_offline_game.domain.service.TimeService
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.MorphButton
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.formatStringWinBet
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.dp
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter.GameAdapter
import fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter.PlayerBoardAdapter
import fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog.*
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
    private lateinit var recyclerViewState: Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlineGameCopyBinding.inflate(inflater, container, false)
        val bankId = requireActivity().intent.extras?.getLong(PLAYER_SAVE_ID)
        initDeck()
        createOfflinePlayerAndDealer(bankId)
        initAdapterList()
        initRecyclerView()
        initBtn()
        initBroadcastTimer()
        hideUnnecessaryView()
        prepareDealerProfileImg()
        disableBtn()
        configureSupportNavigateUp()
        configureListeners()
        return binding.root
    }

    private fun initBtn() {
        binding.apply {
            fragmentOnlineGameHitBtn.apply {
                text = requireContext().resources.getString(R.string.fragment_main_game_hit)
                setPadding((40 * dp()).toInt(), (getPaddingValue() * dp()).toInt(), (40 * dp()).toInt(), (getPaddingValue() * dp()).toInt())
            }
            fragmentOnlineGameStopBtn.apply {
                text = requireContext().resources.getString(R.string.fragment_main_game_stop)
                setPadding((36 * dp()).toInt(), (getPaddingValue() * dp()).toInt(), (36 * dp()).toInt(), (getPaddingValue() * dp()).toInt())
            }
            fragmentOnlineGameSplitBtn.apply {
                text = requireContext().resources.getString(R.string.fragment_main_game_split)
                setPadding((40 * dp()).toInt(), (getPaddingValue() * dp()).toInt(), (40 * dp()).toInt(), (getPaddingValue() * dp()).toInt())
            }
            fragmentOnlineGameDoubleBtn.apply {
                text = requireContext().resources.getString(R.string.fragment_main_game_double)
                setPadding((28 * dp()).toInt(), (getPaddingValue() * dp()).toInt(), (28 * dp()).toInt(), (getPaddingValue() * dp()).toInt())
            }
        }
    }

    private fun getPaddingValue(): Int {
        return 20
    }

    private fun initDeck() {
        deck = prepareDeck()
    }

    private fun initAdapterList() {
        playerBoardAdapter = PlayerBoardAdapter { currentPlayer, btnPressed ->
            if (offlineUser!!.isHelpMode) {
                when (btnPressed) {
                    PLAYER_INFO_PRESSED -> RecyclerViewHelpDialog().show(requireActivity().supportFragmentManager, RecyclerViewHelpDialog().tag)
                    else -> InsuranceHelpDialog().show(requireActivity().supportFragmentManager, InsuranceHelpDialog().tag)
                }
            } else {
                if (offlineUser!!.wallet!!.amount >= offlineUser!!.totalBet + currentPlayer.bet / 2) {
                    currentPlayer.insuranceBet = currentPlayer.bet / 2
                    offlineUser!!.totalBet += currentPlayer.insuranceBet
                    binding.fragmentOnlineGameBankAmountTv.text = (offlineUser!!.wallet!!.amount - offlineUser!!.totalBet).formatStringWinBet()
                } else {
                    makeSnackBar(requireContext().resources.getString(R.string.bet_dialog_not_enough_money))
                }
            }
        }
        dealerHandAdapter = GameAdapter()
    }

    private fun initRecyclerView() {
        playerBoardRv = binding.fragmentOnlineGameBoardRv
        setupVerticalRecyclerView(playerBoardRv)
        dealerCardRv = binding.fragmentOnlineGameDealerRecyclerView
        setupHorizontalRecyclerView(dealerCardRv)
        recyclerViewState = playerBoardRv.layoutManager!!.onSaveInstanceState()!!
    }

    private fun createOfflinePlayerAndDealer(bankId: Long?) {

        if (bankId != null) {
            viewModel.getBank(bankId).observe(viewLifecycleOwner) { wallet ->
                if (wallet.amount <= 50.0) {
                    binding.fragmentOnlineGameRebuyBtn.visibility = View.VISIBLE
                }
                if (offlineUser == null) {
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
                    fragmentOnlineGamePlayerNameBet.text = wallet.pseudo
                }

            }

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
            dealerDrawCard(dealer, deck)
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
            var resultScore: Int
            val score = playerToCompare.score
            val bet = playerToCompare.bet
            val handSize = playerToCompare.hand.size
            val handType = offlineUser.player[indexOfPlayer].playerHandType.valueOf
            val isFirstSplit = offlineUser.player[indexOfPlayer].isPlayerFirstSplit
            if (score in 1..21) {

                resultScore =
                    if (dealer.score < 22) {
                        Utils.compareScore(score, handSize, handType, isFirstSplit, dealer)
                    } else {
                        Utils.compareScore(score, handSize)
                    }
                offlineUser.player[indexOfPlayer].resultScore = resultScore
                winBet += Utils.paymentForPlayer(resultScore, bet)
            }

            if (Utils.isDealerHaveBlackJack(dealer)) {
                binding.fragmentOnlineGameDealerScoreTv.text = requireContext().resources.getString(R.string.online_game_fragment_blackjack)
                winBet += Utils.insurancePay(playerToCompare)
            } else {
                winBet -= Utils.insuranceLoose(playerToCompare)
            }
        }
        offlineUser.wallet!!.amount += winBet
        showLoseBetAMount(winBet)
        addCardToView(offlineUser)
        showReBuyToPlayer(offlineUser)
        offlineUser.isPlaying = false
        lifecycleScope.launch {
            viewModel.updateBank(offlineUser.wallet!!)
        }
    }

    private fun showReBuyToPlayer(offlineUser: OfflineUser) {
        if (offlineUser.wallet!!.amount <= 50.0) {
            binding.fragmentOnlineGameRebuyBtn.visibility = View.VISIBLE
        }
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
            fragmentOnlineGamePlayerBetBtn.visibility = View.GONE
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
            fragmentOnlineGameHitBtn.setUIState(MorphButton.UIState.Loading)
            fragmentOnlineGameStopBtn.setUIState(MorphButton.UIState.Loading)
            fragmentOnlineGameDoubleBtn.setUIState(MorphButton.UIState.Loading)
            fragmentOnlineGameSplitBtn.setUIState(MorphButton.UIState.Loading)
        }
    }

    private fun enableBtn() {
        binding.apply {
            fragmentOnlineGameHitBtn.isEnabled = true
            fragmentOnlineGameStopBtn.isEnabled = true
            fragmentOnlineGameDoubleBtn.isEnabled = true
            fragmentOnlineGameSplitBtn.isEnabled = true
            fragmentOnlineGameHitBtn.setUIState(MorphButton.UIState.Button)
            fragmentOnlineGameStopBtn.setUIState(MorphButton.UIState.Button)
            fragmentOnlineGameDoubleBtn.setUIState(MorphButton.UIState.Button)
            fragmentOnlineGameSplitBtn.setUIState(MorphButton.UIState.Button)
            fragmentOnlineGameRebuyBtn.visibility = View.VISIBLE
        }
    }

    private fun configureSupportNavigateUp() {
        requireActivity().addMenuProvider(object : MenuProvider{

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.activity_offline_game_menu, menu)
                if (offlineUser!!.isHelpMode) {
                    menu[0].title = requireContext().resources.getString(R.string.activity_offline_game_quit)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.offline_game_help) {
                    if (!offlineUser!!.isHelpMode) {
                        binding.fragmentOnlineGameHelpTv.visibility = View.VISIBLE
                        menuItem.title = requireContext().resources.getString(R.string.activity_offline_game_quit)
                        manageUiForHelp()
                    } else {
                        binding.fragmentOnlineGameHelpTv.visibility = View.GONE
                        menuItem.title = requireContext().resources.getString(R.string.activity_offline_game_help)
                        backForPreviousUi()
                    }
                    offlineUser!!.isHelpMode = !offlineUser!!.isHelpMode
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun manageUiForHelp() {
        loadPlayerBoardIntoRecyclerView(Utils.createCustomPlayerListForHelp(), playerBoardAdapter, playerBoardRv)
        val dealerHand = Utils.createDealerHandForHelp()
        loadHandIntoRecyclerView(dealerHand, dealerHandAdapter, dealerCardRv)
        binding.fragmentOnlineGameDealerScoreTv.text = dealerHand[0].value.toString()
        enableBtn()
        hideStartBtn()
        startAnimationForHelp()
    }

    private fun startAnimationForHelp() {
        val scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up_infinite)
        binding.apply {
            fragmentOnlineGameRebuyBtn.animation = scaleAnimation
            fragmentOnlineGameFab.animation = scaleAnimation
            fragmentOnlineGameFabBorderImg.animation = scaleAnimation
            fragmentOnlineGameBankTitleTv.animation = scaleAnimation
            fragmentOnlineGameBankAmountTv.animation = scaleAnimation
            fragmentOnlineGameDealerInformationContainer.animation = scaleAnimation
        }
    }

    private fun stopAnimationForHelp() {
        binding.apply {
            fragmentOnlineGameRebuyBtn.animation = null
            fragmentOnlineGameBoardRv.animation = null
            fragmentOnlineGameFab.animation = null
            fragmentOnlineGameFabBorderImg.animation = null
            fragmentOnlineGameBankTitleTv.animation = null
            fragmentOnlineGameBankAmountTv.animation = null
            fragmentOnlineGameDealerInformationContainer.animation = null
        }
    }

    private fun backForPreviousUi() {
        binding.apply {
            fragmentOnlineGameRebuyBtn.visibility = View.GONE
        }
        stopAnimationForHelp()
        if (!offlineUser!!.isPlaying) {
            showStartBtn()
            disableBtn()
        }
        if (offlineUser!!.player[0].hand.isEmpty()) {
            loadPlayerBoardIntoRecyclerView(ArrayList(), playerBoardAdapter, playerBoardRv)
            loadHandIntoRecyclerView(ArrayList(), dealerHandAdapter, dealerCardRv)
            disableBtn()
            binding.fragmentOnlineGameDealerImg.visibility = View.GONE
            binding.fragmentOnlineGameDealerContainer.visibility = View.GONE
        } else {
            loadPlayerBoardIntoRecyclerView(offlineUser!!.player, playerBoardAdapter, playerBoardRv)
            loadHandIntoRecyclerView(dealer.hand, dealerHandAdapter, dealerCardRv)
            binding.fragmentOnlineGameDealerScoreTv.text = dealer.score.toString()
            val currentPlayer = Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
            playerCanSplit(currentPlayer)
            playerCanDouble(currentPlayer)
        }
    }

    private fun configureListeners() {
        binding.apply {
            val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)

            fragmentOnlineGameFab.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    val alertDialog = BetHelpDialog()
                    alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                } else {
                    fragmentOnlineGameFabBorderImg.startAnimation(animUp)
                    fragmentOnlineGameFab.startAnimation(animUp)
                    goToBetDialog()
                }
            }

            fragmentOnlineGameGameStart.setOnClickListener {
                fragmentOnlineGameGameStart.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                fragmentOnlineGameGameStart.startAnimation(animUp)
                gameStartSequence()
            }

            fragmentOnlineGameHitBtn.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    val alertDialog = HitHelpDialog()
                    alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                } else {
                    fragmentOnlineGameHitBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    fragmentOnlineGameHitBtn.startAnimation(animUp)
                    hitCardSequence()
                }
            }

            fragmentOnlineGameStopBtn.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    val alertDialog = StopHelpDialog()
                    alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                } else {
                    fragmentOnlineGameStopBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    fragmentOnlineGameStopBtn.startAnimation(animUp)
                    stopSequence()
                }
            }

            fragmentOnlineGameDoubleBtn.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    val alertDialog = DoubleHelpDialog()
                    alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                } else {
                    fragmentOnlineGameDoubleBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    fragmentOnlineGameDoubleBtn.startAnimation(animUp)
                    doubleSequence()
                }
            }

            fragmentOnlineGameSplitBtn.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    val alertDialog = SplitHelpDialog()
                    alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                } else {
                    fragmentOnlineGameSplitBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    fragmentOnlineGameSplitBtn.startAnimation(animUp)
                    splitCardSequence()
                }
            }

            fragmentOnlineGameRebuyBtn.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    val alertDialog = RebuyHelpDialog()
                    alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                } else {
                    fragmentOnlineGameRebuyBtn.startAnimation(animUp)
                    rebuySequence()
                }
            }

            fragmentOnlineGameDealerInformationContainer.setOnClickListener {
                if (offlineUser!!.isHelpMode) {
                    DealerHelpDialog().show(requireActivity().supportFragmentManager, DealerHelpDialog().tag)
                }
            }
        }
    }

    private fun rebuySequence() {
        offlineUser!!.wallet!!.amount += 500.0
        binding.fragmentOnlineGameRebuyBtn.visibility = View.GONE
        lifecycleScope.launch {
            viewModel.updateBank(offlineUser!!.wallet!!)
        }
    }

    private fun splitCardSequence() {
        if (offlineUser!!.wallet!!.amount >= offlineUser!!.totalBet + offlineUser!!.defaultBet) {
            offlineUser!!.totalBet += offlineUser!!.defaultBet
            updateWalletViewWhenDoubleOrSplit(offlineUser!!)
            Utils.closeInsurance(offlineUser!!.player)
            val currentPlayer = Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
            Utils.splitPlayerGame(offlineUser!!.player, offlineUser!!.currentHandType, currentPlayer)
            playerSplitTwoAces(offlineUser!!, currentPlayer)
        } else {
            makeSnackBar(requireContext().resources.getString(R.string.bet_dialog_not_enough_money))
        }
    }

    private fun doubleSequence() {
        if (offlineUser!!.wallet!!.amount >= offlineUser!!.totalBet + offlineUser!!.defaultBet) {
            offlineUser!!.totalBet += offlineUser!!.defaultBet
            updateWalletViewWhenDoubleOrSplit(offlineUser!!)
            Utils.closeInsurance(offlineUser!!.player)
            val currentPlayer =
                Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
            currentPlayer.isDouble = true
            currentPlayer.bet = currentPlayer.bet * 2.0
            playerDrawCard(offlineUser!!, deck, currentPlayer)
            addCardToView(offlineUser!!)
            nextHandOrDealerTurn(offlineUser!!)
            playerDoubleBust(offlineUser!!, currentPlayer)
        } else {
            makeSnackBar(requireContext().resources.getString(R.string.bet_dialog_not_enough_money))
        }
    }

    private fun stopSequence() {
        Utils.closeInsurance(offlineUser!!.player)
        nextHandOrDealerTurn(offlineUser!!)
    }

    private fun hitCardSequence() {
        Utils.closeInsurance(offlineUser!!.player)
        val currentPlayer =
            Utils.getCurrentPlayer(offlineUser!!, offlineUser!!.currentHandType)
        playerDrawCard(offlineUser!!, deck, currentPlayer)
        playerBust(offlineUser!!)
        addCardToView(offlineUser!!)
    }

    private fun gameStartSequence() {
        if (offlineUser!!.defaultBet > 0.0) {
            updateWalletViewWhenGameStart(offlineUser!!)
            if (offlineUser!!.wallet!!.amount >= offlineUser!!.defaultBet * offlineUser!!.playerCount) {
                offlineUser!!.totalBet = offlineUser!!.defaultBet * offlineUser!!.playerCount
                offlineUser!!.isPlaying = true
                showMoneyEarned("")
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
                makeSnackBar(requireContext().resources.getString(R.string.bet_dialog_not_enough_money))
            }
        } else {
            makeSnackBar(requireContext().resources.getString(R.string.offline_game_fragment_bet_must_not_be_0))
        }
    }

    private fun updateWalletViewWhenDoubleOrSplit(offlineUser: OfflineUser) {
        binding.fragmentOnlineGameBankAmountTv.text = (offlineUser.wallet!!.amount - offlineUser.totalBet).formatStringWinBet()
    }

    private fun updateWalletViewWhenGameStart(offlineUser: OfflineUser) {
        binding.fragmentOnlineGameBankAmountTv.text = (offlineUser.wallet!!.amount - offlineUser.defaultBet * offlineUser.playerCount).formatStringWinBet()
    }

    private fun goToBetDialog() {
        binding.apply {
            if (!offlineUser!!.isPlaying) {
                val betDialog = BetDialog()
                betDialog.show(requireActivity().supportFragmentManager, betDialog.tag)
            }
        }
    }

    private fun playerSplitTwoAces(offlineUser: OfflineUser, currentPlayer: CustomPlayer) {
        if (Utils.playerHave2Aces(offlineUser)) {
            playerDrawCard(offlineUser, deck, currentPlayer)
            nextHandOrDealerTurn(offlineUser)

            val playerWithSecondAce = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
            playerWithSecondAce.score += if (playerWithSecondAce.isPlayerDrawAce) 0 else 10
            nextHandOrDealerTurn(offlineUser)
        } else {
            playerDrawCard(offlineUser, deck, currentPlayer)
            playerCanSplit(currentPlayer)
            addCardToView(offlineUser)
        }
    }

    private fun nextHandOrDealerTurn(offlineUser: OfflineUser) {
        var currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
        when{
            currentPlayer.isPlayerFirstSplit && offlineUser.currentHandType == HandType.MainHand -> {
                changePlayerHandSequence(offlineUser, HandType.FirstSplit)
            }
            currentPlayer.isPlayerSecondSplit && offlineUser.currentHandType == HandType.FirstSplit -> {
                changePlayerHandSequence(offlineUser, HandType.SecondSplit)
            }
            Utils.getIndexOfPlayerNumberType(offlineUser.currentPlayerNumber)  != offlineUser.playerCount -> {
                offlineUser.currentPlayerNumber = Utils.nextPlayerTurn(offlineUser)
                offlineUser.currentHandType = HandType.MainHand
                Utils.updateCurrentPlayerInArrayListOfCustomPlayer(offlineUser.player, offlineUser.currentPlayerNumber, offlineUser.currentHandType)
                currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
                addCardToView(offlineUser)
                playerCanDouble(currentPlayer)
                playerCanSplit(currentPlayer)

            }
            else -> {
                if (
                    Utils.allPlayerBust(offlineUser.player) &&
                    !Utils.playerHaveInsurance(offlineUser.player)
                        ) {
                    showStartBtn()

                } else if(
                    Utils.allPlayerBust(offlineUser.player) &&
                    dealer.score == 11 &&
                    Utils.playerHaveInsurance(offlineUser.player)
                        ) {
                    dealerDrawOneCardForInsurance(offlineUser)
                }else {
                    startTimer()
                }
                disableBtn()
                offlineUser.currentHandType = HandType.MainHand
                offlineUser.currentPlayerNumber = PlayerNumberType.PLAYER_ONE
                binding.fragmentOnlineGameBankAmountTv.text = offlineUser.wallet!!.amount.formatStringWinBet()
                lifecycleScope.launch {
                    viewModel.updateBank(offlineUser.wallet)
                }
            }
        }
    }

    private fun dealerDrawOneCardForInsurance(offlineUser: OfflineUser) {
        dealerDrawCard(dealer, deck)
        Utils.payAllInsurance(offlineUser.wallet!!, offlineUser.player, Utils.isDealerHaveBlackJack(dealer))
        showStartBtn()
    }

    private fun changePlayerHandSequence(offlineUser: OfflineUser, handType: HandType) {
        offlineUser.currentHandType = handType
        Utils.updateCurrentPlayerInArrayListOfCustomPlayer(offlineUser.player, offlineUser.currentPlayerNumber, offlineUser.currentHandType)
        val currentPlayer = Utils.getCurrentPlayer(offlineUser, offlineUser.currentHandType)
        playerDrawCard(offlineUser, deck, currentPlayer)
        addCardToView(offlineUser)
        playerCanDouble(currentPlayer)
        playerCanSplit(currentPlayer)
    }

    private fun playerDrawCard(offlineUser: OfflineUser, deck: OnlineDeck, player: CustomPlayer) {
        redCardEndOfTheDeck(deck)
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

    private fun redCardEndOfTheDeck(deck: OnlineDeck) {
        if (deck.index!! == 260) {
//            val currentPlayerHand = Utils.currentPlayerHand(offlineUser)
//            currentPlayerHand.add(
//                Card(NumberType.RED, ColorType.RED, 0)
//            )
            isShuffleTime = true
        }
    }

    private fun dealerDrawCard(dealer: Dealer, deck: OnlineDeck) {
        redCardEndOfTheDeck(deck)
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

    private fun playerBust(offlineUser: OfflineUser) {
        val indexOfPlayerHand = offlineUser.currentHandType
        val currentPlayer = Utils.getCurrentPlayer(offlineUser, indexOfPlayerHand)
        if (currentPlayer.score > 21) {
            currentPlayer.resultScore = R.string.fragment_main_game_you_lose
            offlineUser.wallet!!.amount-=currentPlayer.bet
            showLoseBetAMount(-currentPlayer.bet)
            nextHandOrDealerTurn(offlineUser)
//            binding.fragmentOnlineGameBankAmountTv.text = offlineUser.wallet?.amount.toString()
        } else {
            disableDoubleAndSplitBtn()
        }
    }

    private fun playerDoubleBust(offlineUser: OfflineUser, currentPlayer: CustomPlayer) {
        if (currentPlayer.score > 21) {
            currentPlayer.resultScore = R.string.fragment_main_game_you_lose
            offlineUser.wallet!!.amount-=currentPlayer.bet
            showLoseBetAMount(-currentPlayer.bet)
//            binding.fragmentOnlineGameBankAmountTv.text = offlineUser.wallet?.amount.toString()
        }
    }

    private fun showLoseBetAMount(bet: Double) {
        val stringBetView = binding.fragmentOnlineGamePlayerBetBtn.text.toString()
        val moneyAlreadyLose = if (stringBetView.isNotEmpty()) {
            (stringBetView.replace(
                requireContext().resources.getString(R.string.fragment_main_game_you_lose) + " ",
                ""
            ).toDouble())
        } else {
            0.0
        }
        val totalEarnMoney = moneyAlreadyLose + bet
        val string = requireContext().resources.getString(Utils.winOrLoseString(totalEarnMoney)) + " ${(totalEarnMoney).formatStringWinBet()}"
        showMoneyEarned(string)

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
        dealerDrawCard(dealer, deck)
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
        if (firstCard.value == secondCard.value && !isSecondSplit && currentPlayer.playerHandType != HandType.SecondSplit) {
            binding.fragmentOnlineGameSplitBtn.setUIState(MorphButton.UIState.Button)
            binding.fragmentOnlineGameSplitBtn.isEnabled = true
        } else {
            binding.fragmentOnlineGameSplitBtn.setUIState(MorphButton.UIState.Loading)
            binding.fragmentOnlineGameSplitBtn.isEnabled = false
        }
    }

    private fun playerCanDouble(currentPlayer: CustomPlayer) {
        val isPlayerHaveTwoCards = currentPlayer.hand.size == 2
        if (isPlayerHaveTwoCards) {
            binding.fragmentOnlineGameDoubleBtn.setUIState(MorphButton.UIState.Button)
            binding.fragmentOnlineGameDoubleBtn.isEnabled = true
        } else{
            binding.fragmentOnlineGameDoubleBtn.setUIState(MorphButton.UIState.Loading)
            binding.fragmentOnlineGameDoubleBtn.isEnabled = false
        }
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
            binding.fragmentOnlineGameHitBtn.setUIState(MorphButton.UIState.Button)
            binding.fragmentOnlineGameStopBtn.setUIState(MorphButton.UIState.Button)
        }
    }

    private fun disableDoubleAndSplitBtn() {
        binding.apply {
            fragmentOnlineGameDoubleBtn.isEnabled = false
            fragmentOnlineGameSplitBtn.isEnabled = false
            binding.fragmentOnlineGameSplitBtn.setUIState(MorphButton.UIState.Loading)
            binding.fragmentOnlineGameDoubleBtn.setUIState(MorphButton.UIState.Loading)
        }
    }

    private fun hideStartBtn() {
        binding.apply {
            fragmentOnlineGameGameStart.visibility = View.GONE
            fragmentOnlineGameDealerImg.visibility = View.VISIBLE
            fragmentOnlineGameDealerContainer.visibility = View.VISIBLE
        }
    }

    private fun showStartBtn() {
        binding.apply {
            fragmentOnlineGameGameStart.visibility = View.VISIBLE
        }
    }

    private fun showMoneyEarned(stringToShow: String) {
        binding.fragmentOnlineGamePlayerBetBtn.apply {
            visibility = if (stringToShow.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            text = stringToShow
        }
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
        const val INSURANCE_PRESSED = "insurance"
        const val PLAYER_INFO_PRESSED = "player_info"
    }

}