package com.example.usedgoods.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.usedgoods.DBKey.Companion.DB_ARTICLES
import com.example.usedgoods.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class AddArticleActivity : AppCompatActivity() {

    private var selectedUri : Uri? = null
    private val auth : FirebaseAuth by lazy{
        Firebase.auth
    }

    private val storage : FirebaseStorage by lazy{
        Firebase.storage
    }

    private val articleDB : DatabaseReference by lazy{
        Firebase.database.reference.child(DB_ARTICLES)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        findViewById<Button>(R.id.imageAddButton).setOnClickListener {
            when{
                ContextCompat.checkSelfPermission(//κΆν νμ©
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )== PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {

                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1010)
                }
            }
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val title = findViewById<EditText>(R.id.titleEditText).text.toString()
            val price =  findViewById<EditText>(R.id.priceEditText).text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()

            showProgress()

            //μ€κ°μ μ΄λ―Έμ§κ° μμΌλ©΄ μλ‘λ κ³Όμ μ μΆκ°

            if(selectedUri != null){
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(photoUri,   //**λΉλκΈ°**
                    //μ΄λ―Έμ§urlμ κ°μ Έμμ μ²¨λΆνμ¬ μλ‘λ
                    successHandler = { url ->
                        uploadArticle(sellerId,title,price,url)
                    },
                    errorHandler = {//μ€ν¨μ μλ‘λ X ν μ€νΈλ©μμ§
                        Toast.makeText(this,"μ¬μ§ μλ‘λμ μ€ν¨νμ΅λλ€",Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            }else { //**λκΈ°**
                uploadArticle(sellerId,title,price,"")
            }

        }
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String)-> Unit, errorHandler: ()-> Unit){
        val fileName = "${System.currentTimeMillis()}.png"  //λλ€ν Stringμ μ¬λ €λ λ¨
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener{
                if(it.isSuccessful){    //μλ‘λ μλ£μ
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->  //downloadUrlμ μ€μ λ‘ μ κ°μ§κ³  μ¬ μ μμλμ§
                            successHandler(uri.toString())
                        }.addOnFailureListener{
                            errorHandler()
                        }

                } else{//μλ‘λ μλ£ X
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerId :String, title : String, price : String, imageUrl : String){
        val model = ArticleModel(sellerId,title,System.currentTimeMillis(), "$price μ",imageUrl)

        articleDB.push().setValue(model)

        hideProgress()

        finish()//μμ΄ν λ±λ‘

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            1010 ->
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startContentProvider()
                }else{
                    Toast.makeText(this,"κΆνμ κ±°λΆνμ¨μ΅λλ€.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun startContentProvider(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2020)
    }

    private fun showProgress(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible = true
    }

    private fun hideProgress(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK){
            return
        }

        when(requestCode){
            2020 -> {
                val uri = data?.data
                if(uri != null){
                    findViewById<ImageView>(R.id.photoImageView).setImageURI(uri)
                    selectedUri = uri
                }else{
                    Toast.makeText(this, "μ¬μ§μ κ°μ Έμ€μ§ λͺ»νμ΅λλ€.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "μ¬μ§μ κ°μ Έμ€μ§ λͺ»νμ΅λλ€.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionContextPopup(){
        AlertDialog.Builder(this)
            .setTitle("κΆνμ΄ νμν©λλ€.")
            .setMessage("μ¬μ§μ κ°μ Έμ€κΈ° μν΄μ νμν©λλ€.")
            .setPositiveButton("λμ") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1010)
            }
            .create()
            .show()
    }
}