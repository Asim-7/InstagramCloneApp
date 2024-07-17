package com.example.instagramcloneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Adapter.PostAdapter
import com.example.instagramcloneapp.Model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetails : AppCompatActivity() {

    var id : String = ""
    var title : String = ""

    private var postAdapter : PostAdapter? = null
    private var postList : MutableList<Post>? = null
    private var postId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarPostDetails)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val preferences = getSharedPreferences("PREFS", MODE_PRIVATE)
        if (preferences != null){
            postId = preferences.getString("postId", "mone")!!
        }

        val recyclerView : RecyclerView
        recyclerView = findViewById(R.id.recycler_view_post_details_activity)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        retrievePosts()

    }

    private fun retrievePosts() {

        val postsRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                val post = p0.getValue(Post::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
            }

        })

    }
}
