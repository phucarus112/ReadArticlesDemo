package vn.phuclh.myapplication.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// expect: commonMain khai báo contract, mỗi platform implement riêng
// Android dùng Coil, iOS sau này dùng SDWebImage hoặc Ktor image loader
@Composable
expect fun NetworkImage(
    url: String,
    modifier: Modifier = Modifier,
)
