package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiEnhancedDetails
import com.example.ai.AiProductStudio
import com.example.ai.GeneratedProductShot
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ShopyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppMode {
    CUSTOMER,
    SELLER,
    ADMIN
}

enum class CustomerTab {
    HOME,
    CATEGORIES,
    CART,
    ORDERS,
    PROFILE
}

enum class SortOption {
    POPULAR,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    DISCOUNT,
    RATING
}

data class CartUiItem(
    val cartItem: CartItem,
    val product: Product
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ShopyViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = ShopyRepository(db, viewModelScope)

    // Navigation and Mode
    private val _currentMode = MutableStateFlow(AppMode.CUSTOMER)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _customerTab = MutableStateFlow(CustomerTab.HOME)
    val customerTab: StateFlow<CustomerTab> = _customerTab.asStateFlow()

    // Current logged in user
    val currentUser: StateFlow<UserAccount> = repository.currentUser

    // Customer state
    val allApprovedProducts: StateFlow<List<Product>> = repository.getApprovedProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.POPULAR)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        allApprovedProducts,
        _searchQuery,
        _selectedCategory,
        _selectedSort
    ) { products, query, cat, sort ->
        var list = products
        if (!query.isBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        if (cat != null && cat != "All") {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }
        when (sort) {
            SortOption.POPULAR -> list.sortedByDescending { it.reviewCount }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.sellingPrice }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.sellingPrice }
            SortOption.DISCOUNT -> list.sortedByDescending { it.discountPercent }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Product Detail
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // Wishlist
    val wishlistItems: StateFlow<List<WishlistItem>> = currentUser.flatMapLatest { user ->
        repository.getWishlistItems(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val wishlistProductIds: StateFlow<Set<String>> = wishlistItems.map { list ->
        list.map { it.productId }.toSet()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    // Cart
    val cartItems: StateFlow<List<CartItem>> = currentUser.flatMapLatest { user ->
        repository.getCartItems(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cartUiItems: StateFlow<List<CartUiItem>> = combine(
        cartItems,
        allApprovedProducts
    ) { items, products ->
        val productMap = products.associateBy { it.id }
        items.mapNotNull { cartItem ->
            productMap[cartItem.productId]?.let { prod ->
                CartUiItem(cartItem = cartItem, product = prod)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Addresses
    val userAddresses: StateFlow<List<Address>> = currentUser.flatMapLatest { user ->
        repository.getAddresses(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress: StateFlow<Address?> = _selectedAddress.asStateFlow()

    // Customer Orders
    val customerOrders: StateFlow<List<Order>> = currentUser.flatMapLatest { user ->
        repository.getOrdersForCustomer(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Tracking Order Detail
    private val _trackingOrder = MutableStateFlow<Order?>(null)
    val trackingOrder: StateFlow<Order?> = _trackingOrder.asStateFlow()

    // Notifications
    val notifications: StateFlow<List<NotificationItem>> = currentUser.flatMapLatest { user ->
        repository.getNotifications(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Seller State
    val sellerProducts: StateFlow<List<Product>> = currentUser.flatMapLatest { user ->
        repository.getProductsBySeller(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sellerOrders: StateFlow<List<Order>> = currentUser.flatMapLatest { user ->
        repository.getOrdersForSeller(user.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Admin State
    val adminAllProducts: StateFlow<List<Product>> = repository.getAllProductsAdmin()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val adminAllSellers: StateFlow<List<UserAccount>> = repository.getAllSellers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val adminAllOrders: StateFlow<List<Order>> = repository.getAllOrdersAdmin()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // AI Product Studio state
    private val _aiGeneratedShots = MutableStateFlow<List<GeneratedProductShot>>(emptyList())
    val aiGeneratedShots: StateFlow<List<GeneratedProductShot>> = _aiGeneratedShots.asStateFlow()

    private val _isGeneratingAiShots = MutableStateFlow(false)
    val isGeneratingAiShots: StateFlow<Boolean> = _isGeneratingAiShots.asStateFlow()

    private val _aiCopyDetails = MutableStateFlow<AiEnhancedDetails?>(null)
    val aiCopyDetails: StateFlow<AiEnhancedDetails?> = _aiCopyDetails.asStateFlow()

    private val _isGeneratingAiCopy = MutableStateFlow(false)
    val isGeneratingAiCopy: StateFlow<Boolean> = _isGeneratingAiCopy.asStateFlow()

    init {
        viewModelScope.launch {
            userAddresses.collect { list ->
                if (_selectedAddress.value == null && list.isNotEmpty()) {
                    _selectedAddress.value = list.firstOrNull { it.isDefault } ?: list.first()
                }
            }
        }
    }

    // Setters & Actions
    fun setMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun setCustomerTab(tab: CustomerTab) {
        _customerTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSort(sort: SortOption) {
        _selectedSort.value = sort
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun selectAddress(address: Address) {
        _selectedAddress.value = address
    }

    fun viewOrderTracking(order: Order?) {
        _trackingOrder.value = order
    }

    fun toggleWishlist(productId: String) {
        val isWish = wishlistProductIds.value.contains(productId)
        viewModelScope.launch {
            repository.toggleWishlist(currentUser.value.id, productId, isWish)
        }
    }

    fun addToCart(product: Product, size: String, color: String, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(
                userId = currentUser.value.id,
                productId = product.id,
                size = size,
                color = color,
                quantity = quantity
            )
        }
    }

    fun updateCartQuantity(item: CartItem, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(item, newQty)
        }
    }

    fun removeCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.removeCartItem(item)
        }
    }

    fun placeOrder(
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        paymentMethod: String,
        address: Address,
        onSuccess: (Order) -> Unit
    ) {
        viewModelScope.launch {
            val orderNum = "SHP-${(10000..99999).random()}"
            val total = product.sellingPrice * quantity
            val newOrder = Order(
                id = "ord_${UUID.randomUUID().toString().take(8)}",
                orderNumber = orderNum,
                customerId = currentUser.value.id,
                customerName = address.fullName,
                customerPhone = address.phone,
                sellerId = product.sellerId,
                productId = product.id,
                productName = product.name,
                productImage = product.getImageList().firstOrNull() ?: "",
                quantity = quantity,
                selectedSize = size,
                selectedColor = color,
                itemPrice = product.sellingPrice,
                totalPrice = total,
                deliveryAddress = address.formatted(),
                paymentMethod = paymentMethod,
                status = OrderStatus.PLACED,
                trackingId = "SPY${(1000000..9999999).random()}IN",
                courierName = "Shopy Express"
            )
            repository.placeOrder(newOrder)
            // If item was in cart, remove it
            repository.getCartItems(currentUser.value.id).firstOrNull()?.find { it.productId == product.id }?.let {
                repository.removeCartItem(it)
            }
            onSuccess(newOrder)
        }
    }

    fun addNewAddress(
        fullName: String,
        phone: String,
        house: String,
        area: String,
        city: String,
        state: String,
        pincode: String
    ) {
        viewModelScope.launch {
            val addr = Address(
                id = "addr_${UUID.randomUUID().toString().take(8)}",
                userId = currentUser.value.id,
                fullName = fullName,
                phone = phone,
                houseNoStreet = house,
                areaColony = area,
                city = city,
                state = state,
                pincode = pincode,
                isDefault = userAddresses.value.isEmpty()
            )
            repository.addAddress(addr)
            _selectedAddress.value = addr
        }
    }

    fun login(phone: String, name: String, role: UserRole, businessName: String = "") {
        viewModelScope.launch {
            repository.loginOrRegister(phone, name, role, businessName)
        }
    }

    fun switchUserAccount(user: UserAccount) {
        repository.switchUser(user)
    }

    // Seller Actions
    fun createSellerProduct(
        name: String,
        category: String,
        originalPrice: Double,
        discountPercent: Int,
        stock: Int,
        sizes: String,
        colors: String,
        description: String,
        selectedImageUris: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val selling = originalPrice * (100 - discountPercent) / 100.0
            val imagesString = if (selectedImageUris.isNotEmpty()) {
                selectedImageUris.joinToString("|")
            } else {
                "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=700"
            }

            val product = Product(
                id = "prod_${UUID.randomUUID().toString().take(8)}",
                name = name,
                category = category,
                originalPrice = originalPrice,
                discountPercent = discountPercent,
                sellingPrice = selling,
                stock = stock,
                sizes = sizes.ifBlank { "Free Size" },
                colors = colors.ifBlank { "Multi-Color" },
                description = description,
                imageUrls = imagesString,
                sellerId = currentUser.value.id,
                sellerName = currentUser.value.businessName.ifBlank { currentUser.value.name },
                rating = 4.5f,
                reviewCount = 1,
                isApproved = true // Auto-approve demo products for seamless testing
            )
            repository.addProduct(product)
            _aiGeneratedShots.value = emptyList()
            _aiCopyDetails.value = null
            onSuccess()
        }
    }

    fun updateSellerOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            val tracking = if (newStatus == OrderStatus.DISPATCHED) "SPY${(1000000..9999999).random()}IN" else ""
            repository.updateOrderStatus(orderId, newStatus, tracking)
        }
    }

    // AI Product Studio Actions
    fun processProductImageWithAi(sourceBitmap: Bitmap) {
        viewModelScope.launch {
            _isGeneratingAiShots.value = true
            val shots = AiProductStudio.generateStudioShots(getApplication(), sourceBitmap)
            _aiGeneratedShots.value = shots
            _isGeneratingAiShots.value = false
        }
    }

    fun toggleShotSelection(shotId: String) {
        _aiGeneratedShots.value = _aiGeneratedShots.value.map {
            if (it.id == shotId) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun enhanceListingWithAi(productName: String, category: String, rawNotes: String) {
        viewModelScope.launch {
            _isGeneratingAiCopy.value = true
            val result = AiProductStudio.generateProductCopy(productName, category, rawNotes)
            _aiCopyDetails.value = result.getOrNull()
            _isGeneratingAiCopy.value = false
        }
    }

    // Admin Actions
    fun setProductApproval(productId: String, isApproved: Boolean) {
        viewModelScope.launch {
            repository.setProductApproval(productId, isApproved)
        }
    }

    fun setSellerVerification(sellerId: String, isVerified: Boolean) {
        viewModelScope.launch {
            repository.setSellerVerification(sellerId, isVerified)
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }
}
