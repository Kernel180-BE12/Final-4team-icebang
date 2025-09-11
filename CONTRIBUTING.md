# Spring
## Project root
- IDE에서 Final-4team-icebang를 root로 열어야 합니다
- 현재 docker container가 없다면 docker container create 후 spring이 bootstrap되지 않습니다
  - 번거롭겠지만 spring boot restart 부탁드립니다.

## 배포 방법
### 공통
1. main에 checkout
2. tag 발행
3. remote (origin)에 tag push
### Spring
```
git tag user-service-v0.0.1
git push origin user-service-v0.0.1 or git push origin --tags

```
#### 네이밍 규칙
- user-service-v*
### Fast api
```
git tag pre-processing-v0.0.1
git push origin pre-processing-v0.0.1 git push origin --tags
```
#### 네이밍 규칙
- pre-processing-v*
