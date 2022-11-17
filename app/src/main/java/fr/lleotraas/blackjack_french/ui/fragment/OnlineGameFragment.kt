package fr.lleotraas.blackjack_french.ui.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineGameBinding
import fr.lleotraas.blackjack_french.model.*
import fr.lleotraas.blackjack_french.service.TimeService
import fr.lleotraas.blackjack_french.service.TimeService.Companion.TIME_EXTRA
import fr.lleotraas.blackjack_french.ui.activity.OnlineGameActivityViewModel
import fr.lleotraas.blackjack_french.ui.activity.OnlineMainScreenActivity
import fr.lleotraas.blackjack_french.ui.dialog.OnlineBetDialog
import fr.lleotraas.blackjack_french.ui.dialog.OpponentQuitGameDialog
import fr.lleotraas.blackjack_french.ui.dialog.QuitOnlineGameDialog
import fr.lleotraas.blackjack_french.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.FIRST_SPLIT
import fr.lleotraas.blackjack_french.utils.Utils.Companion.FIRST_SPLIT_BET
import fr.lleotraas.blackjack_french.utils.Utils.Companion.INSURANCE_BET
import fr.lleotraas.blackjack_french.utils.Utils.Companion.MAIN_HAND
import fr.lleotraas.blackjack_french.utils.Utils.Companion.MAIN_HAND_BET
import fr.lleotraas.blackjack_french.utils.Utils.Companion.SEARCHED_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.SECOND_SPLIT
import fr.lleotraas.blackjack_french.utils.Utils.Companion.SECOND_SPLIT_BET
import fr.lleotraas.blackjack_french.utils.Utils.Companion.TOTAL_BET
import fr.lleotraas.blackjack_french.utils.Utils.Companion.addCardToDealerList
import fr.lleotraas.blackjack_french.utils.Utils.Companion.addCardToPlayerHandList
import fr.lleotraas.blackjack_french.utils.Utils.Companion.changeTotalBet
import fr.lleotraas.blackjack_french.utils.Utils.Companion.compareScore
import fr.lleotraas.blackjack_french.utils.Utils.Companion.convertTimeInPercent
import fr.lleotraas.blackjack_french.utils.Utils.Companion.createBet
import fr.lleotraas.blackjack_french.utils.Utils.Companion.createDeck
import fr.lleotraas.blackjack_french.utils.Utils.Companion.dealerDrawAnAceOrNot
import fr.lleotraas.blackjack_french.utils.Utils.Companion.disableDoubleBtn
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getBetTypeByInt
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getHandType
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getHandTypeForDoubledBox
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getNextPlayer
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getPlayerHand
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getTimeCount
import fr.lleotraas.blackjack_french.utils.Utils.Companion.isDealerHaveBlackJack
import fr.lleotraas.blackjack_french.utils.Utils.Companion.modifyDealerScoreWithAce
import fr.lleotraas.blackjack_french.utils.Utils.Companion.modifyFirstSplitScore
import fr.lleotraas.blackjack_french.utils.Utils.Companion.modifyPlayerScoreWithAce
import fr.lleotraas.blackjack_french.utils.Utils.Companion.paymentForPlayer
import fr.lleotraas.blackjack_french.utils.Utils.Companion.playerDrawAnAceOrNot
import fr.lleotraas.blackjack_french.utils.Utils.Companion.retrieveBetInWallet
import fr.lleotraas.blackjack_french.utils.Utils.Companion.retrieveInsuranceBetInWallet
import fr.lleotraas.blackjack_french.utils.Utils.Companion.shuffleDeck
import fr.lleotraas.blackjack_french.utils.Utils.Companion.splitPlayerGame
import fr.lleotraas.blackjack_french.utils.Utils.Companion.updateInsurance
import fr.lleotraas.blackjack_french.utils.Utils.Companion.updateWallet

@AndroidEntryPoint
class OnlineGameFragment : Fragment() {

