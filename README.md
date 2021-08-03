common-jvm
=============

Library for common functionality for dataplatform components.

## Publishing new releases

Releasing new version:
```
git checkout main
git pull
./gradlew clean build
./gradlew incrementPatch|incrementMinor|incrementMajor
git add .
git commit -m "Release <new-version>"
git tag -a v<new-version> -m "Release <new-version>"
git push --follow-tags
```

Now the new patch will be picked up by jitpack.io and be built and released.

## Using the library:
Add to build.gradle : 
```
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'com.github.oslokommune:okdata-common-jvm:X.Y.Z'
}
```