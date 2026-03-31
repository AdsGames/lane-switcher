#pragma once

#include <asw/asw.h>

struct Car {
  float x{};
  float y{};
  float speed{};
  int type{};  // 0 = car, 1 = truck
  asw::Texture image;
};
