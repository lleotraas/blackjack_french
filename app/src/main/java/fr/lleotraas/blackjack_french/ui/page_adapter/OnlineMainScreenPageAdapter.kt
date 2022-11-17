package fr.lleotraas.blackjack_french.ui.page_adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.lleotraas.blackjack_french.ui.fragment.OnlineChatFragment
import fr.lleotraas.blackjack_french.ui.fragment.OnlineMainScreenFragment

class OnlineMainScreenPageAdapter(
    private val fragmentManager: FragmentManager,
    private val lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
       return when (position) {
            1 -> OnlineChatFragment()
            else -> OnlineMainScreenFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}