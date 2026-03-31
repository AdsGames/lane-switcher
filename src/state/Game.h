#pragma once

#include <asw/asw.h>
#include <array>
#include <vector>

#include "../Car.h"
#include "../GasCan.h"
#include "../SpikeTrap.h"
#include "../WarningBubble.h"
#include "States.h"

class Game : public asw::scene::Scene<States> {
 public:
  using asw::scene::Scene<States>::Scene;

  void init() override;
  void update(float dt) override;
  void draw() override;

 private:
  // Backgrounds
  asw::Texture background1;
  asw::Texture background2;

  // Character sprites
  std::array<asw::Texture, 3> character;  // neutral, left, right
  std::array<asw::Texture, 3> exhaust;    // stopped, normal, fast

  // Car images
  std::array<asw::Texture, 7> car_images;
  asw::Texture totaled_car;
  asw::Texture totaled_truck;

  // Pickup / hazard images
  asw::Texture gas_can_icon;
  asw::Texture gas_can_img;
  asw::Texture warning_img;
  asw::Texture spike_trap_img;
  asw::Texture start_img;

  // Sounds
  asw::Sample snd_crash;
  asw::Sample snd_rev;
  asw::Sample snd_brake;
  asw::Sample snd_horn;
  asw::Sample snd_pop;
  asw::Sample snd_fill;
  asw::Sample snd_warning;
  asw::Sample snd_neverwork;

  // Font
  asw::Font font;

  // Entity lists
  std::vector<Car> cars;
  std::vector<GasCan> gas_cans;
  std::vector<SpikeTrap> spike_traps;
  std::vector<WarningBubble> warnings;

  // Player state
  float player_x{};
  float player_y{};
  float xspeed{};
  int character_speed{};
  bool turbo{};
  bool can_move_left{};
  bool can_move_right{};
  bool start_speech{};

  // Background scroll
  float scroll_bg1{};
  float scroll_bg2{};

  // Game state
  float gas_amount{};
  float score_f{};
  float cooldown{};  // -1 = not running, >= 0 = elapsed seconds

  // Previous key state for one-shot sound triggers
  bool was_up_held{};
  bool was_down_held{};

  static constexpr float screen_h = 387.0F;
  static constexpr float screen_w = 482.0F;
  static constexpr float max_gas = 300.0F;
};
