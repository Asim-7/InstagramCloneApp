package com.example.instagramcloneapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.instagramcloneapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var signInActivityBinding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signInActivityBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(signInActivityBinding.root)

        signInActivityBinding.signUpLinkBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        signInActivityBinding.loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = signInActivityBinding.emailLogin.text.toString()
        val password = signInActivityBinding.passwordLogin.text.toString()
        when{
            TextUtils.isEmpty(email) -> Toast.makeText(this,"Email is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"Password is required", Toast.LENGTH_SHORT).show()

            else -> {
                val progessDialog = ProgressDialog(this@SignInActivity)
                progessDialog.setTitle("Signing In")
                progessDialog.setMessage("Working on it, please wait!")
                progessDialog.setCanceledOnTouchOutside(false)
                progessDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        progessDialog.dismiss()
                        Toast.makeText(this,"Success! Signed In",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()                    }
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

}
