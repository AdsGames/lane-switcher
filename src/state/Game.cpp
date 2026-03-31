#include "Game.h"

#include <string>

#include "../globals.h"

// AABB collision: (x,y,w,h) format
static bool aabb(float x1,
                 float y1,
                 float w1,
                 float h1,
                 float x2,
                 float y2,
                 float w2,
                 float h2) {
  return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
}

// Is there something touching player from the left? (car's right inside player)
// Java: left(xMin1, xMax1, xMin2, xMax2) = xMin1 < xMax2 && xMax1 > xMax2
static bool blocking_left(float px1, float px2, float /*cx1*/, float cx2) {
  return px1 < cx2 && px2 > cx2;
}

// Is there something touching player from the right? (car's left inside player)
// Java: right(xMin1, xMax1, xMin2, xMax2) = xMin2 < xMax1 && xMin1 < xMin2
static bool blocking_right(float px1, float px2, float cx1, float /*cx2*/) {
  return cx1 < px2 && px1 < cx1;
}

static const float lane_x[4] = {70, 175, 270, 370};
static const float gas_lane_x[5] = {30, 135, 230, 330, 430};

void Game::init() {
  // Load backgrounds
  background1 = asw::assets::load_texture("assets/images/background1.png");
  background2 = asw::assets::load_texture("assets/images/background2.png");

  // Load character sprites
  character[0] = asw::assets::load_texture("assets/images/character.png");
  character[1] = asw::assets::load_texture("assets/images/character_l.png");
  character[2] = asw::assets::load_texture("assets/images/character_r.png");

  exhaust[0] = asw::assets::load_texture("assets/images/exhaust1.png");
  exhaust[1] = asw::assets::load_texture("assets/images/exhaust2.png");
  exhaust[2] = asw::assets::load_texture("assets/images/exhaust3.png");

  // Load car images
  car_images[0] = asw::assets::load_texture("assets/images/car1.png");
  car_images[1] = asw::assets::load_texture("assets/images/car2.png");
  car_images[2] = asw::assets::load_texture("assets/images/car3.png");
  car_images[3] = asw::assets::load_texture("assets/images/car4.png");
  car_images[4] = asw::assets::load_texture("assets/images/car5.png");
  car_images[5] = asw::assets::load_texture("assets/images/car6.png");
  car_images[6] = asw::assets::load_texture("assets/images/car7.png");
  totaled_car = asw::assets::load_texture("assets/images/totaled_car.png");
  totaled_truck = asw::assets::load_texture("assets/images/totaled_truck.png");

  // Load pickup/hazard images
  gas_can_icon = asw::assets::load_texture("assets/images/gasCan.png");
  gas_can_img = asw::assets::load_texture("assets/images/gasCan.png");
  warning_img = asw::assets::load_texture("assets/images/warning.png");
  spike_trap_img = asw::assets::load_texture("assets/images/spikeTrap.png");
  start_img = asw::assets::load_texture("assets/images/start.png");

  // Load sounds
  snd_crash = asw::assets::load_sample("assets/sounds/crash.wav");
  snd_rev = asw::assets::load_sample("assets/sounds/rev.wav");
  snd_brake = asw::assets::load_sample("assets/sounds/brake.wav");
  snd_horn = asw::assets::load_sample("assets/sounds/horn.wav");
  snd_pop = asw::assets::load_sample("assets/sounds/pop.wav");
  snd_fill = asw::assets::load_sample("assets/sounds/fill.wav");
  snd_warning = asw::assets::load_sample("assets/sounds/warning.wav");
  snd_neverwork = asw::assets::load_sample("assets/sounds/neverwork.wav");

  // Load font
  font = asw::assets::load_font("assets/fonts/font.ttf", 16);

  // Reset player
  player_x = screen_w / 2.0F - 20.0F;
  player_y = screen_h - 120.0F;
  xspeed = 0.0F;
  character_speed = 2;
  turbo = false;
  can_move_left = true;
  can_move_right = true;
  start_speech = true;

  // Reset scroll
  scroll_bg1 = 0.0F;
  scroll_bg2 = -screen_h;

  // Reset game state
  gas_amount = max_gas;
  score_f = 0.0F;
  cooldown = -1.0F;
  was_up_held = false;
  was_down_held = false;

  // Clear entities
  cars.clear();
  gas_cans.clear();
  spike_traps.clear();
  warnings.clear();
}

