package com.example.instagramcloneapp.screens.feed

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.adapter.CommentsAdapter
import com.example.instagramcloneapp.model.Comment
import com.example.instagramcloneapp.model.User
import com.example.instagramcloneapp.databinding.ActivityCommentsBinding
import com.example.instagramcloneapp.util.OnSwipeTouchListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CommentsActivity : AppCompatActivity() {

    private var postId : String = ""
    private var publisherId : String = ""
    private var firebaseUser : FirebaseUser? = null
    private var commentAdapter : CommentsAdapter? = null
    private var commentList : MutableList<Comment>? = null
    private lateinit var commentsBinding: ActivityCommentsBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentsBinding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(commentsBinding.root)

        val intent = intent

        postId = intent.getStringExtra("postId")!!
        publisherId = intent.getStringExtra("publisherId")!!

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val recyclerView : RecyclerView = findViewById(R.id.recycler_view_comments)

        val linearLayoutManager = LinearLayoutManager(this)
        //linearLayoutManager.reverseLayout = true

        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImage()

        commentsBinding.postComment.setOnClickListener(View.OnClickListener {

            if (commentsBinding.addComment!!.text.toString() == ""){
                Toast.makeText(this, "Please add comment", Toast.LENGTH_SHORT).show()
            }else{
                addComment()
            }
        })

        recyclerView.setOnTouchListener(object : OnSwipeTouchListener(){
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Left")

            }

            override fun onSwipeRight() {
                Log.e("ViewSwipe", "Right")

                onBackPressed()
                //finish()
            }
        })
    }

    private fun addComment() {

        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = commentsBinding.addComment!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        addNotification()

        commentsBinding.addComment!!.text.clear()
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                /*if (context != null){
                    return
                }*/
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(commentsBinding.profileImageComment)

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    private fun getPostImage(){
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)
            .child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                /*if (context != null){
                    return
                }*/
                if (p0.exists()){
                    val image = p0.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(commentsBinding.postImageComment)

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    private fun readComments(){
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    commentList!!.clear()
                    for (snapshot in p0.children){
                        val comment = snapshot.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(publisherId)
        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "commented: "+ commentsBinding.addComment!!.text.toString()
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}
