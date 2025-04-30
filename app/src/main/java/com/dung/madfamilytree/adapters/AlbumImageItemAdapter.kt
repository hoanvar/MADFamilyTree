package com.dung.madfamilytree.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.callbacks.AlbumImageItemCallBack
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.models.Image
import com.dung.madfamilytree.viewmodels.AlbumDetailViewModel
import com.dung.madfamilytree.viewmodels.SelectMode
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumImageItemAdapter(val listener: (ImageDTO) -> Unit, val viewModel: AlbumDetailViewModel) :
    ListAdapter<ImageDTO, AlbumImageItemAdapter.AlbumImageItemViewHolder>(AlbumImageItemCallBack()) {

    class AlbumImageItemViewHolder(val rootView: View, listener: (ImageDTO) -> Unit) :
        RecyclerView.ViewHolder(rootView) {


        private val selectOneGestureDetector =
            GestureDetector(rootView.context, object : GestureDetector.OnGestureListener {
                override fun onDown(p0: MotionEvent): Boolean {
                    Log.d("GESTURE", "DOWN")
                    return true
                }

                override fun onShowPress(p0: MotionEvent) {
                    Log.d("GESTURE", "SHOWPRESS")
                }

                override fun onSingleTapUp(p0: MotionEvent): Boolean {
                    Log.d("GESTURE", "SINGLETAPUP")
                    listener(currentImageDto!!)
                    return true
                }

                override fun onScroll(
                    p0: MotionEvent?,
                    p1: MotionEvent,
                    p2: Float,
                    p3: Float
                ): Boolean {
                    Log.d("GESTUER", "SCOLL")
                    return true
                }

                override fun onLongPress(p0: MotionEvent) {
                    Log.d("GESTURE", "LONGPRESS")

                    viewModel?.recycleMode?.value = SelectMode.MULTI

                }

                override fun onFling(
                    p0: MotionEvent?,
                    p1: MotionEvent,
                    p2: Float,
                    p3: Float
                ): Boolean {
                    Log.d("GESTURE", "FLING")
                    return true
                }
            })
                .apply {
                    setIsLongpressEnabled(true)
                    setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
                        override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
                            return true
                        }

                        override fun onDoubleTap(p0: MotionEvent): Boolean {
                            return true
                        }

                        override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
                            return true
                        }
                    })
                }
        val imageView = rootView.findViewById<ShapeableImageView>(R.id.image)

        companion object {
            fun clearSelectedImage(){
                selectedImageList.clear()
            }
            fun getSelectedImageList():List<ImageDTO>{
                return selectedImageList
            }
            private val selectedImageList = mutableListOf<ImageDTO>()
            var viewModel: AlbumDetailViewModel? = null
            var currentImageDto: ImageDTO? = null
            fun inflate(
                parent: ViewGroup,
                viewType: Int,
                listener: (ImageDTO) -> Unit,
                viewModel: AlbumDetailViewModel
            ): AlbumImageItemViewHolder {
                this.viewModel = viewModel
                val layoutInflater = LayoutInflater.from(parent.context)
                if (viewType == 0) {
                    val rootView =
                        layoutInflater.inflate(R.layout.add_new_album_image, parent, false)
                    return AlbumImageItemViewHolder(rootView, listener)
                } else {
                    val rootView = layoutInflater.inflate(R.layout.album_image_item, parent, false)
                    rootView.findViewById<CheckBox>(R.id.check_box).visibility = if(viewModel.recycleMode.value == SelectMode.ONE)View.INVISIBLE else View.VISIBLE
                    return AlbumImageItemViewHolder(rootView, listener)
                }
            }
        }

        fun bind(image: ImageDTO, listener: (ImageDTO) -> Unit) {
            if (!image.url.equals("") && !image.url.equals("holder")) {
                Glide.with(imageView.context)
                    .load(image.url)
                    .placeholder(R.drawable.undraw_family_6gj8_1)
                    .into(imageView)
            }
            if (!image.url.equals("")) {
                rootView.findViewById<CheckBox>(R.id.check_box).visibility =
                    if (viewModel?.recycleMode?.value == SelectMode.ONE) View.INVISIBLE else View.VISIBLE
                rootView.findViewById<CheckBox>(R.id.check_box).isChecked = false
            }

//            rootView.setOnClickListener {
//                listener(image)
//            }
            rootView.setOnTouchListener { view, motionEvent ->
                var result = false
                if (viewModel?.recycleMode?.value == SelectMode.ONE) {
                    currentImageDto = image
                    result = selectOneGestureDetector.onTouchEvent(motionEvent)
                } else {
                    if(motionEvent.action == MotionEvent.ACTION_DOWN){
                        rootView.findViewById<CheckBox>(R.id.check_box).isChecked = !rootView.findViewById<CheckBox>(R.id.check_box).isChecked
                        if(rootView.findViewById<CheckBox>(R.id.check_box).isChecked){
                            selectedImageList.add(image)
                        }
                        else {
                            selectedImageList.remove(image)
                        }
                    }
                }
                view.performClick()
                result
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumImageItemViewHolder {
        return AlbumImageItemViewHolder.inflate(parent, viewType, listener, viewModel)
    }

    override fun getItemViewType(position: Int): Int {
        if(getItem(position).url.equals(""))return 0
        else return 1
    }


    override fun onBindViewHolder(holder: AlbumImageItemViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}
