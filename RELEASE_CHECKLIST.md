# Smart Folder - 출시 체크리스트 ✅

## 📦 빌드 완료 현황

### ✅ Release APK
- **파일**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **크기**: 11MB
- **상태**: 빌드 완료 (서명 필요)

### ✅ Release AAB (Android App Bundle)
- **파일**: `app/build/outputs/bundle/release/app-release.aab`
- **크기**: 23MB
- **상태**: 빌드 완료 (서명 필요)
- **권장**: Google Play Store 업로드 시 AAB 사용

---

## 🔐 출시 전 필수 작업

### 1. ⚠️ 키스토어 생성 및 서명 (필수!)

현재 APK/AAB는 서명되지 않았습니다. Google Play에 업로드하려면 서명이 필요합니다.

```bash
# 1단계: 키스토어 생성
keytool -genkey -v -keystore app/release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias smart_folder_key

# 입력 정보 예시:
# Keystore password: [강력한 비밀번호 입력]
# Key password: [강력한 비밀번호 입력]
# CN: Smart Folder Development Team
# OU: Development
# O: Your Company
# L: Seoul
# ST: Seoul
# C: KR

# 2단계: build.gradle.kts 서명 설정 업데이트
# signingConfig = signingConfigs.getByName("release") 주석 해제

# 3단계: 서명된 빌드 생성
./gradlew bundleRelease  # AAB (권장)
./gradlew assembleRelease  # APK
```

**⚠️ 중요: 키스토어 파일 백업**
- 키스토어 파일을 안전한 곳에 백업하세요!
- 분실 시 앱 업데이트 불가능
- Git에 커밋하지 마세요 (.gitignore에 이미 추가됨)

### 2. ⚠️ AdMob App ID 변경 (필수!)

현재 테스트용 AdMob ID를 사용 중입니다.

