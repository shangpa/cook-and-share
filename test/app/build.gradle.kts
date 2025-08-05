import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    //파이어 베이스 용
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
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

        // ✅ local.properties에 등록된 GCP API KEY를 resValue로 추가
        resValue("string", "gcp_vision_api_key", localProperties["GCP_VISION_API_KEY"]?.toString() ?: "")
    }

    buildTypes {
        getByName("release") {
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        dataBinding = true
    }

    // ✅ Tmap SDK에서 JNI 접근 필요할 수 있음
    sourceSets["main"].apply {
        jniLibs.srcDirs("libs")
        resources.srcDirs("libs")
        assets.srcDirs("libs")
    }

    packagingOptions {
        resources {
            excludes.add("META-INF/*")
        }
    }
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
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ✅ Tmap SDK V1, V2 (로컬 aar 파일)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //영수증
    implementation ("com.google.mlkit:text-recognition:16.0.0")

    //firebase 버전 관리용
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    //firebase 알림용
    implementation("com.google.firebase:firebase-messaging")

    //GCV 영수증
    implementation("com.google.cloud:google-cloud-vision:3.26.0")

    //구글로그인
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    //채팅용
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    //이미지 편집
    implementation(files("libs/photoeditor-debug.aar"))
    implementation ("com.github.CanHub:Android-Image-Cropper:4.3.2")

    //동영상 편집용
    implementation("androidx.media3:media3-transformer:1.3.1")
    implementation("com.google.android.material:material:1.11.0")

    implementation("io.socket:socket.io-client:2.0.1") {
        exclude(group = "org.json", module = "json")
    }

    //이미지
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // 메인-냉장고
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    //숏츠
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-datasource:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
}