package com.example.laporsatpam.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.laporsatpam.R
import com.example.laporsatpam.activities.ChatActivity
import com.example.laporsatpam.models.Satpam
import com.makeramen.roundedimageview.RoundedImageView


class SatpamsAdapter(private var context: Context, private var satpams: List<Satpam>) :
    RecyclerView.Adapter<SatpamsAdapter.SatpamViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SatpamViewHolder {
         val view: View = LayoutInflater.from(context).inflate(
             R.layout.item_container_satpam,parent,false)

        return SatpamViewHolder(view)
    }

    override fun onBindViewHolder(holder: SatpamViewHolder, position: Int) {
        val currentSatpam = satpams[position]

        holder.textName.text = currentSatpam.nama
        holder.textTelepon.text = currentSatpam.telepon
        holder.imageProfile.setImageBitmap(currentSatpam.image.let { getSatpamImage(it) })

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("name",currentSatpam.nama )
            intent.putExtra("token", currentSatpam.token)
            intent.putExtra("image", currentSatpam.image)
            intent.putExtra("uid", currentSatpam.id)
            intent.putExtra("telepon", currentSatpam.telepon)

            context.startActivity(intent)
        }

    }

    fun getSatpamImage(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun getItemCount(): Int {
        return satpams.size
    }

     class SatpamViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView)  {

            val textName = itemView.findViewById<TextView>(R.id.textName)
            val textTelepon = itemView.findViewById<TextView>(R.id.textTelepon)
            val imageProfile = itemView.findViewById<RoundedImageView>(R.id.imageProfile)

    }

}