    private lateinit var mBinding: FragmentOnlineGameBinding
    private val mViewModel: OnlineGameActivityViewModel by viewModels()
    private lateinit var currentUser: User
    private var opponent = User()
    private var deck = OnlineDeck()
    private lateinit var playerOneHandAdapter: GameAdapter
    private lateinit var playerOneHandFirstSplitAdapter: GameAdapter
    private lateinit var playerOneHandSecondSplitAdapter: GameAdapter
    private lateinit var playerTwoHandAdapter: GameAdapter
    private lateinit var playerTwoHandFirstSplitAdapter: GameAdapter
    private lateinit var playerTwoHandSecondSplitAdapter: GameAdapter
    private lateinit var dealerHandAdapter: GameAdapter
    private var dealer = Dealer(0, ArrayList(), isDealerDrawAce = false, isDealerScoreSoft = false)
    private val playerOneHand = Player(Array(3){0}, Array(3){ArrayList()}, PlayerNumberType.PLAYER_ONE, isPlayerDrawAce = Array(3){false}, isPlayerScoreSoft = Array(3){false}, isPlayerFirstSplit = false, isPlayerSecondSplit = false)
    private val playerTwoHand = Player(Array(3){0}, Array(3){ArrayList()}, PlayerNumberType.PLAYER_TWO, isPlayerDrawAce = Array(3){false}, isPlayerScoreSoft = Array(3){false}, isPlayerFirstSplit = false, isPlayerSecondSplit = false)
    private val playerTab = listOf(playerOneHand, playerTwoHand)
    private var isEndOfGame = true
    private var serviceIntent: Intent? = null
    private var broadcastTimer: Intent? = null
    private var time = -1.0
    private var isTimerStarted = false
    private var isLoanDialogOpen = false
    private var animation: ViewPropertyAnimator? = null
    private lateinit var callBack: OnBackPressedCallback
    private var isCurrentUserQuit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOnlineGameBinding.inflate(inflater, container, false)
        val currentUserId = requireActivity().intent.extras!!.get(CURRENT_USER_ID) as String
        val opponentId = requireActivity().intent.extras!!.get(SEARCHED_USER_ID) as String
        onBackPressed()
        initAdapters()
        initBroadcastTimer()
        setupRecyclerViews()
        updateUI(currentUserId, opponentId)
        removeOldOnlineDeck(currentUserId)
        configureListeners(currentUserId)
        disableGameBtn()
        mViewModel.isNumberOfGamePlayedUpdated()
        return mBinding.root
    }

    private fun onBackPressed() {
        callBack = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val quitDialog = QuitOnlineGameDialog()
            quitDialog.show(requireActivity().supportFragmentManager, quitDialog.tag)
        }
    }

    private fun disableGameBtn() {
        mBinding.apply {
            fragmentOnlineGameDoubleBtn.isEnabled = false
            fragmentOnlineGameSplitBtn.isEnabled = false
            fragmentOnlineGameHitBtn.isEnabled = false
            fragmentOnlineGameStopBtn.isEnabled = false
        }
    }

    private fun removeOldOnlineDeck(currentUserId: String) {
        if (mViewModel.isDeckExist(currentUserId)) {
            mViewModel.deleteOnlineDeck(currentUserId)
        }
    }

    private fun initAdapters() {
        playerOneHandAdapter = GameAdapter()
        playerOneHandFirstSplitAdapter = GameAdapter()
        playerOneHandSecondSplitAdapter = GameAdapter()
        playerTwoHandAdapter = GameAdapter()
        playerTwoHandFirstSplitAdapter = GameAdapter()
        playerTwoHandSecondSplitAdapter = GameAdapter()
        dealerHandAdapter = GameAdapter()
    }

    private fun initBroadcastTimer() {
        serviceIntent = Intent(requireContext(), TimeService::class.java)
        broadcastTimer = requireActivity().registerReceiver(updateTime, IntentFilter(TimeService.TIMER_UPDATED))
    }

    private val updateTime: BroadcastReceiver= object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TIME_EXTRA, 0.0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressBar.setProgress(
                    convertTimeInPercent(time), true)
            } else {
                mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressBar.progress = convertTimeInPercent(time)
            }
            mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressTimeTv.text = String.format("%2d", getTimeCount(time))

            enableHitAndStopButtons()
            enableSplitButton()
            mBinding.fragmentOnlineGameDoubleBtn.isEnabled = disableDoubleBtn(getCurrentPlayer(), currentUser, dealer, time)
            gameBegin()
            if (time % 2.0 == 0.0) {
                animation?.start()
            }

            event(time)
            Log.e(javaClass.simpleName, "updateTime onReceive: ${time}sec")
        }
    }

    private fun resetTimer() {
        time = -1.0
    }

    private fun event(time: Double) {
        if (time == 20.0 && !isEndOfGame) {
            playerPass()
            resetTimerAndRestart()
        }
        if (time == 10.0 && isCurrentUserQuit) {
            mViewModel.updateOnlineStatus(currentUser.id.toString(), OnlineStatusType.OFFLINE)
            stopAndResetTimer()
            requireActivity().finish()
        }
    }

    private fun showNextTimer() {
        mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressBar.visibility = View.VISIBLE
        mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressTimeTv.visibility = View.VISIBLE
    }

    private fun hidePreviousTimer() {
        mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressBar.visibility = View.GONE
        mBinding.fragmentOnlineGamePlayerTwoFirstSplitProgressTimeTv.visibility = View.GONE
    }

    private fun startTimer() {
        if (!isTimerStarted) {
            serviceIntent?.putExtra(TIME_EXTRA, time)
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

    private fun resetTimerAndRestart() {
        stopTimer()
        resetTimer()
        startTimer()
    }

    private fun stopAndResetTimer() {
        stopTimer()
        resetTimer()
    }

    private fun stopServiceAndUnregisterReceiver() {
        requireActivity().unregisterReceiver(updateTime)
    }

    private fun setupRecyclerViews() {
        setupPlayerOneHandRecyclerView()
        setupPlayerOneHandFirstSplitRecyclerView()
        setupPlayerOneHandSecondSplitRecyclerView()
        setupPlayerTwoHandRecyclerView()
        setupPlayerTwoFirstSplitHandRecyclerView()
        setupPlayerTwoSecondSplitHandRecyclerView()
        setupPlayerDealerHandRecyclerView()
    }

    private fun updateUI(currentUserId: String, searchedUserId: String) {
        hidePictures()
        getCurrentUser(currentUserId)
        getOpponent(searchedUserId)
    }

    private fun getCurrentUser(currentUserId: String) {
        mViewModel.getOnlineUser(currentUserId).observe(viewLifecycleOwner, this::updateCurrentUser)
    }

    private fun updateCurrentUser(currentUser: User?) {
        this.currentUser = currentUser!!
        isCurrentUserQuit = currentUser.onlineStatus == OnlineStatusType.OFFLINE
        loadCurrentUserProfileImage(currentUser)
        showBet(currentUser)
        opponentLeaveGame(currentUser)
        readyPictureCurrentUserVisibility(currentUser)
        prepareDeck(currentUser)
        gameStart()
        contractLoan()
        saveGameStats()
    }

    private fun loadCurrentUserProfileImage(user: User) {
        mViewModel.getAllImage().observe(viewLifecycleOwner) { listOfAllImage ->
            loadUserImageProfile(user, listOfAllImage)
        }
    }

    private fun loadUserImageProfile(user: User, listOfAllImage: HashMap<String, ByteArray>) {
        val userPicture = if (user.isDefaultProfileImage == true) user.userPicture else listOfAllImage[user.id]
        mBinding.apply {
            useGlide(root, userPicture!!, fragmentOnlineGameUserImg, user.pictureRotation!!)
            useGlide(root, userPicture, fragmentOnlineGameUserFirstSplitImg, user.pictureRotation!!)
            useGlide(root, userPicture, fragmentOnlineGameUserSecondSplitImg, user.pictureRotation!!)
            useGlide(root, userPicture, fragmentOnlineGameCurrentUserReadyImg, user.pictureRotation!!)
            fragmentOnlineGameBankAmountTv.text = currentUser.wallet.toString()
            fragmentOnlineGameBetBtn.text = currentUser.bet?.get(TOTAL_BET)?.toString() ?: ""
        }
    }

    private fun contractLoan() {
        if (currentUser.wallet!! < 25.0 && !isLoanDialogOpen) {
            showLoanDialog().show()
            isLoanDialogOpen = true
            stopAndResetTimer()
        }
    }

    private fun showLoanDialog() = AlertDialog.Builder(requireContext()).apply {
        setTitle(requireContext().resources.getString(R.string.online_game_fragment_contract_loan))
        setMessage(requireContext().resources.getString(R.string.online_game_fragment_loan_explication))
        setPositiveButton(requireContext().resources.getString(R.string.dialog_invitation_to_play_positive_btn)) { dialogInterface, _ ->
            isLoanDialogOpen = false
            currentUser.wallet = currentUser.wallet?.plus(1500.0)
            currentUser.numberOfLoan = currentUser.numberOfLoan?.plus(1)
            mViewModel.updateOnlineUserWalletAndLoan(currentUser)
            resetTimerAndRestart()
            Log.e(TAG, "contractLoan: time not stopped")
            dialogInterface.dismiss()
        }
        setNegativeButton(requireContext().resources.getString(R.string.dialog_invitation_to_play_negative_btn)) { dialogInterface, _ ->
            isLoanDialogOpen = false
            resetTimerAndRestart()
            Log.e(TAG, "contractLoan: time not stopped")
            dialogInterface.dismiss()
        }
    }


    private fun playerTwoPictureAnimation() {
        if (
            playerTwoHand.hand[MAIN_HAND].size == 2 &&
            !playerTwoHand.isPlayerFirstSplit &&
            currentUser.playerTurn == PlayerNumberType.PLAYER_TWO
        ) {
            animation = animateImageForPlayer(mBinding.fragmentOnlineGameUserImg)
        }
    }

    private fun showBet(currentUser: User) {
        mBinding.fragmentOnlineGameGameStart.text = String.format("%s \n %s",requireContext().resources.getString(R.string.fragment_main_game_start_game) ,currentUser.bet!![MAIN_HAND_BET].toString())
    }

    private fun getOpponent(searchedUserId: String) {
        mViewModel.getSearchedUser(searchedUserId).observe(viewLifecycleOwner, this::updateOpponent)
    }

    private fun updateOpponent(opponent: User?) {
        this.opponent = opponent!!
        loadOpponentProfileImage(opponent)
        opponentLeaveGame(opponent)
        readyPictureOpponentVisibility(opponent)
        gameStart()
        if (opponent.onlineStatus != OnlineStatusType.OFFLINE && opponent.bet!![FIRST_SPLIT_BET]!! > 0) {
            opponentSplitHand()
        }
    }

    private fun loadOpponentProfileImage(opponent: User) {
        mViewModel.getAllImage().observe(viewLifecycleOwner) { listOfAllImage ->
            loadOpponentImageProfile(opponent, listOfAllImage)
        }
    }

    private fun loadOpponentImageProfile(opponent: User, listOfAllImage: HashMap<String, ByteArray>) {
        mBinding.apply {
            val opponentPicture = if (opponent.isDefaultProfileImage == true) opponent.userPicture else listOfAllImage[opponent.id]
            useGlide(root, opponentPicture!!, fragmentOnlineGameOpponentImg, opponent.pictureRotation!!)
            useGlide(root, opponentPicture, fragmentOnlineGameOpponentFirstSplitImg, opponent.pictureRotation!!)
            useGlide(root, opponentPicture, fragmentOnlineGameOpponentSecondSplitImg, opponent.pictureRotation!!)
            useGlide(root, opponentPicture, fragmentOnlineGameOpponentReadyImg, opponent.pictureRotation!!)
        }
    }

    private fun getOnlineDeck() {
        mViewModel.getOnlineDeck(isCurrentUserOrOpponent().id!!).observe(viewLifecycleOwner, this::updateOnlineDeck)
    }

    private fun updateOnlineDeck(onlineDeck: OnlineDeck?) {
        if (deck.index!! < onlineDeck!!.index!! && onlineDeck.playerTurn != PlayerNumberType.DEALER) {
            if (PlayerNumberType.PLAYER_ONE == currentUser.playerTurn && deck.playerTurn == PlayerNumberType.PLAYER_TWO) {
                opponentDrawCard(getCurrentPlayer())
            } else if(PlayerNumberType.PLAYER_TWO == currentUser.playerTurn && deck.playerTurn == PlayerNumberType.PLAYER_ONE && dealer.score < 17){
                opponentDrawCard(getCurrentPlayer())
            }
        }
//        Log.e(TAG, "INDEX = ${deck.index} \t ONLINE INDEX = ${onlineDeck.index} \t PLAYER TURN = ${onlineDeck.playerTurn} \t CARD = ${onlineDeck.deckList?.get(onlineDeck.index!!)!!.number} \t ${currentUser.pseudo}" )

        deck = onlineDeck
        if (!isTimerStarted) {
            resetTimerAndRestart()
        }
        if (
            currentUser.playerTurn == PlayerNumberType.PLAYER_TWO &&
            currentUser.playerTurn == deck.playerTurn
        ) {
            showNextTimer()
        }
        dealerTurn(onlineDeck)
        gameStart()
        playerTwoPictureAnimation()
    }

    private fun dealerTurn(onlineDeck: OnlineDeck) {
        if (onlineDeck.playerTurn == PlayerNumberType.DEALER) {
            val listOfPlayer = ArrayList<Player>()
            hidePreviousTimer()
            stopAndResetTimer()
            listOfPlayer.add(getCurrentPlayer())
            listOfPlayer.add(getCurrentOpponent())
            while (dealer.score < 17) {
                dealerDrawCard()
            }
            updateScoreUiAndBet(listOfPlayer)
            gameIsOver()
            mViewModel.updateOnlineDeckIndex(isCurrentUserOrOpponent().id!!, onlineDeck.index!!)
            Log.e(TAG, "END OF TURN timer: ${time}sec")
        }
    }

    private fun updateScoreUiAndBet(listOfPlayer: ArrayList<Player>) {
        Log.e(TAG, "Dealer score = ${dealer.score}")
        val dealerHaveBlackjack = isDealerHaveBlackJack(dealer)
        currentUser.bet!![INSURANCE_BET] = updateInsurance(currentUser, dealerHaveBlackjack)
        for (player in listOfPlayer) {
            for (index in 0 until player.score.size) {
                val messageResult = requireContext().resources.getString(compareScore(player.score[index], player.hand[index].size, dealer))
                if (player.playerNumberType == currentUser.playerTurn) {
                    when {
                        player.score[index] in 1..21 -> {
                            currentUser.bet!![getBetTypeByInt(index)] =
                                payment(messageResult, currentUser.bet!![getBetTypeByInt(index)]!!)
                        }
                        else -> {
                            currentUser.bet!![getBetTypeByInt(index)] = paymentForPlayer(
                                requireContext().resources.getString(R.string.fragment_main_game_you_lose),
                                currentUser.bet!![getBetTypeByInt(index)]!!
                            )
                        }
                    }
                }
                if (player.score[index] in 1..21) {
                    showResultScoreUI(player, index, dealer, messageResult)
                }
            }
            if (player.playerNumberType == currentUser.playerTurn) {
                mViewModel.updateOnlineUserBetAndWallet(updateWallet(currentUser))
            }
        }
    }

    private fun payment(messageResult: String, bet: Double): Double {
        return when {
            dealer.score < 22 || messageResult == "BJ" -> {
                paymentForPlayer(messageResult, bet)
            }
            else -> {
                paymentForPlayer(requireContext().resources.getString(R.string.fragment_main_game_you_win), bet)
            }
        }
    }

    private fun showResultScoreUI(player: Player, handType: Int, dealer: Dealer, messageResult: CharSequence) {
        val playerOneResultTvTab = createPlayerOneResultTvTab()
        val playerTwoResultTvTab = createPlayerTwoResultTvTab()
        if (dealer.score < 22) {
            showPlayerOrOpponentResult(player, playerOneResultTvTab[handType], playerTwoResultTvTab[handType]).text = messageResult
        } else {
            mBinding.fragmentOnlineGameDealerResultTv.text = requireContext().resources.getString(R.string.fragment_main_game_dealer_bust)
        }
    }

    private fun createPlayerOneResultTvTab() = listOf(
            mBinding.fragmentOnlineGameResultTv,
            mBinding.fragmentOnlineGameFirstSplitResultTv,
            mBinding.fragmentOnlineGameSecondSplitResultTv
        )

    private fun createPlayerTwoResultTvTab() = listOf(
            mBinding.fragmentOnlineGamePlayerTwoMainHandResultTv,
            mBinding.fragmentOnlineGamePlayerTwoFirstSplitResultTv,
            mBinding.fragmentOnlineGamePlayerTwoSecondSplitResultTv
        )

    private fun createPlayerOneScoreTab() = listOf(
        mBinding.fragmentOnlineGamePlayerScoreTv,
        mBinding.fragmentOnlineGamePlayerFirstSplitScoreTv,
        mBinding.fragmentOnlineGamePlayerSecondSplitScoreTv
    )

    private fun createPlayerTwoScoreTab() = listOf(
        mBinding.fragmentOnlineGamePlayerTwoMainHandScoreTv,
        mBinding.fragmentOnlineGamePlayerTwoFirstSplitScoreTv,
        mBinding.fragmentOnlineGamePlayerTwoSecondSplitScoreTv
    )

    private fun showPlayerOrOpponentResult(playerHand: Player, playerOneResultTv: AppCompatTextView, playerTwoResultTv: AppCompatTextView): AppCompatTextView {
        return if(
            currentUser.playerTurn == PlayerNumberType.PLAYER_ONE && playerHand.playerNumberType == PlayerNumberType.PLAYER_ONE ||
            currentUser.playerTurn == PlayerNumberType.PLAYER_TWO && playerHand.playerNumberType == PlayerNumberType.PLAYER_TWO) {
                playerOneResultTv
        }
        else {
                playerTwoResultTv
        }
    }

    private fun gameStart() {
        if (currentUser.isUserReady!! && opponent.isUserReady!! && dealer.hand.size == 0 && deck.deckList != null) {
            resetTimerAndRestart()

            mBinding.fragmentOnlineGameGameStart.apply {
                isEnabled = false
                animate().apply {
                    duration = 2000
                    alphaBy(-50f)
                }
            }
        }
    }

    private fun gameBegin() {
        if (time == 2.0 && isEndOfGame && currentUser.isUserReady!! && opponent.isUserReady!! && dealer.score == 0) {
            hidePictures()
            distributionSequence()
            mBinding.fragmentOnlineGameGameStart.visibility = View.GONE
            decreasePlayerBetWallet(currentUser.bet!![MAIN_HAND_BET]!!)
            isEndOfGame = false
            Log.e(TAG, "gameBegin")
        }
    }

    private fun distributionSequence() {
        distributionPlayersDrawCard()
        distributionDealerDrawCard()
        distributionPlayersDrawCard()

        Log.e(TAG, "distributionSequence: player draw five cards")
        loadPlayersHandsIntoRecyclerViews(getCurrentPlayer().hand[MAIN_HAND], getCurrentOpponent().hand[MAIN_HAND])
        refreshScoreUI()
        mBinding.apply {
            fragmentOnlineGameDealerScoreTv.visibility = View.VISIBLE
            fragmentOnlineGameUserImg.visibility = View.VISIBLE
            animation = animateImageForPlayer(fragmentOnlineGameUserImg)
            fragmentOnlineGameOpponentImg.visibility = View.VISIBLE
            fragmentOnlineGamePlayerScoreTv.visibility = View.VISIBLE
            fragmentOnlineGamePlayerTwoMainHandScoreTv.visibility = View.VISIBLE
        }
        getCurrentUser(currentUser.id!!)
        mViewModel.updateOnlineDeckIndex(isCurrentUserOrOpponent().id!!, deck.index!!)
        showInsurance()
        playerHaveBlackJack()
        resetTimer()
        startTimer()
        if (
            currentUser.playerTurn == PlayerNumberType.PLAYER_ONE &&
            currentUser.playerTurn == deck.playerTurn
                ) {
            showNextTimer()
        }
    }

    private fun distributionDealerDrawCard() {
        addCardToDealerList(dealer, deck)
        dealer.isDealerDrawAce = dealerDrawAnAceOrNot(deck, dealer)
        dealer.isDealerScoreSoft = modifyDealerScoreWithAce(dealer, deck)
        deck.index = deck.index!! + 1
    }

    private fun distributionPlayersDrawCard() {
        for (index in 0 until 2) {
            addCardToPlayerHandList(playerTab[index], deck, currentUser.splitType!!)
            playerTab[index].isPlayerDrawAce[getHandType(currentUser.splitType)] = playerDrawAnAceOrNot(deck, playerTab[index], currentUser.splitType!!)
            playerTab[index].isPlayerScoreSoft[getHandType(currentUser.splitType)] = modifyPlayerScoreWithAce(playerTab[index], deck, currentUser.splitType!!)
            deck.index = deck.index!! + 1
            Log.e(TAG, "Player N°${index+1} draw a card index:${deck.index}")
        }
    }

    private fun showInsurance() {
        if (dealer.score == 11) {
            stopAndResetTimer()
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle(requireContext().resources.getString(R.string.fragment_main_game_insurance_title))
            alertDialog.setMessage(requireContext().resources.getString(R.string.fragment_main_game_insurance))
            alertDialog.setPositiveButton(requireContext().resources.getString(R.string.bet_dialog_ok)) { dialog, _ ->
                if (currentUser.wallet!! >= currentUser.bet!![MAIN_HAND_BET]!!) {
                    decreasePlayerInsuranceBetWallet(currentUser.bet!![MAIN_HAND_BET]!!)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_main_game_assured), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
                }
                resetTimerAndRestart()
            }
            alertDialog.setNegativeButton(requireContext().resources.getString(R.string.bet_dialog_cancel)) { dialog, _ ->
                resetTimerAndRestart()
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }

    private fun playerHaveBlackJack() {
        //TODO when current player have blackjack, "21" instead "BJ" is show.
        for (player in playerTab) {
            if (
                player.hand[MAIN_HAND].size == 2 &&
                player.score[MAIN_HAND] == 21 &&
                !player.isPlayerFirstSplit
            ) {
                if (
                    currentUser.isGameHost!! && player.playerNumberType == PlayerNumberType.PLAYER_ONE ||
                    !currentUser.isGameHost!! && player.playerNumberType == PlayerNumberType.PLAYER_TWO
                ) {
                    mBinding.fragmentOnlineGamePlayerScoreTv.text =
                        requireContext().resources.getString(R.string.online_game_fragment_blackjack)
                } else {
                    mBinding.fragmentOnlineGamePlayerTwoMainHandScoreTv.text =
                        requireContext().resources.getString(R.string.online_game_fragment_blackjack)
                }
            }
        }
    }

    private fun playerDrawCard() {
        val playerHand = addCardToPlayerHandList(getCurrentPlayer(), deck, currentUser.splitType!!)
        playerHand.isPlayerDrawAce[getHandType(currentUser.splitType)] = playerDrawAnAceOrNot(deck, playerHand, currentUser.splitType!!)
        playerHand.isPlayerScoreSoft[getHandType(currentUser.splitType)] = modifyPlayerScoreWithAce(playerHand, deck, currentUser.splitType!!)
        if (!playerHand.isPlayerScoreSoft[getHandType(currentUser.splitType)]) {
            setPlayerDrawAceAndScoreSoftToFalse(playerHand)
        }
        deck.index = deck.index!! + 1
        mViewModel.updateOnlineDeckIndex(isCurrentUserOrOpponent().id!!, deck.index!!)
        updateSplitHandRecyclerView(playerHand)
        refreshScoreUI()
        playerBust(playerHand, currentUser)
        resetTimerAndRestart()
    }

    private fun playerBust(playerHand: Player, user: User) {
        if (playerHand.score[getHandType(user.splitType)] > 21) {
            showPlayerBust(user)
            when {
                user.splitType == HandType.MAIN_HAND && playerHand.isPlayerFirstSplit ||
                user.splitType == HandType.FIRST_SPLIT && playerHand.isPlayerSecondSplit
                -> {
                    user.splitType = updateSplitType(user)
                    mViewModel.updateSplitType(user)
                    if (currentUser.playerTurn == deck.playerTurn) {
                        playerDrawCard()
                    }
                }
                else -> {
                    nextPlayer()
                }
            }
        }
    }

    private fun showPlayerBust(user: User) {
        if (user.playerTurn == currentUser.playerTurn) {
            createPlayerOneResultTvTab()[getHandType(user.splitType)].text = requireContext().resources.getString(R.string.online_game_fragment_bust)
        } else {
            createPlayerTwoResultTvTab()[getHandType(user.splitType)].text = requireContext().resources.getString(R.string.online_game_fragment_bust)
        }
    }

    private fun updateSplitType(user: User) = when (user.splitType) {
            HandType.MAIN_HAND -> {
                HandType.FIRST_SPLIT
            }
            else -> {
                HandType.SECOND_SPLIT
            }
        }

    private fun setPlayerDrawAceAndScoreSoftToFalse(playerHand: Player) {
        playerHand.isPlayerDrawAce[getHandType(currentUser.splitType)] = false
        playerHand.isPlayerScoreSoft[getHandType(currentUser.splitType)] = false
    }

    private fun setDealerDrawAceAndScoreSoftToFalse(dealer: Dealer) {
        dealer.isDealerDrawAce = false
        dealer.isDealerScoreSoft = false
    }


    private fun refreshScoreUI() {
        val playerOneScoreTvTab = createPlayerOneScoreTab()
        val playerTwoScoreTvTab = createPlayerTwoScoreTab()
        when (currentUser.isGameHost!!) {
            true -> {
                for (index in 0 until 3) {
                    playerOneScoreTvTab[index].text = "${playerOneHand.score[index]}"
                    playerTwoScoreTvTab[index].text = "${playerTwoHand.score[index]}"
                }
            }
            false -> {
                for (index in 0 until 3) {
                    playerTwoScoreTvTab[index].text = "${playerOneHand.score[index]}"
                    playerOneScoreTvTab[index].text = "${playerTwoHand.score[index]}"
                }
            }
        }
        mBinding.fragmentOnlineGameDealerScoreTv.text = "${dealer.score}"
    }

    private fun opponentDrawCard(playerHand: Player) {
        addCardToPlayerHandList(playerHand, deck, opponent.splitType!!)
        playerHand.isPlayerDrawAce[getHandType(currentUser.splitType)] = playerDrawAnAceOrNot(deck, getCurrentPlayer(), currentUser.splitType!!)
        playerHand.isPlayerScoreSoft[getHandType(currentUser.splitType)] = modifyPlayerScoreWithAce(getCurrentPlayer(), deck, currentUser.splitType!!)
        if (!playerHand.isPlayerScoreSoft[getHandType(currentUser.splitType)]) {
            setPlayerDrawAceAndScoreSoftToFalse(playerHand)
        }
        playerBust(playerHand, opponent)
        opponentSplitHand()
        refreshScoreUI()
        resetTimerAndRestart()
    }

    private fun dealerDrawCard() {
        Log.e(TAG, "DEALER DRAW")
        val dealerHand = addCardToDealerList(dealer, deck).hand
        dealer.isDealerDrawAce = dealerDrawAnAceOrNot(deck, dealer)
        dealer.isDealerScoreSoft = modifyDealerScoreWithAce(dealer, deck)
        if (!dealer.isDealerScoreSoft) {
            setDealerDrawAceAndScoreSoftToFalse(dealer)
        }
        deck.index = deck.index!! + 1
        loadDealerHandIntoRecyclerView(dealerHand)
        refreshScoreUI()
    }

    private fun gameIsOver() {
        currentUser.bet = createBet()
        mViewModel.updateUserForNewGame(currentUser, false)
        mViewModel.updateOnlineDeckPlayerTurn(isCurrentUserOrOpponent().id!!, PlayerNumberType.PLAYER_ONE)
        mBinding.fragmentOnlineGameGameStart.apply {
            visibility = View.VISIBLE
            isEnabled = true
            text = String.format("%s \n %s",requireContext().resources.getString(R.string.fragment_main_game_start_game) ,currentUser.bet!![MAIN_HAND_BET].toString())
            animate().apply {
                duration = 2000
                alphaBy(100f)
            }
        }
        isEndOfGame = true
    }

    private fun configureListeners(currentUserId: String) {
        mBinding.apply {

            fragmentOnlineGameGameStart.setOnClickListener {
                if (currentUser.bet!![MAIN_HAND_BET]!! == 0.0) {
                    showBetDialog()
                    resetPlayersHandsAndScores()
                } else {
                    getOnlineDeck()
                    mViewModel.updateUserIsReady(currentUserId, !currentUser.isUserReady!!)
                    fragmentOnlineGameGameStart.isEnabled = false
                }
            }

            fragmentOnlineGameHitBtn.setOnClickListener {
                playerDrawCard()
                resetTimerAndRestart()
            }

            fragmentOnlineGameStopBtn.setOnClickListener {
                resetTimerAndRestart()
                playerPass()
            }

            fragmentOnlineGameDoubleBtn.setOnClickListener {
                if (currentUser.wallet!! >= currentUser.bet!![MAIN_HAND_BET]!!) {
                    val bet = currentUser.bet!![getPlayerHand(currentUser.splitType!!)]
                    currentUser.bet!![getPlayerHand(currentUser.splitType!!)] = currentUser.bet!![getPlayerHand(currentUser.splitType!!)]!!.plus(currentUser.bet!![getPlayerHand(currentUser.splitType!!)]!!)
                    decreasePlayerBetWallet(bet!!)
                    playerDrawCard()
                    if (getCurrentPlayer().score[getHandTypeForDoubledBox(currentUser.splitType!!)] < 22) {
                        nextPlayer()
                    }
                } else {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
                }
            }

            fragmentOnlineGameSplitBtn.setOnClickListener {
                if (currentUser.wallet!! >= currentUser.bet!![MAIN_HAND_BET]!!) {
                    val currentSplitBet = if (!getCurrentPlayer().isPlayerFirstSplit) FIRST_SPLIT_BET else SECOND_SPLIT_BET
                    currentUser.bet!![currentSplitBet] = currentUser.bet!![MAIN_HAND_BET]!!
                    decreasePlayerBetWallet(currentUser.bet!![currentSplitBet]!!)
                    splitPlayerGame(currentUser.splitType!!, getCurrentPlayer())
                    mViewModel.updateOnlineUserWalletAndIsSplitting(currentUser, true)
                    updateSplitHandRecyclerView(getCurrentPlayer())
                    playerDrawCard()
                    resetTimerAndRestart()
                    if (
                        getCurrentPlayer().hand[0][0].value == 1 &&
                        getCurrentPlayer().hand[1][0].value == 1
                    ) {
                        getCurrentPlayer().isPlayerDrawAce[FIRST_SPLIT] = true
                        getCurrentPlayer().isPlayerScoreSoft[FIRST_SPLIT] = true
                        modifyFirstSplitScore(getCurrentPlayer(), FIRST_SPLIT)
                        nextPlayer()
                        nextPlayer()
                    }
                } else {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.bet_dialog_not_enough_money), Toast.LENGTH_SHORT).show()
                }
            }

            fragmentOnlineGameFab.setOnClickListener {
                showBetDialog()
            }
        }
    }

    private fun playerPass() {
        getCurrentPlayer().isPlayerDrawAce[getHandType(currentUser.splitType)] = false
        nextPlayer()
//        mBinding.fragmentOnlineGameDoubleBtn.isEnabled = false
    }

    private fun decreasePlayerBetWallet(bet: Double) {
        currentUser.wallet = retrieveBetInWallet(currentUser.wallet!!, bet)
        currentUser.bet!![TOTAL_BET] = changeTotalBet(currentUser.bet!![TOTAL_BET]!!, bet)
        mViewModel.updateOnlineUserBetAndWallet(currentUser)
    }

    private fun decreasePlayerInsuranceBetWallet(bet: Double) {
        currentUser.wallet = retrieveInsuranceBetInWallet(currentUser.wallet!!, bet)
        currentUser.bet!![INSURANCE_BET] = retrieveInsuranceBetInWallet(currentUser.bet!![INSURANCE_BET]!!, bet)
        currentUser.bet!![TOTAL_BET] = changeTotalBet(currentUser.bet!![TOTAL_BET]!!, bet / 2)
        mViewModel.updateOnlineUserBetAndWallet(currentUser)
    }

    private fun showBetDialog() {
        val betDialog = OnlineBetDialog()
        val bundle = Bundle()
        bundle.putString(CURRENT_USER_ID, currentUser.id)
        betDialog.arguments = bundle
        betDialog.show(requireActivity().supportFragmentManager, betDialog.tag)
    }

    private fun updateSplitHandRecyclerView(playerHand: Player) {
        if (isCurrentPlayer(playerHand)) {
            loadPlayerOneHandIntoRecyclerView(playerHand.hand[MAIN_HAND])
            loadPlayerOneHandFirstSplitIntoRecyclerView(playerHand.hand[FIRST_SPLIT])
            loadPlayerOneHandSecondSplitIntoRecyclerView(playerHand.hand[SECOND_SPLIT])
        } else {
            loadPlayerTwoHandIntoRecyclerView(playerHand.hand[MAIN_HAND])
            loadPlayerTwoHandFirstSplitIntoRecyclerView(playerHand.hand[FIRST_SPLIT])
            loadPlayerTwoHandSecondSplitIntoRecyclerView(playerHand.hand[SECOND_SPLIT])
        }
        showCurrentPlayerSplitPictures(playerHand)
    }

    private fun opponentSplitHand() {
        val playerHand: Player = getCurrentPlayer()
        System.err.println("opponentSplitHand: playerHand = (${playerHand.hand[0]} ${playerHand.hand[1]} ")
        if (opponent.isSplitting) {
            splitPlayerGame(opponent.splitType!!, playerHand)
            opponent.isSplitting = false
            mViewModel.updateIsSplitting(opponent.id!!, false)
            if (playerHand.hand[FIRST_SPLIT][0].value == 1) {
                modifyFirstSplitScore(playerHand, FIRST_SPLIT)
            }
        }
        showOpponentSplitPictures(playerHand)
        loadPlayerTwoHandIntoRecyclerView(playerHand.hand[MAIN_HAND])
        loadPlayerTwoHandFirstSplitIntoRecyclerView(playerHand.hand[FIRST_SPLIT])
        loadPlayerTwoHandSecondSplitIntoRecyclerView(playerHand.hand[SECOND_SPLIT])
    }

    private fun showCurrentPlayerSplitPictures(playerHand: Player) {
        System.err.println("showCurrentPlayerSplitPictures: player hand size = ${playerHand.hand[MAIN_HAND].size} first:${playerHand.hand[FIRST_SPLIT].size} second:${playerHand.hand[SECOND_SPLIT].size}")
        when {
            playerHand.hand[FIRST_SPLIT].size > 0 && playerHand.hand[SECOND_SPLIT].size == 0 -> {
                mBinding.apply {
                    fragmentOnlineGameUserFirstSplitImg.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerFirstSplitRecyclerView.visibility = View.VISIBLE
                }
            }
            playerHand.hand[SECOND_SPLIT].size > 0 -> {
                System.err.println("current player second split picture visible")
                mBinding.apply {
                    fragmentOnlineGameUserSecondSplitImg.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerSecondSplitRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showOpponentSplitPictures(playerHand: Player) {
        when {
            playerHand.hand[FIRST_SPLIT].size > 0  && playerHand.hand[SECOND_SPLIT].size == 0 -> {
                mBinding.apply {
                    fragmentOnlineGameOpponentFirstSplitImg.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerTwoFirstSplitScoreTv.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerTwoFirstSplitHandRecyclerView.visibility = View.VISIBLE
                }
            }
            playerHand.hand[SECOND_SPLIT].size > 0 -> {
                mBinding.apply {
                    System.err.println("opponent second split picture visible")
                    fragmentOnlineGameOpponentSecondSplitImg.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerTwoSecondSplitScoreTv.visibility = View.VISIBLE
                    fragmentOnlineGamePlayerTwoSecondSplitHandRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun nextPlayer() {
        if (deck.playerTurn == getCurrentPlayer().playerNumberType) {
            when {
                currentUser.splitType == HandType.MAIN_HAND && !getCurrentPlayer().isPlayerFirstSplit && deck.playerTurn == currentUser.playerTurn -> {
                    mViewModel.updateOnlineDeckPlayerTurn(isCurrentUserOrOpponent().id!!, getNextPlayer(getCurrentPlayer().playerNumberType))
                    hidePreviousTimer()
                }
                currentUser.splitType == HandType.MAIN_HAND && getCurrentPlayer().isPlayerFirstSplit && deck.playerTurn == currentUser.playerTurn -> {
                    currentUser.splitType = updateSplitType(currentUser)
                    mViewModel.updateSplitType(currentUser)
                    playerDrawCard()
                    animation = animateImageForPlayer(mBinding.fragmentOnlineGameUserFirstSplitImg)
                }
                currentUser.splitType == HandType.FIRST_SPLIT && !getCurrentPlayer().isPlayerSecondSplit && deck.playerTurn == currentUser.playerTurn -> {
                    mViewModel.updateOnlineDeckPlayerTurn(isCurrentUserOrOpponent().id!!, getNextPlayer(getCurrentPlayer().playerNumberType))
                    hidePreviousTimer()
                }
                currentUser.splitType == HandType.FIRST_SPLIT && getCurrentPlayer().isPlayerSecondSplit && deck.playerTurn == currentUser.playerTurn -> {
                    currentUser.splitType = updateSplitType(currentUser)
                    mViewModel.updateSplitType(currentUser)
                    playerDrawCard()
                    animation = animateImageForPlayer(mBinding.fragmentOnlineGameUserSecondSplitImg)
                }
                currentUser.splitType == HandType.SECOND_SPLIT && deck.playerTurn == currentUser.playerTurn -> {
                    mViewModel.updateOnlineDeckPlayerTurn(isCurrentUserOrOpponent().id!!, getNextPlayer(getCurrentPlayer().playerNumberType))
                    hidePreviousTimer()
                }
            }
        }
        stopAndResetTimer()
        Log.e(TAG, "nextPlayer time is reset: ${time}sec")
    }

    private fun resetPlayersHandsAndScores() {
        val playerOneScoreTab = createPlayerOneScoreTab()
        val playerTwoScoreTab = createPlayerTwoScoreTab()
        val playerOneResultTab = createPlayerOneResultTvTab()
        val playerTwoResultTab = createPlayerTwoResultTvTab()
        for (index in 0 until 3) {
            playerOneHand.hand[index].clear()
            playerTwoHand.hand[index].clear()
            playerOneHand.score[index] = 0
            playerTwoHand.score[index] = 0
            playerOneScoreTab[index].visibility = View.GONE
            playerTwoScoreTab[index].visibility = View.GONE
            playerOneResultTab[index].text = ""
            playerTwoResultTab[index].text = ""
            playerOneHand.isPlayerDrawAce[index] = false
            playerOneHand.isPlayerScoreSoft[index] = false
            playerTwoHand.isPlayerDrawAce[index] = false
            playerTwoHand.isPlayerScoreSoft[index] = false
        }
        mBinding.apply {
            fragmentOnlineGameDealerResultTv.text = ""
            fragmentOnlineGameDealerScoreTv.visibility = View.GONE
            fragmentOnlineGameUserImg.visibility = View.GONE
            fragmentOnlineGameUserFirstSplitImg.visibility = View.GONE
            fragmentOnlineGameUserSecondSplitImg.visibility = View.GONE
            fragmentOnlineGameOpponentImg.visibility = View.GONE
            fragmentOnlineGameOpponentFirstSplitImg.visibility = View.GONE
            fragmentOnlineGameOpponentSecondSplitImg.visibility = View.GONE
        }
        dealer.score = 0
        dealer.hand.clear()
        dealer.isDealerDrawAce = false
        dealer.isDealerScoreSoft = false
        playerOneHand.isPlayerFirstSplit = false
        playerOneHand.isPlayerSecondSplit = false
        playerTwoHand.isPlayerFirstSplit = false
        playerTwoHand.isPlayerSecondSplit = false
        currentUser.isSplitting = false
        opponent.isSplitting = false
        resetRecyclerViews()

        mViewModel.resetCurrentUserAndHandType(currentUser, HandType.MAIN_HAND, false)
        currentUser.splitType = HandType.MAIN_HAND
        opponent.splitType = HandType.MAIN_HAND
        refreshScoreUI()
    }

    private fun resetRecyclerViews() {
        loadPlayerOneHandIntoRecyclerView(getCurrentPlayer().hand[MAIN_HAND])
        loadPlayerOneHandFirstSplitIntoRecyclerView(getCurrentPlayer().hand[FIRST_SPLIT])
        loadPlayerOneHandSecondSplitIntoRecyclerView(getCurrentPlayer().hand[SECOND_SPLIT])

        loadPlayerTwoHandIntoRecyclerView(getCurrentOpponent().hand[MAIN_HAND])
        loadPlayerTwoHandFirstSplitIntoRecyclerView(getCurrentOpponent().hand[FIRST_SPLIT])
        loadPlayerTwoHandSecondSplitIntoRecyclerView(getCurrentOpponent().hand[SECOND_SPLIT])

        loadDealerHandIntoRecyclerView(dealer.hand)
    }

    private fun isCurrentUserOrOpponent() = if (currentUser.isGameHost!!) currentUser else opponent

    private fun prepareDeck(currentUserOnline: User) {
        if (currentUserOnline.isGameHost!! && deck.deckList == null || deck.index!! > 260 && isEndOfGame && currentUser.isGameHost!!) {
            deck.deckList = ArrayList()
            deck.deckList!!.addAll(createDeck().deckList)
            shuffleDeck(deck.deckList!!)
            mViewModel.createOnlineDeck(
                currentUserOnline.id!!,
                OnlineDeck(
                    deck.deckList!!,
                    5,
                    PlayerNumberType.PLAYER_ONE
                )
            )
            Log.e(TAG, "prepareDeck: Deck created")
        }

        if (!currentUserOnline.isGameHost!! && deck.deckList == null && opponent.onlineStatus != OnlineStatusType.OFFLINE) {
            getOnlineDeck()
        }
    }

    private fun saveGameStats() {
        mViewModel.updateNumberOfGamePlayed(currentUser)
    }

    private fun enableHitAndStopButtons() {
        if (currentUser.playerTurn == deck.playerTurn && dealer.score < 12 && currentUser.isUserReady!! && opponent.isUserReady!! && time >= 1.0) {
            mBinding.apply {
                fragmentOnlineGameHitBtn.isEnabled = true
                fragmentOnlineGameStopBtn.isEnabled = true
            }
        } else {
            mBinding.apply {
                fragmentOnlineGameHitBtn.isEnabled = false
                fragmentOnlineGameStopBtn.isEnabled = false
                if (time > 2.0 && !isCurrentUserQuit) {
                    stopAndResetTimer()
                }
            }
        }
    }

    private fun enableSplitButton() {
        val currentPlayer = getCurrentPlayer()
        mBinding.fragmentOnlineGameSplitBtn.isEnabled = currentPlayer.hand[getHandType(currentUser.splitType)].size == 2 &&
                currentPlayer.hand[getHandType(currentUser.splitType)][0].value == currentPlayer.hand[getHandType(currentUser.splitType)][1].value &&
                !currentPlayer.isPlayerSecondSplit &&
                currentUser.playerTurn  == deck.playerTurn &&
                dealer.score < 12 &&
                time >= 1.0
    }

    private fun readyPictureCurrentUserVisibility(user: User) {
        mBinding.apply {
            if (user.isUserReady!! && isEndOfGame) {
                fragmentOnlineGameCurrentUserReadyImg.visibility = View.VISIBLE
//                animateImage(fragmentOnlineGameCurrentUserReadyImg).start()
            } else if (!user.isUserReady!! && !isEndOfGame) {
                fragmentOnlineGameCurrentUserReadyImg.visibility = View.GONE
            } else {
                fragmentOnlineGameCurrentUserReadyImg.visibility = View.GONE
            }
        }
    }

    private fun readyPictureOpponentVisibility(opponent: User) {
        mBinding.apply {
            if (opponent.isUserReady!! && isEndOfGame) {
                fragmentOnlineGameOpponentReadyImg.visibility = View.VISIBLE
//                animateImage(fragmentOnlineGameOpponentReadyImg).start()
            } else if (!opponent.isUserReady!! && !isEndOfGame) {
                fragmentOnlineGameOpponentReadyImg.visibility = View.GONE
            } else {
                fragmentOnlineGameOpponentReadyImg.visibility = View.GONE
            }
        }
    }

//    private fun animateImage(imageToAnimate: AppCompatImageView) = imageToAnimate.animate().apply {
//            duration = 1000
//            rotationXBy(180f)
//        }

    private fun animateImageForPlayer(imageToAnimate: AppCompatImageView) = imageToAnimate.animate().apply {
        if (currentUser.playerTurn == deck.playerTurn) {
            duration = 1000
            rotationYBy(360f)
        }
    }

    private fun hidePictures() {
        mBinding.apply {
            fragmentOnlineGameCurrentUserReadyImg.visibility = View.GONE
            fragmentOnlineGameOpponentReadyImg.visibility = View.GONE

            fragmentOnlineGameUserFirstSplitImg.visibility = View.GONE
            fragmentOnlineGameUserSecondSplitImg.visibility = View.GONE
            fragmentOnlineGameOpponentFirstSplitImg.visibility = View.GONE
            fragmentOnlineGameOpponentSecondSplitImg.visibility = View.GONE

            fragmentOnlineGamePlayerFirstSplitScoreTv.visibility = View.GONE
            fragmentOnlineGamePlayerSecondSplitScoreTv.visibility = View.GONE
            fragmentOnlineGamePlayerTwoFirstSplitScoreTv.visibility = View.GONE
            fragmentOnlineGamePlayerTwoSecondSplitScoreTv.visibility = View.GONE

            fragmentOnlineGamePlayerFirstSplitRecyclerView.visibility = View.GONE
            fragmentOnlineGamePlayerSecondSplitRecyclerView.visibility = View.GONE
            fragmentOnlineGamePlayerTwoFirstSplitHandRecyclerView.visibility = View.GONE
            fragmentOnlineGamePlayerTwoSecondSplitHandRecyclerView.visibility = View.GONE
        }
    }

    private fun useGlide(with: View, load: Any, into: AppCompatImageView, pictureRotation: Float) {
        Glide.with(with)
            .load(load)
            .circleCrop()
            .into(into)

        into.rotation = pictureRotation
    }

    private fun getCurrentPlayer(): Player {
        return if (deck.playerTurn == playerOneHand.playerNumberType) playerOneHand else playerTwoHand
    }

    private fun getCurrentOpponent(): Player {
        return if (deck.playerTurn == playerOneHand.playerNumberType) playerTwoHand else playerOneHand
    }

    private fun isCurrentPlayer(playerHand: Player) = currentUser.isGameHost!! && playerHand.playerNumberType == PlayerNumberType.PLAYER_ONE ||
            !currentUser.isGameHost!! && playerHand.playerNumberType == PlayerNumberType.PLAYER_TWO

    private fun setupPlayerOneHandRecyclerView() = mBinding.fragmentOnlineGamePlayerRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerOneHandFirstSplitRecyclerView() = mBinding.fragmentOnlineGamePlayerFirstSplitRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerOneHandSecondSplitRecyclerView() = mBinding.fragmentOnlineGamePlayerSecondSplitRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerTwoHandRecyclerView() = mBinding.fragmentOnlineGamePlayerTwoMainHandRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerTwoFirstSplitHandRecyclerView() = mBinding.fragmentOnlineGamePlayerTwoFirstSplitHandRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerTwoSecondSplitHandRecyclerView() = mBinding.fragmentOnlineGamePlayerTwoSecondSplitHandRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupPlayerDealerHandRecyclerView() = mBinding.fragmentOnlineGameDealerRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadPlayerOneHandIntoRecyclerView(hand: ArrayList<Card>) {
        playerOneHandAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerRecyclerView.adapter = playerOneHandAdapter
    }

    private fun loadPlayerOneHandFirstSplitIntoRecyclerView(hand: ArrayList<Card>) {
        playerOneHandFirstSplitAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerFirstSplitRecyclerView.adapter = playerOneHandFirstSplitAdapter
    }

    private fun loadPlayerOneHandSecondSplitIntoRecyclerView(hand: ArrayList<Card>) {
        playerOneHandSecondSplitAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerSecondSplitRecyclerView.adapter = playerOneHandSecondSplitAdapter
    }

    private fun loadPlayerTwoHandIntoRecyclerView(hand: ArrayList<Card>) {
        playerTwoHandAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerTwoMainHandRecyclerView.adapter = playerTwoHandAdapter
    }

    private fun loadPlayerTwoHandFirstSplitIntoRecyclerView(hand: ArrayList<Card>) {
        playerTwoHandFirstSplitAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerTwoFirstSplitHandRecyclerView.adapter = playerTwoHandFirstSplitAdapter
    }

    private fun loadPlayerTwoHandSecondSplitIntoRecyclerView(hand: ArrayList<Card>) {
        playerTwoHandSecondSplitAdapter.submitList(hand)
        mBinding.fragmentOnlineGamePlayerTwoSecondSplitHandRecyclerView.adapter = playerTwoHandSecondSplitAdapter
    }

    private fun loadDealerHandIntoRecyclerView(hand: ArrayList<Card>) {
        dealerHandAdapter.submitList(hand)
        mBinding.fragmentOnlineGameDealerRecyclerView.adapter = dealerHandAdapter
    }

    private fun loadPlayersHandsIntoRecyclerViews(
        playerOneHand: ArrayList<Card>,
        playerTwoHand: ArrayList<Card>,
    ) {
        if (currentUser.isGameHost!!) {
            loadPlayerOneHandIntoRecyclerView(playerOneHand)
            loadPlayerTwoHandIntoRecyclerView(playerTwoHand)
        } else {
            loadPlayerOneHandIntoRecyclerView(playerTwoHand)
            loadPlayerTwoHandIntoRecyclerView(playerOneHand)
        }

        loadDealerHandIntoRecyclerView(dealer.hand)
    }

    private fun opponentLeaveGame(opponent: User) {
        val intent = Intent(requireActivity(), OnlineMainScreenActivity::class.java)
        intent.putExtra(CURRENT_USER_ID, currentUser.id)
        if (opponent.onlineStatus == OnlineStatusType.OFFLINE && !isCurrentUserQuit) {
            val opponentQuitDialog = OpponentQuitGameDialog()
            opponentQuitDialog.show(requireActivity().supportFragmentManager, opponentQuitDialog.tag)
            isCurrentUserQuit = true
        }
    }

    override fun onResume() {
        super.onResume()
        isCurrentUserQuit = false
    }

    override fun onPause() {
        super.onPause()
        Log.e(javaClass.simpleName, "onPause: paused")
        disconnectCurrentUser()
    }

    override fun onStop() {
        super.onStop()
        isCurrentUserQuit = true
        resetTimerAndRestart()
        Log.e(javaClass.simpleName, "onStop: app stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(javaClass.simpleName, "onDestroy: destroyed")
        deleteOnlineDeckAndMessage()
        stopTimer()
        stopServiceAndUnregisterReceiver()
    }

    private fun deleteOnlineDeckAndMessage() {
        if (currentUser.isGameHost!!) {
            mViewModel.deleteOnlineDeck(currentUser.id!!)
            mViewModel.deleteAllMessage(currentUser.id!!)
            Log.e(javaClass.simpleName, "onDestroyed: chat deleted for userId:${currentUser.id}")
        } else {
            mViewModel.deleteOnlineDeck(opponent.id!!)
        }
    }

    private fun disconnectCurrentUser() {
        if (isCurrentUserQuit) {
            Log.e(javaClass.simpleName, "disconnectCurrentUser: user disconnected")
            mViewModel.updateOnlineStatus(currentUser.id.toString(), OnlineStatusType.OFFLINE)
        }
    }
}