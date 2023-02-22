package fr.lleotraas.blackjack_french.features_setting_menu.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class SettingMenuActivityViewModel @Inject constructor (
    val firebaseHelper: FirebaseHelper,
    val userRepository: UserRepository
) : ViewModel() {



}