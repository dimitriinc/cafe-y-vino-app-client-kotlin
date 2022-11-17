package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.CanastaDao
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.asItemPedido
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.GiftToSend
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemPedido
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.PedidoMetaDoc
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.getCurrentDate
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.User
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * Repository for the operations on the menu items, stored in the "menu" collection, and also on the gifts, stored in the "regalos" collection
 */
class MenuDataRepository @Inject constructor(
    private val fStoreSource: FirebaseFirestoreSource,
    private val canastaDao: CanastaDao,
    private val fStorageSource: FirebaseStorageSource,
    private val fMessaging: FirebaseMessagingSource
) {

    /**
     * Gather the data to build a gift object that will be stored in the "pedidos" collection of the Firestore DB
     * Build an instance, and pass it to the Firestore source
     */
    fun storeGift(
        nombre: String,
        precio: Long,
        mesa: String,
        userId: String,
        user: String
    ) {
        val gift = GiftToSend(
            nombre = nombre,
            precio = precio,
            mesa = mesa,
            userId = userId,
            user = user,
            servido = false,
            timestamp = Timestamp(Date())
        )

        fStoreSource.storeGift(gift)
    }

    /**
     * Start observing the Canasta Room table with LiveData for the convenience of our canasta recycler view
     */
    fun getCanastaItems(): LiveData<List<ItemCanasta>> =
        canastaDao.getAllItems().asLiveData()

    /**
     * Insert an item to the Canasta Room table
     */
    suspend fun addProductToCanasta(item: ItemCanasta) {
        canastaDao.insert(item)
    }

    /**
     * Delete an item from the Canasta Room table
     */
    suspend fun removeProductFromCanasta(item: ItemCanasta) {
        canastaDao.deleteItem(item)
    }

    /**
     * Based on the path to the img resource in the FirebaseStorage, get a StorageReference instance
     * to use it later to load the img into a view with the Glide library
     */
    fun getImgReference(imgPath: String): StorageReference =
        fStorageSource.getImgReference(imgPath)

    /**
     * Prepares the necessary data to create a new pedido in the system
     * Provides the data to the fStore source to store it in the external db
     * Tells the fMessaging source to let the administrators know about a new pedido
     */
    suspend fun sendPedido(user: User, userId: String) {

        // get a canasta snapshot
        val canasta = canastaDao.getAllItems().first()

        // initialize a set that will represent the actual pedido
        val pedido = mutableSetOf<ItemPedido>()

        // create some metadata
        val currentDate = getCurrentDate()
        val metaDocId = Random.nextLong().toString()
        var servidoBarra = true
        var servidoCocina = true

        /**
         * convert canasta to pedido
         * the main difference is that in canasta each product is one entity, so it has the same products stored separately
         * in pedido all the items of a product stored in one instance, and the quantity is stores as the 'count' property
         * so to represent the pedido we use a set of unique objects, and we get the count value with a special query
         */
        canasta.forEach { canastaItem ->
            val itemCount = canastaDao.getItemsByName(canastaItem.name).size.toLong()
//            canastaDao.deleteItemsByName(canastaItem.name)
            pedido.add(canastaItem.asItemPedido(itemCount))
        }
        canastaDao.emptyCanasta()

        /**
         * the admin app can display one pedido in different ways: only kitchen products, only bar products, or all together
         * to help it display the items correctly we need to define the metadata values
         * if there is at least one bar item in the pedido, we set the value to false, the same for the kitchen products
         */
        pedido.forEach { pedidoItem ->
            if (pedidoItem.category == "barra") {
                servidoBarra = false
            } else {
                servidoCocina = false
            }
        }

        // create a document with metadata for the pedido
        val metaDoc = PedidoMetaDoc(
            mesa = user.mesa,
            servidoBarra = servidoBarra,
            servidoCocina = servidoCocina,
            user = user.nombre,
            userId = userId,
            timestamp = Timestamp(Date())
        )

        // send the prepared data to the fStore source to store everything externally
        fStoreSource.storePedido(
            metaDocId,
            pedido,
            metaDoc,
            currentDate
        )

        // tell the messaging service to let the administrators know about the new order
        fMessaging.sendPedidoMessage(
            metaDocId,
            currentDate,
            user.mesa,
            user.nombre
        )
    }

    /**
     * Retransmits the user's bill total cost to the UI
     * converting the value to the Double type for a better representation of the price on the screen
     */
    fun getCuentaTotalFlow(userId: String): Flow<Double?> =
        fStoreSource.getCuentaTotalFlow(userId).map {
            it?.toDouble()
        }

}