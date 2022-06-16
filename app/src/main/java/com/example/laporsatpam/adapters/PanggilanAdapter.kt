package com.example.laporsatpam.adapters


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.laporsatpam.R
import com.example.laporsatpam.activities.PanggilanActivity
import com.example.laporsatpam.models.Satpam
import com.example.laporsatpam.utilities.Constants

class PanggilanAdapter (val context: Context, private val panggilans: List<Satpam>): RecyclerView.Adapter<PanggilanAdapter.PanggilanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PanggilanViewHolder {
        val view: View = LayoutInflater
            .from(context)
            .inflate(R.layout.item_container_panggilan, parent, false)
        return PanggilanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PanggilanViewHolder, position: Int) {
        val currentPanggilan = panggilans[position]

        holder.setPanggilanData(currentPanggilan)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PanggilanActivity::class.java)
            val callIntent = Intent(Intent.ACTION_CALL)
            val phoneNumber = currentPanggilan.telepon
            intent.putExtra(Constants.KEY_NAME, currentPanggilan.nama)
            intent.putExtra(Constants.KEY_TELEPON, currentPanggilan.telepon)

            callIntent.data = Uri.parse("tel:$phoneNumber")
            context.startActivity(callIntent)
        }
    }

    override fun getItemCount(): Int {
        return panggilans.size
    }

    inner class PanggilanViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        private val textNama = itemView.findViewById<TextView>(R.id.nama_satpam)
        private val textTelepon = itemView.findViewById<TextView>(R.id.noTelepon)

        fun setPanggilanData(panggilan: Satpam){
            textNama.text = panggilan.nama
            textTelepon.text = panggilan.telepon
        }
    }
}