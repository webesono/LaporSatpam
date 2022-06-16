package com.example.laporsatpam.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.*
import com.example.laporsatpam.R
import com.example.laporsatpam.databinding.ActivityDashboardBinding
import com.example.laporsatpam.utilities.Constants
import com.example.laporsatpam.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.makeramen.roundedimageview.RoundedImageView

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var preferenceManager: PreferenceManager

    private val TIME_INTERVAL = 2000
    private var backPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        preferenceManager = PreferenceManager(applicationContext)
        getToken()
        loadUserDetails()
        setListener()


    }

    private fun setListener(){

        binding.laporan.setOnClickListener {
            startActivity(Intent(applicationContext, LaporanActivity::class.java))
        }

        binding.imageProfile.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }

        binding.listChat.setOnClickListener{
            startActivity(Intent(applicationContext, UserActivity::class.java))
        }

        binding.panggilan.setOnClickListener{
            startActivity(Intent(applicationContext, PanggilanActivity::class.java))
        }

        binding.imageSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun loadUserDetails(){
        val photo = findViewById<RoundedImageView>(R.id.imageProfile)
        val alamat = findViewById<TextView>(R.id.textAlamat)
        val nama = findViewById<TextView>(R.id.textNama)

        nama.text = preferenceManager.getString(Constants.KEY_NAME)
        alamat.text = preferenceManager.getString(Constants.KEY_ADDRESS)

        val byte: ByteArray = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(byte,0,byte.size)
        photo.setImageBitmap(bitmap)

    }

    private fun getToken (){
        FirebaseMessaging.getInstance().token.addOnSuccessListener ( this::updateToken )
    }

    private fun updateToken(token: String){
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val documentReference: DocumentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(mAuth.currentUser?.uid!!)

        documentReference.update(Constants.KEY_FCM_TOKEN,token)
            .addOnFailureListener{ showToast("Tidak dapat melakukan update token")}
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
    }

    private fun signOut(){
        showToast("Signing out ...")
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()


        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID).toString())

        val updates: HashMap<String, FieldValue> = HashMap()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates as Map<String, Any>)
            .addOnSuccessListener {
                mAuth.signOut()
                preferenceManager.clear()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener{showToast("Tidak dapat logout")}

    }

    override fun onBackPressed() {
        if (backPressed + TIME_INTERVAL > System.currentTimeMillis() ){
            super.onBackPressed()
            return
        }
        else{
            Toast.makeText(baseContext, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()

    }

}