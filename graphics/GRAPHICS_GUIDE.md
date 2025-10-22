# Smart Folder - 그래픽 에셋 가이드

## 📁 디렉토리 구조

```
graphics/
├── app-icon/
│   ├── app-icon.svg            # 앱 아이콘 SVG (편집 가능)
│   └── app-icon-512.png        # 512x512 PNG (생성 필요)
├── feature-graphic/
│   ├── feature-graphic.svg     # Feature Graphic SVG
│   └── feature-graphic.png     # 1024x500 PNG (생성 필요)
├── screenshots/
│   ├── screenshot-template.svg # 스크린샷 템플릿
│   └── *.png                   # 실제 스크린샷들 (생성 필요)
└── GRAPHICS_GUIDE.md           # 이 파일
```

---

## 🎨 Google Play Console 필수 에셋

### 1. 앱 아이콘 (App Icon)

**요구사항:**
- 크기: **512 x 512 pixels**
- 포맷: **32-bit PNG** (투명 배경 없음)
- 최대 용량: 1MB

**디자인:**
- 보라색 그라데이션 배경
- 황금색 폴더 아이콘
- 중앙에 AI 뉴럴 네트워크 심볼
- 흰색 반짝임 효과

**생성 방법:**
```bash
# SVG를 PNG로 변환 (Inkscape, GIMP, 또는 온라인 도구 사용)
# 1. graphics/app-icon/app-icon.svg 파일 열기
# 2. 512x512 PNG로 내보내기
# 3. 파일명: app-icon-512.png
```

**온라인 변환 도구:**
- https://cloudconvert.com/svg-to-png
- https://www.aconvert.com/image/svg-to-png/
- Figma, Adobe Illustrator 사용 가능

---

### 2. Feature Graphic

**요구사항:**
- 크기: **1024 x 500 pixels**
- 포맷: **JPG 또는 24-bit PNG**
- 투명 배경 없음

**디자인 요소:**
- 왼쪽: 앱 아이콘 + "Smart Folder" 제목
- 오른쪽: 휴대폰 목업 (앱 화면)
- 하단: 주요 기능 뱃지 (중복 탐지, 파일 통계, 스마트 알림)
- 배경: 보라색 그라데이션

**생성 방법:**
```bash
# graphics/feature-graphic/feature-graphic.svg 파일 사용
# 1024x500 PNG로 내보내기
```

---

### 3. 스크린샷 (Screenshots)

**요구사항:**
- 최소: **2개** (권장: 4-8개)
- 크기: **최소 320px, 최대 3840px**
- 비율: **16:9 권장** (1080 x 1920)
- 포맷: **JPG 또는 24-bit PNG**

**권장 스크린샷:**

1. **메인 화면** - 파일 카테고리 목록
   - 제목: "AI 기반 파일 자동 정리"
   - 이미지, 동영상, 문서, 음악 카테고리 표시

2. **통계 화면** - 파일 사용량 차트
   - 제목: "파일 통계 한눈에 확인"
   - 파이 차트, 바 차트 표시

3. **중복 파일 화면** - 중복 파일 목록
   - 제목: "중복 파일 찾아서 삭제"
   - 중복 파일 그룹 표시

4. **설정 화면** - 앱 설정 옵션
   - 제목: "간편한 설정"
   - 다크 모드, 알림 설정 등

5. **온보딩 화면** - 앱 소개 (선택사항)
   - 제목: "Smart Folder 시작하기"
   - 주요 기능 소개

**스크린샷 캡처 방법:**

**방법 1: 실제 기기에서 캡처**
```bash
# Android Studio에서 앱 실행
# 실제 기기 또는 에뮬레이터에서 화면 캡처
# 1080 x 1920 이상 권장

# ADB로 스크린샷 캡처
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png graphics/screenshots/
```

**방법 2: 에뮬레이터에서 캡처**
```bash
# Android Studio Emulator 사용
# 1. 앱 실행
# 2. Emulator 우측 툴바 > Camera 아이콘 클릭
# 3. 파일 저장
```

**방법 3: 템플릿 사용**
```bash
# graphics/screenshots/screenshot-template.svg 사용
# 실제 앱 화면을 템플릿에 삽입하여 제작
```

---

## 🎨 디자인 가이드라인

### 색상 팔레트

**주 색상:**
- Primary Purple: `#667eea`
- Secondary Purple: `#764ba2`
- Accent Blue: `#3B82F6`

**카테고리 색상:**
- Images (이미지): `#FDB813` (황금색)
- Videos (동영상): `#3B82F6` (파란색)
- Documents (문서): `#10B981` (녹색)
- Music (음악): `#F59E0B` (주황색)
- APK: `#EF4444` (빨간색)
- Others (기타): `#6B7280` (회색)

