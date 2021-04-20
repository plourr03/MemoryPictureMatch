package com.memory.memorypicturematch.Models

import com.memory.memorypicturematch.utils.DEFAULT_ICONS

class MemoryGame(
    private val boardSize: BoardSize,
    private val customeImages: List<String>?
) {

    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numOfCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null

    init {
        if (customeImages == null) {
            val choosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomizedList = (choosenImages + choosenImages).shuffled()
            cards = randomizedList.map { MemoryCard(it) }
        }else{
            val randomisedImages = (customeImages + customeImages).shuffled()
            cards = randomisedImages.map { MemoryCard(it.hashCode(), it) }
        }
    }

    fun flipCard(position: Int):Boolean {
        numOfCardFlips++
        var foundMatch =false
        val card = cards[position]
        if (indexOfSingleSelectedCard == null) {
            restoreCards()
            indexOfSingleSelectedCard = position
        }else{
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!,position)
            indexOfSingleSelectedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(posision1: Int, position2: Int): Boolean {
        if(cards[posision1].identifier != cards[position2].identifier){
            return false
        }
        cards[posision1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true

    }

    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }

    }

    fun haveWonGame(): Boolean {
        return numPairsFound ==boardSize.getNumPairs()
    }

    fun isCarFacedUp(position: Int): Boolean {
        return cards[position].isFaceUp

    }

    fun getNumMoves(): Int {
        return numOfCardFlips/2
    }
}