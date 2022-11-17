package fr.lleotraas.blackjack_french.ui.fragment


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.RowOnlineMainScreenBinding
import fr.lleotraas.blackjack_french.model.CustomUser
import fr.lleotraas.blackjack_french.model.OnlineStatusType

class OnlineMainScreenAdapter(
    var onPlayerClickShowMenu: (CustomUser, View) -> Unit
) : ListAdapter<CustomUser, OnlineMainScreenAdapter.OnlineMainScreenViewHolder>(Companion) {

    inner class OnlineMainScreenViewHolder(val mBinding: RowOnlineMainScreenBinding): RecyclerView.ViewHolder(mBinding.root)

    companion object : DiffUtil.ItemCallback<CustomUser>() {

        override fun areItemsTheSame(oldItem: CustomUser, newItem: CustomUser): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CustomUser, newItem: CustomUser): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineMainScreenViewHolder {
        return OnlineMainScreenViewHolder(RowOnlineMainScreenBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: OnlineMainScreenViewHolder, position: Int) {
        Log.e("ViewHolder", "onBindViewHolder: listOfCustomUser size: ${currentList.size}")
        val user = currentList[position]
        holder.mBinding.apply {
            onlineMainScreenRowUserPseudo.text = user.pseudo
            onlineMainScreenRowUserWallet.text = user.wallet.toString()
            loadProfileImage(user, holder)
            loadOnlineStatus(user, holder)
        }
    }

    private fun loadProfileImage(
        user: CustomUser,
        holder: OnlineMainScreenViewHolder
    ) {
        holder.mBinding.apply {

            Glide.with(holder.itemView)
                .load(user.userPicture)
                .circleCrop()
                .into(onlineMainScreenRowUserPicture)

            onlineMainScreenRowUserPicture.rotation = user.pictureRotation

            holder.itemView.setOnClickListener {
                onPlayerClickShowMenu(user, it)
            }
        }
    }

    private fun loadOnlineStatus(user: CustomUser, holder: OnlineMainScreenViewHolder) {
        holder.mBinding.apply {
            when (user.onlineStatus) {
                OnlineStatusType.ONLINE -> {
                    onlineMainScreenRowIsPlayingTv.text =
                        holder.itemView.context.resources.getString(R.string.online_main_screen_adapter_is_online)
                    Glide.with(holder.itemView)
                        .load(
                            ContextCompat.getDrawable(
                                holder.itemView.context,
                                R.drawable.ic_online_circle
                            )
                        )
                        .circleCrop()
                        .into(onlineMainScreenRowOnlineStatusImg)
                }

                OnlineStatusType.PLAYING -> {
                    onlineMainScreenRowIsPlayingTv.text =
                        holder.itemView.context.resources.getString(R.string.online_main_screen_adapter_is_playing)
                    Glide.with(holder.itemView)
                        .load(
                            ContextCompat.getDrawable(
                                holder.itemView.context,
                                R.drawable.ic_playing_circle
                            )
                        )
                        .circleCrop()
                        .into(onlineMainScreenRowOnlineStatusImg)
                }

                OnlineStatusType.ASK_FOR_PLAY -> {
                    onlineMainScreenRowIsPlayingTv.text =
                        holder.itemView.context.resources.getString(R.string.online_main_screen_adapter_ask_for_play)
                    Glide.with(holder.itemView)
                        .load(
                            ContextCompat.getDrawable(
                                holder.itemView.context,
                                R.drawable.ic_aks_for_play_circle
                            )
                        )
                        .circleCrop()
                        .into(onlineMainScreenRowOnlineStatusImg)
                }

                OnlineStatusType.WAITING_ANSWER -> {
                    onlineMainScreenRowIsPlayingTv.text =
                        holder.itemView.context.resources.getString(R.string.online_main_screen_adapter_waiting_answer)
                    Glide.with(holder.itemView)
                        .load(
                            ContextCompat.getDrawable(
                                holder.itemView.context,
                                R.drawable.ic_aks_for_play_circle
                            )
                        )
                        .circleCrop()
                        .into(onlineMainScreenRowOnlineStatusImg)
                }

                else -> {
                    onlineMainScreenRowIsPlayingTv.text =
                        holder.itemView.context.resources.getString(R.string.online_main_screen_adapter_is_offline)
                    Glide.with(holder.itemView)
                        .load(
                            ContextCompat.getDrawable(
                                holder.itemView.context,
                                R.drawable.ic_offline_circle
                            )
                        )
                        .circleCrop()
                        .into(onlineMainScreenRowOnlineStatusImg)
                }
            }
        }
    }


}