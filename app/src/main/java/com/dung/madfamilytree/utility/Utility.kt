package com.dung.madfamilytree.utility

import SupabaseClientProvider
import com.dung.madfamilytree.dtos.AccountDTO
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.dtos.InvokingDTO
import com.dung.madfamilytree.dtos.NodeDTO
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.dtos.TreeDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp

object Utility {
    var db: FirebaseFirestore? = null
    var myProfileId: String? = null
    var accountId = "qeCGzYEwV5w7VYpWXtdn"
    var accountName = ""
    var treeId = ""
    var rootId = ""
//    var root_id = ""

    suspend fun addAccount(account: AccountDTO): String {
        return suspendCancellableCoroutine { cont ->
            db?.collection("Account")
                ?.add(account)
                ?.addOnSuccessListener { documentReference ->
                    cont.resume(documentReference.id)
                }
                ?.addOnFailureListener { exception ->
                    cont.resume("")
                }
        }
    }

    suspend fun getTreeName(): String? {
        val doc = db?.collection("Tree")?.document(treeId)?.get()?.await()
        return doc?.toObject(TreeDTO::class.java)?.tree_name
    }

    suspend fun getAlbum(): List<AlbumDTO> {
        return suspendCancellableCoroutine { cont ->
            db?.collection("Invoking")
                ?.whereEqualTo("account", db?.collection("Account")?.document(accountId))
                ?.get()
                ?.addOnSuccessListener { invokeResult ->
                    val tempAlbumList = mutableListOf(AlbumDTO(name = "Thêm mới Album"))
                    for (doc in invokeResult) {
                        val invoking = doc.toObject(InvokingDTO::class.java)
                        invoking.album?.get()
                            ?.addOnSuccessListener { albumSnapshot ->
                                db?.collection("Image")
                                    ?.whereEqualTo("album", invoking.album)
                                    ?.get()
                                    ?.addOnSuccessListener { result ->
                                        val album = albumSnapshot.toObject(AlbumDTO::class.java)
                                        album?.let {
                                            it.id = albumSnapshot.id
                                            if (!result.isEmpty) {
                                                it.thumbnail_img =
                                                    result.iterator().next().reference
                                            }
                                            tempAlbumList.add(it)
                                            if (tempAlbumList.size == invokeResult.size() + 1) {
                                                cont.resume(tempAlbumList)
                                            }
                                        }
                                    }
                            }
                    }
                    if(invokeResult.size() == 0){
                        cont.resume(tempAlbumList)
                    }
                }
        }
    }

    suspend fun deleteImageList(imageList: List<ImageDTO>,successListener:()->Unit) {
        for (image in imageList) {
            SupabaseClientProvider.deleteImage(image)
        }
        suspendCancellableCoroutine<Unit?> { cont ->
            db?.let {

                it.collection("Image")
                    .whereIn("url", imageList.map { image -> image.url })
                    .get()
                    .addOnSuccessListener { result ->
                        var i = 0
                        for (doc in result) {
                            val imageDTO = doc.toObject(ImageDTO::class.java)
                            doc.reference.delete()
                            i++
                            if (i == result.size()) {
                                cont.resume(null)
                            }
                        }
                        successListener()
                    }
            }
        }
    }

    suspend fun isEditable(accountId: String, albumId: String): Boolean {
        return suspendCancellableCoroutine { cont ->
            db?.let {
                it.collection("Invoking")
                    .whereEqualTo("account", it.collection("Account").document(accountId))
                    .whereEqualTo("album", it.collection("Album").document(albumId))
                    .get()
                    .addOnSuccessListener { invokingSnapshots ->
                        val invokingSnapshot = invokingSnapshots.iterator().next()
                        val invokingDTO = invokingSnapshot.toObject(InvokingDTO::class.java)
                        cont.resume(invokingDTO.editable)
                    }
            }
        }
    }


    suspend fun getTreeId(): String {
        val account = getAccountById()
        if(account != null && account.tree_id.isNotEmpty()){
            treeId = account.tree_id
            try {
                val document = db?.collection("Tree")
                    ?.document(treeId)
                    ?.get()
                    ?.await()
                
                if (document != null && document.exists()) {
                    val tree = document.toObject(TreeDTO::class.java)
                    tree?.let {
                        rootId = tree.id_root.toString()
                    }
                    return treeId
                }
            } catch (e: Exception) {
                Log.e("Utility", "Error getting tree document", e)
            }
        }
        return "false"
    }


    suspend fun getAccountById(): AccountDTO? {
        return suspendCancellableCoroutine { cont ->
            db?.collection("Account")
                ?.document(this.accountId)
                ?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val account = document.toObject(AccountDTO::class.java)
                        // Fetch profile name if id_profile exists
                        account?.let { acc ->
                            if (acc.id_profile.isNotEmpty()) {
                                db?.collection("Profile")
                                    ?.document(acc.id_profile)
                                    ?.get()
                                    ?.addOnSuccessListener { profileDoc ->
                                        if (profileDoc != null && profileDoc.exists()) {
                                            val profile = profileDoc.toObject(ProfileDTO::class.java)
                                            accountName = profile?.name ?: ""
                                        }
                                        cont.resume(account)
                                    }
                                    ?.addOnFailureListener {
                                        cont.resume(account)
                                    }
                            } else {
                                cont.resume(account)
                            }
                        } ?: cont.resume(null)
                    } else {
                        cont.resume(null)
                    }
                }
                ?.addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    suspend fun getProfileById(profile_id: String?): ProfileDTO? {
        if(profile_id == null){
            return null
        }
        return suspendCancellableCoroutine { cont ->
            db?.collection("Profile")
                ?.document(profile_id)
                ?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profile = document.toObject(ProfileDTO::class.java)
                        cont.resume(profile)
                    } else {
                        cont.resume(null)
                    }
                }
                ?.addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    suspend fun getProfilePair(id : String): Pair<ProfileDTO?, ProfileDTO?> {
        val pro1 = getProfileById(id)
        var pro2: ProfileDTO? = null
        val id2 = db?.collection("Node")
            ?.document(id)
            ?.get()
            ?.addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val node = document.toObject(NodeDTO::class.java)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (node != null) {
                            pro2 = getProfileById(node.id_profile)
                        }

                    }

                } else {

                }
            }
            ?.addOnFailureListener {

            }
        return Pair(pro1, pro2)
    }



    suspend fun getAllNodesFromDb(): List<NodeDTO> {
        return suspendCancellableCoroutine { cont ->
            val nodes = mutableListOf<NodeDTO>()
            db?.collection("Node")?.get()
                ?.addOnSuccessListener { documents ->
                    for (document in documents) {
                        val node = document.toObject(NodeDTO::class.java)
                        nodes.add(node)
                    }
                    cont.resume(nodes)
                }
                ?.addOnFailureListener { e ->
                    e.printStackTrace()
                    cont.resume(emptyList())
                }
        }
    }

    fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}


