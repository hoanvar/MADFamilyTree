package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.models.Image

class ImageItemAdapter : RecyclerView.Adapter<ImageItemAdapter.ImageItemViewHolder>() {
    var data = listOf<Image>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    class ImageItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView){
        companion object{
            fun inflateFrom(parent: ViewGroup,viewType: Int):ImageItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)

                if(viewType == 0){
                    val view = layoutInflater.inflate(R.layout.add_new_image,parent,false)
                    return ImageItemViewHolder(view)
                }
                else {
                    val view = layoutInflater.inflate(R.layout.image_item,parent,false)
                    return ImageItemViewHolder(view)
                }
            }
        }
        fun bind(image: Image){

        }

    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)return 0
        else return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder.inflateFrom(parent,viewType)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.bind(data[position])
    }
}