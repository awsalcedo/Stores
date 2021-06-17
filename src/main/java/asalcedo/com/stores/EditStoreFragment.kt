package asalcedo.com.stores

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import asalcedo.com.stores.databinding.FragmentEditStoreBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.HttpURLConnection

// Fragmento para  agregar o editar una tienda

class EditStoreFragment : Fragment() {

    private lateinit var mBinding: FragmentEditStoreBinding

    private var mActivity: MainActivity? = null

    //Variables usadas para distinguir si vamos a crear o si estamos editando
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    //La vista se ha creado por completo, es el lugar ideal para manipular todos los elementos, es decir que ninguna referecia a la vista será nula
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Obtener el argumento pasado desde la activity MainActivty, un valor por defecto 0 en caso de que no lo encuentre
        val id = arguments?.getLong(getString(R.string.key_id), 0)

        // Significa que el parámetro se pasó correctamente y que venimos de seleccionar un elemento en la actividad, es decir vamos a editar
        if (id != null && id != 0L) {
            mIsEditMode = true
            //Conseguir el resto de las propiedades de la tienda mediante el DAO
            getStore(id)

        } else { // Modo creación de una tienda
            mIsEditMode = false
            //Para la creación debemos tener un objeto inicializado para luego almacenar sus valores con los de los textEditInput
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }

        setupActionBar()

