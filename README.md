# Smart Folder - AI 기반 스마트 파일 정리 앱

<div align="center">
  <h3>📁 파일을 스마트하게 관리하세요</h3>
  <p>AI 기술을 활용한 자동 파일 분류 및 중복 파일 탐지</p>
</div>

---

## 🎯 주요 기능

### ✨ AI 기반 자동 파일 분류
- 사진, 동영상, 문서, 음악, APK 등 자동으로 카테고리별 분류
- 머신러닝 기반 스마트 분류 알고리즘
- 사용자 패턴 학습으로 정확도 지속 향상

### 🔍 중복 파일 탐지
- 파일 해시 비교를 통한 정확한 중복 파일 탐지
- 한 번에 여러 중복 파일 삭제 가능
- 저장 공간 최적화

### 📊 파일 통계 및 분석
- 카테고리별 파일 크기 시각화
- 저장 공간 사용량 분석
- 파일 개수 및 분포 차트

### 🔔 스마트 알림
- 정기적인 파일 정리 알림
- 중복 파일 발견 시 알림
- 저장 공간 부족 경고

### 🎨 현대적인 UI/UX
- Material Design 3 적용
- 다크 모드 지원
- 직관적이고 깔끔한 인터페이스

---

## 🛠 기술 스택

### 프레임워크 & 언어
- **Kotlin** - 주 개발 언어
- **Jetpack Compose** - 선언적 UI 프레임워크
- **Material Design 3** - 디자인 시스템

### 아키텍처 & 라이브러리
- **MVVM Architecture** - 뷰모델 기반 아키텍처
- **Room Database** - 로컬 데이터베이스
- **Kotlin Coroutines** - 비동기 처리
- **DataStore** - 사용자 설정 저장
- **WorkManager** - 백그라운드 작업

### 통합 서비스
- **Firebase Cloud Messaging** - 푸시 알림
- **Firebase Analytics** - 사용자 분석
- **Google AdMob** - 광고 수익화
- **Coil** - 이미지 로딩

---

## 📦 프로젝트 구조

```
app/
├── src/main/java/com/example/smart_folder_1/
│   ├── data/               # 데이터 레이어
│   │   ├── database/       # Room 데이터베이스
│   │   ├── model/          # 데이터 모델
│   │   └── repository/     # 리포지토리
│   ├── ui/                 # UI 레이어
│   │   ├── components/     # 재사용 가능한 컴포넌트
│   │   ├── screens/        # 화면 컴포저블
│   │   ├── theme/          # 테마 설정
│   │   └── viewmodel/      # 뷰모델
│   ├── ml/                 # 머신러닝 파일 분류기
│   ├── worker/             # WorkManager 작업
│   ├── notification/       # 알림 스케줄러
│   ├── fcm/                # Firebase 메시징
│   ├── ads/                # AdMob 광고 관리
│   └── utils/              # 유틸리티 클래스
└── src/main/res/           # 리소스 파일
```

---

## 🚀 빌드 및 실행

### 요구사항
- **Android Studio** Hedgehog (2023.1.1) 이상
- **JDK** 17
- **Android SDK** 34
- **Minimum SDK** 26 (Android 8.0)

### 빌드 방법

```bash
# 1. 저장소 클론
git clone https://github.com/leemanrank/smart_folder.git
cd smart_folder

# 2. Firebase 설정 (필수!)
# Firebase Console에서 google-services.json 다운로드
# app/ 디렉토리에 google-services.json 파일 추가
# 자세한 내용은 아래 "Firebase 설정" 섹션 참조

# 3. Debug 빌드
./gradlew assembleDebug

# 4. Release 빌드 (서명 필요)
./gradlew assembleRelease  # APK
./gradlew bundleRelease    # AAB (권장)
```

### Firebase 설정

이 프로젝트는 Firebase를 사용하므로 `google-services.json` 파일이 필요합니다.

