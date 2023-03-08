package fr.lleotraas.blackjack_french.features_offline_game.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.PlayerBoardRowBinding
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.CustomPlayer
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.formatStringBet
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

class PlayerBoardAdapter: ListAdapter<CustomPlayer, PlayerBoardAdapter.PlayerBoardViewHolder> (Companion) {

    inner class PlayerBoardViewHolder(val binding: PlayerBoardRowBinding) : RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<CustomPlayer>() {
        override fun areItemsTheSame(oldItem: CustomPlayer, newItem: CustomPlayer): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CustomPlayer, newItem: CustomPlayer): Boolean {
            return oldItem.playerNumber == newItem.playerNumber && oldItem.playerHandType == newItem.playerHandType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerBoardViewHolder {
        return PlayerBoardViewHolder(PlayerBoardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PlayerBoardViewHolder, position: Int) {
        val currentPlayer = currentList[position]
        holder.binding.apply {
            playerBoardBetValueTv.text = currentPlayer.bet.formatStringBet()
            playerBoardScoreValueTv.text = currentPlayer.score.toString()
            showCurrentPlayerIcon(currentPlayer, playerBoardCurrentPlayerIcon)
            showResultScore(currentPlayer, root, playerBoardPlayerResultIcon, playerBoardCurrentPlayerIcon)
            playerHaveBlackjack(currentPlayer, root.context, playerBoardScoreValueTv)
            playerDouble(currentPlayer, root.context, playerBoardDoubleValueTv)
            playerSplit(currentPlayer, root.context, playerBoardSplitTv)
            playerCanInsure(currentPlayer, playerBoardInsuranceFab, playerBoardInsuranceTv)
            playerInsurance(currentPlayer, root.context, playerBoardInsuranceValueTv)
            setupHorizontalRecyclerView(playerBoardCardRv, root.context)
            loadHandIntoRecyclerView(currentPlayer.hand, GameAdapter(), playerBoardCardRv)
        }
    }

    private fun playerCanInsure(
        currentPlayer: CustomPlayer,
        playerBoardInsuranceFab: FloatingActionButton,
        playerBoardInsuranceTv: AppCompatTextView
    ) {
        if (currentPlayer.isInsuranceOpen) {
            playerBoardInsuranceFab.visibility = View.VISIBLE
            playerBoardInsuranceTv.visibility = View.VISIBLE
        }

        playerBoardInsuranceFab.setOnClickListener {
            currentPlayer.insuranceBet = currentPlayer.bet / 2
            playerBoardInsuranceFab.visibility = View.GONE
            playerBoardInsuranceTv.visibility = View.GONE
        }
    }

    private fun playerInsurance(
        currentPlayer: CustomPlayer,
        context: Context,
        playerBoardInsuranceValueTv: AppCompatTextView
    ) {
        if (currentPlayer.insuranceBet > 0.0) {
            playerBoardInsuranceValueTv.setTextColor(ContextCompat.getColor(context, R.color.carpet_color))
        }
    }

    private fun playerSplit(
        currentPlayer: CustomPlayer,
        context: Context,
        playerBoardSplitTv: AppCompatTextView
    ) {
        if (
            currentPlayer.isPlayerFirstSplit ||
            currentPlayer.playerHandType == HandType.FirstSplit ||
            currentPlayer.playerHandType == HandType.SecondSplit
        ) {
            val color = Utils.getColorByPlayerNumber(currentPlayer.playerNumber)
            playerBoardSplitTv.setTextColor(ContextCompat.getColor(context, color))
        }
    }

    private fun playerDouble(
        currentPlayer: CustomPlayer,
        context: Context,
        playerBoardDoubleValueTv: AppCompatTextView
    ) {
        if (currentPlayer.isDouble) {
            playerBoardDoubleValueTv.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
    }

    private fun showCurrentPlayerIcon(
        currentPlayer: CustomPlayer,
        playerBoardCurrentPlayerIcon: AppCompatImageView
    ) {
        if (currentPlayer.isCurrentPlayer) {
            playerBoardCurrentPlayerIcon.visibility = View.VISIBLE
        } else {
            playerBoardCurrentPlayerIcon.visibility = View.GONE
        }
    }

    private fun showResultScore(
        currentPlayer: CustomPlayer,
        root: View,
        resultIcon: AppCompatImageView,
        currentPlayerIcon: AppCompatImageView
    ) {
        if (currentPlayer.resultScore != R.string.fragment_main_game_none) {
            Glide.with(root)
                .load(showResultScoreIcon(currentPlayer.resultScore))
                .into(resultIcon)
            resultIcon.visibility = View.VISIBLE
            currentPlayerIcon.visibility = View.GONE
        } else {
            resultIcon.visibility = View.GONE
        }
    }

    private fun loadHandIntoRecyclerView(hand: ArrayList<Card>?, adapter: GameAdapter, recyclerView: RecyclerView) {
        adapter.submitList(hand)
        recyclerView.adapter = adapter
    }

    private fun setupHorizontalRecyclerView(recyclerView: RecyclerView, context: Context) = recyclerView.apply {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun showResultScoreIcon(resultScore: Int): Int {
        return when (resultScore) {
            R.string.fragment_main_game_draw ->  R.drawable.baseline_front_hand_24
            R.string.fragment_main_game_you_lose -> R.drawable.baseline_cancel_24
            R.string.online_game_fragment_blackjack -> R.drawable.baseline_monetization_on_24
            R.string.fragment_main_game_you_win -> R.drawable.baseline_check_circle_24
            else -> 0
        }
    }

    private fun playerHaveBlackjack(currentPlayer: CustomPlayer, context: Context, scoreTv: AppCompatTextView) {
        if (
            currentPlayer.playerHandType == HandType.MainHand &&
            !currentPlayer.isPlayerFirstSplit &&
            currentPlayer.hand.size == 2 &&
            currentPlayer.score == 21
        ) {
            scoreTv.text = context.resources.getString(R.string.online_game_fragment_blackjack)
        }
    }
}