        setupTextFields()

    }

    private fun setupActionBar() {
        //Conseguir la actividad en la cual está alojada este fragmento y le realizamos un cast
        mActivity = activity as? MainActivity

        //Mostrar una flecha de retroceso en la parte de arriba
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Darle un título a la barra
        mActivity?.supportActionBar?.title =
            if (mIsEditMode) getString(R.string.edit_store_title_edit) else getString(R.string.edit_store_title_add)

        //Para mostrar el menu, hay que decirle que tenga acceso al menu que se mostrará en el action bar
        // y como segunda parte se debe sobreescribir el método onCreateOpionsMenu
        setHasOptionsMenu(true)
    }

    private fun setupTextFields() {
        //Cuando colcoquemos una url en la caja de texto se muestre la imagen en el ImageView
        /*
        mBinding.etPhotoUrl.addTextChangedListener {

        }
         */

        /*
        //Después de que el texto haya sido cambiado
        mBinding.etName.addTextChangedListener {
            validateFields(mBinding.tilName)
        }

        mBinding.etPhone.addTextChangedListener {
            validateFields(mBinding.tilPhone)
        }

        mBinding.etPhotoUrl.addTextChangedListener {
            validateFields(mBinding.tilPhotoUrl)
            loadImage(
                it.toString().trim()
            ) //lo ponemos aquí porque con el etPhotoUrl llamamos dos veces al addTextChangeListener
        }

         */

        //Optimizamos el código usando with ya que usamos varias veces el mBinding
        with(mBinding) {
            //Después de que el texto haya sido cambiado
            etName.addTextChangedListener {
                validateFields(tilName)
            }

            etPhone.addTextChangedListener {
                validateFields(tilPhone)
            }

            etPhotoUrl.addTextChangedListener {
                validateFields(tilPhotoUrl)
                loadImage(
                    it.toString().trim()
                ) //lo ponemos aquí porque con el etPhotoUrl llamamos dos veces al addTextChangeListener
            }
        }
    }


    private fun loadImage(url: String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(mBinding.imPhoto)
    }

    private fun getStore(id: Long) {
        //Con la ayuda de anko vamos a crear la consulta en segundo plano
        doAsync {
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            uiThread {
                //cuando la consulta termine ya podemos rellenar los textInputEditext de la vista a las propiedades del objeto StoreEntity recién consultado
                if (mStoreEntity != null) setUIStore(mStoreEntity!!)
            }
        }
    }

    private fun setUIStore(storeEntity: StoreEntity) {
        //Con la función de alcance with
        with(mBinding) {
            //etName.text = storeEntity.name //Nos da un error porque espera un tipo Editable para solucionar esto lo hacemos con setText()
            etName.setText(storeEntity.name)
            //etPhone.text = Editable.Factory.getInstance().newEditable(storeEntity.phone) // otra forma de asignar un string a un editable
            //Usando las extensiones de Kotlin
            etPhone.text = storeEntity.phone.editable()
            etWebsite.setText(storeEntity.website)
            etPhotoUrl.setText(storeEntity.photoUrl)
            //Se comenta porque es repetitivo ya que se carga en el listener addTextChangedListener que está más arriba
            /*Glide.with(activity!!)
                .load(storeEntity.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imPhoto)*/
        }
    }

    //Usnado extensiones de Kotlin
    private fun String.editable(): Editable =
        Editable.Factory.getInstance().newEditable(this) // this significa el string

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //Inflar el xml del menu
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save -> {
                //Queremos que almacene los datos de la tienda ya sea para crearla o editarla

                if (mStoreEntity != null && validateFields(
                        mBinding.tilPhotoUrl,
                        mBinding.tilPhone,
                        mBinding.tilName
                    )
                ) {
                    /*
                val store = StoreEntity(name = mBinding.etName.text.toString().trim(),
                                        phone = mBinding.etPhone.text.toString().trim(),
                                        website = mBinding.etWebsite.text.toString().trim(),
                                        photoUrl = mBinding.etPhotoUrl.text.toString().trim())*/
                    with(mStoreEntity!!) {
                        name = mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        website = mBinding.etWebsite.text.toString().trim()
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                    }

                    //Usamos anko
                    doAsync {
                        //Almaceno el id con el que se guardó en la BDD para cuando
                        //actualicemos la vista principal podamos actualizar la parte
                        //de favoritos
                        //store.id = StoreApplication.database.storeDao().addStore(store)
                        if (mIsEditMode) StoreApplication.database.storeDao()
                            .updateStore(mStoreEntity!!)
                        else mStoreEntity!!.id =
                            StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        uiThread {

                            hideKeyboard()

                            if (mIsEditMode) {
                                mActivity?.updateStore(mStoreEntity!!)
                                Snackbar.make(
                                    mBinding.root,
                                    R.string.edit_store_message_update_success,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {

                                //Notificar a nuestra vista principal MainActivty que se ha creado una nueva tienda
                                mActivity?.addStore(mStoreEntity!!)

                                //Quitamos el Snackbar para tener mayor control con un Toast ya que el snackbar se solapa con el floatingActionButtom
                                //Snackbar.make(mBinding.root, getString(R.string.edit_store_message_save_success), Snackbar.LENGTH_SHORT).show()
                                Toast.makeText(
                                    mActivity,
                                    getString(R.string.edit_store_message_save_success),
                                    Toast.LENGTH_SHORT
                                ).show()

                                //Al guardar la tienda vemos que los datos en los campos se mantienen, si queremos borrarlos existen dos caminos:
                                //1) Limpiar todos los campos
                                //2) Retornar a la actividad, es decir destruir el fragmento, esta es la más recomendada
                                mActivity?.onBackPressed()
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return super.onOptionsItemSelected(item)
    }

    //Sobrecarga para optimizar código repetitivo

    //Para validar hay que hacerlo de abajo hacia arriba, es decir desde el último campo requerido hasta el primer campo requerido
    //porque luego tenemos que ponerle el foco automáticamente, pero si todos los campos requeridos no son llenados, esto hará
    // que el foco se vaya al primer campo requerido, por eso se hacer la validación de abajo hacia arriba.
    private fun validateFields(vararg textFields: TextInputLayout): Boolean {
        var isValid = true

        for (textField in textFields) {
            if (textField.editText?.text.toString().trim().isEmpty()) {
                textField.error = getString(R.string.helper_required)
                isValid = false
            } else textField.error =
                null //esto es para cuando ya no haya error quite la marca roja alrededor del campo de texto
        }

        if (!isValid) Snackbar.make(
            mBinding.root,
            R.string.edit_store_message_valid,
            Snackbar.LENGTH_SHORT
        ).show()

        return isValid
    }

    private fun validateFields(): Boolean {
        var isValid = true

        //Para validar hay que hacerlo de abajo hacia arriba, es decir desde el último campo requerido hasta el primer campo requerido
        //porque luego tenemos que ponerle el foco automáticamente, pero si todos los campos requeridos no son llenados, esto hará
        // que el foco se vaya al primer campo requerido, por eso se hacer la validación de abajo hacia arriba.
        if (mBinding.etPhotoUrl.text.toString().trim().isEmpty()) {
            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
            //Permite colcar el foco sobre el campo, es decir que parpadee el cursor sobre el campo
            mBinding.etPhotoUrl.requestFocus()
            isValid = false
        }

        if (mBinding.etPhone.text.toString().trim().isEmpty()) {
            mBinding.tilPhone.error = getString(R.string.helper_required)
            //Permite colcar el foco sobre el campo, es decir que parpadee el cursor sobre el campo
            mBinding.etPhone.requestFocus()
            isValid = false
        }

        if (mBinding.etName.text.toString().trim().isEmpty()) {
            mBinding.tilName.error = getString(R.string.helper_required)
            //Permite colcar el foco sobre el campo, es decir que parpadee el cursor sobre el campo
            mBinding.etName.requestFocus()
            isValid = false
        }



        return isValid
    }

    //Oculta el teclado cuando se guarda la tienda
    private fun hideKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) {
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)
        }
    }

    //Se ejecuta antes de onDestroy, es donde se desvincula nuestra vista
    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        //Ocultamos la flecha de retroceso
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        //El método hideFab  se encuentra en la interfaz MainAux, esto nos sirve para comunicarnos
        //desde este fragmento con la actividad
        mActivity?.hideFab(true)
        //Desvinculamos
        setHasOptionsMenu(false)
        super.onDestroy()
    }

}