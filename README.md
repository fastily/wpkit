# ctools
Fastily's tools, bots, and frameworks, intended for use with WMF Wikis.

This repository is also the home to
* [FastilyBot](https://en.wikipedia.org/wiki/User:FastilyBot)
* [MTC!](https://en.wikipedia.org/wiki/Wikipedia:MTC!)

### Dependencies
* [jwiki](https://github.com/fastily/jwiki)

### Minimum System Requirements
* JDK/JRE ≥ 8u40

### See Also
* [toollabs reports](https://tools.wmflabs.org/fastilybot/)

### How To Build
1. Clone https://github.com/fastily/jwiki in the same directory (meaning `ctools` and `jwiki` must be at the same level)
2. In the `ctools` directory, run `./gradlew build -x test`
3. Build the jar by running `./gradlew mtc`
4. The JAR shoud now be available in `build/libs`
