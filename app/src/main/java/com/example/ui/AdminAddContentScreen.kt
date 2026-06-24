package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddContentScreen(isMovieConfig: Boolean, onBack: () -> Unit, viewModel: DramaViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var movieEmbedCode by remember { mutableStateOf("") }
    
    // For Series
    val episodes = remember { mutableStateListOf<EpisodeInput>() }

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
                text = "إضافة محتوى جديد",
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isMovieConfig) "إضافة فيلم" else "إضافة مسلسل",
                        color = Crimson,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان", color = LightGrayInfo) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Crimson,
                        unfocusedBorderColor = DarkGray,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف قصير", color = LightGrayInfo) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Crimson,
                        unfocusedBorderColor = DarkGray,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }

            item {
                var isAdCategory by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = isAdCategory,
                        onClick = { 
                            isAdCategory = !isAdCategory
                            if (isAdCategory) category = "إعلان" else category = ""
                        },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = Crimson, unselectedColor = LightGrayInfo)
                    )
                    Text(
                        text = "تصنيف كإعلان (تظهر في قسم من أجلك)",
                        color = WhiteText,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable { 
                                isAdCategory = !isAdCategory
                                if (isAdCategory) category = "إعلان" else category = ""
                            }
                            .padding(end = 8.dp)
                    )
                }

                if (!isAdCategory) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف (أكشن، دراما...)", color = LightGrayInfo) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Crimson,
                            unfocusedBorderColor = DarkGray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("رابط الصورة", color = LightGrayInfo) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Crimson,
                        unfocusedBorderColor = DarkGray,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (isMovieConfig) {
                item {
                    Text("كود التضمين (Embed Code) للفيلم", color = WhiteText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = movieEmbedCode,
                        onValueChange = { movieEmbedCode = it },
                        label = { Text("مثال: <iframe src=\"...\"></iframe>", color = LightGrayInfo) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Crimson,
                            unfocusedBorderColor = DarkGray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حلقات المسلسل", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { 
                            episodes.add(EpisodeInput(title = "الحلقة ${episodes.size + 1}", embedCode = "")) 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة حلقة", tint = Crimson)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة حلقة", color = Crimson)
                        }
                    }
                }

                itemsIndexed(episodes) { index, episode ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkGray)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الحلقة ${index + 1}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { episodes.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Crimson)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = episode.embedCode,
                            onValueChange = { newCode -> 
                                episodes[index] = episode.copy(embedCode = newCode)
                            },
                            label = { Text("كود التضمين (Embed Code)", color = LightGrayInfo) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Crimson,
                                unfocusedBorderColor = PureBlack,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (episodes.isEmpty()) {
                    item {
                        Text(
                            text = "لا توجد حلقات مضافة حالياً. اضغط على 'إضافة حلقة' للبدء.",
                            color = LightGrayInfo,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        val dramaId = java.util.UUID.randomUUID().toString()
                        val newDrama = com.example.model.supabase.SupabaseDrama(
                            id = dramaId,
                            title = title,
                            description = description,
                            category = category,
                            isMovie = isMovieConfig,
                            movieEmbedCode = if (isMovieConfig) movieEmbedCode else null,
                            imageUrl = imageUrl
                        )
                        val supabaseEpisodes = if (!isMovieConfig) {
                            episodes.mapIndexed { index, ep -> 
                                com.example.model.supabase.SupabaseEpisode(
                                    id = java.util.UUID.randomUUID().toString(),
                                    dramaId = dramaId,
                                    title = "الحلقة ${index + 1}",
                                    embedCode = ep.embedCode,
                                    episodeNumber = index + 1
                                )
                            }
                        } else emptyList()

                        viewModel.saveDramaToSupabase(
                            drama = newDrama,
                            episodes = supabaseEpisodes,
                            onSuccess = { onBack() },
                            onError = { /* Handle error */ }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ ونشر", color = WhiteText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class EpisodeInput(val title: String, val embedCode: String)
