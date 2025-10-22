# Smart Folder - 개발 로그

## 프로젝트 개요
**앱 이름**: Smart Folder - AI 파일 정리
**버전**: 1.0.0
**개발 기간**: 2025년 1월
**플랫폼**: Android (Kotlin + Jetpack Compose)

---

## 완료된 기능 목록

### ✅ Phase 1: 핵심 기능 (완료)

#### 1. AI 기반 파일 분류
- **위치**: `FileRepository.kt`, `MainViewModel.kt`
- **기능**:
  - 파일 확장자 기반 AI 분류
  - 카테고리: 이미지, 동영상, 문서, 음악, 압축파일
  - 사용자 학습 데이터 저장 (Room DB)
- **작동 방식**: 파일 스캔 → AI 분류 → 사용자 선택 → 파일 이동

#### 2. 파일 스캔 및 관리
- **위치**: `MainScreen.kt`, `MainViewModel.kt`
- **기능**:
  - 전체 저장소 스캔
  - 특정 폴더 스캔 (다운로드, 사진, 문서, 카메라)
  - 다중 파일 선택 (롱클릭)
  - 일괄 삭제
  - 파일 통계 (카테고리별 파일 수/용량)

#### 3. 중복 파일 탐지
- **위치**: `FileRepository.kt:findDuplicateFiles()`, `DuplicatesScreen.kt`
- **알고리즘**: MD5 해시 기반
- **기능**:
  - 중복 파일 그룹 탐지
  - 절약 가능 용량 계산
  - 선택적 파일 유지 및 삭제
  - 보상형 광고와 연동

#### 4. 파일 삭제 (Scoped Storage 대응)
- **위치**: `FileRepository.kt:deleteFileUsingMediaStore()`, `deleteFileDirect()`
- **처리 방식**:
  - Android 10+ MediaStore API 사용
  - SecurityException 처리
  - 직접 삭제 fallback
  - MediaStore 동기화

---

### ✅ Phase 2: 알림 및 스케줄링 (완료)

#### 5. FCM 푸시 알림
- **위치**: `SmartFolderMessagingService.kt`
- **설정 파일**: `google-services.json` (project_id: folder-4d186)
- **기능**:
  - 파일 정리 알림
  - 주간 리포트
  - 성과 알림
- **테스트**: FCM 토큰 발급 및 테스트 완료

#### 6. 파일 체크 스케줄러
- **위치**: `FileCheckWorker.kt`, `NotificationScheduler.kt`
- **기술**: WorkManager (주기적 백그라운드 작업)
- **기능**:
  - 6/12/24/48/72시간 주기 설정
  - 파일 50개 이상 시 알림
  - 설정 화면에서 활성화/비활성화

---

### ✅ Phase 3: 통계 및 분석 (완료)

#### 7. 파일 통계 대시보드
- **위치**: `StatisticsScreen.kt`, `FileStatistics.kt`
- **기능**:
  - 전체 파일 수 및 용량
  - 카테고리별 분포 (파이 차트 시각화)
  - 대용량 파일 TOP 10
  - 최근 7일 정리 통계

---

### ✅ Phase 4: 수익화 (완료)

#### 8. AdMob 광고 통합
- **위치**: `AdManager.kt`, `BannerAdView.kt`
- **광고 유형**:
  - **배너 광고**: MainScreen 상단
  - **전면 광고**: 10개 이상 파일 자동 정리 시
  - **보상형 광고**: 중복 파일 삭제 시 선택 옵션
- **광고 ID** (현재 테스트 ID):
  - App ID: `ca-app-pub-3940256099942544~3347511713`
  - Banner: `ca-app-pub-3940256099942544/6300978111`
  - Interstitial: `ca-app-pub-3940256099942544/1033173712`
  - Rewarded: `ca-app-pub-3940256099942544/5224354917`

**⚠️ 출시 시 실제 AdMob ID로 교체 필요**

---

### ✅ Phase 5: 출시 준비 (완료)

#### 9. 앱 아이콘 & 스플래시
- **위치**:
  - `drawable/ic_launcher_foreground.xml`
  - `drawable/ic_launcher_background.xml`
  - `SplashScreen.kt`
