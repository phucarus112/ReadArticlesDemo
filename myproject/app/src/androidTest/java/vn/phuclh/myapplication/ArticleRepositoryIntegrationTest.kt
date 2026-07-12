package vn.phuclh.myapplication

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import vn.phuclh.myapplication.data.local.AppDatabase
import vn.phuclh.myapplication.data.local.ArticleDao
import vn.phuclh.myapplication.data.local.ArticleEntity

// Integration test: test Repository + DAO + Room DB thật (in-memory)
// Chạy trên device/emulator vì cần Android context để tạo Room DB
@RunWith(AndroidJUnit4::class)
class ArticleRepositoryIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ArticleDao

    private val sampleArticles =
        listOf(
            ArticleEntity(
                url = "https://example.com/1",
                title = "Article 1",
                description = "Desc 1",
                urlToImage = null,
                source = "BBC",
                publishedAt = "2024-01-02T00:00:00Z",
                content = null,
                isBookmarked = false,
                isSeen = false,
            ),
            ArticleEntity(
                url = "https://example.com/2",
                title = "Article 2",
                description = "Desc 2",
                urlToImage = null,
                source = "CNN",
                publishedAt = "2024-01-01T00:00:00Z",
                content = null,
                isBookmarked = true,
                isSeen = false,
            ),
        )

    @Before
    fun setup() {
        // in-memory DB: không ghi ra disk, tự xoá sau khi test xong
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    AppDatabase::class.java,
                ).build()
        dao = db.articleDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAll_thenObserveAll_returnsInsertedArticles() =
        runTest {
            dao.insertAll(sampleArticles)

            val result = dao.observeAll().first()

            // observeAll trả theo publishedAt DESC nên article 1 (ngày 2) trước
            assertEquals(2, result.size)
            assertEquals("https://example.com/1", result[0].url)
            assertEquals("https://example.com/2", result[1].url)
        }

    @Test
    fun insertAll_thenObserveBookmarks_returnsOnlyBookmarked() =
        runTest {
            dao.insertAll(sampleArticles)

            val bookmarks = dao.observeBookmarks().first()

            assertEquals(1, bookmarks.size)
            assertEquals("https://example.com/2", bookmarks[0].url)
            assertTrue(bookmarks[0].isBookmarked)
        }

    @Test
    fun toggleBookmark_updatesCorrectly() =
        runTest {
            dao.insertAll(sampleArticles)

            // bookmark article 1 (trước đó false)
            dao.updateBookmark("https://example.com/1", true)

            val bookmarks = dao.observeBookmarks().first()
            assertEquals(2, bookmarks.size)

            // un-bookmark article 2
            dao.updateBookmark("https://example.com/2", false)
            val afterRemove = dao.observeBookmarks().first()
            assertEquals(1, afterRemove.size)
            assertEquals("https://example.com/1", afterRemove[0].url)
        }

    @Test
    fun getUnseenCount_andMarkAllSeen_worksCorrectly() =
        runTest {
            dao.insertAll(sampleArticles)

            // cả 2 article đều isSeen = false
            assertEquals(2, dao.getUnseenCount())

            dao.markAllSeen()

            assertEquals(0, dao.getUnseenCount())
        }

    @Test
    fun insertDuplicate_doesNotReplaceExisting() =
        runTest {
            dao.insertAll(sampleArticles)

            // insert lại article 1 với title khác — IGNORE nên giữ nguyên bản gốc
            dao.insertAll(listOf(sampleArticles[0].copy(title = "Updated Title")))

            val result = dao.getByUrl("https://example.com/1")
            assertEquals("Article 1", result?.title) // title vẫn là cũ
        }
}
