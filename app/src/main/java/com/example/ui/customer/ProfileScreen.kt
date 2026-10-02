package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.ui.AppMode
import com.example.ui.CustomerTab
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ShopyViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }
    var showNewAddressDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        MobileLoginDialog(
            onLogin = { phone, name ->
                viewModel.login(phone, name, UserRole.CUSTOMER)
                showLoginDialog = false
            },
            onDismiss = { showLoginDialog = false }
        )
    }

    if (showNewAddressDialog) {
        NewAddressDialog(
            onSave = { name, phone, house, area, city, state, pin ->
                viewModel.addNewAddress(name, phone, house, area, city, state, pin)
                showNewAddressDialog = false
            },
            onDismiss = { showNewAddressDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopyBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // User Profile Header Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(ShopyPinkContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.name.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = ShopyCrimson
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+91 ${currentUser.phone}",
                                fontSize = 13.sp,
                                color = ShopyTextSecondary
                            )
                            if (currentUser.role != UserRole.CUSTOMER) {
                                Text(
                                    text = "Role: ${currentUser.role}",
                                    fontSize = 11.sp,
                                    color = ShopyCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showLoginDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp).testTag("switch_account_button")
                    ) {
                        Text("Switch/Login", fontSize = 11.sp, color = ShopyCrimson)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Portals Switch Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Marketplace Portals", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ShopyPinkContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setMode(AppMode.SELLER) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = ShopyCrimson)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Seller Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ShopyCrimson)
                            Text("Manage stock & AI studio", fontSize = 10.sp, color = ShopyOnPinkContainer)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ShopyTealLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setMode(AppMode.ADMIN) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = ShopyTeal)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Admin Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ShopyTeal)
                            Text("Moderation & sellers", fontSize = 10.sp, color = Color(0xFF004D40))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Settings / Menu List
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Outlined.LocalShipping,
                    title = "My Orders",
                    subtitle = "Track orders, cancel & return items",
                    onClick = { viewModel.setCustomerTab(CustomerTab.ORDERS) }
                )
                HorizontalDivider(color = ShopyOutline, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "My Wishlist",
                    subtitle = "View and buy your saved items",
                    onClick = { /* navigate to wishlist */ }
                )
                HorizontalDivider(color = ShopyOutline, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = Icons.Outlined.LocationOn,
                    title = "Saved Addresses",
                    subtitle = "Manage delivery addresses",
                    onClick = { showNewAddressDialog = true }
                )
                HorizontalDivider(color = ShopyOutline, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "Payment Methods & UPI",
                    subtitle = "Saved cards, UPI accounts",
                    onClick = { }
                )
                HorizontalDivider(color = ShopyOutline, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Customer Support & FAQs",
                    subtitle = "24x7 Help for returns and refunds",
                    onClick = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Info
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Shopy Marketplace v1.0", fontSize = 12.sp, color = ShopyTextMuted)
            Text("Made with Jetpack Compose & Room", fontSize = 11.sp, color = ShopyTextMuted)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ShopyCrimson, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = ShopyTextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ShopyTextMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun MobileLoginDialog(
    onLogin: (phone: String, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = ShopyCrimson)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login / Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Enter your mobile number to sign in or create an account with OTP verification.",
                    fontSize = 12.sp,
                    color = ShopyTextSecondary
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("10-Digit Mobile Number") },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("login_phone_input")
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("login_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.length >= 10) {
                        onLogin(phone, name)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                modifier = Modifier.testTag("submit_login_button")
            ) {
                Text("Continue / Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