void Game::update(float dt) {
  Scene::update(dt);

  const float scale = dt * 100.0F;

  // Check for death
  if (gas_amount <= 0.0F) {
    g_final_score = static_cast<int>(score_f);
    asw::sound::play(snd_neverwork);
    manager.set_next_scene(States::Lose);
    return;
  }

  // Dismiss start speech on any key press
  if (start_speech) {
    if (asw::input::get_key(asw::input::Key::Left) ||
        asw::input::get_key(asw::input::Key::Right) ||
        asw::input::get_key(asw::input::Key::Up) ||
        asw::input::get_key(asw::input::Key::Down) ||
        asw::input::get_key(asw::input::Key::Space)) {
      start_speech = false;
    }
  }

  // --- Speed control (up/down keys) ---
  bool up_held = asw::input::get_key(asw::input::Key::Up);
  bool down_held = asw::input::get_key(asw::input::Key::Down);

  if (up_held && !was_up_held) {
    character_speed = 3;
    asw::sound::play(snd_rev);
  } else if (down_held && !was_down_held) {
    character_speed = 0;
    asw::sound::play(snd_brake);
  }
  if ((!up_held && was_up_held) || (!down_held && was_down_held)) {
    character_speed = 2;
  }
  was_up_held = up_held;
  was_down_held = down_held;

  // Turbo (Ctrl)
  if (asw::input::get_key(asw::input::Key::LCtrl)) {
    turbo = true;
    character_speed = 8;
  } else {
    turbo = false;
    if (!up_held && !down_held) {
      character_speed = 2;
    }
  }

  // Horn
  if (asw::input::get_key(asw::input::Key::Space)) {
    asw::sound::play(snd_horn);
  }

  // --- Player boundary ---
  if (player_x > screen_w - 46.0F && xspeed > 0.0F) {
    xspeed = 0.0F;
  }
  if (player_x < 0.0F && xspeed < 0.0F) {
    xspeed = 0.0F;
  }

  // --- Check left/right movement blocking ---
  can_move_left = true;
  can_move_right = true;
  for (auto& car : cars) {
    bool y_overlap =
        aabb(player_x + 6, player_y + 2, 28, 67, car.x + 6, car.y + 2, 28, 67);
    if (y_overlap) {
      if (blocking_left(player_x + 6, player_x + 34, car.x + 6, car.x + 34)) {
        can_move_left = false;
        xspeed = 0.0F;
        player_x += 0.2F;
      }
      if (blocking_right(player_x + 6, player_x + 34, car.x + 6, car.x + 34)) {
        can_move_right = false;
        xspeed = 0.0F;
        player_x -= 0.2F;
      }
    }
  }

  // --- Horizontal movement ---
  float move_speed = (static_cast<float>(character_speed) + 1.0F) / 2.0F;
  if (asw::input::get_key(asw::input::Key::Left) && can_move_left) {
    xspeed = -move_speed;
    if (!can_move_right) {
      xspeed = -2.0F;
    }
  } else if (asw::input::get_key(asw::input::Key::Right) && can_move_right) {
    xspeed = move_speed;
    if (!can_move_left) {
      xspeed = 2.0F;
    }
  } else {
    xspeed = 0.0F;
  }

  player_x += xspeed * scale;

  // --- Spawn car ---
  if (asw::random::between(0, 19) == 1) {
    int car_idx = asw::random::between(0, 6);
    int lane = asw::random::between(0, 3);
    float car_x = lane_x[lane];
    int car_type = (car_idx >= 5) ? 1 : 0;

    bool can_spawn = true;
    for (const auto& c : cars) {
      if (aabb(c.x + 6, c.y + 69, 30, 80, car_x + 6, 0, 30, 80)) {
        can_spawn = false;
        break;
      }
    }
    if (can_spawn) {
      Car c;
      c.x = car_x;
      c.y = -80.0F;
      c.speed = static_cast<float>(asw::random::between(2, 3));
      c.type = car_type;
      c.image = car_images[car_idx];
      cars.push_back(c);
    }
  }

  // --- Spawn gas can ---
  if (asw::random::between(0, 299) == 1) {
    int lane = asw::random::between(0, 4);
    float gas_x = gas_lane_x[lane];

    bool can_spawn = true;
    for (const auto& c : cars) {
      if (aabb(c.x + 6, c.y + 69, 30, 80, gas_x + 6, 0, 30, 80)) {
        can_spawn = false;
        break;
      }
    }
    if (can_spawn) {
      GasCan g;
      g.x = gas_x;
      g.y = -40.0F;
      g.image = gas_can_img;
      gas_cans.push_back(g);
    }
  }

  // --- Spawn spike trap warning (only after score > 1000) ---
  int score_int = static_cast<int>(score_f);
  if (score_int > 1000) {
    int chance = 200 - score_int / 100;
    if (chance < 5) {
      chance = 5;
    }
    if (asw::random::between(0, chance) == 1) {
      int lane = asw::random::between(0, 4);
      WarningBubble wb;
      wb.x = gas_lane_x[lane];
      wb.y = 0.0F;
      wb.timer = 0.0F;
      wb.started = false;
      wb.image = warning_img;
      warnings.push_back(wb);
    }
  }

  // --- Update cars ---
  for (auto it = cars.begin(); it != cars.end();) {
    // Check front collision (car overtaking player from behind)
    // Java: bottom(y+6, y+69, car.y+6, car.y+69) = player bottom > car bottom
    // and player top < car bottom
    bool bottom_hit =
        (player_y + 6 < it->y + 69) && (player_y + 69 > it->y + 69);
    bool x_overlap =
        aabb(player_x + 6, player_y + 2, 28, 67, it->x + 6, it->y + 2, 28, 67);
    if (bottom_hit && x_overlap) {
      character_speed = 0;
    }

    // Move car
    it->y += (it->speed + static_cast<float>(character_speed)) * dt * 100.0F;

    // Remove if off screen
    if (it->y > screen_h + 80.0F) {
      it = cars.erase(it);
      continue;
    }

    // Car crashes into player
    if (it->speed > 0.0F && aabb(player_x + 6, player_y + 2, 28, 67, it->x + 6,
                                 it->y + 2, 28, 67)) {
      it->speed = 0.0F;
      it->image = (it->type == 0) ? totaled_car : totaled_truck;
      asw::sound::play(snd_crash);
      cooldown = 0.0F;
    }

    // Car-car collision
    for (auto& other : cars) {
      if (&other != &*it) {
        if (aabb(it->x + 6, it->y + 2, 30, 67, other.x + 6, other.y + 2, 30,
                 67)) {
          it->speed = other.speed;
          break;
        }
      }
    }

    ++it;
  }

  // --- Update warnings ---
  for (auto it = warnings.begin(); it != warnings.end();) {
    if (!it->started) {
      it->started = true;
      asw::sound::play(snd_warning);
    }
    it->timer += dt;
    if (it->timer >= 2.0F) {
      SpikeTrap st;
      st.x = it->x;
      st.y = it->y - 40.0F;
      st.image = spike_trap_img;
      spike_traps.push_back(st);
      it = warnings.erase(it);
    } else {
      ++it;
    }
  }

  // --- Update spike traps ---
  for (auto it = spike_traps.begin(); it != spike_traps.end();) {
    it->y += static_cast<float>(character_speed) * dt * 100.0F;
    if (it->y > screen_h + 80.0F) {
      it = spike_traps.erase(it);
      continue;
    }
    // Player hits spike trap
    if (aabb(player_x + 6, player_y + 2, 28, 67, it->x, it->y, 32, 5)) {
      gas_amount -= 100.0F;
      asw::sound::play(snd_pop);
      it = spike_traps.erase(it);
    } else {
      ++it;
    }
  }

  // --- Update gas cans ---
  for (auto it = gas_cans.begin(); it != gas_cans.end();) {
    it->y += static_cast<float>(character_speed) * dt * 100.0F;
    if (it->y > screen_h + 80.0F) {
      it = gas_cans.erase(it);
      continue;
    }
    // Player collects gas can
    if (aabb(player_x + 6, player_y + 2, 28, 67, it->x + 6, it->y + 2, 28,
             67)) {
      gas_amount += 100.0F;
      if (gas_amount > max_gas) {
        gas_amount = max_gas;
      }
      asw::sound::play(snd_fill);
      it = gas_cans.erase(it);
    } else {
      ++it;
    }
  }

  // --- Scroll background ---
  const float scroll_step = static_cast<float>(character_speed) * dt * 100.0F;
  scroll_bg1 += scroll_step;
  scroll_bg2 += scroll_step;
  if (scroll_bg1 >= screen_h) {
    scroll_bg1 -= screen_h * 2.0F;
  }
  if (scroll_bg2 >= screen_h) {
    scroll_bg2 -= screen_h * 2.0F;
  }

  // --- Score and cooldown ---
  if (cooldown < 0.0F) {
    score_f += scale;
  } else {
    cooldown += dt;
    if (cooldown >= 5.0F) {
      cooldown = -1.0F;
    }
  }

  // --- Gas depletion ---
  gas_amount -= 10.0F * dt;
  if (turbo && character_speed == 8) {
    gas_amount -= 10.0F * dt;
  }
}

