package fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.DialogOnlineGameBinding
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.MainScreenActivityViewModel
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.OnlineMainScreenActivity
import fr.lleotraas.blackjack_french.FacebookSignInActivity
import fr.lleotraas.blackjack_french.GoogleSignInActivity
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.createDeck
import fr.lleotraas.blackjack_french.features_online_game.presentation.OnlineGameActivityViewModel

@AndroidEntryPoint
class OnlineGameDialog: BottomSheetDialogFragment() {

    private lateinit var mBinding: DialogOnlineGameBinding
    private val mViewModel: MainScreenActivityViewModel by viewModels()
    private val onlineViewModel: OnlineGameActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogOnlineGameBinding.inflate(inflater, container, false)
        currentUserLogged()
        configureListeners()
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        currentUserLogged()
    }

    private fun configureListeners() {
        mBinding.apply {

            fragmentMainScreenGoogleSignInBtn.setOnClickListener {
                Log.e(javaClass.simpleName, "configureListeners: ${mViewModel.getCurrentUser()?.uid}")
                val intent = Intent(requireContext(), GoogleSignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
            }

            fragmentMainScreenFacebookSignBtn.setOnClickListener {
                val intent = Intent(requireContext(), FacebookSignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
            }

            fragmentMainScreenOnlineBtn.setOnClickListener {
                val deck = createDeck()
                for (card in deck.deckList) {
                    onlineViewModel.addCard(1, card)
                }
            }
        }
    }

    private fun currentUserLogged() {
        if (mViewModel.isCurrentUserLogged()) {
            val userId = mViewModel.getCurrentUser()!!.uid
            val onlineGameIntent = Intent(requireActivity(), OnlineMainScreenActivity::class.java)
            onlineGameIntent.putExtra(CURRENT_USER_ID, userId)
            startActivity(onlineGameIntent)
        }
    }
}