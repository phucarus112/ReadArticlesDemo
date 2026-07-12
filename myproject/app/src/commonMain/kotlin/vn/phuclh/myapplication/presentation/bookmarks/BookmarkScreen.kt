package vn.phuclh.myapplication.presentation.bookmarks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import vn.phuclh.myapplication.domain.model.Article
import vn.phuclh.myapplication.presentation.components.NetworkImage

@Composable
fun BookmarkScreen(
    onArticleClick: (String) -> Unit,
    viewModel: BookmarkViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookmarkScreenContent(
        uiState = uiState,
        onArticleClick = onArticleClick,
        onRemoveBookmark = { viewModel.removeBookmark(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreenContent(
    uiState: BookmarkUiState,
    onArticleClick: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks", fontWeight = FontWeight.Bold) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            )
        },
    ) { padding ->
        if (uiState.bookmarks.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No bookmarks yet.\nTap the bookmark icon on any article to save it.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(uiState.bookmarks, key = { it.url }) { article ->
                    BookmarkItem(
                        article = article,
                        onClick = { onArticleClick(article.url) },
                        onRemove = { onRemoveBookmark(article.url) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkItem(
    article: Article,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            article.imageUrl?.let { url ->
                NetworkImage(
                    url = url,
                    modifier =
                        Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.small),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.BookmarkRemove,
                    contentDescription = "Remove bookmark",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
