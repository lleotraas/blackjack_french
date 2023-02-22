package fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.page_adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.lleotraas.blackjack_french.features_online_game.presentation.online_game_chat.OnlineGameChatFragment
import fr.lleotraas.blackjack_french.features_online_game.presentation.online_game.OnlineGameFragment

class OnlineGamePageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> OnlineGameChatFragment()
            else -> OnlineGameFragment()
        }
    }


}