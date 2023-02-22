package fr.lleotraas.blackjack_french.features_online_main_screen.domain.model

class CustomUser(
    var id: String,
    var wallet: Double,
    var pseudo: String,
    var userPicture: Any? = null,
    var onlineStatus: OnlineStatusType,
    var pictureRotation: Float
)