package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onAddContentClick: (Boolean) -> Unit,
    onMoviesListClick: () -> Unit,
    onSeriesListClick: () -> Unit,
    onAdsClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var usersCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("...") }
    var dramasCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("...") }
    var viewsCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("...") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            // Fetch users count
            val uCount = com.example.model.SupabaseNetwork.client
                .postgrest["users"]
                .select() {
                    count(Count.EXACT)
                    limit(1)
                }.countOrNull() ?: 0
            usersCount = uCount.toString()

            // Fetch dramas count
            val dCount = com.example.model.SupabaseNetwork.client
                .postgrest["dramas"]
                .select() {
                    count(Count.EXACT)
                    limit(1)
                }.countOrNull() ?: 0
            dramasCount = dCount.toString()

            // Fetch total views (optional: can just sum up the views natively or leave it if complex but we can try simple select)
            val dramas = com.example.model.SupabaseNetwork.client
                .postgrest["dramas"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("views"))
                .decodeList<com.example.model.supabase.SupabaseDrama>()
            
            val totalViews = dramas.sumOf { it.views ?: 0 }
            viewsCount = if (totalViews > 1000000) {
                String.format("%.1fM", totalViews / 1000000f)
            } else if (totalViews > 1000) {
                String.format("%.1fk", totalViews / 1000f)
            } else {
                totalViews.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            usersCount = "0"
            dramasCount = "0"
            viewsCount = "0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "عودة",
                    tint = WhiteText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "لوحة تحكم المدير",
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Stats Section
            item {
                Text(
                    text = "الإحصائيات المباشرة",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "المستخدمين المسجلين",
                        value = usersCount,
                        icon = Icons.Default.Group,
                        color = Crimson
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي المشاهدات",
                        value = viewsCount,
                        icon = Icons.Default.BarChart,
                        color = BadgeHot
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الأعمال (أفلام/مسلسلات)",
                        value = dramasCount,
                        icon = Icons.Default.Tv,
                        color = LightGrayInfo
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "التنزيلات",
                        value = usersCount,
                        icon = Icons.Default.Download,
                        color = BadgeNew
                    )
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "إدارة المحتوى",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                AdminActionItem(
                    title = "إضافة فيلم جديد",
                    subtitle = "رفع فيلم مع كود التضمين",
                    icon = Icons.Default.Add,
                    onClick = { onAddContentClick(true) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                AdminActionItem(
                    title = "إضافة مسلسل جديد",
                    subtitle = "رفع مسلسل مع حلقاته وأكواد التضمين",
                    icon = Icons.Default.Add,
                    onClick = { onAddContentClick(false) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                AdminActionItem(
                    title = "إدارة الأفلام",
                    subtitle = "تعديل أو الحذف",
                    icon = Icons.Default.Tv,
                    onClick = { onMoviesListClick() }
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                AdminActionItem(
                    title = "إدارة المسلسلات والحلقات",
                    subtitle = "إضافة حلقات لعمل موجود، تعديل أو حذف",
                    icon = Icons.Default.Tv,
                    onClick = { onSeriesListClick() }
                )
            }

            // Suggestions Section
            item {
                Text(
                    text = "أقسام أخرى (مقترحة)",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkGray)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    AdminSettingsItem("إدارة المستخدمين", onClick = {})
                    AdminSettingsItem("إدارة الإعلانات", onClick = { onAdsClick() })
                    AdminSettingsItem("التقارير والمخالفات", onClick = {})
                    AdminSettingsItem("إعدادات التطبيق العامة", Icons.Default.Settings, onClick = {})
                    AdminSettingsItem("اختبار اتصال قاعدة البيانات (Supabase)", onClick = {
                        android.widget.Toast.makeText(context, "جاري الاختبار...", android.widget.Toast.LENGTH_SHORT).show()
                        scope.launch {
                            try {
                                val result = com.example.model.SupabaseNetwork.client
                                    .postgrest["dramas"]
                                    .select() {
                                        count(Count.EXACT)
                                        limit(1)
                                    }
                                val num = result.countOrNull() ?: 0
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(context, "تم الاتصال! عدد الأعمال: $num", android.widget.Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(context, "فشل الاتصال: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    })
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGray)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = LightGrayInfo,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                color = WhiteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminActionItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGray)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PureBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Crimson,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = LightGrayInfo,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AdminSettingsItem(title: String, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LightGrayInfo,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = title,
            color = WhiteText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
