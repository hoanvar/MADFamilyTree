package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.AccountItemBinding
import com.dung.madfamilytree.dtos.AccountDTO

class AccountItemAdapter(val clickListener:(id: String,userName: String)->Unit) : RecyclerView.Adapter<AccountItemAdapter.AccountItemViewHolder>() {
    var data = listOf<AccountDTO>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    class AccountItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun inflateFrom(parent: ViewGroup, viewType: Int):AccountItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.account_item,parent,false)
                return AccountItemViewHolder(view)
            }
        }
        fun bind(accountDTO: AccountDTO,clickListener: (id: String,userName: String) -> Unit){
//            binding.userName.setText(accountDTO.username)
            view.findViewById<TextView>(R.id.user_name).text = accountDTO.username
            view.setOnClickListener {
                clickListener(accountDTO.id,accountDTO.username)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountItemViewHolder {
        return AccountItemViewHolder.inflateFrom(parent,viewType)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AccountItemViewHolder, position: Int) {
        holder.bind(data[position],clickListener)
    }
}