# Smart Folder 1 프로젝트 스킬 가이드

## 프로젝트 개요

**프로젝트명:** Smart_Folder_1
**애플리케이션 ID:** com.example.smart_folder_1
**언어:** Kotlin
**최소 SDK:** 26 (Android 8.0)
**타겟 SDK:** 34 (Android 14)

**목적:** AI 기반 파일 분류 및 자동 정리 애플리케이션. 머신러닝과 사용자 학습 패턴을 활용하여 파일을 자동으로 분류하고 카테고리별 폴더로 정리합니다.

---

## 프로젝트 구조

```
Smart_Folder_1/
├── app/
│   ├── src/main/java/com/example/smart_folder_1/
│   │   ├── MainActivity.kt                     # 앱 진입점
│   │   ├── data/                               # 데이터 레이어
│   │   │   ├── database/                       # Room 데이터베이스
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── UserPreference.kt
│   │   │   │   └── UserPreferenceDao.kt
│   │   │   ├── model/                          # 데이터 모델
│   │   │   │   ├── FileItem.kt
│   │   │   │   └── FileCategory.kt
│   │   │   └── repository/                     # 저장소 패턴
│   │   │       └── FileRepository.kt
│   │   ├── ml/                                 # 머신러닝 레이어
│   │   │   └── FileClassifier.kt
│   │   ├── ui/                                 # UI 레이어
│   │   │   ├── screens/
│   │   │   │   └── MainScreen.kt
│   │   │   ├── theme/
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   └── viewmodel/
│   │   │       └── MainViewModel.kt
│   │   └── utils/                              # 유틸리티
│   │       └── PermissionManager.kt
│   └── build.gradle.kts
└── build.gradle.kts
```

---

## 핵심 기술 스택

### UI 프레임워크
- **Jetpack Compose** - 선언형 UI
- **Material Design 3** - 최신 디자인 시스템
- **다크/라이트 테마** 지원
- **Dynamic Color** (Android 12+)

### 아키텍처 패턴
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** - 데이터 접근 추상화
- **Clean Architecture** - 계층 분리

### 상태 관리
- **StateFlow** - 반응형 상태 관리
- **ViewModel** - UI 상태 보존
- **Sealed Class** - 타입 안전 상태 표현

### 비동기 처리
- **Kotlin Coroutines** (1.6.4)
- **Dispatchers.IO** - 백그라운드 작업
- **Flow** - 비동기 데이터 스트림

### 데이터 영속성
- **Room Database** (2.6.1) - SQLite 추상화
- **DataStore Preferences** - 설정 저장
- **KAPT** - 어노테이션 프로세싱

### 머신러닝
- **TensorFlow Lite** (2.12.0)
- **커스텀 분류 엔진** - 사용자 학습 기반

### 백그라운드 작업
- **WorkManager** (2.7.1) - 안정적인 백그라운드 작업

---

## 주요 컴포넌트 및 스킬

### 1. MainActivity (MainActivity.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/MainActivity.kt`

**스킬:**
- Jetpack Compose 설정 및 초기화
- 런타임 권한 요청 처리 (`ActivityResultContracts`)
- PermissionManager를 통한 권한 관리
- ComponentActivity 기반 구조

**주요 코드 패턴:**
```kotlin
setContent {
    SmartFolder1Theme {
        MainScreen(viewModel, permissionManager)
    }
}
```

---

### 2. MainScreen (ui/screens/MainScreen.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/ui/screens/MainScreen.kt`

**스킬:**
- Compose UI 컴포넌트 구성
- UI 상태 처리 (Loading, Success, Error, Empty, Idle)
- TopAppBar, FloatingActionButton 구현
- 파일 리스트 렌더링 및 카테고리 그룹핑
- Dialog 구현 (카테고리 선택, 디렉토리 선택)
- 파일 아이콘 매핑 및 크기 포맷팅

