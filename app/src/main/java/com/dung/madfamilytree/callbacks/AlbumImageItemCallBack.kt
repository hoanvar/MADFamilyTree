package com.dung.madfamilytree.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.models.Image

class AlbumImageItemCallBack : DiffUtil.ItemCallback<ImageDTO>(){
    override fun areItemsTheSame(oldItem: ImageDTO, newItem: ImageDTO): Boolean {
        return oldItem.url.equals(newItem.url)
    }

    override fun areContentsTheSame(oldItem: ImageDTO, newItem: ImageDTO): Boolean {
        return oldItem.equals(newItem)
    }
}