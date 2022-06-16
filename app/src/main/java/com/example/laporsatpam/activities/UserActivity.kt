package com.example.laporsatpam.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.laporsatpam.R
import com.example.laporsatpam.adapters.SatpamsAdapter
import com.example.laporsatpam.databinding.ActivityUserBinding
import com.example.laporsatpam.models.Satpam
import com.example.laporsatpam.utilities.Constants
import com.example.laporsatpam.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        binding= ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        dataSatpam()
        setListener()
    }

    private fun setListener(){
        binding.imageBack.setOnClickListener{
            startActivity(Intent(this,DashboardActivity::class.java))
        }

    }

    private fun dataSatpam(){
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_SATPAM)
            .get()
            .addOnCompleteListener {
                loading(false)
                val isCurrentUser: String = preferenceManager.getString(Constants.KEY_USER_ID).toString()
                if (it.isSuccessful && it.result != null){
                    val satpams = ArrayList<Satpam>()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result){
                        if (isCurrentUser == queryDocumentSnapshot.id){
                            continue
                        }
                        val satpam = Satpam(intent.getSerializableExtra(Constants.KEY_SATPAM))
                        satpam.nama = queryDocumentSnapshot.getString(Constants.KEY_NAME).toString()
                        satpam.telepon = queryDocumentSnapshot.getString(Constants.KEY_TELEPON).toString()
                        satpam.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString()
                        satpam.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                        satpam.id = queryDocumentSnapshot.id
                        satpams.add(satpam)
                    }
                    if (satpams.size > 0){
                        val satpamsAdapter = SatpamsAdapter(this, satpams)
                        binding.satpamsRecycleView.adapter= satpamsAdapter
                        binding.satpamsRecycleView.visibility= View.VISIBLE
                    }else{
                        showErrorMessage()
                    }
                }else{
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage(){
        binding.errorText.text= String.format("%s","Data satpam tidak tersedia")
        binding.errorText.visibility= View.VISIBLE
    }

    private fun loading(isLoading: Boolean){
        if (isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, DashboardActivity::class.java))
    }
}