common-jvm
=============

Library for common functionality for dataplatform components.

## Publishing new releases

```
git checkout master
git pull
./gradlew clean build
./gradlew publish
git add .
git tag -a v<new-version> -m "Release <new-version>"
git push --follow-tags
```