**주요 Composable 함수:**
- `MainScreen()` - 메인 화면 컨테이너
- `FileListView()` - 파일 리스트 뷰
- `FileItemCard()` - 개별 파일 카드
- `CategoryPickerDialog()` - 카테고리 선택 다이얼로그
- `DirectoryPickerDialog()` - 디렉토리 선택 다이얼로그

---

### 3. MainViewModel (ui/viewmodel/MainViewModel.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/ui/viewmodel/MainViewModel.kt`

**스킬:**
- AndroidViewModel 상속으로 Application Context 접근
- StateFlow를 통한 반응형 상태 관리
- Coroutine 스코프 관리 (`viewModelScope`)
- 파일 스캔 및 분류 오케스트레이션
- 사용자 선호도 저장 및 로딩
- 파일 이동 작업 처리
- 자동 정리 기능 구현

**주요 상태:**
```kotlin
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val files: List<FileItem>) : UiState()
    data class Error(val message: String) : UiState()
    object Empty : UiState()
}
```

**주요 메서드:**
- `scanFiles(directoryType: String)` - 파일 스캔
- `classifyAndSaveFiles()` - 파일 분류
- `saveUserPreference()` - 사용자 선호도 저장
- `moveFileToCategory()` - 파일 이동
- `autoOrganizeAllFiles()` - 전체 자동 정리

---

### 4. FileRepository (data/repository/FileRepository.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/data/repository/FileRepository.kt`

**스킬:**
- 데이터 소스 추상화 (파일 시스템 + 데이터베이스)
- 여러 디렉토리 타입 스캔 (DOWNLOADS, PICTURES, DOCUMENTS, DCIM)
- FileClassifier를 통한 파일 분류
- Room Database와의 상호작용
- 파일 이동 시 중복 처리
- 카테고리별 디렉토리 생성 및 관리

**주요 메서드:**
- `scanFiles(directoryType: String): List<FileItem>` - 디렉토리별 파일 스캔
- `classifyFiles(files: List<FileItem>): List<FileItem>` - 파일 분류
- `saveUserPreference(fileItem: FileItem, category: FileCategory)` - 선호도 저장
- `moveFileToCategory(fileItem: FileItem, category: FileCategory): Boolean` - 파일 이동

---

### 5. FileClassifier (ml/FileClassifier.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/ml/FileClassifier.kt`

**스킬:**
- 다단계 파일 분류 전략
  1. 확장자 기반 기본 분류
  2. 사용자 학습 데이터 분석
  3. 파일명 패턴 인식
  4. 키워드 추출 및 유사도 분석
- 신뢰도(confidence) 점수 계산
- 스크린샷, 다운로드, 밈, 업무 파일, 중요 파일 패턴 감지
- 한글 지원 키워드 추출
- 유사도 계산 알고리즘

**주요 메서드:**
- `classifyFile(file: FileItem, userPreferences: List<UserPreference>): Pair<FileCategory, Float>`
- `extractKeywords(fileName: String): List<String>`
- `calculateSimilarity(text1: String, text2: String): Float`

**분류 로직 흐름:**
```
파일 입력
  ↓
확장자 기반 기본 분류
  ↓
사용자 학습 데이터 검색 (확장자 + 키워드 매칭)
  ↓
패턴 인식 (스크린샷, 다운로드 등)
  ↓
최종 카테고리 + 신뢰도 반환
```

---

### 6. AppDatabase (data/database/AppDatabase.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/data/database/AppDatabase.kt`

**스킬:**
- Room Database 싱글톤 패턴
- 데이터베이스 버전 관리
- Entity 및 DAO 등록
- Thread-safe 인스턴스 생성 (`synchronized`)

**구성:**
```kotlin
@Database(entities = [UserPreference::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPreferenceDao(): UserPreferenceDao
}
```

---

### 7. UserPreference Entity (data/database/UserPreference.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/data/database/UserPreference.kt`

**스킬:**
- Room Entity 정의
- 사용자 분류 패턴 저장
- 수동 오버라이드 플래그 관리
- 타임스탬프 기록

