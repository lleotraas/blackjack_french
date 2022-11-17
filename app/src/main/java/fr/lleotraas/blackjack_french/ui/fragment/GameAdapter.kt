package fr.lleotraas.blackjack_french.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.CardImageBinding
import fr.lleotraas.blackjack_french.model.Card
import fr.lleotraas.blackjack_french.model.ColorType
import fr.lleotraas.blackjack_french.utils.Utils.Companion.getCardText

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
        holder.binding.apply {
            Glide.with(holder.binding.root)
                .load(cardImage(currentList[position]))
                .into(cardImageColorImg)

            cardImageNumberTopTv.text = getCardText(currentList[position])
            cardImageNumberBottomTv.text = getCardText(currentList[position])
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