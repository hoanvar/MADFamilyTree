// SupabaseClientProvider.kt
import android.content.Context
import android.net.Uri
import android.util.Log
import com.dung.madfamilytree.dtos.ImageDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClientProvider {

    val client: SupabaseClient =
        createSupabaseClient(
            supabaseUrl = "https://skmefiekzbtoqmbtpdic.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrbWVmaWVremJ0b3FtYnRwZGljIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUxMTUzMDQsImV4cCI6MjA2MDY5MTMwNH0.0CutwdVpKvQdZnnndm8MoQrmfQ1Zdq97j93jFadvjNE"
        ) {
            install(Storage)
        }

    suspend fun uploadImageFromUri(context: Context, uri: Uri):String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return null
            inputStream.close()

            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val path = "uploads/$fileName"

            val bucket = client.storage.from("image")
            bucket.upload(path, bytes)
            return bucket.publicUrl(path)

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun extractFilePathFromUrl(url: String): String? {
        val storageMarker = "/object/public/image/"
        val index = url.indexOf(storageMarker)
        return if (index != -1) {
            url.substring(index + storageMarker.length)
        } else {
            null // Not a valid storage public URL
        }
    }
    suspend fun deleteImage(imageDTO: ImageDTO){
        Log.d("url",extractFilePathFromUrl(imageDTO.url)!!)
        client.storage.from("image").delete(extractFilePathFromUrl(imageDTO.url)!!)
    }


}
