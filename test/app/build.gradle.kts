plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.test"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        dataBinding = true
    }
    // ✅ Tmap SDK에서 JNI 접근 필요할 수 있음
    sourceSets["main"].jniLibs.srcDir("libs")

    sourceSets {
        getByName("main").jniLibs.srcDirs("libs")
        getByName("main").resources.srcDirs("libs")
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    sourceSets["main"].jniLibs.srcDir("libs")
    sourceSets["main"].resources.srcDirs("libs")
    sourceSets["main"].assets.srcDirs("libs")
}


dependencies {
    // 공식 라이브러리
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)

    // 네트워크 라이브러리
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //동영상 플레이어
    implementation("androidx.media3:media3-exoplayer:1.3.1") //동영상 플레이어
    implementation("androidx.media3:media3-ui:1.3.1")

    // 기타
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ✅ Tmap SDK V1, V2 (로컬 aar 파일)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("io.socket:socket.io-client:2.0.1") {
        exclude("org.json", "json")
    }


}

