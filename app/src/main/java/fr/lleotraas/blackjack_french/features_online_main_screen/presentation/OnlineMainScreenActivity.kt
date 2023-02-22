package fr.lleotraas.blackjack_french.features_online_main_screen.presentation

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.ActivityOnlineMainScreenBinding
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.MainScreenActivity
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.page_adapter.OnlineMainScreenPageAdapter
import fr.lleotraas.blackjack_french.features_profile.presentation.ProfileActivity
import fr.lleotraas.blackjack_french.features_setting_menu.presentation.SettingMenuActivity
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import java.io.Serializable

@AndroidEntryPoint
class OnlineMainScreenActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityOnlineMainScreenBinding
    private val mViewModel: OnlineMainScreenActivityViewModel by viewModels()
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var userImg: ImageView
    private lateinit var currentUserId: String
    private var allMessageSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityOnlineMainScreenBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        configureToolbar()
        configureNavigationDrawer()
        configureViewPager()
        configureListeners()
        updateUi()
        newMessageInChatUpdate()
    }

    private fun configureToolbar() {
        setSupportActionBar(mBinding.activityOnlineMainScreenToolbar.toolbar)
    }

    private fun configureNavigationDrawer() {
        val toggleButton = ActionBarDrawerToggle(this, mBinding.activityOnlineMainScreenDrawerLayout, mBinding.activityOnlineMainScreenToolbar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mBinding.activityOnlineMainScreenDrawerLayout.addDrawerListener(toggleButton)
        toggleButton.syncState()
        val parentView = mBinding.activityOnlineMainScreenNavigationView.getHeaderView(0)
        username = parentView.findViewById(R.id.activity_main_screen_drawer_header_username)
        userImg = parentView.findViewById(R.id.activity_main_screen_drawer_header_user_img)
        email = parentView.findViewById(R.id.activity_main_screen_drawer_header_user_email)
    }

    private fun configureViewPager() {
        mBinding.apply {
            activityOnlineMainScreenViewPager.isUserInputEnabled = false
            val fragmentManager = supportFragmentManager
            val adapter = OnlineMainScreenPageAdapter(fragmentManager, lifecycle)
            activityOnlineMainScreenViewPager.adapter = adapter
            setTabLayoutName()
        }
    }

    private fun setTabLayoutName() {
        mBinding.apply {
            TabLayoutMediator(activityOnlineMainScreenTabs, activityOnlineMainScreenViewPager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = resources.getString(R.string.online_main_screen_activity_players_view)
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
            activityOnlineMainScreenTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    activityOnlineMainScreenViewPager.currentItem = tab.position
                    activityOnlineMainScreenTabs.getTabAt(1)?.text = resources.getString(R.string.online_game_activity_chat_view)
                    activityOnlineMainScreenTabs.getTabAt(1)?.icon = (ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_chat_24))
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    activityOnlineMainScreenTabs.getTabAt(1)?.icon = (ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_chat_white_24))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    private fun configureListeners() {
        mBinding.activityOnlineMainScreenNavigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.activity_main_screen_drawer_menu_logout -> {
                    mViewModel.updateOnlineUserStatus(currentUserId, OnlineStatusType.OFFLINE)
                    startActivity(Intent(this, MainScreenActivity::class.java))
                    true
                }
                R.id.activity_main_screen_drawer_setting_menu -> {
                    val intent = Intent(this, SettingMenuActivity::class.java)
                    intent.putExtra(Utils.CURRENT_USER_ID, currentUserId)
                    startActivity(intent)
                    true
                }
                R.id.activity_main_screen_drawer_profile_menu -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra(Utils.CURRENT_USER_ID, currentUserId)
                    startActivity(intent)
                    true
                }
                else -> true
            }
        }
    }

    private fun updateUi() {
        if (mViewModel.isCurrentUserLogged()) {
            mViewModel.updateImageList()
            mViewModel.getOnlineUser(mViewModel.getCurrentUser()?.uid.toString()).observe(this) { user ->
                currentUserId = user?.id.toString()
                username.text = user?.pseudo.toString()
                glideImage(applicationContext, user?.userPicture, userImg)
                userImg.rotation = user?.pictureRotation ?: 0f
                mViewModel.getAllImage().observe(this) { allImage ->
                    glideImage(this, allImage?.get(currentUserId) ?: user?.userPicture, userImg)
                    Log.e(javaClass.simpleName, "updateUi: user image loaded")
                }
            }
        }
    }

    private fun glideImage(context: Context, load: Serializable?, into: ImageView) {
        Glide.with(context)
            .load(load)
            .circleCrop()
            .into(into)
    }

    private fun newMessageInChatUpdate() {
        val currentUserId = mViewModel.getCurrentUser()?.uid.toString()
        mViewModel.listenToMessage().observe(this) { allMessage ->
            Log.e(javaClass.simpleName, "newMessageInChatUpdate: allMessageSize:$allMessageSize")
            if (allMessageSize == 0) {
                allMessageSize = allMessage.size
            }
            if (allMessageSize < allMessage.size) {
                Log.e(javaClass.simpleName, "newMessageInChatUpdate: allMessageSize was inferior to allMessage.size but now it's equal to $allMessageSize")
                mBinding.apply {
                    if (allMessage.last().id != currentUserId) {
                        Log.e(javaClass.simpleName, "newMessageInChatUpdate: last message is send by opponent")
                        activityOnlineMainScreenTabs.getTabAt(1)?.text = resources.getString(R.string.online_game_activity_new_message)
                        activityOnlineMainScreenTabs.getTabAt(1)?.icon?.setColorFilter(ContextCompat.getColor(applicationContext, R.color.black), PorterDuff.Mode.SRC_ATOP)
                        allMessageSize = allMessage.size
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mBinding.apply {
            if (activityOnlineMainScreenTabs.getTabAt(1)?.isSelected == true) {
                activityOnlineMainScreenViewPager.currentItem = 0
            } else {
                startActivity(Intent(applicationContext, MainScreenActivity::class.java))
            }
        }
    }
}