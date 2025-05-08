package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.models.Album
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

class AlbumItemAdapter(val listener: (position: Int) -> Unit) :
    RecyclerView.Adapter<AlbumItemAdapter.AlbumItemViewHolder>() {
    var data = listOf<AlbumDTO>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class AlbumItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        private val imageView: ImageView = rootView.findViewById(R.id.image)
        private val albumName: TextView = rootView.findViewById(R.id.album_name)

        companion object {
            fun inflateFrom(parent: ViewGroup, viewType: Int): AlbumItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                if (viewType == 0) {
                    val view = layoutInflater.inflate(R.layout.add_new_album, parent, false)
                    return AlbumItemViewHolder(view)
                } else {
                    val view = layoutInflater.inflate(R.layout.album_item, parent, false)
                    return AlbumItemViewHolder(view)
                }
            }
        }

        fun bind(position: Int, data: AlbumDTO, listener: (position: Int) -> Unit) {
            albumName.text = data.name
            data.thumbnail_img?.let {
                it.get()
                    .addOnSuccessListener { imageSnapshot ->
                        val image = imageSnapshot.toObject(ImageDTO::class.java)
                        Glide.with(imageView.context)
                            .load(image?.url)
                            .placeholder(R.drawable.undraw_mobile_login_4ntr_1)
                            .into(imageView)
                    }

            }
            rootView.setOnClickListener {
                listener(position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 0
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumItemViewHolder {
        return AlbumItemViewHolder.inflateFrom(parent, viewType)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AlbumItemViewHolder, position: Int) {
        holder.bind(position, data[position], listener)
    }
}