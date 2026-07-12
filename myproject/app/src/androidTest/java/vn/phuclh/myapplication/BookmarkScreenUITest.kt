package vn.phuclh.myapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import vn.phuclh.myapplication.domain.model.Article
import vn.phuclh.myapplication.presentation.bookmarks.BookmarkScreenContent
import vn.phuclh.myapplication.presentation.bookmarks.BookmarkUiState
import vn.phuclh.myapplication.ui.theme.MyApplicationTheme

// UI test: test Composable trực tiếp, không cần ViewModel/DB thật
// Inject state giả vào để kiểm soát hoàn toàn scenario
@RunWith(AndroidJUnit4::class)
class BookmarkScreenUITest {
    @get:Rule
    val composeRule = createComposeRule()

    private val fakeArticles =
        listOf(
            Article(
                url = "https://example.com/1",
                title = "Jetpack Compose Tips",
                source = "Medium",
                description = null,
                imageUrl = null,
                content = null,
                publishedAt = "2024-01-01T00:00:00Z",
                isBookmarked = true,
            ),
            Article(
                url = "https://example.com/2",
                title = "Kotlin Coroutines Guide",
                source = "ProAndroidDev",
                description = null,
                imageUrl = null,
                content = null,
                publishedAt = "2024-01-02T00:00:00Z",
                isBookmarked = true,
            ),
        )

    @Test
    fun emptyState_showsEmptyMessage() {
        composeRule.setContent {
            MyApplicationTheme {
                // Test Composable con trực tiếp với state rỗng, không qua hiltViewModel
                BookmarkScreenContent(
                    uiState = BookmarkUiState(bookmarks = emptyList()),
                    onArticleClick = {},
                    onRemoveBookmark = {},
                )
            }
        }

        composeRule.onNodeWithText("No bookmarks yet.").assertIsDisplayed()
    }

    @Test
    fun withBookmarks_showsArticleTitles() {
        composeRule.setContent {
            MyApplicationTheme {
                BookmarkScreenContent(
                    uiState = BookmarkUiState(bookmarks = fakeArticles),
                    onArticleClick = {},
                    onRemoveBookmark = {},
                )
            }
        }

        composeRule.onNodeWithText("Jetpack Compose Tips").assertIsDisplayed()
        composeRule.onNodeWithText("Kotlin Coroutines Guide").assertIsDisplayed()
    }

    @Test
    fun clickArticle_triggersOnArticleClick() {
        var clickedUrl = ""

        composeRule.setContent {
            MyApplicationTheme {
                BookmarkScreenContent(
                    uiState = BookmarkUiState(bookmarks = fakeArticles),
                    onArticleClick = { clickedUrl = it },
                    onRemoveBookmark = {},
                )
            }
        }

        composeRule.onNodeWithText("Jetpack Compose Tips").performClick()

        assertTrue(clickedUrl == "https://example.com/1")
    }

    @Test
    fun clickRemoveBookmark_triggersOnRemoveBookmark() {
        var removedUrl = ""

        composeRule.setContent {
            MyApplicationTheme {
                BookmarkScreenContent(
                    uiState = BookmarkUiState(bookmarks = fakeArticles),
                    onArticleClick = {},
                    onRemoveBookmark = { removedUrl = it },
                )
            }
        }

        // click icon xoá của article đầu tiên
        composeRule
            .onNodeWithContentDescription("Remove bookmark")
            .performClick()

        assertTrue(removedUrl.isNotEmpty())
    }
}
