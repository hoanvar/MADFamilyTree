package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.models.Image

class AlbumImageItemAdapter : RecyclerView.Adapter<AlbumImageItemAdapter.AlbumImageItemViewHolder>() {
    class AlbumImageItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView){
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
    }
    var data = listOf<Image>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumImageItemViewHolder {
        return AlbumImageItemViewHolder.inflate(parent,viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)return 0
        else return 1
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AlbumImageItemViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}