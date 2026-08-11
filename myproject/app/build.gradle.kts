import java.util.Properties

val localProps =
    Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { load(it) }
    }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Koin
            implementation(libs.koin.core)

            // Ktor
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Room KMP
            implementation(libs.room.runtime)

            // Immutable collections — stable types cho Compose
            implementation(libs.immutable.collections)

            // Coroutines
            implementation(libs.coroutines.android)

            // Lifecycle
            implementation(libs.androidx.lifecycle.runtime.ktx)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)

            // Koin Android
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            // Ktor OkHttp engine (needed so Chucker can attach as an OkHttp interceptor)
            implementation(libs.ktor.client.okhttp)

            // WorkManager
            implementation("androidx.work:work-runtime-ktx:2.10.0")

            // Coil
            implementation("io.coil-kt:coil-compose:2.6.0")

            // Navigation
            implementation("androidx.navigation:navigation-compose:2.8.5")

            // Material Icons
            implementation("androidx.compose.material:material-icons-extended:1.7.8")

            // Lifecycle compose
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
        }

        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.coroutines.test)
        }
    }
}

android {
    namespace = "vn.phuclh.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "vn.phuclh.myapplication"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NEWS_API_KEY", "\"${localProps["NEWS_API_KEY"] ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Dùng debug signing cho release khi chạy benchmark local
            // KHÔNG làm vậy với APK production thật
            signingConfig = signingConfigs.getByName("debug")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        htmlReport = true
        htmlOutput = file("${layout.buildDirectory.get()}/reports/lint/lint-report.html")
        baseline = file("$projectDir/lint-baseline.xml")
        checkDependencies = true
        warningsAsErrors = false
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

ktlint {
    version.set("1.5.0")
    android.set(true)
    // ignoreFailures = true: known KMP + Compose MP issue — generated files can't be excluded
    // ktlintFormat in CI + pre-commit hook ensures source files are always formatted
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$projectDir/config/detekt/baseline.xml")
    parallel = true
}

tasks.register("staticAnalysis") {
    group = "verification"
    description = "Runs ktlint, detekt, and Android Lint"
    dependsOn("ktlintCheck", "detekt", "lintDebug")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.0.0")
    add("benchmarkImplementation", "com.github.chuckerteam.chucker:library-no-op:4.0.0")
    // Baseline Profile — nhận và apply profile khi cài app
    implementation(libs.profileinstaller)

    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.coroutines.test)
}
