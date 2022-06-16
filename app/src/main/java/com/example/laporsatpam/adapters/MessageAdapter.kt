package com.example.laporsatpam.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.laporsatpam.R
import com.example.laporsatpam.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth

import kotlin.collections.ArrayList

class MessageAdapter(val context: Context, val messageList: ArrayList<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_RECEIVE: Int = 1
    private val VIEW_TYPE_SEND: Int = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 1){
            /*inflate receive*/
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_container_received_message, parent, false)
            return ReceiveViewAdapter(view)
        }else{
            /*inflate send*/
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_container_sent_message, parent, false)
            return SendViewAdapter(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentChat = messageList[position]

        if (holder.javaClass == SendViewAdapter::class.java){

            val viewHolder = holder as SendViewAdapter
            holder.sentChat.text = currentChat.message
            holder.date.text = currentChat.timestamp

        }else{
            val viewHolder = holder as ReceiveViewAdapter

            holder.receiveChat.text = currentChat.message
            holder.date.text = currentChat.timestamp
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentChat = messageList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentChat.senderId)){
            return VIEW_TYPE_SEND
        }else{
            return VIEW_TYPE_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    inner class SendViewAdapter(itemView: View): RecyclerView.ViewHolder(itemView){

        val sentChat = itemView.findViewById<TextView>(R.id.textKirimPesan)
        val date = itemView.findViewById<TextView>(R.id.textDateTime)
    }

    inner class ReceiveViewAdapter(itemView: View): RecyclerView.ViewHolder(itemView){

        val receiveChat = itemView.findViewById<TextView>(R.id.textDapatPesan)
        val date = itemView.findViewById<TextView>(R.id.textDateTime2)

    }
}