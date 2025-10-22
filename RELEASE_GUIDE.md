# Smart Folder - 앱 출시 가이드

## 📋 출시 전 체크리스트

### 1. 키스토어 생성 (필수)

Play Store에 앱을 업로드하려면 서명된 APK/AAB가 필요합니다.

```bash
# 키스토어 생성 명령어
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias smart_folder_key

# 입력 정보:
# - Keystore password: 안전한 비밀번호 입력
# - Key password: 안전한 비밀번호 입력
# - CN (이름): 개발자 이름 또는 회사명
# - OU (조직 단위): 선택사항
# - O (조직): 선택사항
# - L (지역): 도시명
# - ST (시/도): 시/도명
# - C (국가 코드): KR
```

생성된 `release-keystore.jks` 파일을 `app/` 디렉토리에 복사하세요.

### 2. 키스토어 정보 설정

`app/build.gradle.kts` 파일에서 키스토어 정보를 업데이트하세요:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release-keystore.jks")
        storePassword = "your_keystore_password"  // 실제 비밀번호로 변경
        keyAlias = "smart_folder_key"             // 실제 alias로 변경
        keyPassword = "your_key_password"         // 실제 비밀번호로 변경
    }
}
```

**보안을 위해 환경변수 사용 권장:**

```bash
# Windows (PowerShell)
$env:KEYSTORE_PASSWORD = "your_password"
$env:KEY_ALIAS = "smart_folder_key"
$env:KEY_PASSWORD = "your_password"

# Windows (CMD)
set KEYSTORE_PASSWORD=your_password
set KEY_ALIAS=smart_folder_key
set KEY_PASSWORD=your_password

# Linux/Mac
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="smart_folder_key"
export KEY_PASSWORD="your_password"
```

그 다음 `build.gradle.kts`의 서명 설정 주석을 해제하세요:
```kotlin
signingConfig = signingConfigs.getByName("release")
```

### 3. AdMob App ID 변경 (필수)

현재 테스트용 AdMob ID를 사용 중입니다. 실제 AdMob 계정에서 앱 ID를 받아 변경하세요.

1. [Google AdMob Console](https://apps.admob.com/)에 접속
2. 새 앱 등록 후 App ID 복사
3. `app/src/main/AndroidManifest.xml` 파일 수정:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"/>
```

### 4. Firebase 프로젝트 확인

`app/google-services.json` 파일이 실제 Firebase 프로젝트 설정과 일치하는지 확인하세요.

### 5. Release 빌드 생성

#### AAB (Android App Bundle) 생성 (권장)
```bash
./gradlew bundleRelease
```

생성된 파일 위치: `app/build/outputs/bundle/release/app-release.aab`

#### APK 생성
```bash
./gradlew assembleRelease
```

생성된 파일 위치: `app/build/outputs/apk/release/app-release.apk`

### 6. 빌드 테스트

Release 빌드를 실제 기기에 설치하여 테스트:

```bash
# AAB를 APK로 변환 후 설치 (bundletool 필요)
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab --output=app-release.apks --mode=universal
bundletool install-apks --apks=app-release.apks

# 또는 직접 APK 설치
adb install app/build/outputs/apk/release/app-release.apk
```

**테스트 항목:**
- [ ] 스플래시 화면 정상 작동
- [ ] 온보딩 화면 정상 표시
- [ ] 파일 권한 요청 정상 작동
- [ ] 파일 스캔 및 분류 기능
- [ ] 중복 파일 탐지 기능
- [ ] 통계 화면 정상 표시
- [ ] 광고 정상 표시
- [ ] 알림 기능 정상 작동
- [ ] 개인정보처리방침 화면 정상 표시
- [ ] 앱 종료 다이얼로그 정상 작동

## 📱 Google Play Console 준비사항

### 1. 앱 정보

**앱 이름:** Smart Folder

**짧은 설명 (80자 이내):**
```
AI 기반 스마트 파일 정리 앱. 파일을 자동으로 분류하고 중복 파일을 찾아 저장 공간을 최적화하세요.
```

**전체 설명 (4000자 이내):**
```
📁 Smart Folder - 스마트한 파일 관리의 시작

Smart Folder는 AI 기술을 활용하여 스마트폰의 파일을 자동으로 정리하고 관리하는 혁신적인 앱입니다.

🎯 주요 기능

✅ AI 기반 자동 파일 분류
- 사진, 동영상, 문서, 음악, APK 등 자동 분류
- 머신러닝 기반 스마트 분류 알고리즘
- 사용자 패턴 학습으로 정확도 향상

✅ 중복 파일 탐지
- 똑같은 파일을 찾아 저장 공간 확보
- 파일 해시 비교로 정확한 중복 탐지
- 한 번에 여러 파일 삭제 가능

✅ 파일 통계 및 분석
- 카테고리별 파일 크기 시각화
- 저장 공간 사용량 분석
- 파일 개수 및 분포 차트

✅ 스마트 알림
- 정기적인 파일 정리 알림
- 중복 파일 발견 시 알림
- 저장 공간 부족 경고

🔒 개인정보 보호
- 모든 데이터는 기기 내부에만 저장
- 외부 서버로 파일 정보 전송 안 함
- 안전한 로컬 처리

🎨 직관적인 디자인
- Material Design 3 적용
- 다크 모드 지원
- 깔끔하고 현대적인 UI

💡 왜 Smart Folder인가?

• 자동화: 손쉬운 파일 정리
• 공간 확보: 중복 파일 제거로 저장 공간 최적화
• 시간 절약: AI가 알아서 분류
• 무료: 기본 기능 완전 무료

📋 필요 권한
- 저장소 접근: 파일 스캔 및 정리
- 알림: 파일 정리 알림 전송
- 인터넷: 광고 표시 (앱 수익화)

🌟 지금 바로 다운로드하고 스마트한 파일 관리를 경험하세요!
```

