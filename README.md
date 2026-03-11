# sungbinland

Codex, Pencil, Claude Code와 함께 만드는 나만의 슈퍼앱

배운 점: 

AI가 짠 코드를 내가 보기 시작하는 순간... 고치고 싶은 욕구가 생기면서 작업이 엄청 느려진다!

규칙도 최소한으로 하거나, 아예 없이 하는 게 짱이다.

aaaaaa

## 릴리스 빌드 및 설치

```bash
# 릴리스 APK 빌드
./gradlew assembleRelease

# USB 연결된 기기에 설치
adb install -r app/build/outputs/apk/release/app-release.apk

# 설치 후 앱이 갱신되지 않으면 강제 종료 후 재시작
adb shell am force-stop sungbinland.app
adb shell monkey -p sungbinland.app -c android.intent.category.LAUNCHER 1
```
