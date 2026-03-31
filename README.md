# LaneSwitcher

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=AdsGames_lane-switcher&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=AdsGames_lane-switcher)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=AdsGames_lane-switcher&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=AdsGames_lane-switcher)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=AdsGames_lane-switcher&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=AdsGames_lane-switcher)

A fast paced car dodging race against time.

This is an old project I made when I was 13. The OOP is horrendous, and it uses applets. Viewer disgression is advised.

## Demo

[Web Demo](https://adsgames.github.io/lane-switcher/)

## Setup

### CMake

```bash
cmake --preset debug
cmake --build --preset debug
```

### Build Emscripten

```bash
emcmake cmake --preset debug
cmake --build --preset debug
```
