package asalcedo.com.stores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Visibility
import asalcedo.com.stores.databinding.ActivityMainBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), OnClickListener, MainAux {

    private lateinit var mbinding: ActivityMainBinding

    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val mbinding = ActivityMainBinding.inflate(LayoutInflater.from(this)) //visible solo dentro el método onCreate
        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        /*
        mbinding.btnSave.setOnClickListener {
            val store = StoreEntity(name = mbinding.etName.text.toString().trim())

            //Insertar primero en la BDD, como no es correcto el uso de Room en el hilo principal, por esa razón se crea otro hilo
            Thread{
                StoreApplication.database.storeDao().addStore(store)
            }.start()


            mAdapter.add(store)
        }
         */



        //Lanzar el fragmento EditStoreFragment que permite añadir/editar una tienda
        mbinding.fab.setOnClickListener {
            launchEditFragment()
        }

        setupRecyclerView()


    }

    private fun launchEditFragment() {
        //Crear una instancia del fragmento que queremos lanzar
        var fragmento = EditStoreFragment()

        //Usar el FragmentManager es el que controla los fragmentos y el FragmentTransaction es el que dedice como se va a ejecutar
        var fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()

        //Configurar como es que queremos que salga nuestro fragmento
        //Hay que decirle en donde (el id del contenerdor de la actividad que se encuentra en el xml) y fragmento
        fragmentTransaction.add(R.id.containerMain, fragmento)

        //Para retroceder a la activity cuando pulsamos el botón back y destruir solo al fragmento y dejar viva la actividad, sino se hace así se sale de toda la activdad
        fragmentTransaction.addToBackStack(null)

        //Aplicar los cambios
        fragmentTransaction.commit()


        //Ocultar el floting action buttom
        //mbinding.fab.hide()

        //Como implementamos la interfaz para comunicar el fragmento con la actividad
        //y por defecto el parametro es false, por eso no le pasamos un parametro
        hideFab()

    }

    private fun setupRecyclerView() {
        mAdapter = StoreAdapter(mutableListOf(), this) //pasamos this porque la actividad ya está implementando la interfaz que creamos
        mGridLayout = GridLayoutManager(this,2)

        getStores()

        mbinding.recyclerView.apply {
            setHasFixedSize(true) //Indica que no va a cambiar el tamaño del item y que optimice los recursos
            layoutManager = mGridLayout
            adapter = mAdapter

        }
    }

    private fun getStores() {

        //Se hace uso de Anko para generar un subproceso, es decir no usar el hilo principal para cuando se realice la consulta a la BDD
        // esto nos servirá para actualizar el adaptador con las nuevas tiendas que vaya ingresando el usuario
        doAsync {
            val stores = StoreApplication.database.storeDao().getAllStores()
            //Cuando finaliza la consulta ahí se envía a setear el adaptador
            uiThread {
                mAdapter.setStores(stores)
            }
        }

    }

    /*
    * OnClickListener
    * */
    override fun onClick(storeEntity: StoreEntity) {
        TODO("Not yet implemented")
    }

    override fun onFavoriteStore(storeEntity: StoreEntity) {
        //Actualizamos el campo isFavorite en la entidad
        storeEntity.isFavorite = !storeEntity.isFavorite
        //Actualizamos en la BDD con la ayuda de Anko
        doAsync {
            StoreApplication.database.storeDao().updateStore(storeEntity)
            uiThread {
                mAdapter.update(storeEntity)
            }
        }
    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
        doAsync {
            StoreApplication.database.storeDao().deleteStore(storeEntity)
            uiThread {
                mAdapter.delete(storeEntity)
            }
        }
    }

    /*
    * Interfaz para comunicar Fragmento con la actividad
    * */

    override fun hideFab(isVisible: Boolean) {
        if(isVisible) mbinding.fab.show() else mbinding.fab.hide()
    }


}