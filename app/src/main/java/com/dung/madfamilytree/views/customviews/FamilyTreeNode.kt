package com.dung.madfamilytree.views.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.dung.madfamilytree.R

class FamilyTreeNode : LinearLayout {
    constructor(context: Context) : super(context){
        setContent(R.layout.family_tree_node_male,context)
    }
    constructor(context: Context,attrs: AttributeSet) : super(context,attrs){
        setContent(R.layout.family_tree_node_male,context)
    }
    constructor(context: Context,attrs: AttributeSet,defStyleAttr: Int) : super(context,attrs,defStyleAttr)
    {
        setContent(R.layout.family_tree_node_male,context)
    }
    constructor(context: Context,attrs: AttributeSet,defStyleAttr: Int,defStyleRes: Int) : super(context,attrs,defStyleAttr,defStyleRes)
    {
        setContent(R.layout.family_tree_node_male,context)
    }
    fun setContent(layoutId: Int,context: Context){
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(layoutId,this,true)
    }

}