package asalcedo.com.stores
/**
 * Interfaz usada para comunicar un fragmento con una actividad
 * */

interface MainAux {
    fun hideFab(isVisible: Boolean = false)
    fun addStore(storeEntity: StoreEntity)
    fun updateStore(storeEntity: StoreEntity)
}