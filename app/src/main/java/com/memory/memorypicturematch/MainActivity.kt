package com.memory.memorypicturematch

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.*
import com.google.firebase.ktx.Firebase
import com.memory.memorypicturematch.Models.BoardSize
import com.memory.memorypicturematch.Models.MemoryGame
import com.memory.memorypicturematch.Models.UserImageList
import com.memory.memorypicturematch.utils.EXTRA_BOARD_SIZE
import com.memory.memorypicturematch.utils.EXTRA_GAME_NAME
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 651
    }


    private lateinit var adaptor: MemoryBoardAdaptor
    private lateinit var rvBoard: RecyclerView
    private lateinit var  tvNumMoves: TextView
    private lateinit var tvNumPair: TextView
    private lateinit var clRoot: CoordinatorLayout

    private val db = Firebase.firestore
    private var gameName: String? = null
    private var custumeGameImages : List<String>? = null
    private lateinit var memoryGame: MemoryGame
    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clRoot=findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPair=findViewById(R.id.tvNumPairs)


        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh -> {
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?",null, View.OnClickListener { setupBoard() })
                }else {
                    setupBoard()
                }
                return true

            }
            R.id.mi_new_size ->{
                showNewSizeFile()
                return true
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }
            R.id.mi_downlaod ->{
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customeGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if(customeGameName == null){
                Log.e(TAG,"Got null custom game name form intent")
                return
            }
            downloadGame(customeGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDownloadDialog() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board,null)
        showAlertDialog("Fectch Memory Game", boardDownloadView, View.OnClickListener {
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownlaodGame)
            val gameToDownlaod = etDownloadGame.text.toString().trim()
            downloadGame(gameToDownlaod)
        })
    }
    private fun downloadGame(customeGameName: String) {
        db.collection("games").document(customeGameName).get().addOnSuccessListener {document ->
            val userImageList =  document.toObject(UserImageList::class.java)
            if(userImageList?.images == null){
                Log.e(TAG, "invalid custom game data from Firestore")
                Snackbar.make(clRoot,"Sorry couldn't find any such game, $customeGameName", Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numbCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numbCards)
            gameName = customeGameName
            custumeGameImages = userImageList.images
            for (imageUrl in userImageList.images){
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot,"You're now playin '$customeGameName'", Snackbar.LENGTH_SHORT).show()
            setupBoard()
        }.addOnFailureListener{exception -> Log.e(TAG,"Exception when retrieving game",exception)  }
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioButtonSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your own memory board",boardSizeView, View.OnClickListener {
            val desiredBoardSize = when(radioButtonSize.checkedRadioButtonId){
                R.id.rb_easy -> BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desiredBoardSize)
            startActivityForResult(intent,
                    CREATE_REQUEST_CODE
            )
        })

    }

    private fun showNewSizeFile() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioButtonSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioButtonSize.check(
                    R.id.rb_easy
            )
            BoardSize.MEDIUM -> radioButtonSize.check(
                    R.id.rb_medium
            )
            BoardSize.HARD -> radioButtonSize.check(
                    R.id.rbHard
            )
        }
        showAlertDialog("Choose New size",boardSizeView, View.OnClickListener {
            boardSize = when(radioButtonSize.checkedRadioButtonId){
                R.id.rb_easy -> BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            custumeGameImages = null
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener ) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton("cancel",null)
                .setPositiveButton("Ok"){_,_ ->
                    positiveClickListener.onClick(null)
                }.show()

    }

    private fun setupBoard() {
        supportActionBar?.title = gameName ?: getString((R.string.app_name))
        when(boardSize){
            BoardSize.EASY -> {
                tvNumPair.text = "Pairs: 0 / 4"
                tvNumMoves.text = "Easy: 4 x 2"
            }
            BoardSize.MEDIUM -> {
                tvNumPair.text = "Pairs: 0 / 9"
                tvNumMoves.text = "Easy: 6 x 3"
            }
            BoardSize.HARD -> {
                tvNumPair.text = "Pairs: 0 / 12"
                tvNumMoves.text = "Easy: 6 x 4"
            }
        }
        tvNumPair.setTextColor(ContextCompat.getColor(this,
                R.color.color_progress_none
        ))
        memoryGame = MemoryGame(
                boardSize,
                custumeGameImages
        )
        adaptor = MemoryBoardAdaptor(
                this,
                boardSize,
                memoryGame.cards,
                object : MemoryBoardAdaptor.CardClickListener {
                    override fun onCardClick(position: Int) {
                        updateGameWithFlip(position)
                    }

                })
        rvBoard.adapter = adaptor
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot,"You Alreay Won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCarFacedUp(position)){
            Snackbar.make(clRoot,"Invalid Move!", Snackbar.LENGTH_SHORT).show()
            return
        }
        if(memoryGame.flipCard(position)){
            Log.i(TAG,"Found A Match! Num Pairs Found ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                    memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                    ContextCompat.getColor(this,
                            R.color.color_progress_none
                    ),
                    ContextCompat.getColor(this,
                            R.color.color_progress_full
                    )
            ) as Int
            tvNumPair.setTextColor(color)
            tvNumPair.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot,"You Won Congrats!!!", Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW, Color.GREEN, Color.WHITE)).oneShot()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        adaptor.notifyDataSetChanged()
    }
}