**필드:**
- `fileName`, `fileExtension`, `fileSize`, `filePath`
- `selectedCategory` - 사용자가 선택한 카테고리
- `isManualOverride` - AI 제안 수락 여부
- `timestamp` - 저장 시간

---

### 8. UserPreferenceDao (data/database/UserPreferenceDao.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/data/database/UserPreferenceDao.kt`

**스킬:**
- Room DAO 패턴
- CRUD 작업 정의
- 커스텀 쿼리 작성
- Suspend 함수로 비동기 지원

**주요 쿼리:**
```kotlin
@Query("SELECT * FROM user_preferences WHERE fileExtension = :extension")
suspend fun getPreferencesByExtension(extension: String): List<UserPreference>

@Query("SELECT COUNT(*) FROM user_preferences")
suspend fun getPreferenceCount(): Int

@Query("DELETE FROM user_preferences WHERE timestamp < :timestamp")
suspend fun deleteOldPreferences(timestamp: Long)
```

---

### 9. FileItem Model (data/model/FileItem.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/data/model/FileItem.kt`

**스킬:**
- 파일 메타데이터 표현
- 타입 감지 속성 (isImage, isVideo, isDocument 등)
- 분류 결과 저장 (suggestedCategory, confidence)

**주요 속성:**
```kotlin
data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val extension: String,
    val mimeType: String?,
    var suggestedCategory: FileCategory?,
    var confidence: Float = 0f
)
```

---

### 10. FileCategory Enum (data/model/FileCategory.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/data/model/FileCategory.kt`

**스킬:**
- 12가지 파일 카테고리 정의
- 카테고리별 색상 코딩
- 확장자 기반 분류 로직

**카테고리:**
1. IMAGES - 이미지 파일
2. VIDEOS - 비디오 파일
3. DOCUMENTS - 문서 파일
4. AUDIO - 오디오 파일
5. ARCHIVES - 압축 파일
6. DOWNLOADS - 다운로드 파일
7. WORK - 업무 관련 파일
8. PERSONAL - 개인 파일
9. SCREENSHOTS - 스크린샷
10. MEMES - 밈/재미있는 이미지
11. IMPORTANT - 중요 파일
12. OTHERS - 기타

---

### 11. PermissionManager (utils/PermissionManager.kt)
**위치:** `app/src/main/java/com/example/smart_folder_1/utils/PermissionManager.kt`

**스킬:**
- Android 버전별 권한 처리
  - Android 13+ (TIRAMISU): READ_MEDIA_* 권한
  - Android 11-12: READ_EXTERNAL_STORAGE
  - Android 10 이하: READ/WRITE_EXTERNAL_STORAGE
- MANAGE_EXTERNAL_STORAGE 전체 접근 권한 처리
- 권한 확인, 요청, 결과 처리

**주요 메서드:**
- `checkPermissions(): Boolean` - 권한 확인
- `getRequiredPermissions(): Array<String>` - 필요 권한 목록
- `shouldShowRationale(): Boolean` - 권한 설명 필요 여부

---

## 데이터 흐름 아키텍처

```
사용자 인터랙션 (UI)
        ↓
MainScreen (Jetpack Compose)
        ↓
MainViewModel (상태 관리)
        ↓
FileRepository (데이터 접근)
        ↓
┌─────────────────────────────────────┐
│  FileClassifier (ML 분류)          │
│  - 확장자 분석                      │
│  - 사용자 학습 데이터 활용          │
│  - 패턴 인식                        │
└─────────────────────────────────────┘
        ↓
┌─────────────────────────────────────┐
│  AppDatabase (Room)                │
│  UserPreferenceDao                 │
└─────────────────────────────────────┘
        ↓
파일 시스템 (Storage)
```

---

## 주요 기능

### 1. 파일 스캔
- 여러 시스템 디렉토리 동시 스캔 (다운로드, 사진, 문서, DCIM)
- 파일만 필터링 (디렉토리 제외)
- 메타데이터 추출 (이름, 크기, 확장자, MIME 타입)

