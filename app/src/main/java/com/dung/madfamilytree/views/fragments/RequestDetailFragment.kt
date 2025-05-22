package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dung.madfamilytree.databinding.FragmentRequestDetailBinding
import com.dung.madfamilytree.dtos.LinkRequests
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.Timestamp

class RequestDetailFragment : Fragment() {

    private lateinit var binding: FragmentRequestDetailBinding
    private val args: RequestDetailFragmentArgs by navArgs()

    private lateinit var request: LinkRequests

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRequestDetailBinding.inflate(inflater, container, false)

        val message = "${args.fromName} đã gửi cho bạn 1 lời mời ghép nối"
        binding.tvRequestMessage.text = message

        // Ẩn nút chờ tải dữ liệu
        binding.btnAccept.isEnabled = false
        binding.btnDecline.isEnabled = false


        // Load dữ liệu yêu cầu
        Utility.db?.collection("LinkRequests")
            ?.document(args.requestId)
            ?.get()
            ?.addOnSuccessListener { doc ->
                request = doc.toObject(LinkRequests::class.java) ?: return@addOnSuccessListener

                // Kiểm tra bảo mật tối thiểu: chỉ người nhận mới được thao tác
                if (request.toId != Utility.accountId) {
                    Toast.makeText(requireContext(), "Bạn không có quyền xử lý yêu cầu này.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return@addOnSuccessListener
                }

                setupButtons()
                binding.btnAccept.isEnabled = true
                binding.btnDecline.isEnabled = true
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Không thể tải yêu cầu", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

        return binding.root
    }

    private fun setupButtons() {
        binding.btnAccept.setOnClickListener {
            updateRequestStatus("success") {
                mergeFamilyTrees(request.fromId, request.toId)
                sendResultNotification(request.fromId, "success")
                Toast.makeText(requireContext(), "Đã xác nhận liên kết", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        binding.btnDecline.setOnClickListener {
            updateRequestStatus("declined") {
                sendResultNotification(request.fromId, "declined")
                Toast.makeText(requireContext(), "Đã hủy lời mời", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun updateRequestStatus(status: String, onSuccess: () -> Unit) {
        Utility.db?.collection("LinkRequests")
            ?.document(args.requestId)
            ?.update("status", status)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendResultNotification(senderId: String, result: String) {
        val resultData = hashMapOf(
            "fromId" to senderId,
            "toId" to request.toId,
            "status" to result,
            "timestamp" to Timestamp.now()
        )
        Utility.db?.collection("LinkResults")?.add(resultData)
    }

    // Gộp cây gia phả: cập nhật treeId của toàn bộ profile toId sang treeId của fromId nếu khác nhau
    private fun mergeFamilyTrees(fromId: String, toId: String) {
        val profileRef = Utility.db?.collection("Profile")

        profileRef?.document(fromId)?.get()
            ?.addOnSuccessListener { fromDoc ->
                profileRef.document(toId).get()
                    .addOnSuccessListener { toDoc ->
                        val fromTreeId = fromDoc.getString("treeId")
                        val toTreeId = toDoc.getString("treeId")

                        if (fromTreeId != null && toTreeId != null && fromTreeId != toTreeId) {
                            // Cập nhật tất cả profile trong toTreeId sang fromTreeId
                            profileRef.whereEqualTo("treeId", toTreeId)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    val batch = Utility.db?.batch()
                                    for (doc in snapshot.documents) {
                                        batch?.update(doc.reference, "treeId", fromTreeId)
                                    }
                                    batch?.commit()?.addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Đã gộp cây gia phả",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else if (toTreeId == null && fromTreeId != null) {
                            profileRef.document(toId).update("treeId", fromTreeId)
                        }
                    }
            }
//    private fun mergeFamilyTrees(fromId: String, toId: String) {
//        val profileRef = Utility.db?.collection("Profile") ?: return
//
//        profileRef.document(fromId).get()
//            .addOnSuccessListener { fromDoc ->
//                profileRef.document(toId).get()
//                    .addOnSuccessListener { toDoc ->
//                        val fromTreeId = fromDoc.getString("treeId")
//                        val toTreeId = toDoc.getString("treeId")
//
//                        // Kiểm tra điều kiện merge
//                        if (fromTreeId != null && toTreeId != null && fromTreeId != toTreeId) {
//                            profileRef.whereEqualTo("treeId", toTreeId).get()
//                                .addOnSuccessListener { snapshot ->
//                                    if (snapshot.isEmpty) {
//                                        Toast.makeText(context, "Không tìm thấy profile nào để gộp", Toast.LENGTH_SHORT).show()
//                                        return@addOnSuccessListener
//                                    }
//
//                                    val batch = Utility.db?.batch()
//                                    for (doc in snapshot.documents) {
//                                        batch?.update(doc.reference, "treeId", fromTreeId)
//                                    }
//
//                                    batch?.commit()
//                                        ?.addOnSuccessListener {
//                                            Toast.makeText(context, "Đã gộp cây gia phả thành công", Toast.LENGTH_SHORT).show()
//                                        }
//                                        ?.addOnFailureListener {
//                                            Toast.makeText(context, "Lỗi khi gộp cây gia phả", Toast.LENGTH_SHORT).show()
//                                        }
//                                }
//                        } else if (toTreeId == null && fromTreeId != null) {
//                            // Nếu profile to chưa có treeId, gán nó vào fromTreeId
//                            profileRef.document(toId).update("treeId", fromTreeId)
//                                .addOnSuccessListener {
//                                    Toast.makeText(context, "Đã thêm profile vào cây gia phả", Toast.LENGTH_SHORT).show()
//                                }
//                                .addOnFailureListener {
//                                    Toast.makeText(context, "Không thể cập nhật treeId cho profile", Toast.LENGTH_SHORT).show()
//                                }
//                        } else {
//                            Toast.makeText(context, "Hai profile đã thuộc cùng một cây gia phả", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(context, "Không thể tải thông tin profile toId", Toast.LENGTH_SHORT).show()
//                    }
//            }
//            .addOnFailureListener {
//                Toast.makeText(context, "Không thể tải thông tin profile fromId", Toast.LENGTH_SHORT).show()
//            }
//    }
    }
}

