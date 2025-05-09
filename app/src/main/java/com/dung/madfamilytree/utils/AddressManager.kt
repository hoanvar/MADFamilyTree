import com.dung.madfamilytree.dtos.District
import com.dung.madfamilytree.dtos.Province
import com.dung.madfamilytree.dtos.Ward
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class AddressManager {
    private var provinces: List<Province> = emptyList()
    
    suspend fun loadProvinces(): List<Province> = withContext(Dispatchers.IO) {
        if (provinces.isEmpty()) {
            val response = URL("https://provinces.open-api.vn/api/?depth=3").readText()
            val type = object : com.google.common.reflect.TypeToken<List<Province>>() {}.type
            provinces = Gson().fromJson(response, type)
        }
        provinces
    }

    fun getDistricts(provinceCode: Int): List<District> {
        return provinces.find { it.code == provinceCode }?.districts ?: emptyList()
    }

    fun getWards(provinceCode: Int, districtCode: Int): List<Ward> {
        return provinces.find { it.code == provinceCode }
            ?.districts?.find { it.code == districtCode }
            ?.wards ?: emptyList()
    }
} 