1. **Firebase Console 접속**
   - https://console.firebase.google.com/ 방문
   - 새 프로젝트 생성 또는 기존 프로젝트 선택

2. **Android 앱 추가**
   - 패키지 이름: `com.example.smart_folder_1`
   - 앱 닉네임: Smart Folder (선택사항)
   - SHA-1 서명 인증서: 나중에 추가 가능

3. **google-services.json 다운로드**
   - Firebase에서 `google-services.json` 파일 다운로드
   - 파일을 `app/` 디렉토리에 복사

4. **Firebase 서비스 활성화**
   - Cloud Messaging (FCM) - 푸시 알림용
   - Analytics - 사용자 분석용

**⚠️ 보안 주의사항:**
- `google-services.json` 파일은 Git에 커밋되지 않습니다
- 각 개발자는 자신의 Firebase 프로젝트를 사용해야 합니다

### Android Studio에서 실행
1. Android Studio에서 프로젝트 열기
2. Gradle Sync 완료 대기
3. 실제 기기 또는 에뮬레이터 연결
4. Run 버튼 클릭 (Shift+F10)

---

## 📱 출시 준비

앱을 Google Play Store에 출시하려면 다음 문서를 참조하세요:

- **[RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md)** - 출시 체크리스트
- **[RELEASE_GUIDE.md](RELEASE_GUIDE.md)** - 상세한 출시 가이드
- **[PLAY_STORE_LISTING.md](PLAY_STORE_LISTING.md)** - Play Store 등록 정보

### 빠른 시작

1. ✅ **빌드 완료됨**
   - Release APK: `app/build/outputs/apk/release/`
   - Release AAB: `app/build/outputs/bundle/release/`

2. ⚠️ **필수 작업**
   - [ ] 키스토어 생성 및 서명
   - [ ] AdMob App ID 변경 (현재 테스트 ID)
   - [ ] 개인정보처리방침 URL 생성

3. 📋 **선택 작업**
   - [ ] 그래픽 에셋 준비 (아이콘, 스크린샷 등)
   - [ ] Google Play Console 등록

---

## 🔒 개인정보 보호

Smart Folder는 사용자의 개인정보를 매우 중요하게 생각합니다:

- ✅ 모든 파일 정보는 **기기 내부에만 저장**
- ✅ 외부 서버로 **파일 정보 전송 안 함**
- ✅ **안전한 로컬 처리**
- ✅ 투명한 권한 사용

자세한 내용은 [개인정보처리방침](app/src/main/assets/privacy_policy.html)을 참조하세요.

---

## 📋 필요 권한

```xml
<!-- 파일 접근 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- 미디어 접근 (Android 13+) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- 알림 -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- 인터넷 (광고 및 FCM) -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🧪 테스트

### 단위 테스트
```bash
./gradlew test
```

### UI 테스트
```bash
./gradlew connectedAndroidTest
```

---

## 📈 버전 히스토리

### v1.0.0 (2025-01-XX)
- 🎉 첫 출시
- ✨ AI 기반 자동 파일 분류
- 🔍 중복 파일 탐지
- 📊 파일 통계 및 분석
- 🔔 스마트 알림 시스템
- 🎨 Material Design 3 UI
- 🌙 다크 모드 지원

---

## 🤝 기여

프로젝트에 기여하고 싶으시다면:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 라이선스

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📞 문의

- **개발자**: Smart Folder Development Team
- **이메일**: leemanrank@gmail.com
- **버전**: 1.0.0

---

## 🙏 감사의 글

- **Jetpack Compose Team** - 훌륭한 UI 프레임워크 제공
- **Material Design Team** - 아름다운 디자인 시스템
- **Firebase Team** - 강력한 백엔드 서비스
- **모든 오픈소스 기여자들** - 프로젝트를 가능하게 해주신 분들

---

<div align="center">
  <p>Made with ❤️ in Korea</p>
  <p>© 2025 Smart Folder. All rights reserved.</p>
</div>
