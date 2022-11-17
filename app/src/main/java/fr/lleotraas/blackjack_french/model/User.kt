package fr.lleotraas.blackjack_french.model

class User(
    var id: String? = null,
    var numberOfLoan: Int? = null,
    var wallet: Double? = null,
    var bet: HashMap<String, Double>? = null,
    var pseudo: String? = null,
    var userPicture: String? = null,
    var pictureRotation: Float? = null,
    var onlineStatus: OnlineStatusType? = OnlineStatusType.OFFLINE,
    var opponent: String? = null,
    var playerTurn: PlayerNumberType? = null,
    var splitType: HandType? = null,
    var numberOfGamePlayed: Int? = null,
    @field:JvmField
    var isDefaultProfileImage: Boolean? = false,
    @field:JvmField
    var isGameHost: Boolean? = false,
    @field:JvmField
    var isUserReady: Boolean? = false,
    @field:JvmField
    var isSplitting: Boolean = false
)