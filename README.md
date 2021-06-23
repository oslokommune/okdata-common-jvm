common-jvm
=============

Library for common functionality for dataplatform components.

## Publishing new releases

Releasing new patch:
```
git checkout main
git pull
./gradlew clean build
./gradlew publish
git add .
git commit -m "Release <new-version>"
git tag -a v<new-version> -m "Release <new-version>"
git push --follow-tags
```

Releasing new minor version:
```
git checkout main
git pull
Change incrementPatch -> incrementMinor in build.gradle:generateMetadataFileForCommonJvmPublication:dependsOn
./gradlew clean build
./gradlew publish
Change back to incrementPatch in build.gradle:generateMetadataFileForCommonJvmPublication:dependsOn
git add .
git commit -m "Release <new-version>"
git tag -a v<new-version> -m "Release <new-version>"
git push --follow-tags
```

Releasing new major version:
```
git checkout main
git pull
Change incrementPatch -> incrementMajor in build.gradle:generateMetadataFileForCommonJvmPublication:dependsOn
./gradlew clean build
./gradlew publish
Change back to incrementPatch in build.gradle:generateMetadataFileForCommonJvmPublication:dependsOn
git add .
git commit -m "Release <new-version>"
git tag -a v<new-version> -m "Release <new-version>"
git push --follow-tags
```