**작업 순서:**
1. [Google AdMob Console](https://apps.admob.com/) 접속
2. 새 앱 등록
3. App ID 복사
4. `app/src/main/AndroidManifest.xml` 파일 수정:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"/>
```

**현재 값 (테스트):** `ca-app-pub-3940256099942544~3347511713`
**변경 필요:** 실제 AdMob App ID로 변경

### 3. 📄 개인정보처리방침 URL 설정

Google Play에 앱을 등록하려면 개인정보처리방침 URL이 필요합니다.

**옵션 1: GitHub Pages (무료, 권장)**
```bash
# 1. GitHub 저장소 생성
# 2. privacy_policy.html 업로드
cp app/src/main/assets/privacy_policy.html .
git add privacy_policy.html
git commit -m "Add privacy policy"
git push

# 3. Settings > Pages > Source를 main branch로 설정
# 4. URL: https://yourusername.github.io/smart-folder/privacy_policy.html
```

**옵션 2: Firebase Hosting (무료)**
```bash
firebase init hosting
cp app/src/main/assets/privacy_policy.html public/
firebase deploy --only hosting
# URL: https://your-project.web.app/privacy_policy.html
```

---

## 📱 Google Play Console 등록

### 1단계: Play Console 계정 생성
- [Google Play Console](https://play.google.com/console/) 접속
- 개발자 등록 ($25 일회성 수수료)

### 2단계: 앱 생성
1. "앱 만들기" 클릭
2. 앱 정보 입력:
   - **이름**: Smart Folder
   - **기본 언어**: 한국어
   - **앱 또는 게임**: 앱
   - **무료 또는 유료**: 무료

### 3단계: 앱 정보 작성

**스토어 등록정보:**
- 앱 이름: Smart Folder - 스마트 파일 정리
- 짧은 설명: (80자) `PLAY_STORE_LISTING.md` 참조
- 전체 설명: (4000자) `PLAY_STORE_LISTING.md` 참조
- 개인정보처리방침 URL: [위에서 생성한 URL]

**그래픽 에셋 (필수):**
- [ ] 앱 아이콘: 512x512 PNG
- [ ] Feature Graphic: 1024x500 JPG/PNG
- [ ] 스크린샷: 최소 2개 (휴대전화)

**카테고리:**
- 카테고리: 생산성
- 태그: 파일관리, 파일정리, 중복파일, 저장공간, AI분류

### 4단계: 콘텐츠 등급
- IARC 설문조사 작성
- 예상 등급: 만 3세 이상

### 5단계: 앱 콘텐츠
- [ ] 개인정보처리방침 URL 제공
- [ ] 광고 포함: 예
- [ ] 앱 내 구매: 아니오
- [ ] 대상 연령대: 만 3세 이상

### 6단계: 출시 트랙 선택

**내부 테스트 (권장, 빠른 검토):**
- 최대 100명의 테스터
- 즉시 업데이트 가능
- 초기 버그 수정에 적합

**프로덕션:**
- 모든 사용자에게 공개
- 단계적 출시 권장 (5% → 10% → 50% → 100%)

### 7단계: AAB 업로드
```bash
# 서명된 AAB 업로드
# app/build/outputs/bundle/release/app-release.aab
```

### 8단계: 출시 노트 작성
```
버전 1.0.0의 새로운 기능:

• AI 기반 자동 파일 분류
• 중복 파일 탐지 및 삭제
• 파일 통계 및 분석
• 스마트 알림 시스템
• Material Design 3 UI
• 다크 모드 지원

Smart Folder와 함께 스마트한 파일 관리를 시작하세요!
```

---

## ✅ 최종 체크리스트

### 빌드 준비
- [x] ProGuard/R8 규칙 설정 완료
- [x] Release 빌드 설정 완료
- [x] 버전 정보 업데이트 (1.0.0)
- [x] Release APK 빌드 성공
- [x] Release AAB 빌드 성공
- [ ] 키스토어 생성 및 서명 완료
- [ ] 서명된 APK/AAB 테스트 완료

### 설정 변경
- [ ] AdMob App ID를 실제 ID로 변경
- [x] Firebase google-services.json 확인
- [x] 개인정보처리방침 작성 완료
- [ ] 개인정보처리방침 URL 생성

### 그래픽 에셋
- [ ] 앱 아이콘 (512x512) 준비
- [ ] Feature Graphic (1024x500) 준비
- [ ] 스크린샷 (최소 2개) 준비
- [ ] 홍보 동영상 (선택사항)

### Play Console
- [ ] 개발자 계정 등록 ($25)
- [ ] 앱 생성 완료
- [ ] 스토어 등록정보 작성
- [ ] 콘텐츠 등급 받기
- [ ] 앱 콘텐츠 섹션 완료
- [ ] AAB 업로드
- [ ] 출시 검토 제출

---

## 📊 예상 일정

| 단계 | 예상 소요 시간 | 비고 |
|------|--------------|------|
| 키스토어 생성 및 서명 | 30분 | 한 번만 수행 |
| AdMob 설정 | 1시간 | 계정 승인 대기 시간 포함 |
| 개인정보처리방침 URL | 30분 | GitHub Pages 사용 시 |
| 그래픽 에셋 제작 | 2-4시간 | 디자인 능력에 따라 다름 |
| Play Console 작성 | 2시간 | 처음 사용 시 |
| Google 검토 | 1-7일 | 평균 1-3일 |

**총 예상 시간**: 출시 제출까지 약 1-2일 (검토 제외)

---

## 🚀 출시 후 할 일

### 1주차
- [ ] Play Console에서 크래시 리포트 모니터링
- [ ] 초기 사용자 리뷰 확인 및 응답
- [ ] Firebase Analytics로 사용자 행동 분석

### 1개월차
- [ ] 사용자 피드백 기반 버그 수정
- [ ] 첫 번째 업데이트 배포 (v1.0.1 또는 v1.1.0)
- [ ] ASO (App Store Optimization) 최적화

### 지속적 관리
- [ ] 월 1회 정기 업데이트
- [ ] 새로운 Android 버전 대응
- [ ] 사용자 요청 기능 추가
- [ ] 광고 수익 분석 및 최적화

---

## 📞 도움이 필요한 경우

### 공식 문서
- [Android Developer 가이드](https://developer.android.com/)
- [Google Play Console 도움말](https://support.google.com/googleplay/android-developer/)
- [Firebase 문서](https://firebase.google.com/docs)
- [AdMob 도움말](https://support.google.com/admob/)

### 참조 문서
- `RELEASE_GUIDE.md`: 상세한 출시 가이드
- `PLAY_STORE_LISTING.md`: Play Store 등록 정보
- `app/src/main/assets/privacy_policy.html`: 개인정보처리방침

---

## 🎉 축하합니다!

출시 준비가 거의 완료되었습니다!

**다음 단계:**
1. ⚠️ 키스토어 생성 및 서명
2. ⚠️ AdMob App ID 변경
3. 📄 개인정보처리방침 URL 생성
4. 🎨 그래픽 에셋 준비
5. 📱 Google Play Console 등록

**화이팅! 🚀**

---

**문서 버전**: 1.0.0
**최종 업데이트**: 2025년 1월
**빌드 일시**: 2025년 10월 22일
