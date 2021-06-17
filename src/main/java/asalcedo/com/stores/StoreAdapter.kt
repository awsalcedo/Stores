package asalcedo.com.stores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import asalcedo.com.stores.databinding.ItemStoreBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class StoreAdapter(private var stores: MutableList<StoreEntity>, private val listener: OnClickListener): RecyclerView.Adapter<StoreAdapter.ViewHolder>() {

    private lateinit var mContext: Context

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemStoreBinding.bind(view)

        fun setListener(storeEntity: StoreEntity){

            with(binding.root){
                setOnClickListener { listener.onClick(storeEntity.id) }

                //Un click largo (setOnLongClickListener) para eliminar el item seleccionado
                setOnLongClickListener {
                    listener.onDeleteStore(storeEntity)
                    true
                }
            }

            binding.cbFavorite.setOnClickListener {
                listener.onFavoriteStore(storeEntity)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context

        val view = LayoutInflater.from(mContext).inflate(R.layout.item_store, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val store = stores[position]
        with(holder){
            setListener(store)
            binding.tvName.text = store.name
            binding.cbFavorite.isChecked = store.isFavorite
            Glide.with(mContext)
                .load(store.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imgPhoto)
        }
    }

    override fun getItemCount(): Int = stores.size

    fun add(storeEntity: StoreEntity) {
        //Mejora porque almacenamos si existe o no existe, es decir una tienda nueva debe ser añadida en caso de que no exista
        if(!stores.contains(storeEntity)){
            stores.add(storeEntity)
            //le notificamos al adaptador de manera puntual que insertamos una tienda, nos pide un índice, como es un elemento nuevo siempre se va
            //a ir al final
            notifyItemInserted(stores.size -1)
        }
        //stores.add(storeEntity)
        //notifyDataSetChanged() // Le indicamos al adaptador que refresque la vista del adaptador
    }

    fun setStores(stores: MutableList<StoreEntity>) {
        this.stores = stores // sustituir el arreglo con un arreglo que contiene las tiendas que el usuario va ingresando por pantalla
        notifyDataSetChanged()  //Notificar al adaptador que ha habido cambios
    }

    fun update(storeEntity: StoreEntity) {
        //Averiguar el indice en el que se encuentra esa tienda actualmente
        val index = stores.indexOf(storeEntity)
        if (index != -1){ // significa que encontró el registro
            stores.set(index, storeEntity)
            //Sólo vamos a refrescar el registro afectado
            notifyItemChanged(index)
        }
    }

    fun delete(storeEntity: StoreEntity){
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}