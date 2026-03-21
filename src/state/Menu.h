#pragma once

#include <asw/asw.h>
#include <array>
#include <vector>

#include "../Car.h"
#include "States.h"

class Menu : public asw::scene::Scene<States> {
 public:
  using asw::scene::Scene<States>::Scene;

  void init() override;
  void update(float dt) override;
  void draw() override;

 private:
  asw::Texture background1;
  asw::Texture background2;
  asw::Texture menu_img;
  asw::Texture button_start;
  asw::Texture button_help;
  asw::Texture help_img;

  asw::Music music;
  asw::Sample late;

  std::array<asw::Texture, 7> car_images;
  asw::Texture totaled_car;
  asw::Texture totaled_truck;
  std::vector<Car> cars;

  float scroll_bg1{};
  float scroll_bg2{};
  bool help_on{};

  static constexpr float character_speed = 2.0F;
  static constexpr float screen_h = 387.0F;
};
