package fr.lleotraas.blackjack_french.ui.activity

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.ActivityOnlineGameBinding
import fr.lleotraas.blackjack_french.ui.page_adapter.OnlineGamePageAdapter

@AndroidEntryPoint
class OnlineGameActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityOnlineGameBinding
    private val mViewModel: OnlineGameActivityViewModel by viewModels()
    private var allMessageSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        configureViewPager()
//        initializeMessageInCHat()
        newMessageInChatUpdate()
    }

    private fun configureViewPager() {
        mBinding.apply {
            activityOnlineGameViewPager.isUserInputEnabled = false
            val fragmentManager = supportFragmentManager
            val adapter = OnlineGamePageAdapter(fragmentManager, lifecycle)
            activityOnlineGameViewPager.adapter = adapter
            setTabLayoutName()
        }
    }

    private fun setTabLayoutName() {
        mBinding.apply {
            TabLayoutMediator(activityOnlineGameTabs, activityOnlineGameViewPager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = resources.getString(R.string.online_game_activity_board_view)
                    }
                    else -> {
                        tab.text = resources.getString(R.string.online_game_activity_chat_view)
                        tab.icon = (ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_chat_24))
                    }
                }
            }.attach()
        }
        setTabLayoutListener()
    }

    private fun setTabLayoutListener() {
        mBinding.apply {
            activityOnlineGameTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    activityOnlineGameViewPager.currentItem = tab.position
                    activityOnlineGameTabs.getTabAt(1)?.text = resources.getString(R.string.online_game_activity_chat_view)
                    activityOnlineGameTabs.getTabAt(1)?.icon = (ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_chat_24))
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    activityOnlineGameTabs.getTabAt(1)?.icon = (ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_chat_white_24))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

//    private fun initializeMessageInCHat() {
//        val currentUserId = mViewModel.getCurrentUser()?.uid.toString()
//        mViewModel.getAllMessage(currentUserId).observe(this) { allMessage ->
//            if (allMessageSize == 0) {
//                allMessageSize = allMessage.size
//                Log.e(javaClass.simpleName, "initializeMessageInCHat: allMessageSize was 0 but now it's equal to $allMessageSize")
//            }
//        }
//
//    }

    private fun newMessageInChatUpdate() {
        val currentUserId = mViewModel.getCurrentUser()?.uid.toString()
        mViewModel.listenToMessage(currentUserId).observe(this) { allMessage ->
            Log.e(javaClass.simpleName, "newMessageInChatUpdate: allMessageSize:$allMessageSize")
             if (allMessageSize < allMessage.size) {
                 Log.e(javaClass.simpleName, "newMessageInChatUpdate: allMessageSize was inferior to allMessage.size but now it's equal to $allMessageSize")
                mBinding.apply {
                    if (allMessage.last().id != currentUserId) {
                        Log.e(javaClass.simpleName, "newMessageInChatUpdate: last message is send by opponent")
                        activityOnlineGameTabs.getTabAt(1)?.text = resources.getString(R.string.online_game_activity_new_message)
                        activityOnlineGameTabs.getTabAt(1)?.icon?.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black), PorterDuff.Mode.SRC_ATOP)
                        allMessageSize = allMessage.size
                    }
                }
            }
        }
    }
}