package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.firebase.firestore.DocumentSnapshot
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_offline_game.presentation.offline_game.OfflineGameFragment
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.*
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class Utils {

    companion object {

        private fun getColorType(value: Int) = ColorType.values().first { it.value == value }
        private fun getNumberType(value: Int) = NumberType.values().first { it.index == value }
        const val CURRENT_USER_ID = "user_id"
        const val SEARCHED_USER_ID = "searched_user_id"
        const val MAIN_HAND = "main_hand"
        const val FIRST_SPLIT = "first_split"
        const val SECOND_SPLIT = "second_split"
        const val INSURANCE = "insurance_bet"
        const val TOTAL = "total_bet"
        const val DEFAULT_BET = "default_bet"
        const val HAND = "hand"
        const val FIRST_SPLIT_HAND = "first_split_hand"
        const val SECOND_SPLIT_HAND = "second_split_hand"
        const val REQUEST_CODE_SIGN_IN = 2
        const val USER_ID = "id"
        const val NUMBER_OF_LOAN = "numberOfLoan"
        const val WALLET = "wallet"
        const val BET = "bet"
        const val PSEUDO = "pseudo"
        const val USER_PICTURE = "profilePicture"
        const val PICTURE_ROTATION = "pictureRotation"
        const val ONLINE_STATUS = "onlineStatus"
        const val OPPONENT = "opponent"
        const val PLAYER_TURN = "playerTurn"
        const val SPLIT_TYPE = "splitType"
        const val NUMBER_OF_GAME_PLAYED = "numberOfGamePlayed"
        const val IS_DEFAULT_IMAGE_PROFILE = "isDefaultImageProfile"
        const val IS_GAME_HOST = "isGameHost"
        const val IS_USER_READY = "isUserReady"
        const val IS_SPLITTING = "isSplitting"
        const val DATE = "date"
        const val MESSAGE = "message"
        const val CUSTOM_USER_PICTURE = "custom_user_picture"
        const val WALLET_STATE = "walletStateWhenGameEnding"

        fun createDeck(): Deck {
            var color: ColorType
            var number: NumberType
            val deck = Deck()

//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.THREE,ColorType.SPADE,3))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.FIVE,ColorType.HEART,5))
//            deck.deckList.add(Card(NumberType.SIX,ColorType.HEART,6))
//            deck.deckList.add(Card(NumberType.HEIGHT,ColorType.DIAMOND,8))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.DIAMOND,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.CLUB,10))

            for (k in 1..6) {
                for (i in 1..4) {
                    color = getColorType(i)
                    for (j in 1..13) {
                        number = getNumberType(j)
                        val card = Card(number, color, number.value)
                        deck.deckList.add(card)
                    }
                }
            }
            return deck
        }

        fun prepareDeckForBaseRule(): Deck {
            val color: ColorType
            val number: NumberType
            val deck = Deck()

            deck.deckList.add(Card(NumberType.THREE,ColorType.CLUB,3))
            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
            deck.deckList.add(Card(NumberType.SIX,ColorType.HEART,6))
            deck.deckList.add(Card(NumberType.TWO,ColorType.HEART,2))
            deck.deckList.add(Card(NumberType.FOUR,ColorType.CLUB,4))
            deck.deckList.add(Card(NumberType.FIVE,ColorType.DIAMOND,5))
            deck.deckList.add(Card(NumberType.HEIGHT,ColorType.DIAMOND,8))
            deck.deckList.add(Card(NumberType.TEN,ColorType.SPADE,10))
            deck.deckList.add(Card(NumberType.KING,ColorType.CLUB,10))
            deck.deckList.add(Card(NumberType.TWO,ColorType.HEART,2))
            deck.deckList.add(Card(NumberType.SEVEN,ColorType.DIAMOND,7))
            deck.deckList.add(Card(NumberType.THREE,ColorType.SPADE,3))
            deck.deckList.add(Card(NumberType.SIX,ColorType.CLUB,6))
            deck.deckList.add(Card(NumberType.SIX,ColorType.CLUB,6))
            deck.deckList.add(Card(NumberType.QUEEN,ColorType.HEART,10))
            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))

            return deck
        }

        fun shuffleDeck(deck: ArrayList<Card>): ArrayList<Card> {
            deck.shuffle()
            return deck
        }

        fun createBet(defaultBetValue: Double): HashMap<String, Double> {
            val bet = HashMap<String, Double>()
            bet[MAIN_HAND] = 0.0
            bet[FIRST_SPLIT_HAND] = 0.0
            bet[SECOND_SPLIT_HAND] = 0.0
            bet[INSURANCE] = 0.0
            bet[DEFAULT_BET] = defaultBetValue
            bet[TOTAL] = 0.0
            return bet
        }

        fun getCardText(card: Card): String {
            val cardText = when (card.number) {
                NumberType.ACE -> "A"
                NumberType.JACK -> "J"
                NumberType.QUEEN -> "Q"
                NumberType.KING -> "K"
                else -> card.value.toString()
            }
            return cardText
        }

        fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
            observe(lifecycleOwner, object : Observer<T> {
                override fun onChanged(t: T) {
                    observer.onChanged(t)
                    removeObserver(this)
                }
            })
        }

        fun getPlayerHand(handType: HandType): String = when(handType) {
                HandType.MainHand -> MAIN_HAND
                HandType.FirstSplit -> FIRST_SPLIT
                else -> SECOND_SPLIT
            }

        fun getHandType(handType: HandType?) = when(handType) {
            HandType.MainHand -> 0
            HandType.FirstSplit -> 1
            else -> 2
        }

        fun getBetTypeByInt(index: Int) = when(index) {
            0 -> MAIN_HAND
            1 -> FIRST_SPLIT
            else -> SECOND_SPLIT
        }

        fun getNextPlayer(player: PlayerNumberType): PlayerNumberType = when(player) {
            PlayerNumberType.PLAYER_ONE -> PlayerNumberType.PLAYER_TWO
            PlayerNumberType.PLAYER_TWO -> PlayerNumberType.DEALER
            else -> PlayerNumberType.PLAYER_ONE
        }

        fun retrieveBetInWallet(wallet: Double, bet: Double): Double =
            wallet - bet

        fun changeTotalBet(totalBet: Double, bet: Double) =
            totalBet + bet

        fun paymentForPlayer(resultScore: Int, bet: Double): Double {
            return when(resultScore) {
                R.string.fragment_main_game_you_win -> bet
                R.string.fragment_main_game_you_lose -> -bet
                R.string.online_game_fragment_blackjack -> bet + bet * 0.5
                else -> 0.0
            }
        }

        fun updateOnlineWallet(user: User): User {
            for (userBet in user.bet!!.entries) {
                user.wallet = user.wallet?.plus(userBet.value)
            }
            user.wallet = user.wallet?.minus(user.bet!![TOTAL]!!)
            return user
        }

        fun makeBet(
            unity: Int,
            dozens: Int,
            hundred: Int,
            thousand: Int,
            tenOfThousand: Int
        ) = tenOfThousand * 10000.0 + thousand * 1000.0 + hundred * 100.0 + dozens * 10.0 + unity

        fun makeTotalBet(
            unity: Int,
            dozens: Int,
            hundred: Int,
            thousand: Int,
            tenOfThousand: Int,
            numberOfBox: Int
        ) = (tenOfThousand * 10000.0 + thousand * 1000.0 + hundred * 100.0 + dozens * 10.0 + unity) * numberOfBox

        fun getArrayOfBetString(bet: String): CharArray {
            val string = bet.replaceAfter(".", "").replace(".","")
            return string.toCharArray()
        }

        fun getIndex(betArraySize: Int, betTabTvSize: Int, index: Int) = (betTabTvSize - betArraySize) + index



        fun convertTimeInPercent(seconds: Double): Int {
            return (seconds / 20.0 * 100.0).toInt()
        }

        fun getTimeCount(seconds: Double): Int = abs(20 - seconds.toInt())

        fun getInvertedTabIndex(tabSize: Int, index: Int) = abs((tabSize - 1) - index)

        private fun createCustomUser(user: User, listOfImage: HashMap<String, ByteArray>?) = CustomUser(
            user.id.toString(),
            user.wallet.toString().toDouble(),
            user.pseudo.toString(),
            if (user.isDefaultProfileImage == true) user.userPicture else listOfImage?.get(user.id.toString()),
            user.onlineStatus!!,
            user.pictureRotation!!
        )

        fun createListOfCustomUser(listOfOnlineUser: ArrayList<User>?, allImage: HashMap<String, ByteArray>?, userId: String): ArrayList<CustomUser> {
            val listOFCustomUser = ArrayList<CustomUser>()
            if (listOfOnlineUser != null) {
                for (user in listOfOnlineUser) {
                    if (user.id.toString() != userId) {
                        Log.e("OnlineMainScreen", "updateUserList: username: ${user.pseudo}")
                        listOFCustomUser.add(createCustomUser(user, allImage))
                    }
                }
            }
            Log.e("OnlineMainScreen", "updateUserList: listOfCustomUser size: ${listOFCustomUser.size}")
            return listOFCustomUser
        }

        fun makeStringFromUri(uri: Uri): String {
            return uri.lastPathSegment.toString().replaceBefore("/", "").replace("/","")
        }

        fun formatDate(date: String): String {
            return date
                .replaceAfter(":","")
                .replaceAfterLast(" ", "")
                .replaceAfterLast(" ","")
                .replaceBefore(" ","")
        }

        fun userStatsComparator(userStatsList: ArrayList<UserStats>) {
            Collections.sort(userStatsList, UserStatsComparator())
        }

        fun loadCustomPhotoInChat(listOfChat: ArrayList<Message>, listOfImage: HashMap<String, ByteArray>): ArrayList<Message> {
            listOfChat.forEach { chat ->
                listOfImage.forEach { image ->
                    if (chat.userPicture?.contains("http") == false &&
                                chat.id == image.key
                            ) {
                        chat.customUserPicture = image.value
                    }
                }
            }
            return listOfChat
        }

        fun convertDocumentToUser(documentSnapshot: DocumentSnapshot?): User? {
            return if(documentSnapshot?.data?.isNotEmpty() == true) User(
                documentSnapshot.data?.get(USER_ID).toString(),
                documentSnapshot.data?.get(NUMBER_OF_LOAN).toString().toInt(),
                documentSnapshot.data?.get(WALLET).toString().toDouble(),
                documentSnapshot.data?.get(BET) as HashMap<String, Double>,
                documentSnapshot.data?.get(PSEUDO).toString(),
                documentSnapshot.data?.get(USER_PICTURE).toString(),
                documentSnapshot.data?.get(PICTURE_ROTATION).toString().toFloat(),
                documentSnapshot.data?.get(ONLINE_STATUS).toString().toOnlineStatusType(),
                documentSnapshot.data?.get(OPPONENT).toString(),
                if (documentSnapshot.data?.get(PLAYER_TURN) != null) documentSnapshot.data?.get(
                    PLAYER_TURN
                ).toString().toPlayerNumberType() else null,
                if (documentSnapshot.data?.get(SPLIT_TYPE) != null) documentSnapshot.data?.get(
                    SPLIT_TYPE
                ).toString().toSplitType() else null,
                documentSnapshot.data?.get(NUMBER_OF_GAME_PLAYED).toString().toInt(),
                documentSnapshot.data?.get(IS_DEFAULT_IMAGE_PROFILE).toString().toBoolean(),
                documentSnapshot.data?.get(IS_GAME_HOST).toString().toBoolean(),
                documentSnapshot.data?.get(IS_USER_READY).toString().toBoolean(),
                documentSnapshot.data?.get(IS_SPLITTING).toString().toBoolean(),
            ) else null
        }

        fun convertDocumentToMessage(documentSnapshot: DocumentSnapshot) =
                Message(
                    documentSnapshot.data?.get(USER_ID).toString(),
                    documentSnapshot.data?.get(PSEUDO).toString(),
                    documentSnapshot.data?.get(DATE).toString(),
                    documentSnapshot.data?.get(MESSAGE).toString(),
                    documentSnapshot.data?.get(USER_PICTURE).toString(),
                    if (documentSnapshot.data?.get(CUSTOM_USER_PICTURE) != null) documentSnapshot.data?.get(
                        CUSTOM_USER_PICTURE
                    ).toString().toByteArray() else null,
                    if (documentSnapshot.data?.get(PICTURE_ROTATION) != null) documentSnapshot.data?.get(
                        PICTURE_ROTATION
                    ).toString().toFloat() else 0f
                )

        fun createPlayerHand(playerNumber: Int, defaultBetValue: Double): Player = Player(
            createScoreMap(),
            createHandMap(),
            createBet(defaultBetValue),
            PlayerNumberType.values()[playerNumber],
            createResultScoreMap(),
            isPlayerDrawAce = createBooleanMap(),
            isPlayerScoreSoft = createBooleanMap(),
            isPlayerFirstSplit = false,
            isPlayerSecondSplit = false
        )

        private fun createResultScoreMap(): java.util.HashMap<String, Int> {
            val map = HashMap<String, Int>()
            map[MAIN_HAND] = R.string.fragment_main_game_none
            map[FIRST_SPLIT_HAND] = R.string.fragment_main_game_none
            map[SECOND_SPLIT_HAND] = R.string.fragment_main_game_none
            return map
        }

        private fun createBooleanMap(): HashMap<String, Boolean> {
            val map = HashMap<String, Boolean>()
            map[MAIN_HAND] = false
            map[FIRST_SPLIT_HAND] = false
            map[SECOND_SPLIT_HAND] = false
            return map
        }

        private fun createHandMap(): HashMap<String, ArrayList<Card>> {
            val map = HashMap<String, ArrayList<Card>>()
            map[MAIN_HAND] = ArrayList()
            map[FIRST_SPLIT_HAND] = ArrayList()
            map[SECOND_SPLIT_HAND] = ArrayList()
            return map
        }

        private fun createScoreMap(): HashMap<String, Int> {
            val map = HashMap<String, Int>()
            map[MAIN_HAND] = 0
            map[FIRST_SPLIT_HAND] = 0
            map[SECOND_SPLIT_HAND] = 0
            return map
        }

        fun createDealer() = Dealer(
            0,
            ArrayList(),
            isDealerDrawAce = false,
            isDealerScoreSoft = false
        )

        fun getHandTab(index: Int) =
            when(index) {
                0 -> MAIN_HAND
                1 -> FIRST_SPLIT_HAND
                else -> SECOND_SPLIT_HAND
            }

        fun createCustomPlayerList(listSize: Int, defaultBet: Double): ArrayList<CustomPlayer> {
            val arrayOfCustomPlayer = ArrayList<CustomPlayer>()
            for (playerIndex in 0 until listSize) {
                arrayOfCustomPlayer.add(
                    CustomPlayer(
                        bet = defaultBet,
                        score = 0,
                        insuranceBet = 0.0,
                        isDouble = false,
                        hand = ArrayList(),
                        playerNumber = getPlayerNumberType(playerIndex),
                        playerHandType = HandType.MainHand,
                        isCurrentPlayer = getPlayerNumberType(playerIndex) == PlayerNumberType.PLAYER_ONE,
                        resultScore = R.string.fragment_main_game_none,
                        isPlayerDrawAce = false,
                        isPlayerScoreSoft = false,
                        isPlayerFirstSplit = false,
                        isPlayerSecondSplit = false,
                        isInsuranceOpen = false
                    )
                )
            }
            return arrayOfCustomPlayer
        }

        fun updateCurrentPlayerInArrayListOfCustomPlayer(arrayOfCustomPlayer: ArrayList<CustomPlayer>, currentPlayerNumber: PlayerNumberType, currentHandType: HandType) {
            for (customPlayerIndex in 0 until arrayOfCustomPlayer.size) {
                arrayOfCustomPlayer[customPlayerIndex].isCurrentPlayer =
                    arrayOfCustomPlayer[customPlayerIndex].playerNumber == currentPlayerNumber &&
                            arrayOfCustomPlayer[customPlayerIndex].playerHandType == currentHandType
            }
        }

        fun createCustomPlayer(
            arrayOfCustomPlayer: ArrayList<CustomPlayer>,
            playerIndex: Int,
            hand: ArrayList<Card>
            ): CustomPlayer {
            return CustomPlayer(
                bet = arrayOfCustomPlayer[playerIndex].bet,
                score = ScoreUtils.getHandScore(hand),
                insuranceBet = 0.0,
                isDouble = false,
                hand = hand,
                playerNumber = arrayOfCustomPlayer[playerIndex].playerNumber,
                playerHandType = getHandTypeFromInt(if (arrayOfCustomPlayer[playerIndex].isPlayerSecondSplit) 2 else 1),
                isCurrentPlayer = false,
                resultScore = arrayOfCustomPlayer[playerIndex].resultScore,
                isPlayerDrawAce = false,
                isPlayerScoreSoft = false,
                isPlayerFirstSplit = false,
                isPlayerSecondSplit = false,
                isInsuranceOpen = false
            )
        }

        fun createCustomPlayer(): CustomPlayer {
            return CustomPlayer(
                bet = 0.0,
                score = 0,
                insuranceBet = 0.0,
                isDouble = false,
                hand = ArrayList(),
                playerNumber = PlayerNumberType.PLAYER_ONE,
                playerHandType = HandType.MainHand,
                isCurrentPlayer = false,
                resultScore = R.string.fragment_main_game_none,
                isPlayerDrawAce = false,
                isPlayerScoreSoft = false,
                isPlayerFirstSplit = false,
                isPlayerSecondSplit = false,
                isInsuranceOpen = false
            )
        }

        fun nextPlayerTurn(offlineUser: OfflineUser): PlayerNumberType {
           return when (offlineUser.currentPlayerNumber) {
                PlayerNumberType.PLAYER_ONE -> PlayerNumberType.PLAYER_TWO
                PlayerNumberType.PLAYER_TWO -> PlayerNumberType.PLAYER_THREE
                PlayerNumberType.PLAYER_THREE -> PlayerNumberType.PLAYER_FOUR
                PlayerNumberType.PLAYER_FOUR -> PlayerNumberType.PLAYER_FIVE
                PlayerNumberType.PLAYER_FIVE -> PlayerNumberType.PLAYER_SIX
                else -> PlayerNumberType.PLAYER_SEVEN
            }
        }

        fun getPlayerIndex(arrayOfCustomPlayer: ArrayList<CustomPlayer>, currentPlayer: CustomPlayer): Int {
            var indexToReturn = 0
            for (index in 0 until arrayOfCustomPlayer.size) {
                if (currentPlayer == arrayOfCustomPlayer[index]) {
                    indexToReturn = index
                }
            }
            return indexToReturn
        }

        fun getCurrentPlayer(offlineUser: OfflineUser): CustomPlayer {
            var player = createCustomPlayer()
            for (index in 0 until offlineUser.player.size) {
                if (
                    offlineUser.currentPlayerNumber == offlineUser.player[index].playerNumber &&
                    offlineUser.currentHandType == offlineUser.player[index].playerHandType
                ) {
                    player = offlineUser.player[index]
                }
            }
            return player
        }

        fun createCustomPlayerListForHelp(): ArrayList<CustomPlayer> {
            val listOfCustomPlayer = createCustomPlayerList(1, 10.0)
            listOfCustomPlayer[0].hand.add(Card(NumberType.HEIGHT, ColorType.HEART, 8, true))
            listOfCustomPlayer[0].hand.add(Card(NumberType.HEIGHT, ColorType.DIAMOND, 8, true))
            listOfCustomPlayer[0].score = 16
            listOfCustomPlayer[0].isInsuranceOpen = true
            listOfCustomPlayer[0].isDouble = true
            listOfCustomPlayer[0].insuranceBet = -1.0
            listOfCustomPlayer[0].isPlayerFirstSplit = true
            listOfCustomPlayer[0].isHelpMode = true
            return listOfCustomPlayer
        }

        fun createDealerHandForHelp(): ArrayList<Card> {
            val dealerHand = ArrayList<Card>()
            dealerHand.add(Card(NumberType.ACE, ColorType.SPADE, 11, true))
            return dealerHand
        }

        fun getColorByPlayerNumber(playerNumberType: PlayerNumberType): Int {
            return when(playerNumberType) {
                PlayerNumberType.PLAYER_ONE -> R.drawable.rounded_border_fb_color
                PlayerNumberType.PLAYER_TWO -> R.drawable.rounded_border_google_color
                PlayerNumberType.PLAYER_THREE -> R.drawable.rounded_border_purple_200_color
                PlayerNumberType.PLAYER_FOUR -> R.drawable.rounded_border_teal_color
                PlayerNumberType.PLAYER_FIVE -> R.drawable.rounded_border_light_green_color
                PlayerNumberType.PLAYER_SIX -> R.drawable.rounded_border_yellow_color
                else -> R.drawable.rounded_border_green_color
            }
        }

        private fun getHandTypeFromInt(index: Int): HandType {
            return when(index) {
                0 -> HandType.MainHand
                1 -> HandType.FirstSplit
                else -> HandType.SecondSplit
            }
        }

        private fun getPlayerNumberType(playerIndex: Int): PlayerNumberType {
            return when (playerIndex) {
                0 -> PlayerNumberType.PLAYER_ONE
                1 -> PlayerNumberType.PLAYER_TWO
                2 -> PlayerNumberType.PLAYER_THREE
                3 -> PlayerNumberType.PLAYER_FOUR
                4 -> PlayerNumberType.PLAYER_FIVE
                5 -> PlayerNumberType.PLAYER_SIX
                else -> PlayerNumberType.PLAYER_SEVEN
            }
        }

        fun getIndexOfPlayerNumberType(playerNumberType: PlayerNumberType): Int {
            return when(playerNumberType) {
                PlayerNumberType.PLAYER_ONE -> 1
                PlayerNumberType.PLAYER_TWO -> 2
                PlayerNumberType.PLAYER_THREE -> 3
                PlayerNumberType.PLAYER_FOUR -> 4
                PlayerNumberType.PLAYER_FIVE -> 5
                PlayerNumberType.PLAYER_SIX -> 6
                else -> 7
            }
        }

        fun winOrLoseString(winBet: Double) : Int {
            return if (winBet < 0.0) {
                R.string.fragment_main_game_you_lose
            } else {
                R.string.fragment_main_game_you_win
            }
        }

        fun Double.formatStringWinBet(): String = if (this.toString().last() == '0') this.toString().replaceAfter(".","").replace(".","") else this.toString()

        fun Double.formatStringBet() = this.toString().replaceAfter(".","").replace(".","") + "€"

        fun Double.formatStringWallet(): String = String.format("%7.2f", this)

        private fun String.toOnlineStatusType() = OnlineStatusType.valueOf(this)

        private fun String.toPlayerNumberType() = PlayerNumberType.valueOf(this)

        private fun String.toSplitType(): HandType = HandType.MainHand.toHandType(this)

        fun getTutorialNameId(allWallet: List<Wallet>): Long {
            var idToReturn = -1L
            for (index in allWallet.indices) {
                if (allWallet[index].pseudo == OfflineGameFragment.TUTORIAL_NAME) {
                    idToReturn = allWallet[index].id
                }
            }
            return idToReturn
        }

    }
}