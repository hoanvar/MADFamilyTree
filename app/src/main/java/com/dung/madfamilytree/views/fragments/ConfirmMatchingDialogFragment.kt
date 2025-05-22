package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.dung.madfamilytree.R

class ConfirmMatchingDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(): ConfirmMatchingDialogFragment {
            return ConfirmMatchingDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.dialog_confirm_matching, container, false)

        val returnButton = view.findViewById<AppCompatButton>(R.id.showResultBtn)
        returnButton.setOnClickListener {
            dismiss() // Đóng dialog
            // Optional: chuyển sang màn hình chính
            // startActivity(Intent(activity, HomeActivity::class.java))
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
