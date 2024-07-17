package com.example.instagramcloneapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagramcloneapp.Model.User
import com.example.instagramcloneapp.databinding.ActivityAccountSettingsBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storagePriflePicRef : StorageReference? = null
    private lateinit var accountSettingsBinding: ActivityAccountSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountSettingsBinding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(accountSettingsBinding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storagePriflePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        with(accountSettingsBinding) {
            logoutBtnAccountSettings.setOnClickListener {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            saveInfoProfileBtn.setOnClickListener {
                if (checker == "clicked"){
                    uploadImageAndUpdateInfo()
                }else{
                    updateUserInfoOnly()
                }
            }

            closeProfileBtn.setOnClickListener {
                finish()
            }

            changeImageTxtBtn.setOnClickListener {

                checker = "clicked"

                CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(this@AccountSettingsActivity)
            }
        }

        userInfo()
    }

    //getting cropped image from crop image library
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            //data is image
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            accountSettingsBinding.profileImageAccountSettings.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndUpdateInfo() {

        when {
            imageUri == null -> Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT).show()
            accountSettingsBinding.fullNameAccountSettings.text.toString() == "" -> Toast.makeText(this, "Please write full name!", Toast.LENGTH_SHORT).show()
            accountSettingsBinding.usernameAccountSettings.text.toString() == "" -> Toast.makeText(this, "Please write username!", Toast.LENGTH_SHORT).show()
            accountSettingsBinding.bioAccountSettings.text.toString() == "" -> Toast.makeText(this, "Please write bio!", Toast.LENGTH_SHORT).show()

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Updating! Please wait...")
                progressDialog.show()

                val fileRef = storagePriflePicRef!!.child(firebaseUser.uid + ".jpg")

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

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = accountSettingsBinding.fullNameAccountSettings.text.toString().toLowerCase()
                        userMap["username"] = accountSettingsBinding.usernameAccountSettings.text.toString().toLowerCase()
                        userMap["bio"] = accountSettingsBinding.bioAccountSettings.text.toString().toLowerCase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this@AccountSettingsActivity,"Success! Account Updated",Toast.LENGTH_SHORT).show()

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

    private fun updateUserInfoOnly() {

        when {
            accountSettingsBinding.fullNameAccountSettings.text.toString() == "" -> Toast.makeText(this, "Please write full name!", Toast.LENGTH_SHORT).show()
            accountSettingsBinding.usernameAccountSettings.text.toString() == "" -> Toast.makeText(this, "Please write username!", Toast.LENGTH_SHORT).show()
            accountSettingsBinding.bioAccountSettings.text.toString() == "" -> Toast.makeText(this, "Please write bio!", Toast.LENGTH_SHORT).show()
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = accountSettingsBinding.fullNameAccountSettings.text.toString().toLowerCase()
                userMap["username"] = accountSettingsBinding.usernameAccountSettings.text.toString().toLowerCase()
                userMap["bio"] = accountSettingsBinding.bioAccountSettings.text.toString().toLowerCase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this@AccountSettingsActivity,"Success! Account Updated",Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                /*if (context != null){
                    return
                }*/
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(accountSettingsBinding.profileImageAccountSettings)

                    accountSettingsBinding.usernameAccountSettings.setText(user.getUsername())
                    accountSettingsBinding.fullNameAccountSettings.setText(user.getFullname())
                    accountSettingsBinding.bioAccountSettings.setText(user.getBio())
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

}
