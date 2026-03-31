#pragma once

#include <asw/asw.h>

struct WarningBubble {
  float x{};
  float y{};
  float timer{};
  bool started{};
  asw::Texture image;
};
