# Maven Central 배포 가이드 (Central Portal)

## 1단계: Central Portal 계정 생성
1. https://central.sonatype.com 접속
2. GitHub 계정으로 가입 (권장)
3. 로그인 완료

## 2단계: Namespace 등록 및 검증
1. Central Portal → Namespaces 메뉴 이동
2. GitHub로 가입한 경우 `io.github.leewoo97` 자동 등록/검증 확인
3. 자동 등록이 없으면:
   - Add Namespace 클릭
   - `io.github.leewoo97` 입력
   - GitHub 저장소에 verification key 추가하여 검증 완료

## 3단계: User Token 발급
1. Central Portal 우측 상단 계정 메뉴 → View Account
2. Generate User Token 클릭
3. Username과 Password 복사 (이것이 배포 인증 정보)

## 4단계: Maven settings.xml 설정
`~/.m2/settings.xml` 파일에 아래 내용 추가:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

⚠️ **절대 GitHub에 올리지 마세요!**

## 5단계: GPG 키 생성 및 업로드

### GPG 설치
- Mac: `brew install gnupg`
- Windows: https://www.gnupg.org/download/
- Linux: `apt-get install gnupg` 또는 `yum install gnupg`

### GPG 키 생성
```bash
gpg --full-gen-key
```
- 알고리즘: RSA and RSA
- 키 길이: 4096
- 이름/이메일: 배포자 정보 입력

### 공개키 업로드
```bash
# 키 ID 확인
gpg --list-keys

# 공개키 업로드
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### GPG 설정 (선택사항)
Maven이 자동으로 GPG를 찾지만, 수동 설정도 가능:

`~/.m2/settings.xml`에 추가:
```xml
<profiles>
  <profile>
    <id>gpg</id>
    <properties>
      <gpg.executable>gpg</gpg.executable>
      <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
    </properties>
  </profile>
</profiles>

<activeProfiles>
  <activeProfile>gpg</activeProfile>
</activeProfiles>
```

## 6단계: GitHub 저장소 생성 및 푸시
```bash
# GitHub에서 새 저장소 생성: sse-console-spring-boot-starter
# https://github.com/leewoo97/sse-console-spring-boot-starter

# Git 초기화 (아직 안 했다면)
git init
git add .
git commit -m "Initial commit"

# 원격 저장소 연결 및 푸시
git remote add origin https://github.com/leewoo97/sse-console-spring-boot-starter.git
git branch -M main
git push -u origin main
```

## 7단계: 로컬 빌드 테스트
```bash
# 프로젝트 빌드 및 로컬 설치
mvn clean install

# 로컬 Maven 저장소에 배포 (테스트용)
mvn clean deploy -DaltDeploymentRepository=local::file:./target/local-repo
```

로컬 프로젝트에서 테스트:
```xml
<repositories>
  <repository>
    <id>local-test</id>
    <url>file:///경로/to/target/local-repo</url>
  </repository>
</repositories>

<dependency>
  <groupId>io.github.leewoo97</groupId>
  <artifactId>sse-console-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 8단계: Maven Central 배포
```bash
# 배포 실행
mvn clean deploy

# GPG 패스프레이즈 입력 요구 시 입력
```

## 9단계: Central Portal에서 확인 및 퍼블리시
1. https://central.sonatype.com 로그인
2. View Deployments 메뉴 이동
3. 업로드된 배포 확인 (PENDING → VALIDATED)
4. **Publish 버튼 클릭** (이 순간부터 Maven Central에 공개)

⚠️ **주의사항:**
- Publish 후에는 동일 버전 재업로드 불가
- 삭제/수정 불가능
- 신중하게 Publish 클릭!

## 10단계: 배포 확인
약 15-30분 후 Maven Central에서 검색 가능:
- https://central.sonatype.com
- https://mvnrepository.com

사용자가 의존성 추가:
```xml
<dependency>
  <groupId>io.github.leewoo97</groupId>
  <artifactId>sse-console-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 배포 체크리스트
- [ ] Central Portal 가입 완료
- [ ] Namespace (io.github.leewoo97) 검증 완료
- [ ] User Token 발급 및 settings.xml 설정
- [ ] GPG 키 생성 및 공개키 업로드
- [ ] GitHub 저장소 생성 및 코드 푸시
- [ ] 로컬 빌드 테스트 성공
- [ ] mvn clean deploy 성공
- [ ] Central Portal에서 VALIDATED 확인
- [ ] Publish 클릭
- [ ] Maven Central 검색 확인

## 다음 버전 배포 시
1. pom.xml의 version 변경 (예: 1.0.0 → 1.0.1)
2. 변경사항 커밋 및 푸시
3. `mvn clean deploy` 실행
4. Central Portal에서 Publish

## 문제 해결
- **GPG 서명 실패**: `gpg --list-keys`로 키 확인, 패스프레이즈 재확인
- **인증 실패**: settings.xml의 토큰 정보 재확인
- **Namespace 오류**: Central Portal에서 namespace 검증 상태 확인
- **배포 실패**: `mvn clean deploy -X`로 상세 로그 확인
