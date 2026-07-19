# 성빈랜드

개인용 운동 보조 안드로이드 앱. 단일 화면으로 **오늘 요일 루틴 확인 → 휴식 타이머 → 수행 세트 집계**만 빠르게 처리한다.

## 기능

1. **오늘의 루틴** — 요일(월~금)에 해당하는 분류와 24세트 구성. 주말은 휴식일 안내.
2. **휴식 타이머** — 기본 55초 카운트다운, `+10초`로 연장. 다이얼로그는 완료될 때만 닫히며(바깥 터치·뒤로가기 무시), 완료 시 진동 + 화면 가득 콘페티.
3. **오늘 수행 세트** — 타이머가 한 사이클 완료될 때마다 +1. 날짜가 바뀌면 자동 초기화.

## 기술 스택

의도적으로 구식(Holo 시절) 스택으로 구성했다. Jetpack Compose·AndroidX 대부분·DI를 쓰지 않는다.

- **UI**: 순수 `android.view`/`android.widget` 프로그래매틱 구성 (XML 레이아웃 없음), 프레임워크 테마 `Theme.Holo.Light.NoActionBar`
- **Activity**: 플레인 `android.app.Activity` (AppCompat·ComponentActivity 아님)
- **상태/비동기**: RxJava2 (+ RxAndroid)
- **저장**: `SharedPreferences` (Room·SQLite 사용 안 함)
- **콘페티**: `SurfaceView` + 전용 렌더 스레드로 메인 스레드와 분리
- **DI 없음**, **ViewModel 없음** (상태는 화면 재구성 + prefs로 유지)
- Kotlin 버전은 카탈로그의 버전 앵커로 AGP 내장분 대신 최신(2.4.20-Beta1) 고정

## 구조

단일 `:app` 모듈, 관심사별 패키지 분리.

```
sungbinland.app                진입점 (MainActivity)
sungbinland.workout.domain     WeeklyRoutine(주간 루틴 데이터), RestTimer(타이머 로직)
sungbinland.workout.data       SetCountStore(세트 수 저장·반응형 노출)
sungbinland.workout.haptic     Haptics(진동)
sungbinland.workout.ui         WorkoutView(화면 조립), RestTimerDialog, ConfettiView,
                               Palette(색상 토큰), ViewSupport(공용 뷰 헬퍼)
```

## 빌드 / 설치

```bash
./gradlew :app:installRelease
```

- 서명: `release` 구성 (`keystore.jks`)
- `minSdk`/`compileSdk`/`targetSdk` 등 SDK 설정은 `settings.gradle.kts`·`app/build.gradle.kts` 참조
