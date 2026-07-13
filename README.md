# News Digest

Ứng dụng **Kotlin Multiplatform (KMP)** hiển thị tin tức nổi bật từ [NewsAPI.org](https://newsapi.org), cho phép lưu bookmark để đọc offline và nhận thông báo nền khi có bài viết mới. UI được viết một lần bằng **Compose Multiplatform**, sẵn sàng mở rộng sang iOS.

> **Cấu trúc repo:** toàn bộ code nằm trong thư mục con **`myproject/`**, không phải ở gốc repo. Mọi lệnh Gradle chạy từ trong `myproject/`.

---

## Tech Stack

| Lớp | Công nghệ |
|-----|-----------|
| Ngôn ngữ | Kotlin 2.1, Kotlin Multiplatform |
| UI | Compose Multiplatform (`commonMain`) |
| DI | Koin (KMP-compatible, không annotation) |
| Networking | Ktor Client + engine OkHttp |
| Serialization | kotlinx.serialization |
| Database | Room KMP 2.7 (source of truth) |
| Background | WorkManager (`PeriodicWorkRequest` 15 phút) |
| Ảnh | Coil |
| Điều hướng | Navigation Compose |
| Debug mạng | Chucker (chỉ bản debug) |
| Phát hiện leak | LeakCanary (chỉ bản debug) |
| Static analysis | ktlint, detekt, Android Lint |
| CI | GitHub Actions |

---

## Hướng dẫn Build và Chạy

### Yêu cầu

| Công cụ | Phiên bản |
|---------|-----------|
| Android Studio | Ladybug trở lên |
| JDK | 17 trở lên (CI dùng Temurin 17; máy dev dùng JDK nào cũng được, Gradle tự nhận qua `JAVA_HOME`) |
| Android SDK | API 35 (compile) / API 26 tối thiểu |

> JDK **không còn bị hardcode** trong `gradle.properties` — Gradle dùng JDK mà môi trường cung cấp, nên project chạy được trên cả máy local lẫn CI.

### Cấu hình API Key

1. Đăng ký key miễn phí tại <https://newsapi.org/register>.
2. Mở (hoặc tạo) file `myproject/local.properties`.
3. Thêm dòng:
   ```
   NEWS_API_KEY=your_key_here
   ```
   `local.properties` đã nằm trong `.gitignore` và **không bao giờ được commit**. Key được inject vào `BuildConfig` lúc build → `ApiConfig.NEWS_API_KEY`.

Với build trên CI, key được nạp từ **GitHub Actions secret** tên `NEWS_API_KEY` (Settings → Secrets and variables → Actions).

### Build & Chạy

```bash
cd myproject

# Build APK debug
./gradlew assembleDebug

# Cài trực tiếp lên thiết bị/máy ảo đang kết nối
./gradlew installDebug
```

Hoặc mở project bằng Android Studio và nhấn **Run** (Shift+F10).

---

## Kiến trúc

### Clean Architecture + MVI trên nền KMP

```
commonMain/                     ← code chạy mọi platform
├── presentation/   Compose UI, ViewModel, UiState, Intent (MVI)
├── domain/         Use case, interface repository, domain model
├── data/           Room, Ktor, repository implementation
├── platform/       expect fun: scheduleBackgroundSync, showNewArticlesNotification
└── util/           expect: ApiConfig, UrlEncoder

androidMain/                    ← implementation riêng Android
├── data/di/        Koin module, NewsSyncWorker (WorkManager)
├── platform/       actual: WorkManager, NotificationManager
├── util/           actual: BuildConfig, java.net.URLEncoder
└── MyApplication, MainActivity
```

### Luồng dữ liệu một chiều (unidirectional)

```
Người dùng → ViewModel.handleIntent(ArticlesIntent) → UseCase → Repository
                                                                    ↓
                                                     Room (source of truth)
                                                                    ↓
                              ViewModel ← StateFlow ← Dao.observe*() (Flow)
                                  ↓
                       Compose UI (collectAsStateWithLifecycle)
```

### Các quyết định thiết kế nổi bật

**Room là source of truth.** `refreshArticles()` ghi dữ liệu vào Room; UI lắng nghe Room qua `Flow`. Nhờ đó màn hình luôn hiển thị dữ liệu từ ổ đĩa — pull-to-refresh không chặn danh sách, và đọc offline hoạt động tự nhiên.

**MVI với sealed class `ArticlesIntent`.** Mọi hành động người dùng được mô hình hóa thành intent có kiểu rõ ràng, gửi vào một hàm `handleIntent()` duy nhất — state machine dễ đọc, dễ test.

**Koin thay Hilt.** Hilt chỉ chạy Android; Koin tương thích KMP nên dùng chung được cho cả iOS sau này. Khai báo bằng DSL thuần (`single`, `factory`, `viewModelOf`), không cần annotation hay annotation processor.

**`expect`/`actual` cho phần platform-specific.** Business logic + UI viết một lần ở `commonMain`; chỉ WorkManager, Notification, BuildConfig, URL encoding mới có bản `actual` riêng cho Android. Thêm iOS chỉ cần viết thêm các `actual`.

**Ktor engine OkHttp.** Chọn engine OkHttp (thay vì Android) để **Chucker** cắm được vào tầng interceptor, xem toàn bộ request/response khi debug. Bản release dùng `chucker-no-op` nên không ảnh hưởng production.

**`BuildConfig` cho thông tin nhạy cảm.** API key đọc từ `local.properties` lúc build, inject vào `BuildConfig`. Không có secret nào nằm trong source code.

---

## Static Analysis & CI

Gộp 3 công cụ vào một task:

```bash
cd myproject
./gradlew staticAnalysis   # ktlint + detekt + Android Lint
```

Pre-commit hook tự chạy `ktlintFormat` trước mỗi commit — cài một lần:

```bash
git config core.hooksPath myproject/.githooks
```

**GitHub Actions** (`.github/workflows/android.yml`) chạy mỗi lần push/PR vào `main`:

```
checkout → JDK 17 → ktlint format check → staticAnalysis
        → testDebugUnitTest → assembleDebug + bundleDebug
        → upload APK/AAB lên Artifacts
```

APK/AAB tải về ở tab **Actions** → chọn run xanh → mục **Artifacts** (cuối trang).

---

## Hướng cải thiện nếu có thêm thời gian

- **Mở rộng sang iOS.** Kiến trúc KMP đã sẵn sàng — chỉ cần thêm source set `iosMain` và implement các `actual fun` (BGTaskScheduler, UNUserNotificationCenter, Ktor Darwin engine).
- **Phân trang.** Tích hợp Paging 3 để tải thêm dần thay vì giới hạn cứng số bài.
- **Tìm kiếm.** Thanh tìm kiếm với debounce hoặc full-text search của Room (`FTS4`).
- **UX lỗi chi tiết.** Phân biệt lỗi mạng với lỗi hết quota (HTTP 429) và hiển thị thông báo phù hợp.
- **Mở rộng test.** Đã có unit test (MockEngine) + integration test (Room in-memory) + UI test (Compose). Bổ sung coverage cho các use case còn lại.
- **Proguard / R8.** `isMinifyEnabled` hiện là `false`; bật lên với rules phù hợp để giảm kích thước và obfuscate bản release.
