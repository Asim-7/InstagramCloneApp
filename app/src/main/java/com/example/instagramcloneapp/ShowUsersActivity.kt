package com.example.instagramcloneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Adapter.UserAdapter
import com.example.instagramcloneapp.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {

    var id : String = ""
    var title : String = ""

    var userAdapter : UserAdapter? = null
    var userList : List<User>? = null
    var idList : List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView : RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()
        when(title){
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(id)
            .child(intent.getStringExtra("storyid")!!)
            .child("views")

        ref.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    ( idList as ArrayList<String>).clear()
                    for (snapshot in p0.children){
                        ( idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    private fun getFollowers() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    ( idList as ArrayList<String>).clear()
                    for (snapshot in p0.children){
                        ( idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun getFollowing() {

        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    ( idList as ArrayList<String>).clear()
                    for (snapshot in p0.children){
                        ( idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun getLikes() {
        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(id)
        LikesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    ( idList as ArrayList<String>).clear()
                    for (snapshot in p0.children){
                        ( idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }
        })

    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children){
                    val user = snapshot.getValue(User::class.java)
                    for (id in idList!!){
                        if (user!!.getUid() == id){
                            (userList as ArrayList<User>).add(user!!)
                        }
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }
}
