#pragma once

#include <asw/asw.h>

#include "States.h"

class Splash : public asw::scene::Scene<States> {
 public:
  using asw::scene::Scene<States>::Scene;

  void init() override;
  void update(float dt) override;
  void draw() override;

 private:
  asw::Texture intro;
  float timer{};
};
