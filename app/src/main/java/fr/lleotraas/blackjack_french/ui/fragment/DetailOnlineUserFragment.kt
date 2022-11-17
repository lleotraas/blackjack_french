package fr.lleotraas.blackjack_french.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.github.aachartmodel.aainfographics.aachartcreator.*
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentDetailOnlineUserBinding
import fr.lleotraas.blackjack_french.model.OnlineStatusType
import fr.lleotraas.blackjack_french.model.OnlineUser
import fr.lleotraas.blackjack_french.model.User
import fr.lleotraas.blackjack_french.ui.activity.DetailOnlineUserActivityViewModel
import fr.lleotraas.blackjack_french.ui.dialog.InvitationToPlayDialog
import fr.lleotraas.blackjack_french.ui.dialog.WaitingForAnswerDialog
import fr.lleotraas.blackjack_french.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.IS_DEFAULT_IMAGE_PROFILE
import fr.lleotraas.blackjack_french.utils.Utils.Companion.ONLINE_STATUS
import fr.lleotraas.blackjack_french.utils.Utils.Companion.PICTURE_ROTATION
import fr.lleotraas.blackjack_french.utils.Utils.Companion.PSEUDO
import fr.lleotraas.blackjack_french.utils.Utils.Companion.SEARCHED_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.USER_PICTURE
import fr.lleotraas.blackjack_french.utils.Utils.Companion.WALLET
import fr.lleotraas.blackjack_french.utils.Utils.Companion.formatDate

@AndroidEntryPoint
class DetailOnlineUserFragment : Fragment() {

    private lateinit var mBinding: FragmentDetailOnlineUserBinding

    private val mViewModel: DetailOnlineUserActivityViewModel by viewModels()
    private var currentUser: OnlineUser? = null
    private lateinit var searchedUSer: OnlineUser
    private lateinit var getCurrentUser: LiveData<OnlineUser?>
    private var isUserClickOnDetailActivity = false
    private var isUserAskForPlay = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDetailOnlineUserBinding.inflate(inflater, container, false)
        val currentUserId = requireActivity().intent.extras!!.get(CURRENT_USER_ID) as String
        val searchedUserId = requireActivity().intent.extras!!.get(SEARCHED_USER_ID) as String
        getCurrentUser = mViewModel.getOnlineUser(currentUserId)
        updateUi(currentUserId, searchedUserId)
        getUserStatsTab(searchedUserId)
        configureListeners(currentUserId, searchedUserId)
        return mBinding.root
    }

    private fun updateUi(currentUserId: String, searchedUserId: String) {
        getCurrentUser.observe(viewLifecycleOwner) { currentUserOnline ->
            currentUser = currentUserOnline

            if (currentUserOnline?.onlineUser?.get(ONLINE_STATUS) == OnlineStatusType.ASK_FOR_PLAY && !isUserAskForPlay) {
                isUserAskForPlay = true
                isUserClickOnDetailActivity = true
                val alertDialog = InvitationToPlayDialog()
                val bundle = Bundle()
                bundle.putString(CURRENT_USER_ID, currentUserId)
                bundle.putString(SEARCHED_USER_ID, searchedUserId)
                alertDialog.arguments = bundle
                alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
            }

            if (currentUserOnline?.onlineUser?.get(ONLINE_STATUS) == OnlineStatusType.ONLINE) {
                isUserAskForPlay = false
            }

        }

        mViewModel.getSearchedUser(searchedUserId).observe(viewLifecycleOwner) { userSearched ->
            if ( userSearched != null) {
                mBinding.apply {
                    searchedUSer = userSearched
                    playerInfoDialogUserPseudo.text = userSearched.onlineUser[PSEUDO].toString()
                    playerInfoDialogUserWallet.text = userSearched.onlineUser[WALLET].toString()
                    if (userSearched.onlineUser[IS_DEFAULT_IMAGE_PROFILE] == true) {
                        Glide.with(mBinding.root)
                            .load(userSearched.onlineUser[USER_PICTURE])
                            .circleCrop()
                            .into(playerInfoDialogUserPicture)
                    } else {
                        mViewModel.getAllProfileImage().observe(viewLifecycleOwner) { listOfAllImage ->
                            Glide.with(mBinding.root)
                                .load(listOfAllImage[searchedUserId])
                                .circleCrop()
                                .into(playerInfoDialogUserPicture)

                            playerInfoDialogUserPicture.rotation = (userSearched.onlineUser[PICTURE_ROTATION] ?: 0f) as Float
                        }
                    }

                }
            }
        }
    }

    private fun createGraphView(arrayList: ArrayList<Any>, arrayList2: ArrayList<String>) {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Line)
            .zoomType(AAChartZoomType.XY)
            .animationType(AAChartAnimationType.Elastic)
            .title("User Wallet")
            .dataLabelsEnabled(false)
            .categories(arrayList2.toTypedArray())
            .yAxisTitle("")
            .series(arrayOf(
                AASeriesElement()
                    .name("User Economy")
                    .data(arrayList.toTypedArray())
                    .color("#BB86FC")
            ))
        mBinding.playerInfoGraph.aa_drawChartWithChartModel(aaChartModel)
    }

    private fun getUserStatsTab(searchedUserId: String) {
        mViewModel.getAllUserStats(searchedUserId).observe(viewLifecycleOwner) { listOfGamePlayed ->
            val arrayOfWalletVariation = ArrayList<Any>()
            val arrayOfDate = ArrayList<String>()
            for (gamePlayed in listOfGamePlayed) {
                arrayOfWalletVariation.add(gamePlayed.walletStateWhenGameEnding ?: 0.0)
                arrayOfDate.add(formatDate(gamePlayed.date.toString()))
            }
            createGraphView(arrayOfWalletVariation, arrayOfDate)
        }
    }

    private fun configureListeners(currentUserId: String, searchedUserId: String) {
        mBinding.apply {

            playerInfoDialogAddFriendBtn.setOnClickListener {

            }

            playerInfoDialogPlayWithBtn.setOnClickListener {
                isUserClickOnDetailActivity = true
                mViewModel.updateOnlineStatusAskForPlay(currentUserId, searchedUserId, OnlineStatusType.WAITING_ANSWER)
                mViewModel.updateUserIsGameHost(currentUserId, true)
                val alertDialog = WaitingForAnswerDialog()
                val bundle = Bundle()
                bundle.putString(CURRENT_USER_ID, currentUserId)
                bundle.putString(SEARCHED_USER_ID, searchedUserId)
                alertDialog.arguments = bundle
                alertDialog.isCancelable = false
                getCurrentUser.removeObserver {}
                alertDialog.show(requireActivity().supportFragmentManager, alertDialog.tag)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(javaClass.simpleName, "onResume: resumed & user is online")
        isUserClickOnDetailActivity = false
        mViewModel.updateOnlineUserStatus(mViewModel.getCurrentUser()?.uid.toString(), OnlineStatusType.ONLINE)
    }

    override fun onPause() {
        super.onPause()
        Log.e(javaClass.simpleName, "onPause: paused")
    }

    override fun onStop() {
        super.onStop()
        Log.e(javaClass.simpleName, "onStop: activity stopped" )
        if (!isUserClickOnDetailActivity) {
            currentUser?.onlineUser?.get(USER_ID)?.let {
                mViewModel.updateOnlineUserStatus(it.toString(), OnlineStatusType.OFFLINE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(javaClass.simpleName, "onDestroy: activity destroyed" )
//        mViewModel.updateOnlineStatusAskForPlay(currentUser.id.toString(), OnlineStatusType.OFFLINE)
    }
}