### 2. 그래픽 에셋 (필요한 항목)

#### 필수:
- **앱 아이콘**: 512x512 PNG (32비트, 투명 배경 없음)
- **Feature Graphic**: 1024x500 JPG/PNG
- **스크린샷**: 최소 2개, 최대 8개
  - 휴대전화: 320px - 3840px (16:9 비율 권장)
  - 7인치 태블릿: 1024 x 500 이상
  - 10인치 태블릿: 1024 x 500 이상

#### 선택사항:
- **홍보 동영상**: YouTube 링크
- **TV 배너**: 1280x720 JPG/PNG (TV 출시 시)

### 3. 콘텐츠 등급

[IARC 설문조사](https://support.google.com/googleplay/android-developer/answer/9859655)를 통해 콘텐츠 등급 받기

예상 등급: **만 3세 이상** (파일 관리 앱, 특별한 콘텐츠 없음)

### 4. 개인정보처리방침 URL

Play Console에 개인정보처리방침 URL을 제공해야 합니다. 다음 옵션 중 선택:

1. **GitHub Pages 호스팅** (무료)
   - `privacy_policy.html` 파일을 GitHub 저장소에 업로드
   - GitHub Pages 활성화
   - URL: `https://yourusername.github.io/smart-folder/privacy_policy.html`

2. **Google Sites** (무료)
   - [Google Sites](https://sites.google.com/)에서 새 사이트 생성
   - 개인정보처리방침 내용 복사
   - 공개 URL 사용

3. **개인 웹사이트**
   - 본인 웹사이트에 호스팅

### 5. 대상 고객 및 콘텐츠

- **대상 연령대**: 만 3세 이상
- **광고 포함 여부**: 예 (AdMob 사용)
- **앱 내 구매**: 아니오
- **대상 국가**: 한국 (또는 전 세계)

## 🚀 배포 프로세스

### 1단계: 내부 테스트 (권장)
- 소수의 테스터와 빠른 반복 테스트
- 최대 100명의 테스터

### 2단계: 비공개 테스트
- 더 큰 그룹으로 테스트
- 실제 사용 환경에서 테스트

### 3단계: 공개 테스트 (선택)
- 누구나 참여 가능한 베타 테스트
- 정식 출시 전 마지막 검증

### 4단계: 프로덕션 출시
- Google Play 스토어에 정식 공개
- 단계적 출시 권장 (5% → 10% → 50% → 100%)

## ⚠️ 주의사항

### 1. 키스토어 관리
- **절대로 키스토어 파일을 잃어버리지 마세요!**
- 키스토어가 없으면 앱 업데이트 불가능
- 안전한 곳에 백업 보관 (클라우드, 외장 하드 등)
- Git에 키스토어 절대 업로드 금지 (`.gitignore`에 추가)

### 2. 버전 관리
- 앱 업데이트 시마다 `versionCode` 증가 필수
- `versionName`은 사용자에게 표시되는 버전 (예: 1.0.0, 1.1.0)

### 3. ProGuard/R8
- Release 빌드 후 반드시 테스트
- 크래시 발생 시 ProGuard 규칙 추가 필요
- mapping.txt 파일 보관 (크래시 리포트 분석용)

### 4. Google Play 정책 준수
- [Google Play 정책](https://play.google.com/about/developer-content-policy/) 확인
- 저작권 침해 금지
- 정확한 앱 설명 작성
- 적절한 콘텐츠 등급 설정

## 📞 문의

출시 과정에서 문제가 발생하면 다음을 참고하세요:

- **Android Developer 공식 문서**: https://developer.android.com/
- **Google Play Console 고객센터**: https://support.google.com/googleplay/android-developer/
- **Firebase 문서**: https://firebase.google.com/docs
- **AdMob 고객센터**: https://support.google.com/admob/

## 📈 출시 후 관리

### 모니터링
- Play Console에서 크래시 리포트 확인
- 사용자 리뷰 및 평점 모니터링
- Firebase Analytics로 사용자 행동 분석

### 업데이트
- 정기적인 버그 수정 및 기능 개선
- Android 신규 버전 대응
- 사용자 피드백 반영

### 마케팅
- ASO (App Store Optimization) 최적화
- 소셜 미디어 홍보
- 사용자 리뷰 응답

---

**앱 버전**: 1.0.0
**최종 업데이트**: 2025년 1월
**개발자**: leemanrank@gmail.com
