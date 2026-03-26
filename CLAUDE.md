# 디바이스 빌드 설치 규칙

- 디바이스에 앱 설치 시 항상 릴리스 빌드(`./gradlew :app:installRelease`)를 사용한다. debug 빌드와 release 빌드는 서명 키가 달라서 전환 시 앱 제거가 필요하고, 이때 사용자 데이터가 삭제된다.
- `adb uninstall` 실행 전에 반드시 사용자에게 확인을 받는다. 내부 데이터(DB, SharedPreferences 등)가 모두 삭제되므로 임의로 진행하지 않는다.
