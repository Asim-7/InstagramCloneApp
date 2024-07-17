package com.example.instagramcloneapp.screens.feed

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.instagramcloneapp.MainActivity
import com.example.instagramcloneapp.databinding.ActivityAddPostBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storagePostPicRef : StorageReference? = null
    private lateinit var activityAddPostBinding: ActivityAddPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAddPostBinding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(activityAddPostBinding.root)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        activityAddPostBinding.saveNewPostBtn.setOnClickListener {
            uploadImage()
        }

        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this@AddPostActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            //data is image
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            activityAddPostBinding.imagePost.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        when{
            imageUri == null -> Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(activityAddPostBinding.descriptionPost.text.toString()) -> Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show()

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Uploading! Please wait...")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                var uploadTask : StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful){
                        val downloadUri = task.result
                        myUrl = downloadUri.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key     //generating random key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"] = postId!!
                        postMap["description"] = activityAddPostBinding.descriptionPost.text.toString().toLowerCase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this@AddPostActivity,"Success! Post Uploaded",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                        progressDialog.dismiss()

                    }else{
                        progressDialog.dismiss()
                    }
                } )

            }
        }
    }
}
