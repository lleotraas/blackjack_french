package fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineGameBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_offline_game.domain.service.TimeService
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.observeOnce
import fr.lleotraas.blackjack_french.features_offline_game.presentation.GameActivityViewModel
import fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter.GameAdapter
import fr.lleotraas.blackjack_french.features_offline_game.presentation.dialog.BetDialog
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType
import org.mockito.NotExtensible

@AndroidEntryPoint
class OfflineGameFragment :Fragment() {

    private lateinit var binding: FragmentOnlineGameBinding
    private val viewModel: GameActivityViewModel by viewModels()
    private lateinit var offlineUser: OfflineUser
    private lateinit var dealer: Dealer
    private lateinit var deck: OnlineDeck
    private val arrayOfAdapter = ArrayList<HashMap<String, GameAdapter>>()
    private val arrayListOfRv = ArrayList<HashMap<String, RecyclerView>>()
    private val arrayOfScore = ArrayList<HashMap<String, AppCompatTextView>>()
    private val arrayOfDoubleIcon = ArrayList<HashMap<String, AppCompatImageView>>()
    private val arrayOfCurrentPlayerIcon = ArrayList<HashMap<String, AppCompatImageView>>()
    private lateinit var dealerHandAdapter: GameAdapter
    private var serviceIntent: Intent? = null
    private var broadcastTimer: Intent? = null
    private var time = -1.0
    private var currentTime = 0.0
    private var isTimerStarted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlineGameBinding.inflate(inflater, container, false)
        val bankId = requireActivity().intent.extras?.getLong(PLAYER_SAVE_ID)
        initDeck()
        initWallet(bankId)
        initPlayer(bankId)
        createArrayWithSizeOfSeven()
        createAdapterList()
        createRecyclerViewList()
        createArrayOfScore()
        createArrayOfDoubleIcon()
        createArrayOfCurrentPlayerIcon()
        initRecyclerViews()
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

    private fun createArrayWithSizeOfSeven() {
        for (i in 0 until 7) {
            arrayOfAdapter.add(HashMap())
            arrayListOfRv.add(HashMap())
            arrayOfScore.add(HashMap())
            arrayOfDoubleIcon.add(HashMap())
            arrayOfCurrentPlayerIcon.add(HashMap())
        }
    }

    private fun createAdapterList() {
        for (i in 0 until 7) {
            arrayOfAdapter[i][Utils.MAIN_HAND] = GameAdapter()
            arrayOfAdapter[i][Utils.FIRST_SPLIT_HAND] = GameAdapter()
            arrayOfAdapter[i][Utils.SECOND_SPLIT_HAND] = GameAdapter()
        }
        dealerHandAdapter = GameAdapter()
    }

