package com.example.laporsatpam.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.laporsatpam.R
import com.example.laporsatpam.databinding.ActivityLoginBinding
import com.example.laporsatpam.utilities.Constants
import com.example.laporsatpam.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        setListener()
    }

    private fun setListener(){
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (notEmpty(email, pass)){
                login(email, pass)
            }
        }
    }

    private fun login(email: String,pass: String){
        loading(true)
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()

        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_USER_ID, mAuth.currentUser!!.uid)
                        .get()
                        .addOnCompleteListener(this){ task ->
                            if (task.isSuccessful && task.result != null
                                && task.result.documents.size > 0){
                                val documentSnapshot: DocumentSnapshot = task.result.documents[0]
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                                documentSnapshot.getString(Constants.KEY_NAME)?.let { it1 ->
                                    preferenceManager.putString(Constants.KEY_NAME,
                                        it1
                                    )
                                }
                                documentSnapshot.getString(Constants.KEY_ADDRESS)?.let { it1 ->
                                    preferenceManager.putString(Constants.KEY_ADDRESS,
                                        it1
                                    )
                                }
                                documentSnapshot.getString(Constants.KEY_IMAGE)?.let { it1 ->
                                    preferenceManager.putString(Constants.KEY_IMAGE,
                                        it1
                                    )
                                }
                                Intent(applicationContext, DashboardActivity::class.java).also {
                                    it.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(it)
                                    showToast("!!! Selamat Datang !!!")
                                }
                            }
                            else{
                                mAuth.signOut()
                                loading(false)
                                showToast("Login Gagal ")
                            }

                        }

                }else{
                    mAuth.signOut()
                    loading(false)
                    showToast("Login Gagal")
                }
            }
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
    }

    private fun notEmpty(email:String, pass: String):Boolean {

        if (email.isEmpty()){
            showToast("Email wajib terisi !")
            binding.etEmail.requestFocus()
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showToast("Email tidak valid !")
            binding.etEmail.requestFocus()
            return false
        }
        else if (pass.isEmpty()){
            showToast("Password belum terisi !")
            binding.etPassword.requestFocus()
            return false
        }
        else{
            return true
        }
    }

    private fun loading(isLoading: Boolean){
        if (isLoading){
            binding.btnLogin.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnLogin.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            Intent(this@LoginActivity, DashboardActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

}