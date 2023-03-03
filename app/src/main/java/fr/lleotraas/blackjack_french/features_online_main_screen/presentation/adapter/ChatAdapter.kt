package fr.lleotraas.blackjack_french.features_online_main_screen.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.lleotraas.blackjack_french.databinding.RowChatBinding
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message

class ChatAdapter : ListAdapter<Message, ChatAdapter.ChatViewHolder>(Companion) {

    inner class ChatViewHolder(val mBinding: RowChatBinding): RecyclerView.ViewHolder(mBinding.root)

    companion object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.message == newItem.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(RowChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = currentList[position]
        holder.mBinding.apply {
            rowChatUsername.text = message.message
            rowChatProfileName.text = message.userName
            if (message.customUserPicture != null) {
                Glide.with(root)
                    .load(message.customUserPicture)
                    .circleCrop()
                    .into(rowChatProfileImg)

                rowChatProfileImg.rotation = message.pictureRotation ?: 0f
            } else {
                Glide.with(root)
                    .load(message.userPicture)
                    .circleCrop()
                    .into(rowChatProfileImg)

                rowChatProfileImg.rotation = message.pictureRotation ?: 0f
            }


        }
    }
}