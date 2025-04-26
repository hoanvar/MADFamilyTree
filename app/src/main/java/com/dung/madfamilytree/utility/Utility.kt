package com.dung.madfamilytree.utility

import SupabaseClientProvider
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.dtos.InvokingDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object Utility {
    var db: FirebaseFirestore? = null
    var accountId = "qeCGzYEwV5w7VYpWXtdn"
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
                }
        }
    }

    suspend fun deleteImageList(imageList: List<ImageDTO>) {
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
                            if(i == result.size()){
                                cont.resume(null)
                            }
                        }
                    }
            }
        }
    }
}