# \# News Digest

# 

# Ứng dụng Android hiển thị các tin tức nổi bật từ \[NewsAPI.org](https://newsapi.org), cho phép người dùng lưu bookmark để đọc offline và nhận thông báo khi có bài viết mới.

# 

# \---

# 

# \## Hướng Dẫn Build và Chạy

# 

# \### Yêu cầu

# 

# | Công cụ | Phiên bản |

# |---------|-----------|

# | Android Studio | Hedgehog trở lên |

# | JDK | 18.0.2 (JDK hệ thống, \*\*không dùng\*\* JBR của Android Studio) |

# | Android SDK | API 35 (compile) / API 26 tối thiểu |

# 

# > \*\*Lưu ý JDK:\*\* `gradle.properties` trỏ Gradle đến `C:\\Program Files\\Java\\jdk-18.0.2`. Nếu JDK của bạn cài ở chỗ khác, hãy cập nhật `org.gradle.java.home` trong `gradle.properties` cho phù hợp.

# 

# \### Cấu hình API Key

# 

# 1\. Đăng ký key miễn phí tại <https://newsapi.org/register>.

# 2\. Mở (hoặc tạo) file `local.properties` ở thư mục gốc của project.

# 3\. Thêm dòng sau:

# &#x20;  ```

# &#x20;  NEWS\_API\_KEY=your\_key\_here

# &#x20;  ```

# &#x20;  `local.properties` đã có trong `.gitignore` và \*\*không bao giờ được commit\*\* lên source control.

# 

# \### Build \& Chạy

# 

# ```bash

# \# Build APK debug

# ./gradlew assembleDebug

# 

# \# Cài trực tiếp lên thiết bị/máy ảo đang kết nối

# ./gradlew installDebug

# ```

# 

# Hoặc mở project bằng Android Studio và nhấn \*\*Run\*\* (Shift+F10).

# 

# \---

# 

# \## Kiến Trúc

# 

# \### Clean Architecture + MVI

# 

# ```

# presentation/   ← Compose, ViewModel, UiState, Intent

# domain/         ← Use case, interface repository, domain model

# data/           ← Room, Retrofit, WorkManager, implementation repository

# util/           ← ApiConfig, NotificationHelper

# ```

# 

# \*\*Luồng dữ liệu một chiều (unidirectional):\*\*

# 

# ```

# Người dùng → ViewModel.handleIntent() → UseCase → Repository

# &#x20;                                                       ↓

# &#x20;                                             Room (source of truth)

# &#x20;                                                       ↓

# &#x20;                             ViewModel ← StateFlow ← Dao.observe\*()

# &#x20;                                 ↓

# &#x20;                           Compose UI (collectAsStateWithLifecycle)

# ```

# 

# \### Các quyết định thiết kế nổi bật

# 

# \*\*`refreshArticles()` ghi dữ liệu vào Room; giao diện lắng nghe Room qua `Flow`. Nhờ đó, màn hình luôn hiển thị dữ liệu từ ổ đĩa — pull-to-refresh không bao giờ chặn danh sách và chức năng đọc offline hoạt động tự nhiên.

# 

# \*\*MVI với sealed class `ArticlesIntent`.\*\* Tất cả hành động của người dùng được mô hình hóa thành các intent có kiểu rõ ràng, gửi vào một hàm `handleIntent()` duy nhất trên ViewModel, giúp state machine dễ đọc và dễ test.

# 

# \*\*`@Immutable` trên domain model.\*\* Đánh dấu `Article` bằng `@Immutable` giúp Compose compiler bỏ qua recompose cho các item trong danh sách không có dữ liệu thay đổi, giảm lag khi cuộn.

# 

# \*\*WorkManager + `HiltWorkerFactory`.\*\* Đồng bộ nền chạy mỗi 15 phút qua `PeriodicWorkRequest`. Auto-initializer mặc định của WorkManager bị tắt trong manifest để Hilt có thể cung cấp worker factory, giữ DI nhất quán toàn app.

# 

# \*\*`BuildConfig` cho thông tin nhạy cảm.\*\* API key được đọc từ `local.properties` lúc build và inject vào `BuildConfig`. Không có secret nào tồn tại trong source code.

# 

# \---

# 

# \## Hướng Cải Thiện Nếu Có Thêm Thời Gian

# 

# \- \*\*Phân trang (Pagination).\*\* NewsAPI hỗ trợ `page` và `pageSize`. Tích hợp Jetpack Paging 3 sẽ cho phép danh sách tải thêm dần thay vì giới hạn cứng 30 bài.

# 

# \- \*\*Tìm kiếm.\*\* Thanh tìm kiếm kết hợp debounce gọi API hoặc full-text search của Room (`FTS4`) sẽ nâng cao đáng kể tính hữu dụng của app.

# 

# \- \*\*UX lỗi chi tiết hơn.\*\* Hiện tại lỗi chỉ hiển thị nút retry đơn giản. Phân biệt lỗi mạng với lỗi hết quota API (HTTP 429) và hiển thị thông báo phù hợp sẽ cải thiện trải nghiệm người dùng.

# 

# \- \*\*Unit test cho use case và ViewModel.\*\* Kiến trúc hiện tại hoàn toàn có thể test — use case là hàm thuần túy trên interface repository, ViewModel expose `StateFlow` có thể collect trong `runTest`. Đây là bước tiếp theo có giá trị cao nhất.

# 

# \- \*\*CI pipeline.\*\* Một workflow GitHub Actions chạy `./gradlew test lint` trên mỗi PR sẽ phát hiện lỗi sớm.

# 

# \- \*\*Proguard / R8 cho bản release.\*\* `isMinifyEnabled` hiện đang là `false`; bật lên với file rules phù hợp sẽ giảm kích thước APK và obfuscate binary.



