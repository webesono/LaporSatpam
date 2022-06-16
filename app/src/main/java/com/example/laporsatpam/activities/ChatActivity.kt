package com.example.laporsatpam.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.laporsatpam.R
import com.example.laporsatpam.models.ChatMessage
import com.example.laporsatpam.adapters.MessageAdapter
import com.example.laporsatpam.databinding.ActivityChatBinding
import com.example.laporsatpam.utilities.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.makeramen.roundedimageview.RoundedImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<ChatMessage>
    private lateinit var database: DatabaseReference

    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)
        binding.chatRecycleView.adapter = messageAdapter

        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        loadDetails()
        addToRecycleView()
        setListener()
    }

    private fun tanggalReadable( date: Date): String{
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun setListener(){
        binding.imageBack.setOnClickListener{onBackPressed()}
        binding.sendButton.setOnClickListener{ sendMessage() }
        binding.imageInfo.setOnClickListener { info() }
    }

    private fun loadDetails(){
        val nama = intent.getStringExtra("name")
        binding.textName.text = nama
    }

    private fun sendMessage(){
        database = FirebaseDatabase.getInstance().reference
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val chat = binding.inputMessage.text.toString()
        val date = tanggalReadable(Date())
        val messageObject = ChatMessage(chat, senderUid, date)

        if (notEmpty()){
            database.child(Constants.KEY_COLLECTION_CHAT)
                .child(senderRoom!!)
                .child("messages").push()
                .setValue(messageObject)
                .addOnSuccessListener {
                    database.child(Constants.KEY_COLLECTION_CHAT).child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            binding.inputMessage.setText("")

        }
    }

    private fun addToRecycleView(){
        isLoading(true)
        database = FirebaseDatabase.getInstance().reference
        database.child(Constants.KEY_COLLECTION_CHAT).child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(ChatMessage::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecycleView.scrollToPosition(messageAdapter.itemCount-1)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        isLoading(false)
    }

    private fun info(){
        val isiNama = intent.getStringExtra("name")
        val isiImage = intent.getStringExtra("image")
        val isiAlamat = intent.getStringExtra("telepon")

        val bottomSheetDialog = BottomSheetDialog(
            this@ChatActivity,
            R.style.BottomSheetDialogTheme
        )
        val bottomSheetView: View = LayoutInflater.from(applicationContext)
            .inflate(
                R.layout.layout_bottom_sheet,
                findViewById(R.id.bottomSheetContainer)
            )
        bottomSheetDialog.setContentView(bottomSheetView)

        val image = bottomSheetView.findViewById<RoundedImageView>(R.id.imageProfileDetail)
        val nama = bottomSheetView.findViewById<TextView>(R.id.textNameDetail)
        val alamat = bottomSheetView.findViewById<TextView>(R.id.textAlamatDetail)

        nama.text = isiNama
        alamat.text = isiAlamat
        image.setImageBitmap(decodeImage(isiImage.toString()))

        bottomSheetDialog.show()
    }
    private fun decodeImage(isiImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(isiImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun isLoading(loading: Boolean){
        if (loading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }


    private fun notEmpty(): Boolean{
        return binding.inputMessage.text.isNotEmpty()
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, UserActivity::class.java))
    }
}