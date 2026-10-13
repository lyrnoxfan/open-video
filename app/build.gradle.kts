plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lxvideo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lxvideo"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "1.8.2"
        // 视频源服务器与密码 baked 进 BuildConfig（免登录设计）。
        // 换成你自己的视频源地址；如需鉴权，填入 PD_PASSWORD。
        // 注意：含真实地址/密码的 APK 请勿公开发布。
        buildConfigField("String", "PD_SERVER", "\"https://your-video-server.example.com\"")
        buildConfigField("String", "PD_PASSWORD", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 固定 debug 签名：VM 更换会导致默认 ~/.android/debug.keystore 丢失，
    // 曾在 Blog 项目出现签名对不上的事故。本地构建用这把 key，保证后续版本都能直接覆盖安装；
    // CI 上没有这把 key 时回退默认 debug 签名。
    signingConfigs {
        getByName("debug") {
            val ks = file("/home/hatch/workspace/apk-build/lyrnox-debug.keystore")
            if (ks.exists()) {
                storeFile = ks
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    // 注意: compose 1.7+ 的 KMP 产物里 core/extended 图标都在 icons.filled 同一个包下,
    // extended 只是补全 core 没有的图标 (Image/Movie/Folder/GridView 等)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(libs.kotlinx.coroutines.android)

    // HTTP + cookies
    implementation(libs.okhttp)
    // 音视频播放
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    // 本地存储：点赞/收藏/历史/断点（DataStore Preferences，无代码生成）
    implementation(libs.androidx.datastore.preferences)

    // 删掉 security-crypto 后 exifinterface 降级到 1.3.6（本地缓存只有 1.3.7），
    // 显式钉住 1.3.7，与老 App 解析结果一致
    constraints {
        implementation("androidx.exifinterface:exifinterface:1.3.7")
    }
}
