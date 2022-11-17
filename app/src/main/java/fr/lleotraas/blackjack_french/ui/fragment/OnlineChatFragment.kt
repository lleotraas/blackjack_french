package fr.lleotraas.blackjack_french.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.databinding.FragmentOnlineChatBinding
import fr.lleotraas.blackjack_french.model.Message
import fr.lleotraas.blackjack_french.model.User
import fr.lleotraas.blackjack_french.ui.activity.OnlineMainScreenActivityViewModel
import fr.lleotraas.blackjack_french.utils.Utils.Companion.loadCustomPhotoInChat
import java.util.*

@AndroidEntryPoint
class OnlineChatFragment : Fragment() {

    private lateinit var mBinding: FragmentOnlineChatBinding
    private val mViewModel: OnlineMainScreenActivityViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOnlineChatBinding.inflate(inflater, container, false)
        getCurrentUser()
        updateChat()
        listenToMessage()
        setupChatRecyclerView()
        initAdapter()
        configureListeners()
        return mBinding.root
    }

    private fun getCurrentUser() {
        mViewModel.getCurrentUser()?.let {
            mViewModel.getOnlineUser(it.uid).observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    currentUser = user
                }
            }
        }
    }

    private fun listenToMessage() {
        mViewModel.listenToMessage().observe(viewLifecycleOwner) { listOfChat ->
            val chatList = ArrayList<Message>()
            chatList.addAll(listOfChat)
            mViewModel.getAllImage().observe(viewLifecycleOwner) { listOfImage ->
                loadChatIntoRecyclerView(loadCustomPhotoInChat(chatList, listOfImage))
            }
        }
    }

    private fun initAdapter() {
        chatAdapter = ChatAdapter()
    }

    private fun updateChat() {
        mViewModel.getAllMessage().observe(viewLifecycleOwner) { listOfChat ->
            val chatList = ArrayList<Message>()
            chatList.addAll(listOfChat)
            loadChatIntoRecyclerView(chatList)
        }
    }

    private fun configureListeners() {
        mBinding.apply {
            fragmentOnlineChatSendMsgBtn.setOnClickListener {
                val currentDateTime = Calendar.getInstance().timeInMillis
                mViewModel.sendMessage(
                    Message(
                        currentUser.id,
                        currentUser.pseudo,
                        currentDateTime.toString(),
                        fragmentOnlineChatInput.text.toString(),
                        currentUser.userPicture,
                        pictureRotation = currentUser.pictureRotation
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
}