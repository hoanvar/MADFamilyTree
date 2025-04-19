package com.dung.madfamilytree.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.callbacks.AlbumImageItemCallBack
import com.dung.madfamilytree.models.Image
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumImageItemAdapter(val listener: (Image) -> Unit) : ListAdapter<Image,AlbumImageItemAdapter.AlbumImageItemViewHolder>(AlbumImageItemCallBack()) {
    class AlbumImageItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView){
        val imageView = rootView.findViewById<ShapeableImageView>(R.id.image)
        companion object{
            fun inflate(parent: ViewGroup,viewType: Int): AlbumImageItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                if(viewType == 0){
                    val rootView = layoutInflater.inflate(R.layout.add_new_album_image,parent,false)
                    return AlbumImageItemViewHolder(rootView)
                }
                else {
                    val rootView = layoutInflater.inflate(R.layout.album_image_item,parent,false)
                    return AlbumImageItemViewHolder(rootView)
                }
            }
        }
        fun bind(image: Image,listener: (Image) -> Unit){
            rootView.setOnClickListener {
                listener(image)
            }
            image.ImageURI?.let {
                val context = imageView.context
                Glide.with(imageView.context)
                    .load(it)
                    .placeholder(R.drawable.undraw_family_6gj8_1)
                    .into(imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumImageItemViewHolder {
        return AlbumImageItemViewHolder.inflate(parent,viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)return 0
        else return 1
    }


    override fun onBindViewHolder(holder: AlbumImageItemViewHolder, position: Int) {
        holder.bind(getItem(position),listener)
    }
}