### 2. AI 기반 파일 분류
- 확장자 기반 기본 분류
- 사용자 학습 패턴 분석
- 파일명 패턴 인식
- 신뢰도 점수 제공

### 3. 사용자 학습 시스템
- 사용자의 분류 선택 저장
- 수동 오버라이드 vs AI 제안 추적
- 시간 경과에 따른 패턴 학습

### 4. 파일 자동 정리
- 카테고리별 폴더로 자동 이동
- 중복 파일명 처리
- 일괄 자동 정리 기능

### 5. 권한 관리
- Android 버전별 권한 처리
- 런타임 권한 요청
- Scoped Storage 준수

---

## 개발 스킬 체크리스트

### Kotlin & Android
- ✅ Kotlin 코루틴 및 Flow
- ✅ Jetpack Compose UI
- ✅ Material Design 3
- ✅ MVVM 아키텍처
- ✅ Repository 패턴
- ✅ Room Database
- ✅ DataStore Preferences
- ✅ WorkManager
- ✅ 런타임 권한 처리

### 머신러닝
- ✅ TensorFlow Lite 통합
- ✅ 커스텀 분류 알고리즘
- ✅ 사용자 학습 패턴 분석
- ✅ 신뢰도 점수 계산

### UI/UX
- ✅ 선언형 UI (Compose)
- ✅ 다크/라이트 테마
- ✅ 반응형 레이아웃
- ✅ Material 디자인 가이드라인

### 데이터 관리
- ✅ SQLite (Room)
- ✅ 파일 시스템 접근
- ✅ 비동기 데이터 처리
- ✅ 상태 관리 (StateFlow)

---

## 빌드 및 실행

### 요구사항
- Android Studio Hedgehog 이상
- JDK 17
- Kotlin 1.9.22
- Gradle 8.2

### 빌드 명령어
```bash
./gradlew clean build
./gradlew assembleDebug
./gradlew assembleRelease
```

### 테스트 실행
```bash
./gradlew test                    # 유닛 테스트
./gradlew connectedAndroidTest    # 인스트루먼트 테스트
```

---

## 트러블슈팅 스킬

### 1. 권한 문제
- AndroidManifest.xml에서 필요한 권한 확인
- Android 버전별 권한 요구사항 확인
- Settings 앱에서 "모든 파일 접근" 권한 활성화

### 2. Room 데이터베이스 마이그레이션
- 스키마 변경 시 버전 업데이트 필수
- 마이그레이션 전략 정의 또는 `fallbackToDestructiveMigration()` 사용

### 3. Compose 리컴포지션 최적화
- `remember`를 사용한 상태 보존
- `derivedStateOf`로 계산된 상태 최적화
- 불필요한 리컴포지션 방지

### 4. 코루틴 메모리 누수 방지
- `viewModelScope` 사용으로 자동 취소
- 수동 Job 관리 시 `cancel()` 호출

---

## 확장 가능한 영역

### 1. 추가 분류 카테고리
`FileCategory.kt`에 새 enum 값 추가 및 색상 정의

### 2. 고급 ML 모델 통합
TensorFlow Lite 모델(.tflite) 추가 및 `FileClassifier.kt`에서 활용

### 3. 클라우드 동기화
Room 데이터베이스를 Firebase/서버와 동기화

### 4. 일정 기반 자동 정리
WorkManager를 사용한 주기적 자동 정리

### 5. 파일 미리보기
이미지/비디오 썸네일 생성 및 표시

---

## 참고 문서

- [Jetpack Compose 공식 문서](https://developer.android.com/jetpack/compose)
- [Room 데이터베이스 가이드](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines 가이드](https://kotlinlang.org/docs/coroutines-guide.html)
- [Material Design 3](https://m3.material.io/)
- [TensorFlow Lite for Android](https://www.tensorflow.org/lite/android)

---

**마지막 업데이트:** 2025-10-17
**작성자:** Claude Code Assistant