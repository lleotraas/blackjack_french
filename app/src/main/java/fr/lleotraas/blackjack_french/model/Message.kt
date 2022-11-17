package fr.lleotraas.blackjack_french.model

class Message(
    val id: String? = null,
    val userName: String? = null,
    val date: String? = null,
    val message: String? = null,
    val userPicture: String? = null,
    var customUserPicture: ByteArray? = null,
    var pictureRotation: Float? = 0f
)