package com.dung.madfamilytree.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.dung.madfamilytree.models.Image

class AlbumImageItemCallBack : DiffUtil.ItemCallback<Image>(){
    override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
        return oldItem.ImageURI?.equals(newItem.ImageURI) ?: false
    }

    override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
        return oldItem.ImageURI?.equals(newItem.ImageURI) ?: false
    }
}