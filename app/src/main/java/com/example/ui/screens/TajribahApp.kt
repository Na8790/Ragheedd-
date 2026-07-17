package com.example.ui.screens

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TajribahApp(viewModel: MainViewModel) {
    val screenStack by viewModel.screenStack.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()
    val activeBannerNotification by viewModel.activeBannerNotification.collectAsState()

    val currentScreen = screenStack.lastOrNull() ?: Screen.Splash
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Intercept Back Press according to user guidelines
    BackHandler(enabled = true) {
        if (screenStack.size > 1) {
            viewModel.popScreen()
        } else {
            // Root screen back press
            if (currentScreen is Screen.MainDashboard || currentScreen is Screen.Login || currentScreen is Screen.Register) {
                viewModel.setShowExitDialog(true)
            } else {
                activity?.finish()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OffWhite,
        contentWindowInsets = WindowInsets.navigationBars // Handles Safe Area/Edge-to-Edge
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Application Screens routing
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Splash -> SplashScreen {
                        viewModel.popScreen()
                        viewModel.pushScreen(Screen.Login)
                    }
                    is Screen.Login -> LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            viewModel.popScreen()
                            viewModel.pushScreen(Screen.MainDashboard)
                        },
                        onNavigateToRegister = {
                            viewModel.pushScreen(Screen.Register)
                        }
                    )
                    is Screen.Register -> RegisterScreen(
                        viewModel = viewModel,
                        onRegisterSuccess = {
                            viewModel.popScreen()
                            viewModel.pushScreen(Screen.MainDashboard)
                        },
                        onNavigateToLogin = {
                            viewModel.popScreen()
                        }
                    )
                    is Screen.MainDashboard -> MainDashboardScreen(viewModel)
                    is Screen.AiPlanner -> AiPlannerScreen(viewModel)
                    is Screen.ExperienceDetail -> ExperienceDetailScreen(viewModel, targetScreen.experience)
                    is Screen.CarRental -> CarRentalScreen(viewModel)
                    is Screen.GuidesList -> GuidesListScreen(viewModel)
                    is Screen.AdminDashboard -> AdminDashboardScreen(viewModel)
                    is Screen.TechnicalSupport -> TechnicalSupportScreen(viewModel)
                    is Screen.Gifts -> GiftsScreen(viewModel)
                    is Screen.AboutApp -> AboutAppScreen(viewModel)
                    is Screen.Notifications -> NotificationsScreen(viewModel)
                }
            }

            // In-app interactive banner notifications
            activeBannerNotification?.let { notif ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .testTag("notification_banner")
                        .clickable { viewModel.clearBanner() },
                    colors = CardDefaults.cardColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(WarmGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alert",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = notif.title,
                                color = WarmGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.body,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Exit App dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowExitDialog(false) },
                    title = {
                        Text(
                            text = "هل تريد الخروج من التطبيق؟",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "سيتم إغلاق منصة تِجربة للرحلات الذكية.",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.setShowExitDialog(false)
                                activity?.finish()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedError),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("نعم، خروج", color = Color.White, fontFamily = FontFamily.SansSerif)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowExitDialog(false) }) {
                            Text("إلغاء", color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN (2 Seconds credit check)
// ==========================================
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // Show for 2 seconds as requested
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepBlue, Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant glowing sand gold travel icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                    .border(2.dp, WarmGold, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Tajribah Logo",
                    tint = WarmGold,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "تِـجـربـة",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "Tajribah",
                color = WarmGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "منصة التجارب المحلية والسفر الذكي",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Mandatory Engineer Raghad Credit at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Developed by Engineer Raghad",
                color = WarmGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تم تصميم وتطوير التطبيق بواسطة المهندسة رغد",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

// ==========================================
// 2. LOGIN SCREEN (With Raghad attribution)
// ==========================================
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("ahmed.traveler@tajribah.com") }
    var password by remember { mutableStateOf("123456") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Logo",
                    tint = DeepBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "تسجيل الدخول",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "مرحباً بك مجدداً في منصة تِجربة",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Email Form
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; isError = false },
                    label = { Text("البريد الإلكتروني", fontFamily = FontFamily.SansSerif) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email")
                        .shadow(1.dp, RoundedCornerShape(14.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBlue.copy(alpha = 0.6f),
                        focusedLabelColor = DeepBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Form
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; isError = false },
                    label = { Text("كلمة المرور", fontFamily = FontFamily.SansSerif) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password")
                        .shadow(1.dp, RoundedCornerShape(14.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBlue.copy(alpha = 0.6f),
                        focusedLabelColor = DeepBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يرجى إدخال بيانات صحيحة",
                        color = RedError,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.length >= 4) {
                            onLoginSuccess()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تسجيل الدخول", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick guest entrance
                TextButton(
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("الدخول كـزائر لاستكشاف التجارب 🧭", color = WarmGold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("ليس لديك حساب؟ ", color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    Text(
                        text = "سجل الآن",
                        color = DeepBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() },
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // Required Copyright Attribution footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "© جميع الحقوق محفوظة",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "تصميم وتطوير: المهندسة رغد",
                    color = DeepBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

// ==========================================
// 3. REGISTER SCREEN (With Raghad attribution)
// ==========================================
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Logo",
                    tint = DeepBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "إنشاء حساب جديد",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "انضم إلينا واكتشف كرم الضيافة التراثية",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل", fontFamily = FontFamily.SansSerif) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني", fontFamily = FontFamily.SansSerif) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور", fontFamily = FontFamily.SansSerif) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank() && password.length >= 4) {
                            viewModel.userName.value = name
                            viewModel.userEmail.value = email
                            onRegisterSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("register_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إنشاء الحساب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("لديك حساب بالفعل؟ ", color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    Text(
                        text = "سجل دخولك",
                        color = DeepBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() },
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // Required Copyright Attribution footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "© جميع الحقوق محفوظة",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "تصميم وتطوير: المهندسة رغد",
                    color = DeepBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

// ========================================================
// 4. MAIN DASHBOARD SCREEN (Tabs & Raised Bottom Navigation)
// ========================================================
@Composable
fun MainDashboardScreen(viewModel: MainViewModel) {
    val activeTab by viewModel.currentTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Content Area takes remaining space
        Box(modifier = Modifier.weight(1.0f)) {
            when (activeTab) {
                DashboardTab.Home -> HomeScreenTab(viewModel)
                DashboardTab.Experiences -> ExperiencesTab(viewModel)
                DashboardTab.Trips -> TripsTab(viewModel)
                DashboardTab.Bookings -> BookingsTab(viewModel)
                DashboardTab.Profile -> ProfileTab(viewModel)
            }
        }

        // Lifted, floating custom bottom navigation bar with proper Safe Area padding
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp) // Lifted up with padding
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GlassDeepBlue),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomNavItem(
                    tab = DashboardTab.Profile,
                    label = "حسابي",
                    icon = Icons.Default.Person,
                    isActive = activeTab == DashboardTab.Profile,
                    onSelected = { viewModel.setTab(DashboardTab.Profile) }
                )
                BottomNavItem(
                    tab = DashboardTab.Bookings,
                    label = "حجوزاتي",
                    icon = Icons.Default.Event,
                    isActive = activeTab == DashboardTab.Bookings,
                    onSelected = { viewModel.setTab(DashboardTab.Bookings) }
                )
                BottomNavItem(
                    tab = DashboardTab.Trips,
                    label = "الرحلات",
                    icon = Icons.Default.Map,
                    isActive = activeTab == DashboardTab.Trips,
                    onSelected = { viewModel.setTab(DashboardTab.Trips) }
                )
                BottomNavItem(
                    tab = DashboardTab.Experiences,
                    label = "التجارب",
                    icon = Icons.Default.Explore,
                    isActive = activeTab == DashboardTab.Experiences,
                    onSelected = { viewModel.setTab(DashboardTab.Experiences) }
                )
                BottomNavItem(
                    tab = DashboardTab.Home,
                    label = "الرئيسية",
                    icon = Icons.Default.Home,
                    isActive = activeTab == DashboardTab.Home,
                    onSelected = { viewModel.setTab(DashboardTab.Home) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    tab: DashboardTab,
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onSelected: () -> Unit
) {
    val activeTag = "tab_${tab.name.lowercase()}"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(activeTag)
            .clickable { onSelected() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) WarmGold else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isActive) WarmGold else Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.SansSerif
        )
    }
}

// ==========================================
// 5. HOME SCREEN TAB
// ==========================================
@Composable
fun HomeScreenTab(viewModel: MainViewModel) {
    val user by viewModel.userName.collectAsState()
    val experiences by viewModel.experiencesList.collectAsState()
    val trips by viewModel.tripsList.collectAsState()
    val notifications by viewModel.notificationsList.collectAsState()
    val unreadNotifCount = notifications.count { !it.isRead }

    var showFilterDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(horizontal = 16.dp)
    ) {
        // Welcome and Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Header icons (Notifications, Support, Admin)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Admin Dashboard
                    IconButton(
                        onClick = { viewModel.pushScreen(Screen.AdminDashboard) },
                        modifier = Modifier.testTag("admin_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = DeepBlue
                        )
                    }

                    // Tech Support
                    IconButton(
                        onClick = { viewModel.pushScreen(Screen.TechnicalSupport) },
                        modifier = Modifier.testTag("tech_support_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Support",
                            tint = DeepBlue
                        )
                    }

                    // Notifications Icon with badge
                    Box {
                        IconButton(onClick = { viewModel.pushScreen(Screen.Notifications) }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = DeepBlue
                            )
                        }
                        if (unreadNotifCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(RedError, CircleShape)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadNotifCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Welcome User Info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "أهلاً بك، $user 👋",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "اكتشف متعة الرحلات والتجارب التراثية",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // Hero illustration generated dynamically
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.tajribah_hero),
                        contentDescription = "Tajribah Travel Illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient Cover
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DeepBlue.copy(alpha = 0.85f))
                                )
                            )
                    )
                    // Text Overlay matching the design layout (Split into Left Price / Right Info)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Frosted Glass Price Badge on the Left
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "450 ر.س",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        // Info on the Right
                        Column(horizontalAlignment = Alignment.End) {
                            // "Featured Experience" Mini-badge
                            Box(
                                modifier = Modifier
                                    .background(WarmGold, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "تجربة مختارة",
                                    color = DeepBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يوم مع نحال في مزارع العلا",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "العلا، المملكة العربية السعودية",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }
        }

        // Search and Filter Bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glassmorphic Filter Button
                IconButton(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Filters", tint = DeepBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                val query by viewModel.searchQuery.collectAsState()
                
                // Glassmorphic Search Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("ابحث عن تجربتك التالية...", fontSize = 14.sp, fontFamily = FontFamily.SansSerif) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = WarmGold) },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(52.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBlue.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.65f)
                    ),
                    singleLine = true
                )
            }
        }

        // Smart AI travel Assistant CTA - Styled with Glassmorphism and gold tint
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.pushScreen(Screen.AiPlanner) }
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = WarmGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "المساعد السياحي الذكي 🤖",
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "صمم جدول رحلتك المفصل يوم بيوم بالذكاء الاصطناعي مجاناً!",
                            color = DarkGray.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Quick Categories Buttons
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "التصنيفات الرئيسية",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DeepBlue,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(10.dp))
            val currentCategory by viewModel.selectedCategory.collectAsState()
            val categories = listOf(
                "الكل" to Icons.Default.Category,
                "نحل" to Icons.Default.Eco,
                "فخar" to Icons.Default.Brush,
                "صيد" to Icons.Default.Water,
                "طهي" to Icons.Default.Restaurant,
                "تخييم" to Icons.Default.Cabin,
                "خيل" to Icons.Default.Pets,
                "جبال" to Icons.Default.Landscape
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (cat, icon) ->
                    val isSelected = currentCategory == cat
                    Box(
                        modifier = Modifier
                            .clickable { viewModel.selectedCategory.value = cat }
                            .background(
                                if (isSelected) DeepBlue else Color.White.copy(alpha = 0.65f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, if (isSelected) DeepBlue else Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = when(cat) {
                                "الكل" -> "🌎 الكل"
                                "نحل" -> "🐝 مناحل عسل"
                                "فخar" -> "🏺 الفخار واليدوي"
                                "صيد" -> "🎣 صيد بحري"
                                "طهي" -> "🍳 طهي شعبي"
                                "تخييم" -> "⛺ تخييم رصد"
                                "خيل" -> "🐎 ركوب خيل"
                                "جبال" -> "⛰️ هايكنج جبال"
                                else -> cat
                            },
                            color = if (isSelected) Color.White else DarkGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        // Top Experiences Horizontal Row
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { viewModel.setTab(DashboardTab.Experiences) }) {
                    Text("عرض الكل", color = WarmGold, fontFamily = FontFamily.SansSerif)
                }
                Text(
                    text = "أفضل التجارب المحلية 🏺",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepBlue,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(experiences.take(4)) { exp ->
                    ExperienceCard(exp) {
                        viewModel.pushScreen(Screen.ExperienceDetail(exp))
                    }
                }
            }
        }

        // Surprise Trips & Hot Offers
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "الرحلات السياحية المفاجئة 🤫",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DeepBlue,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Text(
                text = "سافر لوجهة سرية غامضة مليئة بالمغامرات الرائعة",
                fontSize = 11.sp,
                color = Color.Gray,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(10.dp))

            val surpriseTrips = trips.filter { it.isSurprise }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(surpriseTrips) { trip ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable {
                                viewModel.addNotification("مغامرة غامضة!", "لقد بدأت استكشاف المغامرة الغامضة: '${trip.title}'!")
                                viewModel.setTab(DashboardTab.Trips)
                            },
                        colors = CardDefaults.cardColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(WarmGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Surprise",
                                    tint = WarmGold,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = trip.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${trip.price} ريال / شخص",
                                    color = WarmGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text(
                                    text = "⏳ ${trip.duration}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }
                }
            }
        }

        // Travel Utilities Shortcuts
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "الخدمات السياحية الإضافية",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DeepBlue,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gifts Store Card
                Card(
                    modifier = Modifier
                        .weight(1.0f)
                        .clickable { viewModel.pushScreen(Screen.Gifts) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(WarmGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = "Gifts", tint = WarmGold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("متجر الهدايا الحرفية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        Text("استبدل نقاطك بهدايا", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    }
                }

                // Car Rental Card
                Card(
                    modifier = Modifier
                        .weight(1.0f)
                        .clickable { viewModel.pushScreen(Screen.CarRental) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(DeepBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = "Cars", tint = DeepBlue)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("تأجير السيارات", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        Text("سيارات دفع رباعي وعائلية", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    }
                }

                // Guides List Card
                Card(
                    modifier = Modifier
                        .weight(1.0f)
                        .clickable { viewModel.pushScreen(Screen.GuidesList) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(EmeraldGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.SupervisedUserCircle, contentDescription = "Guides", tint = EmeraldGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("المرشدون المحليون", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        Text("مرشدون مرخصون للرحلات", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        }

        // About app shortcut (At bottom)
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.pushScreen(Screen.AboutApp) }
                    .background(LightBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "About", tint = DeepBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حول منصة تِجربة والمطور", fontSize = 12.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Custom Interactive Search Filter Dialog Box
    if (showFilterDialog) {
        Dialog(onDismissRequest = { showFilterDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "فلترة وتصفية النتائج",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepBlue,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("تحديد الحد الأقصى للسعر (ريال)", fontSize = 14.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    var priceSlider by remember { mutableStateOf(500f) }
                    Slider(
                        value = priceSlider,
                        onValueChange = { priceSlider = it },
                        valueRange = 100f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = WarmGold,
                            activeTrackColor = DeepBlue
                        )
                    )
                    Text("${priceSlider.toInt()} ريال", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("تصفية حسب المدينة", fontSize = 14.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    var selectedCityFilter by remember { mutableStateOf("الكل") }
                    val filterCities = listOf("الكل", "عسير", "القطيف", "جدة", "العلا")

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filterCities) { city ->
                            val isSel = selectedCityFilter == city
                            Box(
                                modifier = Modifier
                                    .clickable { selectedCityFilter = city }
                                    .background(if (isSel) WarmGold else LightBlue, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(city, color = if (isSel) Color.White else DarkGray, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("إلغاء", color = Color.Gray, fontFamily = FontFamily.SansSerif)
                        }
                        Button(
                            onClick = {
                                viewModel.maxPriceFilter.value = priceSlider.toDouble()
                                showFilterDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                        ) {
                            Text("تطبيق", color = Color.White, fontFamily = FontFamily.SansSerif)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExperienceCard(exp: ExperienceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("experience_card_${exp.id}")
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DeepBlue.copy(alpha = 0.8f), DeepBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Large illustrative category icon
                Icon(
                    imageVector = when(exp.category) {
                        "نحل" -> Icons.Default.Eco
                        "فخار" -> Icons.Default.Brush
                        "صيد" -> Icons.Default.Water
                        "طهي" -> Icons.Default.Restaurant
                        "خيل" -> Icons.Default.Pets
                        "جبال" -> Icons.Default.Landscape
                        else -> Icons.Default.AutoAwesome
                    },
                    contentDescription = exp.title,
                    tint = WarmGold,
                    modifier = Modifier.size(54.dp)
                )

                // Location badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(exp.city, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                }
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = exp.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = exp.hostName,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${exp.price} ريال",
                        color = WarmGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = exp.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = WarmGold, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. EXPERIENCES TAB (التجارب)
// ==========================================
@Composable
fun ExperiencesTab(viewModel: MainViewModel) {
    val experiences by viewModel.experiencesList.collectAsState()
    val currentCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val maxPrice by viewModel.maxPriceFilter.collectAsState()

    val filteredList = experiences.filter {
        (currentCategory == "الكل" || it.category == currentCategory) &&
        (it.price <= maxPrice) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.city.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "تصفح وحجز التجارب المحلية 🏺",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DeepBlue,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Text(
            text = "عش كأنك من أهل البلد مع أصحاب المهن والخبرات",
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد تجارب مطابقة لمعايير البحث حالياً 🔍", color = Color.Gray, fontFamily = FontFamily.SansSerif)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredList) { exp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.pushScreen(Screen.ExperienceDetail(exp)) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, LightBlue)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Details
                            Column(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .padding(end = 12.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = exp.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DeepBlue,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(exp.hostName, fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${exp.price} ريال / شخص",
                                        color = WarmGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⏳ ${exp.duration}", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(exp.city, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeepBlue)
                                    }
                                }
                            }

                            // Right Icon representation
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(DeepBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when(exp.category) {
                                        "نحل" -> Icons.Default.Eco
                                        "فخار" -> Icons.Default.Brush
                                        "صيد" -> Icons.Default.Water
                                        "طهي" -> Icons.Default.Restaurant
                                        "خيل" -> Icons.Default.Pets
                                        "جبال" -> Icons.Default.Landscape
                                        else -> Icons.Default.AutoAwesome
                                    },
                                    contentDescription = exp.title,
                                    tint = DeepBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. TRIPS TAB (الرحلات)
// ==========================================
@Composable
fun TripsTab(viewModel: MainViewModel) {
    val trips by viewModel.tripsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "الرحلات السياحية الذكية 🗺️",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DeepBlue,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Text(
            text = "استكشف الكنوز التراثية والرحلات المنظمة باحترافية",
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1.0f)
        ) {
            items(trips) { trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (trip.isSurprise) DeepBlue else Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (trip.isSurprise) WarmGold else LightBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (trip.isSurprise) {
                                Box(
                                    modifier = Modifier
                                        .background(WarmGold, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("وجهة غامضة 🤐", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = WarmGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(trip.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = trip.title,
                                color = if (trip.isSurprise) Color.White else DeepBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = trip.description,
                            color = if (trip.isSurprise) Color.White.copy(alpha = 0.8f) else Color.Gray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.bookExperience(trip.title, "رحلات", trip.price, 1)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("حجز الآن", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${trip.price} ريال",
                                    color = if (trip.isSurprise) WarmGold else DeepBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text(
                                    text = "⏳ ${trip.duration} | 📍 ${trip.city}",
                                    color = if (trip.isSurprise) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. EXPERIENCE DETAIL SCREEN (🏺)
// ==========================================
@Composable
fun ExperienceDetailScreen(viewModel: MainViewModel, exp: ExperienceItem) {
    var quantity by remember { mutableStateOf(1) }
    var currentReviewTab by remember { mutableStateOf(0) } // 0: Details, 1: Reviews

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // Upper TopAppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.toggleFavorite(exp.id) }
            ) {
                Icon(
                    imageVector = if (exp.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (exp.isFavorite) RedError else DeepBlue
                )
            }
            Text(
                text = "تفاصيل التجربة",
                fontWeight = FontWeight.Bold,
                color = DeepBlue,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif
            )
            IconButton(
                onClick = { viewModel.popScreen() }
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        // Main Detail view scroll
        Column(
            modifier = Modifier
                .weight(1.0f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Visual Banner representation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(DeepBlue, Color(0xFF1E293B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(exp.category) {
                            "نحل" -> Icons.Default.Eco
                            "فخار" -> Icons.Default.Brush
                            "صيد" -> Icons.Default.Water
                            "طهي" -> Icons.Default.Restaurant
                            "خيل" -> Icons.Default.Pets
                            "جبال" -> Icons.Default.Landscape
                            else -> Icons.Default.AutoAwesome
                        },
                        contentDescription = "Visual",
                        tint = WarmGold,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = exp.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = DeepBlue,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Row rating & city
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = WarmGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${exp.rating} (${exp.reviewCount} تقييم)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .background(LightBlue, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("📍 ${exp.city}", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Host Card with chat action (direct conversation)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LightBlue)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.selectChatPartner(exp.hostName)
                            // Redirect user to technical/direct chat layout
                            viewModel.addNotification("محادثة مع المضيف", "تم بدء دردشة مباشرة مع ${exp.hostName}")
                            // Simply navigate to tech support screen to emulate direct chat
                            viewModel.pushScreen(Screen.TechnicalSupport)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "chat", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("محادثة", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مضيف التجربة والخبرة", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                        Text(exp.hostName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details or Reviews Tabs
            TabRow(
                selectedTabIndex = currentReviewTab,
                containerColor = Color.Transparent,
                contentColor = DeepBlue
            ) {
                Tab(selected = currentReviewTab == 0, onClick = { currentReviewTab = 0 }) {
                    Text("التفاصيل", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                }
                Tab(selected = currentReviewTab == 1, onClick = { currentReviewTab = 1 }) {
                    Text("التقييمات (${exp.reviews.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentReviewTab == 0) {
                // Details description
                Text(
                    text = exp.description,
                    color = DarkGray,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Duration & group size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = "Duration", tint = WarmGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("المدة المقدرة", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                        Text(exp.duration, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = "Group", tint = WarmGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("المجموعة", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                        Text("عائلية وفردية", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.SansSerif)
                    }
                }
            } else {
                // Reviews List view
                if (exp.reviews.isEmpty()) {
                    Text("لا توجد تقييمات مكتوبة لهذه التجربة بعد. شارك برأيك بعد التجربة!", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        exp.reviews.forEach { rev ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, LightBlue)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Like", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(rev.likes.toString(), fontSize = 11.sp)
                                        }
                                        Text(rev.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        repeat(rev.rating.toInt()) {
                                            Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = WarmGold, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(rev.comment, color = DarkGray, fontSize = 12.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), fontFamily = FontFamily.SansSerif)

                                    // Host Reply attribution
                                    rev.hostReply?.let { reply ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(LightGold, RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("رد مضيف التجربة:", color = WarmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                                Text(reply, color = DarkGray, fontSize = 11.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), fontFamily = FontFamily.SansSerif)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Checkout Bottom action bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        viewModel.bookExperience(exp.title, exp.category, exp.price, quantity)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    modifier = Modifier
                        .weight(1.0f)
                        .height(50.dp)
                        .testTag("book_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حجز التجربة الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.SansSerif)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text("السعر الإجمالي", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    Text("${exp.price * quantity} ريال", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    // Quantity selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Less", modifier = Modifier.size(16.dp))
                        }
                        Text(quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { quantity++ }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "More", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. BOOKINGS TAB (الحجوزات)
// ==========================================
@Composable
fun BookingsTab(viewModel: MainViewModel) {
    val bookings by viewModel.bookingsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "حجوزاتي المؤكدة 🎫",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DeepBlue,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Text(
            text = "تفاصيل حجوزاتك الحالية ورمز QR الصالح للاستخدام",
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.EventBusy, contentDescription = "No Bookings", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد لديك حجوزات حالية", color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("ابدأ بتصفح وحجز تجربة فريدة الآن!", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, LightBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(EmeraldGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(booking.status, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }
                                Text(booking.itemTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "التاريخ: ${booking.date}", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                                Text(text = "الفئة: ${booking.category}", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Mock QR Code area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(OffWhite, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QR Code",
                                        tint = DeepBlue,
                                        modifier = Modifier.size(90.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(booking.qrCode, fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.refundBooking(booking.id, booking.itemTitle, booking.price * booking.quantity)
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = RedError)
                                ) {
                                    Text("إلغاء واسترداد المبلغ 💰", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                                }

                                Text(
                                    text = "المدفوع: ${booking.price * booking.quantity} ريال",
                                    color = DeepBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. SMART AI TRAVEL PLANNER SCREEN
// ==========================================
@Composable
fun AiPlannerScreen(viewModel: MainViewModel) {
    val city by viewModel.plannerCity.collectAsState()
    val budget by viewModel.plannerBudget.collectAsState()
    val people by viewModel.plannerPeopleCount.collectAsState()
    val duration by viewModel.plannerDuration.collectAsState()
    val interests by viewModel.plannerInterests.collectAsState()
    val plannerState by viewModel.plannerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // AppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp)) // Spacer
            Text("مخطط السفر بالذكاء الاصطناعي 🤖", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Column(
            modifier = Modifier
                .weight(1.0f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (plannerState is MainViewModel.PlannerState.Idle) {
                // Settings Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                        Text("حدد تفاصيل رحلتك", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = city,
                            onValueChange = { viewModel.plannerCity.value = it },
                            label = { Text("المدينة المستهدفة (مثال: أبها، العلا، جدة)", fontFamily = FontFamily.SansSerif) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = budget,
                            onValueChange = { viewModel.plannerBudget.value = it },
                            label = { Text("الميزانية الإجمالية التقريبية بالريال", fontFamily = FontFamily.SansSerif) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = people,
                            onValueChange = { viewModel.plannerPeopleCount.value = it },
                            label = { Text("عدد الأشخاص الراغبين بالسفر", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { viewModel.plannerDuration.value = it },
                            label = { Text("مدة الرحلة (بالأيام)", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = interests,
                            onValueChange = { viewModel.plannerInterests.value = it },
                            label = { Text("الاهتمامات (مثال: نحل، فخار، هايكنج، تراث)", fontFamily = FontFamily.SansSerif) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.runAiTravelPlanner() },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("run_ai_planner_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("خطط لي بالذكاء الاصطناعي ✨", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }
                }
            } else if (plannerState is MainViewModel.PlannerState.Loading) {
                // Loading view with custom animations
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = WarmGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("يتم الآن التواصل مع ذكاء Gemini... 🧠", fontWeight = FontWeight.Bold, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        Text("نصمم لك رحلة تراثية متكاملة وسفر ذكي مخصص وممتع", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
                    }
                }
            } else if (plannerState is MainViewModel.PlannerState.Success) {
                // Custom rendered success travel plan
                val plan = (plannerState as MainViewModel.PlannerState.Success).planText
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.resetPlanner() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = DeepBlue)
                            }
                            Text("جدول رحلتك المخصص الذكي 📋", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = LightBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        SelectionContainer {
                            Text(
                                text = plan,
                                color = DarkGray,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.bookExperience("جدول رحلة ذكي: $city", "رحلات ذكية", 500.0, 1)
                                viewModel.popScreen()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تأكيد حجز الباقة المقترحة بالكامل (500 ريال)", color = Color.White, fontFamily = FontFamily.SansSerif)
                        }
                    }
                }
            } else if (plannerState is MainViewModel.PlannerState.Error) {
                // Error screen
                val errMsg = (plannerState as MainViewModel.PlannerState.Error).errorMessage
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, RedError)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = "Error", tint = RedError, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("حدث خطأ أثناء الاتصال بالخادم", fontWeight = FontWeight.Bold, color = DeepBlue)
                        Text(errMsg, color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetPlanner() }, colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)) {
                            Text("حاول مجدداً", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. PROFILE TAB
// ==========================================
@Composable
fun ProfileTab(viewModel: MainViewModel) {
    val name by viewModel.userName.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val balance by viewModel.userWalletBalance.collectAsState()
    val points by viewModel.userPoints.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        // Avatar circle
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(DeepBlue, CircleShape)
                .border(2.dp, WarmGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
        Text(email, fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // Wallet Balance & Points Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LightBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("المحفظة الرقمية 💰", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$balance ريال", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue)
                }
            }

            // Points Card
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LightBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("نقاط تِجربة ⭐", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$points نقطة", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WarmGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Menu settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LightBlue)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileMenuItem("حول التطبيق والمطور ℹ️") { viewModel.pushScreen(Screen.AboutApp) }
                Divider(color = LightBlue)
                ProfileMenuItem("مركز الإشعارات والرسائل 🔔") { viewModel.pushScreen(Screen.Notifications) }
                Divider(color = LightBlue)
                ProfileMenuItem("المرشد السياحي والمضيفين 🗺️") { viewModel.pushScreen(Screen.GuidesList) }
                Divider(color = LightBlue)
                ProfileMenuItem("لوحة تحكم المشرف 🔐") { viewModel.pushScreen(Screen.AdminDashboard) }
                Divider(color = LightBlue)
                ProfileMenuItem("تسجيل الخروج 🚪") {
                    viewModel.popScreen()
                    viewModel.pushScreen(Screen.Login)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ProfileMenuItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Go", tint = Color.Gray)
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGray, fontFamily = FontFamily.SansSerif)
    }
}

// ==========================================
// 12. CAR RENTAL SCREEN (🚗)
// ==========================================
@Composable
fun CarRentalScreen(viewModel: MainViewModel) {
    val cars by viewModel.carsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp))
            Text("تأجير السيارات الفخمة والعائلية 🚗", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cars) { car ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = WarmGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(car.rating.toString(), fontWeight = FontWeight.Bold)
                            }
                            Text(car.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("النوع: ${car.type}", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                            Text("ناقل الحركة: ${car.transmission}", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.bookExperience("تأجير سيارة: ${car.name}", "تأجير السيارات", car.pricePerDay, 1)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                            ) {
                                Text("تأجير الآن", color = Color.White, fontFamily = FontFamily.SansSerif)
                            }
                            Text("${car.pricePerDay} ريال / يوم", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 13. GUIDES LIST SCREEN (🗺️)
// ==========================================
@Composable
fun GuidesListScreen(viewModel: MainViewModel) {
    val guides by viewModel.guidesList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp))
            Text("المرشدون السياحيون الموثقون 🗺️", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(guides) { guide ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = WarmGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${guide.rating} (${guide.reviewsCount} تقييم)", fontWeight = FontWeight.Bold)
                            }
                            Text(guide.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("التخصص: ${guide.specialty}", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                        Text(guide.bio, fontSize = 12.sp, color = DarkGray, fontFamily = FontFamily.SansSerif, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.bookExperience("حجز مرشد سياحي: ${guide.name}", "المرشدون", guide.pricePerDay, 1)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                            ) {
                                Text("حجز المرشد", color = Color.White, fontFamily = FontFamily.SansSerif)
                            }
                            Text("${guide.pricePerDay} ريال / يوم", color = WarmGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 14. GIFTS STORE SCREEN (🎁)
// ==========================================
@Composable
fun GiftsScreen(viewModel: MainViewModel) {
    val gifts by viewModel.giftsList.collectAsState()
    val points by viewModel.userPoints.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp))
            Text("متجر الهدايا التراثية الحرفية 🎁", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        // Points banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = LightGold)
        ) {
            Text(
                text = "رصيد نقاطك الحالي هو: $points نقطة ⭐",
                fontWeight = FontWeight.Bold,
                color = DeepBlue,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gifts) { gift ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(gift.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(gift.description, fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.SansSerif, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (points >= gift.pointsRequired) {
                                        viewModel.userPoints.value -= gift.pointsRequired
                                        viewModel.addNotification("تم استبدال الهدية بنجاح! 🎁", "مبروك، قمت باستبدال هدية '${gift.title}'. سيقوم الحرفي بطلب الاتصال لتجهيز الشحن.")
                                    } else {
                                        viewModel.addNotification("نقاط غير كافية ❌", "عذراً، رصيد نقاطك لا يكفي لاستبدال هذه الهدية الحرفية.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                            ) {
                                Text("استبدل بالنقاط", color = Color.White, fontFamily = FontFamily.SansSerif)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("أو اشتري بـ: ${gift.price} ريال", fontSize = 11.sp, color = Color.Gray)
                                Text("المطلوب: ${gift.pointsRequired} نقطة ⭐", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 15. TECHNICAL SUPPORT SCREEN (الدعم الفني)
// ==========================================
@Composable
fun TechnicalSupportScreen(viewModel: MainViewModel) {
    val messages by viewModel.supportMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp))
            Text("الدعم الفني والشكاوى 🎧", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        // Conversation messages area
        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) DeepBlue else Color.White
                        ),
                        border = if (isUser) null else BorderStroke(1.dp, LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.message,
                                color = if (isUser) Color.White else DarkGray,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Typing inputs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendSupportMessage(messageText)
                        messageText = ""
                    }
                }
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "Send", tint = DeepBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("اكتب تفاصيل طلبك للمهندسة رغد والدعم الفني...", fontSize = 13.sp, fontFamily = FontFamily.SansSerif) },
                modifier = Modifier.weight(1.0f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ==========================================
// 16. NOTIFICATIONS SCREEN
// ==========================================
@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val notifs by viewModel.notificationsList.collectAsState()

    // Mark all as read when opening
    LaunchedEffect(Unit) {
        viewModel.markNotificationsAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp))
            Text("مركز الإشعارات والتنبيهات 🔔", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        if (notifs.isEmpty()) {
            Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد إشعارات حالياً 📭", color = Color.Gray, fontFamily = FontFamily.SansSerif)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifs) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LightBlue)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notif.body, color = DarkGray, fontSize = 12.sp, fontFamily = FontFamily.SansSerif, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 17. ABOUT APP SCREEN (With Raghad credits)
// ==========================================
@Composable
fun AboutAppScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp))
            Text("حول منصة تِجربة ℹ️", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Column(
            modifier = Modifier
                .weight(1.0f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Tajribah",
                tint = WarmGold,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("منصة تِجربة – Tajribah", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepBlue)
            Text("الإصدار v1.0.0 (إنتاج كامل جاهز للنشر)", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "منصة تِجربة هي منصة رائدة عالمية تجمع بين حجز الرحلات السياحية المنظمة والرحلات المفاجئة وتأجير السيارات، وشراء التجارب الثقافية والحرفية الأصيلة مباشرة من أصحاب المهن والخبرات المحلية في المملكة العربية السعودية.",
                color = DarkGray,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mandatory Credits for Raghad
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightGold),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تم تصميم وتطوير التطبيق بواسطة المهندسة رغد",
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Designed & Developed by Engineer Raghad",
                        fontWeight = FontWeight.Medium,
                        color = WarmGold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = WarmGold.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "© جميع الحقوق محفوظة | تصميم وتطوير: المهندسة رغد",
                        color = DarkGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// 18. ADMIN DASHBOARD SCREEN (لوحة التحكم)
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val registeredUsers by viewModel.adminRegisteredUsers.collectAsState()
    val complaints by viewModel.adminComplaints.collectAsState()
    val experiences by viewModel.experiencesList.collectAsState()

    var showAddExpDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { showAddExpDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Experience", tint = DeepBlue)
            }
            Text("لوحة تحكم المشرف 🔐", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBlue, fontFamily = FontFamily.SansSerif)
            IconButton(onClick = { viewModel.popScreen() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        }

        // Stats boxes row
        Column(
            modifier = Modifier
                .weight(1.0f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.weight(1.0f), colors = CardDefaults.cardColors(containerColor = DeepBlue)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("إجمالي المبيعات", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
                        Text("12,450 ريال", color = WarmGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Card(modifier = Modifier.weight(1.0f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("التجارب المتاحة", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
                        Text(experiences.size.toString(), color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Card(modifier = Modifier.weight(1.0f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المستخدمون", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
                        Text(registeredUsers.size.toString(), color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Users list
            Text("قائمة المستخدمين المسجلين", fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    registeredUsers.forEach { user ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نشط 🟢", color = EmeraldGreen, fontSize = 11.sp)
                            Text(user, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Divider(color = OffWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complaints list
            Text("الشكاوى ومراجعات الجودة", fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                complaints.forEach { cmp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { viewModel.resolveComplaint(cmp.id) },
                                    enabled = cmp.status == "Pending",
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                ) {
                                    Text(if (cmp.status == "Resolved") "محلولة ✅" else "حل الشكوى", fontSize = 10.sp, color = Color.White)
                                }
                                Text(cmp.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cmp.content, fontSize = 12.sp, color = DarkGray, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("بواسطة: ${cmp.userName} (${cmp.email})", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Footer credits for Engineer Raghad
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "System Designed & Developed by Engineer Raghad",
                color = WarmGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Interactive add experience Dialog for Admin Dashboard
    if (showAddExpDialog) {
        var addTitle by remember { mutableStateOf("") }
        var addHost by remember { mutableStateOf("") }
        var addCat by remember { mutableStateOf("نحل") }
        var addPrice by remember { mutableStateOf("100") }
        var addCity by remember { mutableStateOf("عسير") }
        var addDesc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddExpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("إضافة تجربة جديدة للمنصة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = addTitle, onValueChange = { addTitle = it }, label = { Text("عنوان التجربة", fontFamily = FontFamily.SansSerif) }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addHost, onValueChange = { addHost = it }, label = { Text("اسم المضيف", fontFamily = FontFamily.SansSerif) }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addPrice, onValueChange = { addPrice = it }, label = { Text("السعر بالريال", fontFamily = FontFamily.SansSerif) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addCity, onValueChange = { addCity = it }, label = { Text("المدينة", fontFamily = FontFamily.SansSerif) }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addDesc, onValueChange = { addDesc = it }, label = { Text("وصف التجربة والخبرة", fontFamily = FontFamily.SansSerif) }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { showAddExpDialog = false }) { Text("إلغاء") }
                        Button(
                            onClick = {
                                if (addTitle.isNotBlank() && addHost.isNotBlank()) {
                                    viewModel.addNewExperience(
                                        addTitle,
                                        addHost,
                                        addCat,
                                        addPrice.toDoubleOrNull() ?: 100.0,
                                        addCity,
                                        addDesc
                                    )
                                    showAddExpDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
                        ) {
                            Text("إضافة ونشر 🚀", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