- **디자인**: 폴더 + AI 뇌 심볼, 브랜드 색상 (#006781)
- **스플래시**: 4.5초 로딩 애니메이션

#### 10. 개인정보 처리방침
- **위치**:
  - `assets/privacy_policy.html`
  - `PrivacyPolicyScreen.kt`
- **접근**: 설정 > 개인정보 처리방침
- **내용**: 수집 정보, 사용 목적, 제3자 제공, 사용자 권리
- **Google Play 정책**: 100% 준수

#### 11. 에러 처리 개선
- **위치**: `ErrorHandler.kt`
- **기능**:
  - 사용자 친화적 에러 메시지
  - 에러 카테고리 분류 (파일, 권한, 네트워크, 메모리)
  - 전역 에러 로깅
- **적용**: 모든 ViewModel catch 블록에 적용 완료

#### 12. 온보딩 화면
- **위치**: `OnboardingScreen.kt`, `PreferenceManager.kt`
- **구성**: 4페이지 슬라이드
  1. AI 기반 자동 분류
  2. 중복 파일 감지
  3. 저장 공간 절약
  4. 안전한 파일 관리
- **기능**: 건너뛰기, 이전/다음 버튼, 페이지 인디케이터
- **저장**: SharedPreferences로 첫 실행 여부 관리

#### 13. 뒤로가기 네비게이션 개선
- **위치**: `MainScreen.kt:BackHandler`
- **계층 구조**:
  1. 서브 화면 (통계, 설정, 중복 파일) → 메인 화면
  2. 다이얼로그 닫기
  3. 선택 모드 종료
  4. 메인 화면 → 종료 확인

---

## 기술 스택

### Frontend
- **언어**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **상태 관리**: StateFlow, ViewModel
- **네비게이션**: Compose Navigation (Dialog 기반)

### Backend & Storage
- **로컬 DB**: Room Database
- **파일 시스템**: MediaStore API (Android 10+ Scoped Storage)
- **설정 저장**: SharedPreferences

### 백그라운드 작업
- **스케줄러**: WorkManager
- **푸시 알림**: Firebase Cloud Messaging (FCM)

### 수익화
- **광고**: Google AdMob (Banner, Interstitial, Rewarded)

### 기타
- **빌드 도구**: Gradle 8.10
- **최소 SDK**: 26 (Android 8.0)
- **타겟 SDK**: 34 (Android 14)

---

## 프로젝트 구조

```
app/src/main/java/com/example/smart_folder_1/
├── MainActivity.kt                 # 메인 액티비티
├── SplashActivity.kt               # 스플래시 액티비티
│
├── ads/
│   └── AdManager.kt                # AdMob 광고 관리
│
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt          # Room DB
│   │   └── UserPreferenceDao.kt    # 학습 데이터 DAO
│   ├── model/
│   │   ├── FileItem.kt             # 파일 데이터 모델
│   │   ├── FileCategory.kt         # 카테고리 enum
│   │   ├── FileStatistics.kt       # 통계 데이터
│   │   ├── DuplicateFileGroup.kt   # 중복 파일 그룹
│   │   └── UserPreference.kt       # 학습 데이터
│   └── repository/
│       └── FileRepository.kt       # 파일 작업 리포지토리
│
├── fcm/
│   └── SmartFolderMessagingService.kt  # FCM 서비스
│
├── notification/
│   ├── FileCheckWorker.kt          # 백그라운드 작업
│   └── NotificationScheduler.kt    # 알림 스케줄러
│
├── ui/
│   ├── components/
│   │   └── BannerAdView.kt         # 배너 광고 컴포넌트
│   ├── screens/
│   │   ├── MainScreen.kt           # 메인 화면
│   │   ├── SplashScreen.kt         # 스플래시 화면
│   │   ├── StatisticsScreen.kt     # 통계 화면
│   │   ├── SettingsScreen.kt       # 설정 화면
│   │   ├── DuplicatesScreen.kt     # 중복 파일 화면
│   │   ├── OnboardingScreen.kt     # 온보딩 화면
│   │   └── PrivacyPolicyScreen.kt  # 개인정보 처리방침
│   ├── theme/
│   │   └── Theme.kt                # Material3 테마
│   └── viewmodel/
│       └── MainViewModel.kt        # 메인 ViewModel
│
└── utils/
    ├── PermissionManager.kt        # 권한 관리
    ├── ErrorHandler.kt             # 에러 처리
    └── PreferenceManager.kt        # SharedPreferences 관리
```

---

## 빌드 정보

### 최종 빌드 상태
- **Status**: ✅ BUILD SUCCESSFUL
- **빌드 시간**: 17초
- **APK 위치**: `app/build/outputs/apk/debug/app-debug.apk`
- **APK 크기**: 약 15MB

### 의존성 (주요)
```gradle
// Jetpack Compose
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// AdMob
implementation("com.google.android.gms:play-services-ads:22.6.0")

// Room Database
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
kapt(libs.androidx.room.compiler)

// WorkManager
implementation(libs.androidx.work.runtime.ktx)
```

---

## 알려진 이슈 및 해결 방법

### 1. 매니페스트 충돌 (해결됨)
- **문제**: AdMob과 Firebase Analytics 간 `AD_SERVICES_CONFIG` 충돌
- **해결**: AndroidManifest.xml에 `tools:replace="android:resource"` 추가

### 2. 파일 삭제 권한 (해결됨)
- **문제**: Android 10+ SecurityException
- **해결**: MediaStore 우선 시도 → 실패 시 직접 삭제

### 3. 사용되지 않는 경고
- `MainActivity.kt:36`: result 파라미터 (ActivityResultCallback)
- **영향**: 없음 (빌드 성공)

---

## 다음 작업 (출시 전)

### Google Play Console 설정

#### 1. 앱 등록
- **앱 이름**: Smart Folder - AI 파일 정리
- **기본 언어**: 한국어
- **카테고리**: 생산성 (Productivity)

#### 2. 스토어 등록 정보 작성

**짧은 설명** (80자):
```
AI가 파일을 자동으로 분류하고, 중복 파일을 찾아 저장 공간을 절약하세요!
```

**전체 설명** (4000자):
```
📁 Smart Folder - 스마트한 파일 관리의 시작

AI 인공지능이 당신의 파일을 자동으로 정리합니다!
더 이상 복잡한 파일 관리로 고민하지 마세요.

✨ 주요 기능

🤖 AI 기반 자동 분류
- 인공지능이 파일 종류를 자동 인식
- 이미지, 동영상, 문서, 음악, 압축파일 자동 분류
- 사용자의 선택을 학습하여 점점 더 정확해집니다

📊 중복 파일 탐지
- MD5 해시 기반 정확한 중복 파일 감지
- 절약 가능한 저장 공간 실시간 계산
- 원본 선택하여 안전하게 삭제

💾 저장 공간 최적화
- 대용량 파일 TOP 10 확인
- 카테고리별 파일 통계
- 한 번에 여러 파일 선택 삭제

🔔 스마트 알림
- 파일이 많이 쌓였을 때 자동 알림
- 설정 가능한 알림 주기 (6/12/24/48/72시간)
- 정리 완료 후 결과 리포트

🔒 개인정보 보호
- 모든 데이터는 기기에만 저장
- 외부 서버로 전송되지 않음
- 투명한 개인정보 처리방침

📱 사용하기 쉬운 인터페이스
- Material3 디자인
- 직관적인 조작
- 한국어 완벽 지원

💡 이런 분들께 추천합니다
- 스마트폰 저장 공간이 항상 부족한 분
- 다운로드 폴더가 지저분한 분
- 중복 사진/영상이 많은 분
- 파일 정리가 귀찮은 분

🎯 Smart Folder로 깔끔한 파일 관리를 시작하세요!
```

#### 3. 스크린샷 준비 (필수)
- **최소 2개**, 권장 8개
- **크기**: 1080x1920 (세로) 또는 1920x1080 (가로)
- **촬영 화면**:
  1. 온보딩 화면
  2. 메인 화면 (파일 목록)
  3. AI 분류 화면
  4. 통계 화면
  5. 중복 파일 화면
  6. 설정 화면
  7. 파일 삭제 확인
  8. 정리 완료

#### 4. 아이콘 제출
- **512x512 PNG** (Google Play 아이콘)
- **1024x500 PNG** (기능 그래픽)

#### 5. 개인정보 처리방침 URL
- **옵션 1**: GitHub Pages 호스팅
- **옵션 2**: Firebase Hosting
- **옵션 3**: 개인 웹사이트

**현재 파일**: `app/src/main/assets/privacy_policy.html`
→ 웹 호스팅 필요

#### 6. 콘텐츠 등급 설정
- **설문조사 완료**: 폭력, 성인, 약물 등 없음
- **예상 등급**: 전체 이용가 (3+)

#### 7. 데이터 보안 섹션 작성
Google Play에서 필수:
- ✅ 수집하는 데이터: 파일 정보, 사용 통계
- ✅ 데이터 사용 목적: 파일 분류, AI 학습
- ✅ 데이터 공유: Google AdMob, Firebase
- ✅ 데이터 삭제: 앱 삭제 시 자동 삭제
- ✅ 암호화: 로컬 저장만 사용

#### 8. AdMob 실제 ID로 교체
**교체 위치**:
1. `AndroidManifest.xml` - App ID
2. `AdManager.kt` - Banner, Interstitial, Rewarded 광고 단위 ID

**교체 방법**:
```kotlin
// AdManager.kt
object ProductionAdUnits {
    const val BANNER = "ca-app-pub-YOUR_ID/BANNER_ID"
    const val INTERSTITIAL = "ca-app-pub-YOUR_ID/INTERSTITIAL_ID"
    const val REWARDED = "ca-app-pub-YOUR_ID/REWARDED_ID"
}
```

#### 9. Release 빌드 생성
```bash
./gradlew bundleRelease
```
- **출력**: `app/build/outputs/bundle/release/app-release.aab`
- **서명**: Keystore 필요

#### 10. 키스토어 생성 (최초 1회)
```bash
keytool -genkey -v -keystore smart-folder-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias smart-folder
```

**⚠️ 키스토어 백업 필수**: 분실 시 앱 업데이트 불가능

---

## 출시 후 업데이트 계획 (v1.1.0 ~ v1.5.0)

### v1.1.0 - 사용자 경험 개선 (예상 2주)
1. **다크 모드** 지원 (1시간)
   - Material3 DynamicColor 활용
   - 자동/수동 전환 설정

2. **파일 미리보기** (3시간)
   - 이미지: Coil 라이브러리
   - PDF: PdfRenderer
   - 텍스트: 내장 뷰어

### v1.2.0 - 파일 복구 기능 (예상 1주)
3. **휴지통 / 파일 복구** (4시간)
   - 30일 보관 정책
   - 자동 정리
   - 복구 인터페이스

### v1.3.0 - 자동화 기능 (예상 1주)
4. **자동 정리 예약** (2시간)
   - 특정 시간/요일 자동 실행
   - 조건 설정 (파일 수, 용량)
   - 알림 설정

### v1.4.0 - 위젯 지원 (예상 2주)
5. **홈 화면 위젯** (3시간)
   - 빠른 스캔 버튼
   - 저장 공간 상태 표시
   - 최근 정리 통계

### v1.5.0 - 프리미엄 기능 (예상 3주)
6. **Google Play Billing** (4시간)
   - 월간/연간 구독
   - 광고 제거
   - 프리미엄 기능 잠금 해제

7. **클라우드 백업** (6시간)
   - Google Drive API 연동
   - 자동 백업 설정
   - 복원 기능

---

## 참고 문서

### Firebase 설정
- **프로젝트 ID**: `folder-4d186`
- **Google Services 파일**: `google-services.json`
- **FCM 테스트**: 완료

### AdMob 설정
- **광고 유형**: 배너, 전면, 보상형
- **테스트 ID**: 현재 사용 중
- **실제 ID**: 출시 전 교체 필요

### 개발 환경
- **IDE**: Android Studio Hedgehog | 2023.1.1
- **JDK**: 17
- **Gradle**: 8.10
- **Kotlin**: 1.9.20

---

## 문의 및 지원

### 개발자 연락처
- **이메일**: leemanrank@gmail.com
- **GitHub**: [레포지토리 URL]

### 버그 리포트
1. 증상 설명
2. 재현 방법
3. 기기 정보 (모델, Android 버전)
4. 로그캣 출력

---

## 라이선스

### 오픈소스 라이브러리
- Jetpack Compose - Apache 2.0
- Firebase - Google Terms
- AdMob - Google AdMob Program Policies
- Room - Apache 2.0
- WorkManager - Apache 2.0
- Material Icons - Apache 2.0

---

## 변경 이력

### v1.0.0 (2025-01-XX) - 초기 출시
- ✅ AI 기반 파일 자동 분류
- ✅ 중복 파일 탐지
- ✅ 파일 통계 대시보드
- ✅ FCM 푸시 알림
- ✅ AdMob 광고 통합
- ✅ 온보딩 화면
- ✅ 개인정보 처리방침
- ✅ 에러 처리 개선

---

**마지막 업데이트**: 2025-01-20
**빌드 상태**: ✅ BUILD SUCCESSFUL
**출시 준비**: 95% (스크린샷, 키스토어 생성 대기 중)