void Game::draw() {
  // Backgrounds
  asw::draw::sprite(background1, asw::Vec2f(0, scroll_bg1));
  asw::draw::sprite(background2, asw::Vec2f(0, scroll_bg2));

  // Cars
  for (const auto& car : cars) {
    asw::draw::sprite(car.image, asw::Vec2f(car.x, car.y));
  }

  // Gas cans
  for (const auto& g : gas_cans) {
    asw::draw::sprite(g.image, asw::Vec2f(g.x, g.y));
  }

  // Warnings
  for (const auto& wb : warnings) {
    asw::draw::sprite(wb.image, asw::Vec2f(wb.x, wb.y));
  }

  // Spike traps
  for (const auto& st : spike_traps) {
    asw::draw::sprite(st.image, asw::Vec2f(st.x, st.y));
  }

  // Player character (based on xspeed direction)
  int char_frame = 0;
  if (xspeed < 0.0F) {
    char_frame = 1;
  } else if (xspeed > 0.0F) {
    char_frame = 2;
  }
  asw::draw::sprite(character[char_frame], asw::Vec2f(player_x, player_y));

  // Exhaust (based on character_speed)
  int exhaust_frame = 1;
  if (character_speed <= 0) {
    exhaust_frame = 0;
  } else if (character_speed > 2) {
    exhaust_frame = 2;
  }
  asw::draw::sprite(exhaust[exhaust_frame],
                    asw::Vec2f(player_x + 4, player_y + 69));

  // Start speech bubble
  if (start_speech) {
    asw::draw::sprite(start_img, asw::Vec2f(player_x - 100, player_y - 80));
  }

  // Score message
  std::string score_msg;
  if (cooldown < 0.0F) {
    score_msg = "Score:" + std::to_string(static_cast<int>(score_f));
  } else {
    score_msg = "No score for " +
                std::to_string(static_cast<int>(5.0F - cooldown)) + " seconds";
  }
  asw::draw::text(font, score_msg, asw::Vec2f(20, 20),
                  asw::Color(255, 255, 255));

  // Gas bar: black border, red fill
  asw::draw::sprite(gas_can_icon, asw::Vec2f(40, 340));
  asw::draw::rect_fill(asw::Quadf(88, 348, max_gas + 4, 24),
                       asw::Color(0, 0, 0));
  asw::draw::rect_fill(asw::Quadf(90, 350, static_cast<float>(gas_amount), 20),
                       asw::Color(255, 0, 0));
}
