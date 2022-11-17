package fr.lleotraas.blackjack_french.ui.activity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class SettingMenuActivityViewModel @Inject constructor (
    val firebaseHelper: FirebaseHelper,
    val userRepository: UserRepository
) : ViewModel() {



}