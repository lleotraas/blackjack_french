package fr.lleotraas.blackjack_french.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineChatBinding
import fr.lleotraas.blackjack_french.model.Message
import fr.lleotraas.blackjack_french.model.User
import fr.lleotraas.blackjack_french.ui.activity.OnlineGameActivityViewModel
import fr.lleotraas.blackjack_french.ui.dialog.QuitOnlineGameDialog
import fr.lleotraas.blackjack_french.utils.Utils.Companion.loadCustomPhotoInChat
import java.util.*

@AndroidEntryPoint
class OnlineGameChatFragment : Fragment() {

    private lateinit var mBinding: FragmentOnlineChatBinding
    private val mViewModel: OnlineGameActivityViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private var currentUser: User? = null
    private lateinit var callBack: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOnlineChatBinding.inflate(inflater, container, false)
        onBackPressed()
        getCurrentUser()
        setupChatRecyclerView()
        initAdapter()
        configureListeners()
        return mBinding.root
    }

    private fun onBackPressed() {
        callBack = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val quitDialog = QuitOnlineGameDialog()
            quitDialog.show(requireActivity().supportFragmentManager, quitDialog.tag)
        }
    }

    private fun getCurrentUser() {
        mViewModel.getCurrentUser()?.let {
            mViewModel.getOnlineUser(it.uid).observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    currentUser = user
                    updateChat()
                    listenToMessage()
                }
            }
        }
    }

    private fun listenToMessage() {
        val userId = if (currentUser?.isGameHost == true) currentUser?.id else currentUser?.opponent
        if (userId?.isNotEmpty() == true) {
            mViewModel.listenToMessage(userId).observe(viewLifecycleOwner) { listOfChat ->
                val chatList = ArrayList<Message>()
                chatList.addAll(listOfChat)
                mViewModel.getAllImage().observe(viewLifecycleOwner) { listOfImage ->
                    loadChatIntoRecyclerView(loadCustomPhotoInChat(chatList, listOfImage))
                }
            }
        }
    }

    private fun initAdapter() {
        chatAdapter = ChatAdapter()
    }

    private fun updateChat() {
        val userId = if (currentUser?.isGameHost == true) currentUser?.id else currentUser?.opponent
        if (userId?.isNotEmpty() == true) {
            mViewModel.getAllMessage(userId).observe(viewLifecycleOwner) { listOfChat ->
                val chatList = ArrayList<Message>()
                chatList.addAll(listOfChat)
                loadChatIntoRecyclerView(chatList)
            }
        }
    }

    private fun configureListeners() {
        mBinding.apply {
            fragmentOnlineChatSendMsgBtn.setOnClickListener {
                val currentDateTime = Calendar.getInstance().timeInMillis
                val userId = if (currentUser?.isGameHost == true) currentUser?.id else currentUser?.opponent
                mViewModel.sendMessage(
                    userId!!
                    , Message(
                        currentUser?.id,
                        currentUser?.pseudo,
                        currentDateTime.toString(),
                        fragmentOnlineChatInput.text.toString(),
                        currentUser?.userPicture
                    )
                )
                fragmentOnlineChatInput.text?.clear()
            }
        }
    }

    private fun setupChatRecyclerView() = mBinding.fragmentOnlineChatRv.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        itemAnimator = null
    }

    private fun loadChatIntoRecyclerView(chatList: ArrayList<Message>) {
        chatList.reverse()
        chatAdapter.submitList(chatList)
        mBinding.apply {
            fragmentOnlineChatRv.scrollToPosition(chatList.size - 1)
            fragmentOnlineChatRv.adapter = chatAdapter
            root.viewTreeObserver.addOnGlobalLayoutListener{
                val heightDiff = root.rootView.height - root.height
                if (heightDiff > 100) {
                    if (chatAdapter.itemCount > 0) {
                        fragmentOnlineChatRv.smoothScrollToPosition(0)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(javaClass.simpleName, "onStop: stopped")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(javaClass.simpleName, "onDestroy: ${javaClass.simpleName} destroyed")
        if (currentUser?.isGameHost == true) {
            mViewModel.deleteAllMessage(currentUser?.id.toString())
            Log.e(javaClass.simpleName, "onDestroy: chat deleted for userId:${currentUser?.id}")
        }
    }
}