    private fun createRecyclerViewList() {
        arrayListOfRv[0][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerRecyclerView
        arrayListOfRv[0][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerFirstSplitRecyclerView
        arrayListOfRv[0][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerSecondSplitRecyclerView
        arrayListOfRv[1][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerTwoMainHandRecyclerView
        arrayListOfRv[1][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerTwoFirstSplitHandRecyclerView
        arrayListOfRv[1][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerTwoSecondSplitHandRecyclerView
        arrayListOfRv[2][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerThreeMainHandRecyclerView
        arrayListOfRv[2][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerThreeFirstSplitHandRecyclerView
        arrayListOfRv[2][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerThreeSecondSplitHandRecyclerView
        arrayListOfRv[3][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerFourMainHandRecyclerView
        arrayListOfRv[3][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerFourFirstSplitHandRecyclerView
        arrayListOfRv[3][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerFourSecondSplitHandRecyclerView
        arrayListOfRv[4][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerFiveMainHandRecyclerView
        arrayListOfRv[4][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerFiveFirstSplitHandRecyclerView
        arrayListOfRv[4][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerFiveSecondSplitHandRecyclerView
        arrayListOfRv[5][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerSixMainHandRecyclerView
        arrayListOfRv[5][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerSixFirstSplitHandRecyclerView
        arrayListOfRv[5][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerSixSecondSplitHandRecyclerView
        arrayListOfRv[6][Utils.MAIN_HAND] = binding.fragmentOnlineGamePlayerSevenMainHandRecyclerView
        arrayListOfRv[6][Utils.FIRST_SPLIT_HAND] = binding.fragmentOnlineGamePlayerSevenFirstSplitHandRecyclerView
        arrayListOfRv[6][Utils.SECOND_SPLIT_HAND] = binding.fragmentOnlineGamePlayerSevenSecondSplitHandRecyclerView
    }

    private fun createArrayOfScore() {
        binding.apply {
            arrayOfScore[0][Utils.MAIN_HAND] = fragmentOnlineGamePlayerScoreTv
            arrayOfScore[0][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFirstSplitScoreTv
            arrayOfScore[0][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSecondSplitScoreTv
            arrayOfScore[1][Utils.MAIN_HAND] = fragmentOnlineGamePlayerTwoMainHandScoreTv
            arrayOfScore[1][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerTwoFirstSplitScoreTv
            arrayOfScore[1][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerTwoSecondSplitScoreTv
            arrayOfScore[2][Utils.MAIN_HAND] = fragmentOnlineGamePlayerThreeMainHandScoreTv
            arrayOfScore[2][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerThreeFirstSplitScoreTv
            arrayOfScore[2][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerThreeSecondSplitScoreTv
            arrayOfScore[3][Utils.MAIN_HAND] = fragmentOnlineGamePlayerFourMainHandScoreTv
            arrayOfScore[3][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFourFirstSplitScoreTv
            arrayOfScore[3][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerFourSecondSplitScoreTv
            arrayOfScore[4][Utils.MAIN_HAND] = fragmentOnlineGamePlayerFiveMainHandScoreTv
            arrayOfScore[4][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFiveFirstSplitScoreTv
            arrayOfScore[4][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerFiveSecondSplitScoreTv
            arrayOfScore[5][Utils.MAIN_HAND] = fragmentOnlineGamePlayerSixMainHandScoreTv
            arrayOfScore[5][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerSixFirstSplitScoreTv
            arrayOfScore[5][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSixSecondSplitScoreTv
            arrayOfScore[6][Utils.MAIN_HAND] = fragmentOnlineGamePlayerSevenMainHandScoreTv
            arrayOfScore[6][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerSevenFirstSplitScoreTv
            arrayOfScore[6][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSevenSecondSplitScoreTv
        }
    }

    private fun createArrayOfDoubleIcon() {
        binding.apply {
            arrayOfDoubleIcon[0][Utils.MAIN_HAND] = fragmentOnlineGamePlayerOneMainHandDoubleImg
            arrayOfDoubleIcon[0][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerOneFirstSplitDoubleImg
            arrayOfDoubleIcon[0][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerOneSecondSplitDoubleImg
            arrayOfDoubleIcon[1][Utils.MAIN_HAND] = fragmentOnlineGamePlayerTwoMainHandDoubleImg
            arrayOfDoubleIcon[1][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerTwoFirstSplitDoubleImg
            arrayOfDoubleIcon[1][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerTwoSecondSplitDoubleImg
            arrayOfDoubleIcon[2][Utils.MAIN_HAND] = fragmentOnlineGamePlayerThreeMainHandDoubleImg
            arrayOfDoubleIcon[2][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerThreeFirstSplitDoubleImg
            arrayOfDoubleIcon[2][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerThreeSecondSplitDoubleImg
            arrayOfDoubleIcon[3][Utils.MAIN_HAND] = fragmentOnlineGamePlayerFourMainHandDoubleImg
            arrayOfDoubleIcon[3][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFourFirstSplitDoubleImg
            arrayOfDoubleIcon[3][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerFourSecondSplitDoubleImg
            arrayOfDoubleIcon[4][Utils.MAIN_HAND] = fragmentOnlineGamePlayerFiveMainHandDoubleImg
            arrayOfDoubleIcon[4][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFiveFirstSplitDoubleImg
            arrayOfDoubleIcon[4][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerFiveSecondSplitDoubleImg
            arrayOfDoubleIcon[5][Utils.MAIN_HAND] = fragmentOnlineGamePlayerSixMainHandDoubleImg
            arrayOfDoubleIcon[5][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerSixFirstSplitDoubleImg
            arrayOfDoubleIcon[5][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSixSecondSplitDoubleImg
            arrayOfDoubleIcon[6][Utils.MAIN_HAND] = fragmentOnlineGamePlayerSevenMainHandDoubleImg
            arrayOfDoubleIcon[6][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerSevenFirstSplitDoubleImg
            arrayOfDoubleIcon[6][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSevenSecondSplitDoubleImg
        }
    }

    private fun createArrayOfCurrentPlayerIcon() {
        binding.apply {
            arrayOfCurrentPlayerIcon[0][Utils.MAIN_HAND] = fragmentOnlineGameUserImg
            arrayOfCurrentPlayerIcon[0][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGameUserFirstSplitImg
            arrayOfCurrentPlayerIcon[0][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGameUserSecondSplitImg
            arrayOfCurrentPlayerIcon[1][Utils.MAIN_HAND] = fragmentOnlineGameOpponentImg
            arrayOfCurrentPlayerIcon[1][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGameOpponentFirstSplitImg
            arrayOfCurrentPlayerIcon[1][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGameOpponentSecondSplitImg
            arrayOfCurrentPlayerIcon[2][Utils.MAIN_HAND] = fragmentOnlineGamePlayerThreeImg
            arrayOfCurrentPlayerIcon[2][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerThreeFirstSplitImg
            arrayOfCurrentPlayerIcon[2][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerThreeSecondSplitImg
            arrayOfCurrentPlayerIcon[3][Utils.MAIN_HAND] = fragmentOnlineGamePlayerFourImg
            arrayOfCurrentPlayerIcon[3][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFourFirstSplitImg
            arrayOfCurrentPlayerIcon[3][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerFourSecondSplitImg
            arrayOfCurrentPlayerIcon[4][Utils.MAIN_HAND] = fragmentOnlineGamePlayerFiveImg
            arrayOfCurrentPlayerIcon[4][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerFiveFirstSplitImg
            arrayOfCurrentPlayerIcon[4][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerFiveSecondSplitImg
            arrayOfCurrentPlayerIcon[5][Utils.MAIN_HAND] = fragmentOnlineGamePlayerSixImg
            arrayOfCurrentPlayerIcon[5][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerSixFirstSplitImg
            arrayOfCurrentPlayerIcon[5][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSixSecondSplitImg
            arrayOfCurrentPlayerIcon[6][Utils.MAIN_HAND] = fragmentOnlineGamePlayerSevenImg
            arrayOfCurrentPlayerIcon[6][Utils.FIRST_SPLIT_HAND] = fragmentOnlineGamePlayerSevenFirstSplitImg
            arrayOfCurrentPlayerIcon[6][Utils.SECOND_SPLIT_HAND] = fragmentOnlineGamePlayerSevenSecondSplitImg
        }
    }

    private fun initWallet(bankId: Long?) {

        if (bankId != null) {

            viewModel.getBank(bankId).observeOnce(viewLifecycleOwner) { wallet ->
                Log.d(javaClass.simpleName, "initWallet: wallet pseudo:${wallet.pseudo}")
                offlineUser = OfflineUser(
                    wallet
                )
                dealer = Utils.createDealer()
                viewModel.updateOfflineUser(offlineUser)
                binding.fragmentOnlineGameBankAmountTv.text = wallet.amount.toString()
            }

        }
    }

    private fun initPlayer(bankId: Long?) {

        if (bankId != null) {

            viewModel.getOfflineUser().observe(viewLifecycleOwner) { user ->
                binding.apply {
                    Log.d(javaClass.simpleName, "initPlayer: user pseudo: ${user.wallet?.pseudo}")
                    fragmentOnlineGamePlayerBetBtn.text = user.bet[user.playerIndex][Utils.MAIN_HAND].toString()
                    fragmentOnlineGameBankAmountTv.text = user.wallet?.amount.toString()
                }
            }

        }

    }

    private fun initRecyclerViews() {
        for (rv in arrayListOfRv) {
            setupRecyclerView(rv[Utils.MAIN_HAND]!!)
            setupRecyclerView(rv[Utils.FIRST_SPLIT_HAND]!!)
            setupRecyclerView(rv[Utils.SECOND_SPLIT_HAND]!!)
        }
        setupRecyclerView(binding.fragmentOnlineGameDealerRecyclerView)
    }

    private fun initBroadcastTimer() {
        serviceIntent = Intent(requireContext(), TimeService::class.java)
        broadcastTimer = requireActivity().registerReceiver(updateTime, IntentFilter(TimeService.TIMER_UPDATED))
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimeService.TIME_EXTRA, 0.0)
            event()
            Log.e(javaClass.simpleName, "updateTime onReceive: ${time}sec")
        }
    }

    private fun event() {
        if (time == currentTime && dealer.score in 2..16 && offlineUser.player[offlineUser.player.size -1].hand[Utils.MAIN_HAND]!!.size >= 2) {
            currentTime++
            dealerDrawCard(offlineUser, dealer, deck)
        }

        if (time == currentTime && dealer.score > 16) {
            resetTimer()
            stopTimer()
            offlineUser.playerIndex = 0
            compareScoreWithDealer()
        }
    }

    private fun compareScoreWithDealer() {
        for (indexOfPlayer in 0 until offlineUser.player.size) {
            for (indexOfPlayerHand in 0 until offlineUser.player[indexOfPlayer].hand.size) {
                val score = offlineUser.player[indexOfPlayer].score[Utils.getHandTab(indexOfPlayerHand)]!!
                var resultScore: Int
                if (score in 1..21) {
                    resultScore = if (dealer.score < 22) {
                        Utils.compareScore(
                            score,
                            offlineUser.player[indexOfPlayer].hand[Utils.getHandTab(indexOfPlayerHand)]!!.size,
                            Utils.getHandTab(indexOfPlayerHand),
                            offlineUser.player[indexOfPlayer].isPlayerFirstSplit,
                            dealer
                        )
                    } else {
                        Utils.compareScore(
                            score,
                            offlineUser.player[indexOfPlayer].hand.size
                        )
                    }
                    handleResultScore(resultScore, indexOfPlayer, indexOfPlayerHand)
                }
            }
        }
    }

    private fun handleResultScore(resultScore: Int, indexOfPlayer: Int, indexOfPlayerHand: Int) {
        when (resultScore) {
            R.string.fragment_main_game_draw -> { showScoreIcon(indexOfPlayer, indexOfPlayerHand, R.drawable.baseline_front_hand_24) }
            R.string.fragment_main_game_you_lose -> { showScoreIcon(indexOfPlayer, indexOfPlayerHand, R.drawable.baseline_cancel_24) }
            R.string.online_game_fragment_blackjack -> { showScoreIcon(indexOfPlayer, indexOfPlayerHand, R.drawable.baseline_monetization_on_24) }
            R.string.fragment_main_game_you_win -> { showScoreIcon(indexOfPlayer, indexOfPlayerHand, R.drawable.baseline_check_circle_24) }
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

            fragmentOnlineGameFab.setOnClickListener {
                val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                fragmentOnlineGameFab.startAnimation(animUp)
                fragmentOnlineGameFabBorderImg.startAnimation(animUp)
                val betDialog = BetDialog()
                betDialog.show(requireActivity().supportFragmentManager, betDialog.tag)
            }

            fragmentOnlineGameGameStart.setOnClickListener {
                val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                fragmentOnlineGameGameStart.startAnimation(animUp)
                offlineUser.player = Utils.createArrayListOfPlayerHand(offlineUser.player.size)
                dealer = Utils.createDealer()
                resetDoubleIcon(arrayOfDoubleIcon)
                resetRecyclerView(dealer)
                resetScoreView()
                distributionSequence(deck)
                enableHitAndStopBtn()
                hideStartBtn()
                hideSplitScore()
                showCurrentPlayerIcon(
                    arrayOfCurrentPlayerIcon[offlineUser.playerIndex][offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf]!!
                )
            }

            fragmentOnlineGameHitBtn.setOnClickListener {

                val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                fragmentOnlineGameHitBtn.startAnimation(animUp)
                disableDoubleAndSplitBtn()
                playerDrawCard(offlineUser.player, deck, offlineUser.playerIndex)
                playerBust(offlineUser, deck)
                addCardToView(offlineUser, dealer)
                showScoreView(offlineUser, dealer)
            }

            fragmentOnlineGameStopBtn.setOnClickListener {
                val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                fragmentOnlineGameStopBtn.startAnimation(animUp)
                nextHandOrDealerTurn(deck)
            }

            fragmentOnlineGameDoubleBtn.setOnClickListener {
                val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                fragmentOnlineGameDoubleBtn.startAnimation(animUp)
                showDoubleIconVisibility(
                    arrayOfDoubleIcon,
                    offlineUser.playerIndex,
                    offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf,
                    View.VISIBLE
                )
                playerDrawCard(offlineUser.player, deck, offlineUser.playerIndex)
                addCardToView(offlineUser, dealer)
                showScoreView(offlineUser, dealer)
                nextHandOrDealerTurn(deck)

            }

            fragmentOnlineGameSplitBtn.setOnClickListener {
                val animUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                fragmentOnlineGameStopBtn.startAnimation(animUp)
                Utils.splitPlayerGame(
                    offlineUser.player[offlineUser.playerIndex].currentHandType,
                    offlineUser.player[offlineUser.playerIndex]
                )
                playerDrawCard(offlineUser.player, deck, offlineUser.playerIndex)
                playerCanSplit(offlineUser)
                addCardToView(offlineUser, dealer)
                showScoreView(offlineUser, dealer)
            }
        }
    }

    private fun resetDoubleIcon(
        arrayOfDoubleIcon: ArrayList<HashMap<String, AppCompatImageView>>
    ) {
        for (i in 0 until arrayOfDoubleIcon.size) {
            for (j in 0 until arrayOfDoubleIcon[i].size) {
                showDoubleIconVisibility(
                    arrayOfDoubleIcon,
                    i,
                    Utils.getHandTab(j),
                    View.GONE
                )
            }
        }
    }

    private fun showDoubleIconVisibility(
        arrayOfDoubleIcon: ArrayList<HashMap<String, AppCompatImageView>>,
        playerIndex: Int,
        playerHand: String,
        visibility: Int
    ) {
        arrayOfDoubleIcon[playerIndex][playerHand]?.visibility = visibility
    }

    private fun nextHandOrDealerTurn(deck: OnlineDeck) {
        val previousIcon = arrayOfCurrentPlayerIcon[offlineUser.playerIndex][offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf]!!
        hidePreviousPlayerIcon(previousIcon)
        when{
            offlineUser.player[offlineUser.playerIndex].isPlayerFirstSplit && offlineUser.player[offlineUser.playerIndex].currentHandType == HandType.MainHand -> {
                offlineUser.player[offlineUser.playerIndex].currentHandType = HandType.FirstSplit
                playerDrawCard(offlineUser.player, deck, offlineUser.playerIndex)
                addCardToView(offlineUser, dealer)
                showScoreView(offlineUser, dealer)
                playerCanDouble(offlineUser)
                playerCanSplit(offlineUser)
            }
            offlineUser.player[offlineUser.playerIndex].isPlayerSecondSplit && offlineUser.player[offlineUser.playerIndex].currentHandType == HandType.FirstSplit -> {
                offlineUser.player[offlineUser.playerIndex].currentHandType = HandType.SecondSplit
                playerDrawCard(offlineUser.player, deck, offlineUser.playerIndex)
                addCardToView(offlineUser, dealer)
                showScoreView(offlineUser, dealer)
                playerCanDouble(offlineUser)
                playerCanSplit(offlineUser)
            }
            offlineUser.playerIndex < offlineUser.player.size - 1 -> {
                offlineUser.playerIndex++
                playerCanDouble(offlineUser)
                playerCanSplit(offlineUser)
            }
            else -> {
                startTimer()
                disableBtn()
            }
        }
        val nextIcon = arrayOfCurrentPlayerIcon[offlineUser.playerIndex][offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf]!!
        showCurrentPlayerIcon(nextIcon)
    }

    private fun playerDrawCard(player: ArrayList<Player>, deck: OnlineDeck, playerIndex: Int) {
        Utils.addCardToPlayerHandList(player[playerIndex], deck, player[playerIndex].currentHandType)
        player[playerIndex].isPlayerDrawAce[player[playerIndex].currentHandType.valueOf] =
            Utils.playerDrawAnAceOrNot(deck, player[playerIndex], player[playerIndex].currentHandType)
        player[playerIndex].isPlayerScoreSoft[player[playerIndex].currentHandType.valueOf] =
            Utils.playerHaveASoftScoreOrNot(player[playerIndex], deck, player[playerIndex].currentHandType)
        if (!player[playerIndex].isPlayerScoreSoft[player[playerIndex].currentHandType.valueOf]!!) {
            setPlayerDrawAceAndScoreSoftToFalse(player[playerIndex])
        }
        deck.index = Utils.incrementDeckIndex(deck)
    }

    private fun dealerDrawCard(offlineUser: OfflineUser, dealer: Dealer, deck: OnlineDeck) {
        Utils.addCardToDealerList(dealer, deck)
        dealer.isDealerDrawAce = Utils.dealerDrawAnAceOrNot(deck, dealer)
        dealer.isDealerScoreSoft = Utils.modifyDealerScoreWithAce(dealer, deck)
        dealerHaveBlackjack(dealer)
        addCardToView(offlineUser, dealer)
        showScoreView(offlineUser, dealer)
        deck.index = Utils.incrementDeckIndex(deck)
        showStartBtn()
    }

    private fun playerBust(offlineUser: OfflineUser, deck: OnlineDeck) {
        val indexOfPlayer = offlineUser.playerIndex
        val indexOfPlayerHand = offlineUser.player[offlineUser.playerIndex].currentHandType
        if (offlineUser.player[indexOfPlayer].score[indexOfPlayerHand.valueOf]!! > 21) {
            nextHandOrDealerTurn(deck)
            showScoreIcon(
                indexOfPlayer,
                Utils.getHandType(indexOfPlayerHand),
                R.drawable.baseline_cancel_24
            )
        }
    }

    private fun prepareDeck(): OnlineDeck {
        val deck = OnlineDeck()
        deck.deckList = Utils.createDeck().deckList
        Utils.shuffleDeck(deck.deckList!!)
        return deck
    }

    private fun distributionSequence(deck: OnlineDeck) {
        giveOneCardToAllBox(offlineUser, deck)
        dealerDrawCard(offlineUser, dealer, deck)
        giveOneCardToAllBox(offlineUser, deck)
        playerCanSplit(offlineUser)
        playerCanDouble(offlineUser)
        playerHaveBlackjack(offlineUser)
        binding.fragmentOnlineGameNestedScrollView.post {
            binding.fragmentOnlineGameNestedScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun giveOneCardToAllBox(offlineUser: OfflineUser, deck: OnlineDeck) {
        for (playerIndex in 0 until offlineUser.player.size) {
            playerDrawCard(offlineUser.player, deck, playerIndex)
        }
    }

    private fun setPlayerDrawAceAndScoreSoftToFalse(playerHand: Player) {
        playerHand.isPlayerDrawAce[offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf] = false
        playerHand.isPlayerScoreSoft[offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf] = false
    }

    private fun playerHaveBlackjack(offlineUser: OfflineUser) {
        for (i in 0 until offlineUser.player.size) {
            if(Utils.isPlayerHaveBlackjack(offlineUser.player[i])){
                arrayOfScore[i][Utils.MAIN_HAND]?.text = requireContext().resources.getString(R.string.online_game_fragment_blackjack)
            }
        }
    }

    private fun dealerHaveBlackjack(dealer: Dealer) {
        if(Utils.isDealerHaveBlackJack(dealer)) {
            binding.fragmentOnlineGameDealerScoreTv.text = requireContext().resources.getString(R.string.online_game_fragment_blackjack)
        }
    }

    private fun playerCanSplit(offlineUser: OfflineUser) {
        val firstCard = offlineUser.player[offlineUser.playerIndex].hand[offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf]!![0]
        val secondCard = offlineUser.player[offlineUser.playerIndex].hand[offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf]!![1]
        val isSecondSplit = offlineUser.player[offlineUser.playerIndex].isPlayerSecondSplit
        binding.fragmentOnlineGameSplitBtn.isEnabled = firstCard.value == secondCard.value && !isSecondSplit
    }

    private fun playerCanDouble(offlineUser: OfflineUser) {
        val playerHandType = offlineUser.player[offlineUser.playerIndex].currentHandType.valueOf
        if (offlineUser.player[offlineUser.playerIndex].hand[playerHandType]?.size == 2) {
            binding.fragmentOnlineGameDoubleBtn.isEnabled = true
        }
    }

    private fun addCardToView(offlineUser: OfflineUser, dealer: Dealer?) {
        for (i in 0 until offlineUser.player.size) {
            for (j in 0 until offlineUser.player[i].hand.size) {
                loadHandIntoRecyclerView(
                    offlineUser.player[i].hand[Utils.getHandTab(j)],
                    arrayOfAdapter[i][Utils.getHandTab(j)]!!,
                    arrayListOfRv[i][Utils.getHandTab(j)]!!,
                    )
            }
        }
        loadHandIntoRecyclerView(
            dealer?.hand,
            dealerHandAdapter,
            binding.fragmentOnlineGameDealerRecyclerView
        )
    }

    private fun glideImage(
        icon: Int,
        currentPlayerIcon: AppCompatImageView
    ) {
        Glide.with(binding.root)
            .load(ContextCompat.getDrawable(requireContext(), icon))
            .centerCrop()
            .into(currentPlayerIcon)
    }

    private fun hidePreviousPlayerIcon(imageView: AppCompatImageView) {
        if (imageView.drawable != ContextCompat.getDrawable(requireContext(),R.drawable.baseline_cancel_24) ) {
            imageView.visibility = View.GONE
        }
    }

    private fun showCurrentPlayerIcon(imageView: AppCompatImageView) {
        imageView.visibility = View.VISIBLE
        glideImage(R.drawable.baseline_play_arrow_24, imageView)
    }

    private fun showScoreIcon(
        indexOfPlayer: Int,
        indexOfPlayerHand: Int,
        icon: Int
    ) {
        val view = arrayOfCurrentPlayerIcon[indexOfPlayer][Utils.getHandTab(indexOfPlayerHand)]
        view?.visibility = View.VISIBLE
        glideImage(icon, view!!)
    }

    private fun resetRecyclerView(dealer: Dealer?) {
        for (i in 0 until arrayListOfRv.size) {
            for (j in 0 until arrayListOfRv[i].size) {
                loadHandIntoRecyclerView(
                    null,
                    arrayOfAdapter[i][Utils.getHandTab(j)]!!,
                    arrayListOfRv[i][Utils.getHandTab(j)]!!,
                )
            }
        }
        loadHandIntoRecyclerView(
            dealer?.hand,
            dealerHandAdapter,
            binding.fragmentOnlineGameDealerRecyclerView
        )
    }

    private fun showScoreView(offlineUser: OfflineUser, dealer: Dealer) {
        binding.apply {
            for (i in 0 until offlineUser.player.size) {
                for (j in 0 until offlineUser.player[i].hand.size) {
                    if (offlineUser.player[i].score[Utils.getHandTab(j)]!! > 0) {
                        arrayOfScore[i][Utils.getHandTab(j)]?.text = offlineUser.player[i].score[Utils.getHandTab(j)].toString()
                    }
                }
            }
            fragmentOnlineGameDealerScoreTv.text = dealer.score.toString()
            playerHaveBlackjack(offlineUser)
        }
    }

    private fun resetScoreView() {
        binding.apply {
            for (playerIndex in 0 until arrayOfScore.size) {
                for (playerHandIndex in 0 until arrayOfScore[playerIndex].size) {
                    arrayOfScore[playerIndex][Utils.getHandTab(playerHandIndex)]?.text = ""
                    arrayOfCurrentPlayerIcon[playerIndex][Utils.getHandTab(playerHandIndex)]?.visibility = View.GONE
                }
            }
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
        binding.fragmentOnlineGameDealerImg.visibility = View.GONE
    }

    private fun hideSplitScore() {
        binding.apply {
            fragmentOnlineGamePlayerFirstSplitScoreTv.text = ""
            fragmentOnlineGamePlayerSecondSplitScoreTv.text = ""
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) = recyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadHandIntoRecyclerView(hand: ArrayList<Card>?, adapter: GameAdapter, recyclerView: RecyclerView) {
        adapter.submitList(hand)
        recyclerView.adapter = adapter
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        requireActivity().unregisterReceiver(updateTime)
    }

    companion object {
        const val PLAYER_SAVE_ID = "player_save_id"
    }

}