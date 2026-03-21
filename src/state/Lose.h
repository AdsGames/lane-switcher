#pragma once

#include <asw/asw.h>

#include "States.h"

class Lose : public asw::scene::Scene<States> {
 public:
  using asw::scene::Scene<States>::Scene;

  void init() override;
  void update(float dt) override;
  void draw() override;

 private:
  asw::Texture lose_img;
  asw::Font font;
  float timer{};
};