**배경:**
- Gradient 1: `#667eea` → `#764ba2`
- Gradient 2: `#764ba2` → `#667eea`

### 폰트

- 제목: **Arial / Noto Sans KR**, Bold, 48-60px
- 본문: **Arial / Noto Sans KR**, Regular, 24-36px
- 강조: **Arial / Noto Sans KR**, SemiBold, 32-48px

### 아이콘 스타일

- 이모지 사용: 📁 📸 🎬 📄 🎵 🤖 🔍 📊 🔔
- 또는 Material Design Icons
- 크기: 32dp 이상
- 색상: 카테고리별 색상 또는 흰색

---

## 🛠 SVG를 PNG로 변환하기

### 온라인 도구 (빠르고 간편)

1. **CloudConvert** (권장)
   - https://cloudconvert.com/svg-to-png
   - 고품질, 크기 지정 가능

2. **SVG2PNG**
   - https://svgtopng.com/
   - 간단한 인터페이스

3. **Convertio**
   - https://convertio.co/svg-png/
   - 다양한 옵션

### 데스크톱 도구

1. **Inkscape** (무료, 오픈소스)
   ```bash
   # 설치
   # Windows: https://inkscape.org/release/

   # 사용법
   # 1. SVG 파일 열기
   # 2. File > Export PNG Image
   # 3. 크기 설정 (512x512 또는 1024x500)
   # 4. Export 클릭
   ```

2. **GIMP** (무료, 오픈소스)
   ```bash
   # 설치
   # Windows: https://www.gimp.org/downloads/

   # 사용법
   # 1. File > Open > SVG 파일 선택
   # 2. 크기 지정 후 Import
   # 3. File > Export As > PNG
   ```

3. **Adobe Illustrator** (유료)
   - 고품질 변환 가능
   - File > Export > Export As > PNG

### 명령줄 도구 (개발자용)

**ImageMagick** (설치 필요)
```bash
# 앱 아이콘 변환
convert app-icon.svg -resize 512x512 app-icon-512.png

# Feature Graphic 변환
convert feature-graphic.svg -resize 1024x500 feature-graphic.png
```

**Inkscape CLI** (Inkscape 설치 필요)
```bash
inkscape app-icon.svg --export-filename=app-icon-512.png --export-width=512 --export-height=512
```

---

## 📋 체크리스트

### 필수 에셋 (Google Play Console 제출 전)

- [ ] **앱 아이콘** - 512x512 PNG, 32-bit
- [ ] **Feature Graphic** - 1024x500 JPG/PNG
- [ ] **스크린샷 (최소 2개)** - 1080x1920 권장

### 선택 에셋 (권장)

- [ ] 스크린샷 4-8개
- [ ] 홍보 동영상 (YouTube 링크)
- [ ] 7인치 태블릿 스크린샷
- [ ] 10인치 태블릿 스크린샷

---

## 🎯 빠른 시작 가이드

1. **SVG 파일 확인**
   - `app-icon.svg` - 앱 아이콘
   - `feature-graphic.svg` - Feature Graphic
   - `screenshot-template.svg` - 스크린샷 템플릿

2. **PNG 변환**
   - CloudConvert 또는 Inkscape 사용
   - 앱 아이콘: 512x512
   - Feature Graphic: 1024x500

3. **스크린샷 캡처**
   - 앱 실행 후 화면 캡처
   - 또는 템플릿 사용

4. **Play Console 업로드**
   - Store settings > Main store listing
   - Graphic assets 섹션에 업로드

---

## 💡 팁 & 트릭

### 고품질 이미지 유지
- SVG는 벡터 포맷이므로 어떤 크기로도 변환 가능
- PNG 변환 시 해상도 설정 중요
- 앱 아이콘은 512x512 이상 권장 (Play Store 자동 리사이즈)

### 스크린샷 디자인
- 실제 앱 화면 + 설명 텍스트 조합
- 배경에 그라데이션 추가로 시각적 효과
- 각 스크린샷은 하나의 기능에 집중

### A/B 테스트
- Feature Graphic 여러 버전 제작
- Play Console의 Store Listing Experiments 사용
- 전환율 높은 디자인 선택

---

## 📞 도움말

### SVG 편집
- **온라인 에디터**: https://editor.method.ac/
- **Figma**: https://www.figma.com/ (무료, 협업 가능)
- **Adobe Illustrator**: 전문가용

### 디자인 영감
- **Dribbble**: https://dribbble.com/search/app-icon
- **Behance**: https://www.behance.net/
- **Google Play Store**: 인기 앱들의 에셋 참고

### 문의
- 이메일: leemanrank@gmail.com
- GitHub: https://github.com/leemanrank/smart_folder

---

**문서 버전**: 1.0.0
**최종 업데이트**: 2025년 1월
