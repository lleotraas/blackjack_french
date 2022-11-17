package fr.lleotraas.blackjack_french.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.ActivityMainScreenBinding
import fr.lleotraas.blackjack_french.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.observeOnce

@AndroidEntryPoint
open class MainScreenActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainScreenBinding
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var userImg: ImageView
    private val mViewModel: MainScreenActivityViewModel by viewModels()
    private var currentUserId: String? = null
    private lateinit var getAllImage: LiveData<HashMap<String, ByteArray>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainScreenBinding.inflate(layoutInflater)
        getAllImage = mViewModel.getAllImage()
        setContentView(mBinding.root)
        configureToolbar()
        configureNavigationDrawer()
        configureListeners()
        updateUi()
    }

    private fun configureToolbar() {
        setSupportActionBar(mBinding.activityMainScreenToolbar.toolbar)
    }

    private fun configureNavigationDrawer() {
        val toggleButton = ActionBarDrawerToggle(this, mBinding.activityMainScreenDrawerLayout, mBinding.activityMainScreenToolbar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mBinding.activityMainScreenDrawerLayout.addDrawerListener(toggleButton)
        toggleButton.syncState()
        val parentView = mBinding.activityMainScreenNavigationView.getHeaderView(0)
        username = parentView.findViewById(R.id.activity_main_screen_drawer_header_username)
        userImg = parentView.findViewById(R.id.activity_main_screen_drawer_header_user_img)
        email = parentView.findViewById(R.id.activity_main_screen_drawer_header_user_email)
    }

    private fun configureListeners() {
        mBinding.activityMainScreenNavigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.activity_main_screen_drawer_menu_logout -> {
                    mViewModel.signOut()
                    updateUi()
                    true
                }
                R.id.activity_main_screen_drawer_setting_menu -> {
                    if (currentUserId != null) {
                        val intent = Intent(this, SettingMenuActivity::class.java)
                        intent.putExtra(CURRENT_USER_ID, currentUserId)
                        startActivity(intent)
                    }
                    true
                }
                R.id.activity_main_screen_drawer_profile_menu -> {
                    if (currentUserId != null) {
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra(CURRENT_USER_ID, currentUserId)
                        startActivity(intent)
                    }
                    true
                }
                else -> true
            }

        }
    }

    private fun updateUi() {
        if (mViewModel.isCurrentUserLogged()) {
            Log.e(javaClass.simpleName, "updateUi: user connected uid:${mViewModel.getCurrentUser()?.uid}")
            mViewModel.updateImageList()
            mViewModel.getOnlineUser(mViewModel.getCurrentUser()?.uid.toString()).observeOnce(this) { user ->
                currentUserId = user?.id.toString()
                username.text = user?.pseudo
                getAllImage.observe(this) { allImage ->
                    Glide.with(this)
                        .load(allImage[user?.id] ?: user?.userPicture)
                        .circleCrop()
                        .into(userImg)
                    userImg.rotation = user?.pictureRotation ?: 0f
                    Log.e(javaClass.simpleName, "updateUi: user image loaded")
                }
            }
        } else {
            currentUserId = null
            username.text = ""
            Glide.with(this)
                .load(ContextCompat.getDrawable(this, R.drawable.ic_account_circle))
                .circleCrop()
                .into(userImg)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("MainScreenActivity", "onBackPressed: finnish")
        finishAffinity()
    }
}