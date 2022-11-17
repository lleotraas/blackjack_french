package fr.lleotraas.blackjack_french.model

class CustomUser(
    var id: String,
    var wallet: Double,
    var pseudo: String,
    var userPicture: Any? = null,
    var onlineStatus: OnlineStatusType,
    var pictureRotation: Float
)