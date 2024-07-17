package com.example.instagramcloneapp.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.screens.user.AccountSettingsActivity
import com.example.instagramcloneapp.adapter.MyImagesAdapter
import com.example.instagramcloneapp.model.Post
import com.example.instagramcloneapp.model.User

import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.screens.user.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList : List<Post>? = null
    var myImagesAdapter : MyImagesAdapter? = null
    var myImagesAdapterSavedImg : MyImagesAdapter? = null
    var postListSaved : List<Post>? = null
    var mySavedImgs : List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null){
            this.profileId = pref.getString("profileId", "none").toString()
        }
        if (profileId == firebaseUser.uid){
            view.findViewById<Button>(R.id.edit_account_settings_button).text = "Edit Profile"
        }
        else if (profileId != firebaseUser.uid){
            checkFollowAndFollowingBtnStatus()
        }

        //RecyclerView for Uploaded Images
        val recyclerViewUploadedImages : RecyclerView
        recyclerViewUploadedImages = view.findViewById(R.id.recycler_view_uploaded_pics)
        recyclerViewUploadedImages.setHasFixedSize(true)
        val linearLayoutManger : LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadedImages.layoutManager = linearLayoutManger

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<Post>) }
        recyclerViewUploadedImages.adapter = myImagesAdapter

        //RecyclerView for Saved Images
        val recyclerViewSavedImages : RecyclerView
        recyclerViewSavedImages = view.findViewById(R.id.recycler_view_saved_pics)
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManger2 : LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSavedImages.layoutManager = linearLayoutManger2

        postListSaved = ArrayList()
        myImagesAdapterSavedImg = context?.let { MyImagesAdapter(it, postListSaved as ArrayList<Post>) }
        recyclerViewSavedImages.adapter = myImagesAdapterSavedImg

        recyclerViewSavedImages.visibility = View.GONE
        recyclerViewUploadedImages.visibility = View.VISIBLE

        var uploadImgsBtn = view.findViewById<ImageButton>(R.id.images_grid_view_btn)
        uploadImgsBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.GONE
            recyclerViewUploadedImages.visibility = View.VISIBLE
        }

        var savedImgsBtn = view.findViewById<ImageButton>(R.id.images_save_grid_view_btn)
        savedImgsBtn.setOnClickListener {
            recyclerViewUploadedImages.visibility = View.GONE
            recyclerViewSavedImages.visibility = View.VISIBLE
        }

        view.findViewById<TextView>(R.id.total_followers).setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.total_following).setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.edit_account_settings_button).setOnClickListener {
            //startActivity(Intent(context, AccountSettingsActivity::class.java))

            val getBtnText = view.findViewById<Button>(R.id.edit_account_settings_button).text.toString()

            when{
                getBtnText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getBtnText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }
                    addNotification()
                }

                getBtnText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }
                }
            }
        }

        getFollowers()
        getFollowing()
        userInfo()
        myPhotos()
        getTotalNumOfPosts()
        mySaves()

        return view
    }

    private fun checkFollowAndFollowingBtnStatus() {

        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        if (followingRef != null){
            followingRef.addValueEventListener(object : ValueEventListener{

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()){
                        view?.findViewById<Button>(R.id.edit_account_settings_button)?.text = "Following"
                    }else{
                        view?.findViewById<Button>(R.id.edit_account_settings_button)?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })
        }

    }

    private fun getFollowers(){

        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    view?.findViewById<TextView>(R.id.total_followers)?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun getFollowing(){

        val followingsRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    view?.findViewById<TextView>(R.id.total_following)?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun myPhotos(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    (postList as ArrayList<Post>).clear()

                    for (snapshot in p0.children){
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.getPublisher().equals(profileId)){
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

        })
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        userRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                /*if (context != null){
                    return
                }*/
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.findViewById<ImageView>(R.id.pro_image_profile_frag))
                        view?.findViewById<TextView>(R.id.profile_fragment_username)?.text = user.getUsername()
                        view?.findViewById<TextView>(R.id.full_name_profile_frag)?.text = user.getFullname()
                        view?.findViewById<TextView>(R.id.bio_profile_frag)?.text = user.getBio()
                    }
                    catch (e: Exception) {
                        // handler
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    private fun getTotalNumOfPosts(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    var postCounter = 0
                    for (snap in p0.children){
                        val post = snap.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId){
                            postCounter++
                        }
                    }
                    view?.findViewById<TextView>(R.id.total_posts)?.text = " " + postCounter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun mySaves() {
        mySavedImgs = ArrayList()
        val savedRef = FirebaseDatabase.getInstance()
            .reference
            .child("Saves")
            .child(firebaseUser.uid)
        savedRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (snap in p0.children){
                        ( mySavedImgs as ArrayList<String> ).add(snap.key!!)
                    }
                    readSavedImgsData()
                }
            }
            override fun onCancelled(p0: DatabaseError) {   }
        })
    }

    private fun readSavedImgsData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    ( postListSaved as ArrayList<Post> ).clear()
                    for (snap in p0.children){
                        val post = snap.getValue(Post::class.java)
                        for (key in mySavedImgs!!){
                            if (post!!.getPostid() == key){
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }
            override fun onCancelled(p0: DatabaseError) {   }
        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(profileId)
        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }

}
