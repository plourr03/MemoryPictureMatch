package com.memory.memorypicturematch

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.memory.memorypicturematch.Models.BoardSize
import com.memory.memorypicturematch.Models.MemoryCard
import com.memory.memorypicturematch.R
import com.squareup.picasso.Picasso
import kotlin.math.min

class MemoryBoardAdaptor(
        private val context: Context,
        private val boardSize: BoardSize,
        private val cards: List<MemoryCard>,
        private val cardClickListener: CardClickListener
) :
        RecyclerView.Adapter<MemoryBoardAdaptor.ViewHolder>() {

    companion object{
        private  const val  MARGIN_SIZE = 10
        private const val TAG = "MemoryBoardAdaptor"
    }

    interface  CardClickListener{
        fun onCardClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardWidth = parent.width /boardSize.getWidth()-(2* MARGIN_SIZE)
        val cardHeight = parent.height/boardSize.getHeight()-(2* MARGIN_SIZE)
        val cardSideLength = min(cardWidth,cardHeight)
        val view =   LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)
        val layoutParms =  view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParms.height = cardSideLength
        layoutParms.width = cardSideLength
        layoutParms.setMargins(
                MARGIN_SIZE,
                MARGIN_SIZE,
                MARGIN_SIZE,
                MARGIN_SIZE
        )
        return ViewHolder(view)
    }

    override fun getItemCount() = boardSize.numCards

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class  ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val  imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)
        fun bind(position: Int) {
            val memeoryCard = cards[position]
            if(memeoryCard.isFaceUp){
                if(memeoryCard.imageUrl != null){
                    Picasso.get().load(memeoryCard.imageUrl).placeholder(R.drawable.ic_image).into(imageButton)
                }else{
                    imageButton.setImageResource(memeoryCard.identifier)
                }
            }else{
                imageButton.setImageResource(if(memeoryCard.isFaceUp) cards[position].identifier else R.drawable.bjieodbhpjz)
            }

            imageButton.alpha = if(memeoryCard.isMatched) .4f else 1.0f
            val colorStateList=if(memeoryCard.isMatched) ContextCompat.getColorStateList(context,
                    R.color.color_gray
            )else null
            ViewCompat.setBackgroundTintList(imageButton,colorStateList)
            imageButton.setOnClickListener {
                Log.i(TAG,"Clicked On Position $position")
                cardClickListener.onCardClick(position)
            }

        }
    }
}
