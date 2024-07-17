package com.example.instagramcloneapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Model.Post
import com.example.instagramcloneapp.PostDetails
import com.example.instagramcloneapp.R
import com.squareup.picasso.Picasso

class MyImagesAdapter( private val mContext: Context, mPost: List<Post>)
    : RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>(){

    private var mPost : List<Post>? = null

    init {
        this.mPost = mPost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post : Post = mPost!![position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostid())
            editor.apply()
            /*(mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment())
                .commit()*/
            val intent = Intent(mContext, PostDetails::class.java)
            intent.putExtra("id",post.getPostid())
            intent.putExtra("title", "Post")
            mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(@NonNull itemView: View)
        : RecyclerView.ViewHolder(itemView){

        var postImage : ImageView

        init {
            postImage = itemView.findViewById(R.id.post_image_item)
        }
    }
}