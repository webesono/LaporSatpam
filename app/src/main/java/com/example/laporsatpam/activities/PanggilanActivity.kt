package com.example.laporsatpam.activities

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.example.laporsatpam.adapters.PanggilanAdapter
import com.example.laporsatpam.databinding.ActivityPanggilanBinding
import com.example.laporsatpam.models.Satpam
import com.example.laporsatpam.utilities.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class PanggilanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanggilanBinding

    override fun onStart() {
        super.onStart()

        val requestSinglePermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                startActivity(Intent(this, DashboardActivity::class.java))

            }
        }

        requestSinglePermission.launch(Manifest.permission.CALL_PHONE)

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityPanggilanBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        checkPermissions()
        dataSatpam()
        setListener()


    }
    private fun setListener() {
        binding.imageBack.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }

    private fun dataSatpam(){
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_SATPAM)
            .get()
            .addOnCompleteListener {
                loading(false)

                if (it.isSuccessful && it.result != null){
                    val satpams = ArrayList<Satpam>()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result){

                        val satpam = Satpam(intent.getSerializableExtra(Constants.KEY_SATPAM))
                        satpam.nama = queryDocumentSnapshot.getString(Constants.KEY_NAME).toString()
                        satpam.telepon = queryDocumentSnapshot.getString(Constants.KEY_TELEPON).toString()
                        satpam.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString()
                        satpam.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                        satpam.id = queryDocumentSnapshot.id
                        satpams.add(satpam)
                    }
                    if (satpams.size > 0){
                        val satpamsAdapter = PanggilanAdapter(this, satpams)
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