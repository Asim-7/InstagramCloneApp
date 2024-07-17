package com.example.instagramcloneapp.screens.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.instagramcloneapp.MainActivity
import com.example.instagramcloneapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var signUpBinding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)

        signUpBinding.signInLinkBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        signUpBinding.signUpBtn.setOnClickListener {
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullname = signUpBinding.fullNameSignUp.text.toString()
        val username = signUpBinding.usernameSignUp.text.toString()
        val email = signUpBinding.emailSignUp.text.toString()
        val password = signUpBinding.passwordSignUp.text.toString()

        when{
            TextUtils.isEmpty(fullname) -> Toast.makeText(this,"Full name is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this,"Username is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"Email is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"Password is required",Toast.LENGTH_SHORT).show()

            else -> {
                val progessDialog = ProgressDialog(this@SignUpActivity)
                progessDialog.setTitle("SignUp")
                progessDialog.setMessage("Working on it, please wait!")
                progessDialog.setCanceledOnTouchOutside(false)
                progessDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        task ->
                        if (task.isSuccessful){
                            saveUserInfo(fullname, username, email, progessDialog)
                        }
                        else{
                            val message = task.exception!!.toString()
                            Toast.makeText(this,"Error: $message",Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progessDialog.dismiss()
                        }
                    }
            }
        }

    }

    private fun saveUserInfo(fullname: String, username: String, email: String, progessDialog: ProgressDialog)
    {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullname.toLowerCase()
        userMap["username"] = username.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "Hi its me developer, I am just testing"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-clone-app-9c3ad.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=e6f9aebd-3a1f-4e0f-be1d-fbafd362c083"

        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    progessDialog.dismiss()
                    Toast.makeText(this,"Success! Account Created",Toast.LENGTH_SHORT).show()

                    //Following yourself by default
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this,"Error: $message",Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progessDialog.dismiss()
                }
            }

    }
}
