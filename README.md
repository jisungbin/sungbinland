# 성빈랜드

개인용 운동 보조 안드로이드 앱. 단일 화면으로 **오늘 요일 루틴 확인 → 휴식 타이머 → 수행 세트 집계**만 빠르게 처리한다.

## 기능

1. **오늘의 루틴** — 요일에 해당하는 분류와 부위별 종목을 보여준다. 종목은 부위마다 준비된 풀에서 주 단위로 순환하며, 한 주 안에서는 절대 바뀌지 않는다. 루틴이 없는 요일은 휴식일로 안내한다.
2. **요일 교환** — 오늘 루틴을 다른 요일 루틴과 맞바꾼다. 이번 주에만 유효하고 다음 주에 원래 배치로 돌아간다. 이미 진행한 세트가 있으면 막는다.
3. **휴식 타이머** — 카운트다운 다이얼로그. 완료될 때만 닫히며(바깥 터치·뒤로가기 무시), 완료 시 진동 + 화면 가득 콘페티.
4. **오늘 수행 세트** — 타이머가 한 사이클 완료될 때마다 목표를 못 채운 첫 종목에 +1. 날짜가 바뀌면 자동 초기화.

## 기술 스택

의도적으로 구식(Holo 시절) 스택으로 구성했다. Jetpack Compose·AndroidX·DI를 쓰지 않는다.

- **UI**: 순수 `android.view`/`android.widget` 프로그래매틱 구성 (XML 레이아웃 없음), 프레임워크 Holo 테마
- **Activity**: 플레인 `android.app.Activity` (AppCompat·ComponentActivity 아님)
- **상태/비동기**: RxJava2 단독 — 메인 스레드 Scheduler도 직접 구현해 RxAndroid를 들이지 않는다
- **저장**: `SharedPreferences` (Room·SQLite 사용 안 함)
- **콘페티**: `SurfaceView` + 전용 렌더 스레드로 메인 스레드와 분리
- **DI 없음**, **ViewModel 없음** (상태는 화면 재구성 + prefs로 유지)

## 구조

단일 `:app` 모듈, 관심사별 패키지 분리.

```
sungbinland.app                진입점
sungbinland.workout.domain     주간 루틴 정의와 종목 순환 규칙, 타이머 로직
sungbinland.workout.data       세트 집계·요일 배치의 로컬 저장
sungbinland.workout.rx         RxJava2 메인 스레드 Scheduler
sungbinland.workout.haptic     진동
sungbinland.workout.ui         화면 조립, 다이얼로그, 콘페티, 색상 토큰, 공용 뷰 헬퍼
```

종목 순환은 난수 대신 주차 번호를 인덱스로 삼아 풀을 순서대로 훑는다. 저장할 상태가 없고 같은 주라면 언제 계산해도 결과가 같다. 설계 근거와 밟았던 함정은 `WeeklyRoutine.kt` 주석 참고.

## 빌드 / 설치

```bash
./gradlew :app:installRelease
```

버전·SDK·서명 설정은 `gradle/libs.versions.toml`과 `app/build.gradle.kts` 참조.
