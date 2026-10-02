package com.example.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.ui.ShopyViewModel
import com.example.ui.SortOption
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShopyViewModel,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val wishlistIds by viewModel.wishlistProductIds.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Women Ethnic",
        "Men Fashion",
        "Electronics",
        "Footwear",
        "Jewellery & Bags",
        "Home & Living"
    )

    Column(modifier = modifier.fillMaxSize().background(ShopyBackground)) {
        // Search Bar Section
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ShopyOutlineVariant),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 0.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ShopyTextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Search kurtis, shirts, electronics, shoes...",
                            fontSize = 13.sp,
                            color = ShopyTextMuted
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("search_input")
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = ShopyTextSecondary)
                    }
                }
            }
        }

        // Horizontal Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = (selectedCategory == null && cat == "All") || (selectedCategory == cat)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (cat == "All") viewModel.selectCategory(null) else viewModel.selectCategory(cat)
                    },
                    label = {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ShopyPinkContainer,
                        selectedLabelColor = ShopyCrimson
                    ),
                    modifier = Modifier.testTag("cat_chip_$cat")
                )
            }
        }

        // Sort Bar & Total Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${products.size} Products available",
                fontSize = 12.sp,
                color = ShopyTextSecondary,
                fontWeight = FontWeight.Medium
            )

            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (selectedSort) {
                            SortOption.POPULAR -> "Popular"
                            SortOption.PRICE_LOW_TO_HIGH -> "Price: Low to High"
                            SortOption.PRICE_HIGH_TO_LOW -> "Price: High to Low"
                            SortOption.DISCOUNT -> "Top Discount"
                            SortOption.RATING -> "Highest Rating"
                        },
                        fontSize = 11.sp
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Popularity") },
                        onClick = { viewModel.setSort(SortOption.POPULAR); showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Price: Low to High") },
                        onClick = { viewModel.setSort(SortOption.PRICE_LOW_TO_HIGH); showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Price: High to Low") },
                        onClick = { viewModel.setSort(SortOption.PRICE_HIGH_TO_LOW); showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Top Discount") },
                        onClick = { viewModel.setSort(SortOption.DISCOUNT); showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Customer Rating") },
                        onClick = { viewModel.setSort(SortOption.RATING); showSortMenu = false }
                    )
                }
            }
        }

        // Product Grid with Hero Banners at Top
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().testTag("product_grid")
        ) {
            // Hero Banner Carousel Item
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Shopping Festival Banner
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.shopy_banner_1790945779372),
                                contentDescription = "Festival Sale",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Gradient Overlay with text
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color.Black.copy(alpha = 0.35f)
                                    )
                                    .padding(14.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ShopyCrimson)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "MEGA SAVINGS FESTIVAL",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Up to 70% Off on Top Categories",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        "Free Delivery + Cash on Delivery Available",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Trust Bar (Meesho style)
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = ShopyTeal, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Free Delivery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Payments, contentDescription = null, tint = ShopyGreenSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cash On Delivery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Replay, contentDescription = null, tint = ShopySecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("7-Day Returns", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Products
            if (products.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = ShopyTextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No products found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ShopyTextPrimary
                            )
                            Text(
                                text = "Try searching for something else or reset filters",
                                fontSize = 13.sp,
                                color = ShopyTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.selectCategory(null)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson)
                            ) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onProductClick = { onProductClick(product) },
                        onWishlistToggle = { viewModel.toggleWishlist(product.id) }
                    )
                }
            }
        }
    }
}
