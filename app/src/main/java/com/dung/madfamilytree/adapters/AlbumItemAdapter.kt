package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.models.Album

class AlbumItemAdapter(val listener:()->Unit) : RecyclerView.Adapter<AlbumItemAdapter.AlbumItemViewHolder>() {
    var data = listOf<Album>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class AlbumItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        companion object {
            fun inflateFrom(parent: ViewGroup, viewType: Int): AlbumItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                if (viewType == 0) {
                    val view = layoutInflater.inflate(R.layout.add_new_album,parent,false)
                    return AlbumItemViewHolder(view)
                }
                else {
                    val view = layoutInflater.inflate(R.layout.album_item,parent,false)
                    return AlbumItemViewHolder(view)
                }
            }
        }
        fun bind(data: Album,listener: ()->Unit){
            rootView.setOnClickListener{
                listener()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 0
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumItemViewHolder {
        return AlbumItemViewHolder.inflateFrom(parent,viewType)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AlbumItemViewHolder, position: Int) {
        holder.bind(data[position],listener)
    }
}