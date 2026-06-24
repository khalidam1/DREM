package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WhiteText

@Composable
fun MyListScreen(viewModel: DramaViewModel, onDramaClick: (String) -> Unit) {
    val favorites by viewModel.allFavorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
    ) {
        Text(
            text = "قائمتي",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteText,
            modifier = Modifier.padding(16.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "لا توجد مسلسلات في قائمتك بعد.", color = WhiteText)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites) { favorite ->
                    val dramaItem = DramaItem(
                        id = favorite.id,
                        title = favorite.title,
                        imageUrl = favorite.imageUrl,
                        category = favorite.category,
                        views = "" // Not stored in favorites, so we just pass empty
                    )
                    DramaPoster(drama = dramaItem, onClick = { onDramaClick(favorite.id) })
                }
            }
        }
    }
}
