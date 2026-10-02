package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.SampleData
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ShopyRepository(
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    private val productDao = db.productDao()
    private val userDao = db.userDao()
    private val cartDao = db.cartDao()
    private val wishlistDao = db.wishlistDao()
    private val addressDao = db.addressDao()
    private val orderDao = db.orderDao()
    private val notificationDao = db.notificationDao()

    private val _currentUser = MutableStateFlow(SampleData.sampleUsers.first())
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            // Verify if products table is populated; if empty, seed immediately
            val firstList = productDao.getAllApprovedProducts().firstOrNull()
            if (firstList.isNullOrEmpty()) {
                AppDatabase.populateInitialData(db)
            }
        }
    }

    fun switchUser(user: UserAccount) {
        _currentUser.value = user
    }

    suspend fun loginOrRegister(phone: String, name: String, role: UserRole, businessName: String = ""): UserAccount {
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) {
            _currentUser.value = existing
            return existing
        }
        val newUser = UserAccount(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            phone = phone,
            name = if (name.isNotBlank()) name else "User $phone",
            role = role,
            businessName = businessName,
            isVerified = role != UserRole.SELLER // Auto-verify customers, sellers need admin verification
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser
        return newUser
    }

    // Products
    fun getApprovedProducts(): Flow<List<Product>> = productDao.getAllApprovedProducts()
    fun getAllProductsAdmin(): Flow<List<Product>> = productDao.getAllProductsAdmin()
    fun getProductsBySeller(sellerId: String): Flow<List<Product>> = productDao.getProductsBySeller(sellerId)
    fun getProductById(id: String): Flow<Product?> = productDao.getProductById(id)

    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun setProductApproval(id: String, isApproved: Boolean) {
        productDao.setProductApproval(id, isApproved)
    }

    // Cart
    fun getCartItems(userId: String): Flow<List<CartItem>> = cartDao.getCartItems(userId)

    suspend fun addToCart(userId: String, productId: String, size: String, color: String, quantity: Int = 1) {
        val item = CartItem(
            id = "cart_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            productId = productId,
            quantity = quantity,
            selectedSize = size,
            selectedColor = color
        )
        cartDao.insertCartItem(item)
    }

    suspend fun updateCartQuantity(item: CartItem, newQty: Int) {
        if (newQty <= 0) {
            cartDao.deleteCartItem(item)
        } else {
            cartDao.updateCartItem(item.copy(quantity = newQty))
        }
    }

    suspend fun removeCartItem(item: CartItem) {
        cartDao.deleteCartItem(item)
    }

    suspend fun clearCart(userId: String) {
        cartDao.clearCart(userId)
    }

    // Wishlist
    fun getWishlistItems(userId: String): Flow<List<WishlistItem>> = wishlistDao.getWishlistItems(userId)

    suspend fun toggleWishlist(userId: String, productId: String, isWishlisted: Boolean) {
        if (isWishlisted) {
            wishlistDao.deleteWishlist(userId, productId)
        } else {
            wishlistDao.insertWishlist(
                WishlistItem(
                    id = "wish_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    productId = productId
                )
            )
        }
    }

    // Addresses
    fun getAddresses(userId: String): Flow<List<Address>> = addressDao.getAddressesForUser(userId)

    suspend fun addAddress(address: Address) {
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(address: Address) {
        addressDao.deleteAddress(address)
    }

    // Orders
    fun getOrdersForCustomer(customerId: String): Flow<List<Order>> = orderDao.getOrdersForCustomer(customerId)
    fun getOrdersForSeller(sellerId: String): Flow<List<Order>> = orderDao.getOrdersForSeller(sellerId)
    fun getAllOrdersAdmin(): Flow<List<Order>> = orderDao.getAllOrdersAdmin()
    fun getOrderById(id: String): Flow<Order?> = orderDao.getOrderById(id)

    suspend fun placeOrder(order: Order) {
        orderDao.insertOrder(order)
        // Also add order confirmation notification
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = order.customerId,
                title = "Order Confirmed! 🎉",
                message = "Your order #${order.orderNumber} for ${order.productName} has been received.",
                type = "ORDER"
            )
        )
        // Notify seller
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = order.sellerId,
                title = "New Order Received! 🛍️",
                message = "New order #${order.orderNumber} placed for ${order.productName} (Qty: ${order.quantity}).",
                type = "SELLER"
            )
        )
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus, trackingId: String = "") {
        val updatedTime = System.currentTimeMillis()
        orderDao.updateOrderStatus(orderId, status, trackingId, updatedTime)
    }

    // Admin & Sellers
    fun getAllSellers(): Flow<List<UserAccount>> = userDao.getAllSellers()
    fun getAllUsers(): Flow<List<UserAccount>> = userDao.getAllUsers()

    suspend fun setSellerVerification(sellerId: String, isVerified: Boolean) {
        userDao.setSellerVerification(sellerId, isVerified)
    }

    // Notifications
    fun getNotifications(userId: String): Flow<List<NotificationItem>> = notificationDao.getNotificationsForUser(userId)

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }
}
