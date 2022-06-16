package com.example.laporsatpam.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.laporsatpam.R
import com.example.laporsatpam.databinding.ActivityLaporanBinding
import com.example.laporsatpam.utilities.Constants
import com.example.laporsatpam.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class LaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        setListener()

    }
    private fun setListener(){
        binding.btnKirim.setOnClickListener{
            val judul = binding.inputJudul.text.toString().trim()
            val isi = binding.inputDeskripsi.text.toString().trim()

            if (notEmpty(judul,isi)){
                tambahLaporan(judul,isi)
            }

        }
    }

    private fun tambahLaporan(judul: String, isi: String){
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val laporan = hashMapOf(
            Constants.KEY_EMAIL to mAuth.currentUser?.email.toString().trim(),
            Constants.KEY_LAPORAN_JDL to judul,
            Constants.KEY_LAPORAN_ISI to isi,
            Constants.KEY_SENDER_NAME to preferenceManager.getString(Constants.KEY_NAME),
            Constants.KEY_TIMESTAMP to tanggalReadable(Date())
        )
        database.collection(Constants.KEY_COLLECTION_LAPORAN)
            .add(laporan)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    loading(false)
                    finish()
                    showToast("Laporan anda berhasil terkirim")
                    startActivity(intent)
                }
                else{
                    showToast("Gagal mengirimkan laporan")
                    loading(false)
                }
            }
    }

    private fun tanggalReadable( date: Date): String{
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun notEmpty(judul: String, isi: String) :Boolean {
        if (judul.isEmpty()){
            showToast("Judul harus terisi")
            binding.inputJudul.requestFocus()
            return false
        }
        else if (isi.isEmpty()){
            showToast("Deskripsi harus terisi")
            binding.inputDeskripsi.requestFocus()
            return false
        }
        else if (isi.length<10){
            showToast("Berikan deskripsi yang jelas, minimal 10 karakter")
            binding.inputDeskripsi.requestFocus()
            return false
        }
        else{
            return true
        }
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
    }

    private fun loading(isLoading: Boolean){
        if (isLoading){
            binding.progressBar.visibility = View.VISIBLE
            binding.btnKirim.visibility = View.INVISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnKirim.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, DashboardActivity::class.java))
    }
}