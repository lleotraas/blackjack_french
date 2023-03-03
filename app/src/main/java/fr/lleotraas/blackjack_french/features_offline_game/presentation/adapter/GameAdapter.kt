package fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.CardImageBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.ColorType
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.getCardText

class GameAdapter : ListAdapter<Card, GameAdapter.MainGameViewHolder> (Companion) {

    inner class MainGameViewHolder(val binding: CardImageBinding) : RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<Card>() {

        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.number == newItem.number
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainGameViewHolder {
        return MainGameViewHolder(CardImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MainGameViewHolder, position: Int) {
        val currentCard = currentList[position]
        if (currentCard.isAnimate) {
            holder.binding.root.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_view)
//            holder.binding.cardImageColorImg.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.card_fade_in)

            currentCard.isAnimate = false
        }
        if (currentCard.color != ColorType.RED) {
            holder.binding.apply {
                Glide.with(holder.binding.root)
                    .load(cardImage(currentCard))
                    .into(cardImageColorImg)

                cardImageNumberTopTv.text = getCardText(currentCard)
                cardImageNumberBottomTv.text = getCardText(currentCard)
            }
        } else {
            holder.binding.apply {
                cardImageContainer.setBackgroundColor(ContextCompat.getColor(holder.binding.root.context, R.color.red))
                cardImageColorImg.visibility = View.GONE
                cardImageNumberTopTv.visibility = View.GONE
                cardImageNumberBottomTv.visibility = View.GONE
            }
        }
    }

    private fun cardImage(card: Card): Int {
        val image = when (card.color) {
            ColorType.CLUB -> {
                R.drawable.club
            }
            ColorType.DIAMOND -> {
                R.drawable.diamond
            }
            ColorType.HEART -> {
                R.drawable.heart
            }
            else -> {
                R.drawable.spade
            }
        }
        return image
    }
}