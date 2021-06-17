package asalcedo.com.stores

import androidx.room.*

@Dao
interface StoreDao {
    @Query(value = "SELECT * FROM StoreEntity")
    fun getAllStores(): MutableList<StoreEntity>

    @Query(value = "SELECT * FROM StoreEntity WHERE id = :id")
    fun getStoreById(id: Long): StoreEntity

    @Insert
    fun addStore(storeEntity: StoreEntity) : Long

    @Update
    fun updateStore(storeEntity: StoreEntity)

    @Delete
    fun deleteStore(storeEntity: StoreEntity)
}