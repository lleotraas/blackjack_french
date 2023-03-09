package fr.lleotraas.blackjack_french.features_online_main_screen.presentation.main_screen

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineMainScreenBinding
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.CustomUser
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.dialog.InvitationToPlayDialog
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.OnlineMainScreenActivityViewModel
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.adapter.OnlineMainScreenAdapter
import fr.lleotraas.blackjack_french.features_detail.presentation.DetailOnlineUserActivity
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.SEARCHED_USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.createListOfCustomUser

@AndroidEntryPoint
class OnlineMainScreenFragment : Fragment() {

    private lateinit var mBinding: FragmentOnlineMainScreenBinding
    private val mViewModel: OnlineMainScreenActivityViewModel by viewModels()
    private lateinit var onlineMainScreenAdapter: OnlineMainScreenAdapter

    private lateinit var getCurrentUser: LiveData<User?>
    private var currentUser: User? = null
    private var isUserClickOnDetailActivity = false
    private var isUserAskForPlay = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOnlineMainScreenBinding.inflate(inflater, container, false)
        val currentUserId = requireActivity().intent.extras!!.getString(CURRENT_USER_ID)
        if (currentUserId != null) {
            getCurrentUser = mViewModel.getOnlineUser(currentUserId)
            mViewModel.updateOnlineUserStatus(currentUserId, OnlineStatusType.ONLINE)
            initAdapters(currentUserId)
            setupOnlineUserRecyclerView()
            updateUI(currentUserId)
        }
        configureListeners()
        return mBinding.root
    }

    private fun initAdapters(currentUserId: String) {
        onlineMainScreenAdapter = OnlineMainScreenAdapter { _, _ ->
            onlineMainScreenAdapter.onPlayerClickShowMenu = { user, view ->
                isUserClickOnDetailActivity = true
                val intent = Intent(requireContext(), DetailOnlineUserActivity::class.java)
                intent.putExtra(CURRENT_USER_ID, currentUserId)
                intent.putExtra(SEARCHED_USER_ID, user.id)
                val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), view, requireContext().resources.getString(
                    R.string.animation_online_main_screen_to_detail))
                getCurrentUser.removeObserver {}
                startActivity(intent, options.toBundle())
            }
        }
    }


    private fun updateUI(currentUserId: String) {
        getCurrentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                this.currentUser = currentUser
            }
            mBinding.apply {

                if (
                    currentUser?.onlineStatus != OnlineStatusType.ASK_FOR_PLAY &&
                    currentUser?.onlineStatus != OnlineStatusType.PLAYING
                ) {
                    mViewModel.updateOnlineUserStatus(currentUserId, OnlineStatusType.ONLINE)
                    mViewModel.updateUserIsGameHost(currentUserId, false)
                    Log.e(javaClass.simpleName, "updateUI: user is online")
                }

                if (
                    currentUser?.onlineStatus == OnlineStatusType.ASK_FOR_PLAY &&
                    !isUserAskForPlay
                ) {
                    isUserAskForPlay = true
                    isUserClickOnDetailActivity = true
                        val alertDialog = InvitationToPlayDialog()
                        val bundle = Bundle()
                        bundle.putString(CURRENT_USER_ID, currentUserId)
                        bundle.putString(SEARCHED_USER_ID, currentUser.opponent)
                        alertDialog.isCancelable = false
                        alertDialog.arguments = bundle
                        getCurrentUser.removeObserver {}
                        alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
                    Log.e(javaClass.simpleName, "updateUI: user is ask for play")
                }

                if (currentUser?.onlineStatus == OnlineStatusType.ONLINE) {
                    isUserAskForPlay = false
                }
            }
        }
        updateUserList()
    }

    private fun updateUserList() {
        var listOfImage = HashMap<String, ByteArray>()
        mViewModel.getAllImage().observe(viewLifecycleOwner) { allImage ->
            if (allImage != null) {
                listOfImage = allImage
                Log.e(javaClass.simpleName, "updateUserList: allImage size: ${allImage.size}")
            }
        }

        mViewModel.getAllOnlineUser().observe(viewLifecycleOwner) { listOfOnlineUser ->
            mViewModel.compareImageList()
            Log.e(javaClass.simpleName, "updateUserList: listOfOnlineUser size:${listOfOnlineUser.size}")
            loadOnlineUserIntoRecyclerView(createListOfCustomUser(listOfOnlineUser, listOfImage, mViewModel.getCurrentUser()!!.uid))
        }

        mViewModel.allUserUpdated().observe(viewLifecycleOwner) { listOfOnlineUser ->
            Log.e(javaClass.simpleName, "updateUserList: listOfOnlineUserUpdated size:${listOfOnlineUser.size}")
            loadOnlineUserIntoRecyclerView(createListOfCustomUser(listOfOnlineUser, listOfImage, mViewModel.getCurrentUser()!!.uid))
        }
    }

    private fun configureListeners() {

    }

    private fun setupOnlineUserRecyclerView() = mBinding.fragmentOnlineMainScreenOnlineUsersRv.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        itemAnimator = null
    }

    private fun loadOnlineUserIntoRecyclerView(listOfOnlineUser: ArrayList<CustomUser>) {
        onlineMainScreenAdapter.submitList(listOfOnlineUser)
        Log.e(javaClass.simpleName, "loadOnlineUserIntoRecyclerView: listOfOnlineUser size:${listOfOnlineUser.size}")
        mBinding.fragmentOnlineMainScreenOnlineUsersRv.adapter = onlineMainScreenAdapter
    }



    override fun onResume() {
        super.onResume()
        Log.e(javaClass.simpleName, "onResume: resumed")
        isUserClickOnDetailActivity = false
        mViewModel.compareImageList()
        if (currentUser?.onlineStatus == OnlineStatusType.OFFLINE) {
            mViewModel.updateOnlineUserStatus(currentUser?.id.toString(), OnlineStatusType.ONLINE)
        }
    }

    override fun onStop() {
        super.onStop()
        getCurrentUser.removeObserver {}
        Log.e(javaClass.simpleName, "onStop: stopped isUserClickOnDetailActivity = $isUserClickOnDetailActivity")

        if (!isUserClickOnDetailActivity) {
            mViewModel.updateOnlineUserStatus(mViewModel.getCurrentUser()!!.uid, OnlineStatusType.OFFLINE)
            Log.e(javaClass.simpleName, "onStop: user is offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(javaClass.simpleName, "onDestroy: destroyed")
        getCurrentUser.removeObserver {}
    }
}