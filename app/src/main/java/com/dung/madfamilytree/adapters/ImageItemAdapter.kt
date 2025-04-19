package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.models.Image

class ImageItemAdapter(val listener: (Image?) -> Unit) :
    RecyclerView.Adapter<ImageItemAdapter.ImageItemViewHolder>() {
    var data = listOf<Image>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ImageItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        val imageView = rootView.findViewById<ImageView>(R.id.image_view)

        companion object {
            fun inflateFrom(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                if (viewType == 0) {
                    val view = layoutInflater.inflate(R.layout.add_new_image, parent, false)
                    return ImageItemViewHolder(view)
                } else {
                    val view = layoutInflater.inflate(R.layout.image_item, parent, false)
                    return ImageItemViewHolder(view)
                }
            }
        }

        fun bind(image: Image, listener: (image: Image?) -> Unit) {
            if (image.ImageURI != null) {
                rootView.findViewById<ImageView>(R.id.delete_btn)
                    .setOnClickListener {
                        listener(image)
                    }
            }
            else {
                rootView.setOnClickListener {
                    listener(null)
                }
            }
            image.ImageURI?.let {
                Glide.with(imageView.context)
                    .load(it)
                    .placeholder(R.drawable.undraw_family_6gj8_1)
                    .into(imageView)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 0
        else return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder.inflateFrom(parent, viewType)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.bind(data[position], listener)
    }
}