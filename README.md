# 성빈랜드

개인용 운동 보조 안드로이드 앱. 단일 화면에서 **매일 할 네 종목 입력 → 휴식 타이머 → 수행 세트 집계**를 빠르게 처리한다.

## 기능

1. **오늘의 운동** — 첫 종목부터 넷째 종목까지 네 슬롯의 세트 진행도를 기록한다. 슬롯마다 4세트, 하루 총 16세트다.
3. **휴식 타이머** — 60초 카운트다운에서 `+10초`로 늘리고 `-10초`로 줄인다. 완료될 때만 닫히며(바깥 터치·뒤로가기 무시), 완료 시 진동 + 화면 가득 콘페티.
4. **오늘 수행 세트** — 타이머가 한 사이클 완료될 때마다 목표를 못 채운 첫 종목에 +1. 날짜가 바뀌면 자동 초기화.
5. **운동 시작 시각** — 상단 시각을 누르면 HH:MM으로 직접 고칠 수 있고, 경과 시간이 그 시각부터 다시 계산된다.

## 기술 스택

의도적으로 구식(Holo 시절) 스택으로 구성했다. Jetpack Compose·AndroidX·DI를 쓰지 않는다.

- **UI**: 순수 `android.view`/`android.widget` 프로그래매틱 구성 (XML 레이아웃 없음), 프레임워크 Holo 테마
- **Activity**: 플레인 `android.app.Activity` (AppCompat·ComponentActivity 아님)
- **상태/비동기**: 타입 기반 로컬 EventBus와 Android Handler
- **저장**: `SharedPreferences` (Room·SQLite 사용 안 함)
- **콘페티**: `SurfaceView` + 전용 렌더 스레드로 메인 스레드와 분리
- **DI 없음**, **ViewModel 없음** (상태는 화면 재구성 + prefs로 유지)

## 구조

단일 `:app` 모듈, 관심사별 패키지 분리.

```
sungbinland.app                진입점
sungbinland.workout.domain     운동 항목과 타이머 로직
sungbinland.workout.data       오늘의 세트 집계 로컬 저장
sungbinland.workout.haptic     진동
sungbinland.workout.ui         화면 조립, 다이얼로그, 콘페티, 색상 토큰, 공용 뷰 헬퍼
```


## 빌드 / 설치

```bash
./gradlew :app:installRelease
```

버전·SDK·서명 설정은 `gradle/libs.versions.toml`과 `app/build.gradle.kts` 참조.
