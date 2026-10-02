package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CustomerTab
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

data class CategoryMeta(
    val name: String,
    val icon: ImageVector,
    val subtext: String,
    val color: Color
)

@Composable
fun CategoriesScreen(
    viewModel: ShopyViewModel,
    modifier: Modifier = Modifier
) {
    val categoryList = listOf(
        CategoryMeta("Women Ethnic", Icons.Filled.Woman, "Sarees, Kurtis, Suits, Dupattas", Color(0xFFE91E63)),
        CategoryMeta("Men Fashion", Icons.Filled.Man, "Shirts, Jeans, T-Shirts, Ethnic", Color(0xFF1976D2)),
        CategoryMeta("Electronics", Icons.Filled.Headphones, "Earbuds, Smart Watches, Cables", Color(0xFF7B1FA2)),
        CategoryMeta("Footwear", Icons.Filled.RollerSkating, "Sports Shoes, Ethnic Juttis, Flats", Color(0xFFE65100)),
        CategoryMeta("Jewellery & Bags", Icons.Filled.Diamond, "Choker Sets, Handbags, Wallets", Color(0xFFC2185B)),
        CategoryMeta("Home & Living", Icons.Filled.Bed, "Bedsheets, Choppers, Decor", Color(0xFF00897B))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopyBackground)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("All Categories", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Browse by department and discover great deals", fontSize = 12.sp, color = ShopyTextSecondary)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categoryList) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable {
                            viewModel.selectCategory(item.name)
                            viewModel.setCustomerTab(CustomerTab.HOME)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(item.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                tint = item.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtext,
                                fontSize = 10.sp,
                                color = ShopyTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
