package com.example.laporsatpam.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.laporsatpam.R
import com.example.laporsatpam.databinding.ActivityProfileBinding
import com.example.laporsatpam.utilities.Constants
import com.example.laporsatpam.utilities.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private var encodedImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager(applicationContext)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataProfile()
        setListener()


    }



    private fun setListener(){
        binding.layoutImage.setOnClickListener { choiceImage() }

        binding.btnSimpan.setOnClickListener { updateData() }

        binding.ubahPassword.setOnClickListener { ShowPopUpUbahPassword() }

    }



    private fun updateData(){

        val nama = binding.inputNama.text.toString().trim()
        val alamat = binding.inputAlamat.text.toString().trim()
        var image = encodedImage

        if (image == ""){
            image = preferenceManager.getString(Constants.KEY_IMAGE).toString()
        }


        if (notEmpty(nama, alamat)){
            preferenceManager.putString(Constants.KEY_NAME, nama)
            preferenceManager.putString(Constants.KEY_ADDRESS, alamat)
            preferenceManager.putString(Constants.KEY_IMAGE, image)
            dataUpdate(nama, alamat, image)
        }
    }


    private fun dataUpdate(nama: String, alamat: String, image: String){
        isLoading(true)
        database = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        database.collection(Constants.KEY_COLLECTION_USERS)
            .document(mAuth.currentUser?.uid!!)
            .update(Constants.KEY_NAME, nama,
                Constants.KEY_ADDRESS, alamat,
                Constants.KEY_IMAGE, image)
            .addOnCompleteListener { it ->
                if (it.isSuccessful){
                    Intent(applicationContext, DashboardActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                        showToast("Berhasil diperbarui")
                    }
                }else{
                    showToast("Gagal memperbarui")
                    isLoading(false)
                }
            }
        isLoading(false)
    }

    private fun getDataProfile(){
        val mAuth = FirebaseAuth.getInstance()
        val fImage= decodeImage(preferenceManager.getString(Constants.KEY_IMAGE).toString())
        binding.imageProfile.setImageBitmap(fImage)
        binding.textPhoto.visibility = View.INVISIBLE
        binding.inputNama.setText( preferenceManager.getString(Constants.KEY_NAME))
        binding.inputAlamat.setText( preferenceManager.getString(Constants.KEY_ADDRESS))
        binding.inputEmail.hint = mAuth.currentUser?.email
    }

    private fun decodeImage(image: String): Bitmap {
        val bytes: ByteArray = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun encodeImage(bitmap: Bitmap): String{
        val previewWidth = 150
        val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, previewWidth,previewHeight,false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            if (it.data != null) {
                val imageUri: Uri = it.data!!.data!!
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.textPhoto.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun choiceImage(){
        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickImage.launch(intent)
    }

    private fun notEmpty(nama: String, alamat: String):Boolean {
        if (nama.isEmpty()){
            showToast("Nama Wajib Diisi !")
            binding.inputNama.requestFocus()
            return false
        }
        else if (alamat.isEmpty()){
            showToast("Alamat Wajib Diisi !")
            binding.inputAlamat.requestFocus()
            return false
        }
        else{
            return true
        }
    }

    private fun ShowPopUpUbahPassword(){
        val bottomSheetDialog = BottomSheetDialog( this, R.style.BottomSheetDialogTheme)

        val sheetChangePassView: View = LayoutInflater.from(applicationContext)
            .inflate(R.layout.activity_change_pass, findViewById(R.id.changePassContainer))


        sheetChangePassView.findViewById<Button>(R.id.btnUbahMainPass).setOnClickListener {
            val etNewPass = sheetChangePassView.findViewById<EditText>(R.id.etNewPass)
            val etNewPassConfm = sheetChangePassView.findViewById<EditText>(R.id.etNewPassConfm)
            val etOldPass = sheetChangePassView.findViewById<EditText>(R.id.etOldPass)
            val newPass = etNewPass.text.toString().trim()
            val newPassConfm = etNewPassConfm.text.toString().trim()
            val oldPass = etOldPass.text.toString().trim()

            if (ifPassNotEmpty(newPass, newPassConfm)){
                reAuth(newPass, oldPass)
                bottomSheetDialog.dismiss()
            }else{
                showToast("Gagal ubah password")
            }

        }

        bottomSheetDialog.setContentView(sheetChangePassView)
        bottomSheetDialog.show()
    }

    private fun ubahPass(newPass: String){

        val user = mAuth.currentUser!!
        user.let {
            user.updatePassword(newPass).addOnCompleteListener {
                if (it.isSuccessful){
                    showToast("Berhasil memperbarui password utama!")
                }else{
                    showToast("Gagal ubah password")
                }
            }
        }
    }

    private fun reAuth(newPass: String, oldPass: String){
        val user = mAuth.currentUser!!
        val credential = EmailAuthProvider.getCredential(user.email.toString(), oldPass)

        user.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful){
                ubahPass(newPass)
                showToast("User re-Authenticated")
            }else{
                showToast("Gagal")
            }

        }
    }


    private fun ifPassNotEmpty(newPass: String, newPassConfm: String):Boolean {
        if (newPass.isEmpty() || newPass.length < 8){
            showToast("Password baru minimal 8 karakter")
            return false
        }
        else if (newPass != newPassConfm){
            showToast("Password baru dengan password konfirmasinya tidak sama")
            return false
        }
        else{
            return true
        }
    }

    private fun isLoading(loading: Boolean){
        if (loading){
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSimpan.visibility = View.INVISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnSimpan.visibility = View.VISIBLE
        }
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan,Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, DashboardActivity::class.java))
    }
}