plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 移除 compose 插件，因为我们使用传统View系统
}

android {
    namespace = "com.xljxlj.EinkTest"
    compileSdk = 34  // 降低到稳定版本

    defaultConfig {
        applicationId = "com.xljxlj.EinkTest"
        minSdk = 16  // Android 4.1 (Jelly Bean)
        targetSdk = 34
        versionCode = 1
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // 移除 compose 构建特性
}

dependencies {
    // 移除所有Compose相关依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)  // 添加AppCompat支持
    implementation(libs.androidx.constraintlayout)  // 添加ConstraintLayout

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}