#include "Menu.h"

#include <string>

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

static const int lane_x[4] = {70, 175, 270, 370};

void Menu::init() {
  background1 = asw::assets::load_texture("assets/images/background1.png");
  background2 = asw::assets::load_texture("assets/images/background2.png");
  menu_img = asw::assets::load_texture("assets/images/menu.png");
  button_start = asw::assets::load_texture("assets/images/button_start.png");
  button_help = asw::assets::load_texture("assets/images/button_help.png");
  help_img = asw::assets::load_texture("assets/images/help.png");

  car_images[0] = asw::assets::load_texture("assets/images/car1.png");
  car_images[1] = asw::assets::load_texture("assets/images/car2.png");
  car_images[2] = asw::assets::load_texture("assets/images/car3.png");
  car_images[3] = asw::assets::load_texture("assets/images/car4.png");
  car_images[4] = asw::assets::load_texture("assets/images/car5.png");
  car_images[5] = asw::assets::load_texture("assets/images/car6.png");
  car_images[6] = asw::assets::load_texture("assets/images/car7.png");
  totaled_car = asw::assets::load_texture("assets/images/totaled_car.png");
  totaled_truck = asw::assets::load_texture("assets/images/totaled_truck.png");

  music = asw::assets::load_music("assets/sounds/music.mp3");
  late = asw::assets::load_sample("assets/sounds/late.wav");

  scroll_bg1 = 0.0F;
  scroll_bg2 = -screen_h;
  help_on = false;
  cars.clear();

  asw::sound::play_music(music, 255);
}

void Menu::update(float dt) {
  Scene::update(dt);

  const float scroll_step = character_speed * dt * 100.0F;

  // Scroll background
  scroll_bg1 += scroll_step;
  scroll_bg2 += scroll_step;
  if (scroll_bg1 >= screen_h) {
    scroll_bg1 -= screen_h * 2.0F;
  }
  if (scroll_bg2 >= screen_h) {
    scroll_bg2 -= screen_h * 2.0F;
  }

  // Spawn decorative cars
  if (asw::random::between(0, 19) == 1) {
    int car_idx = asw::random::between(0, 6);
    int lane = asw::random::between(0, 3);
    int car_x = lane_x[lane];
    int car_type = (car_idx >= 5) ? 1 : 0;

    bool can_spawn = true;
    for (const auto& car : cars) {
      if (aabb(car.x + 6, car.y + 69, 30, 80, car_x + 6, 0, 30, 80)) {
        can_spawn = false;
        break;
      }
    }
    if (can_spawn) {
      Car c;
      c.x = static_cast<float>(car_x);
      c.y = -80.0F;
      c.speed = static_cast<float>(asw::random::between(2, 3));
      c.type = car_type;
      c.image = car_images[car_idx];
      cars.push_back(c);
    }
  }

  // Move cars
  for (auto it = cars.begin(); it != cars.end();) {
    it->y += (it->speed + character_speed) * dt * 100.0F;
    if (it->y > 387.0F + 80.0F) {
      it = cars.erase(it);
    } else {
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
  }

  // Button click detection
  const auto& mouse = asw::input::get_mouse();
  if (asw::input::get_mouse_button_down(asw::input::MouseButton::Left)) {
    float mx = mouse.position.x;
    float my = mouse.position.y;

    if (help_on) {
      help_on = false;
    } else if (mx >= 150 && mx <= 330 && my >= 160 && my <= 200) {
      // Start button
      asw::sound::play(late);
      manager.set_next_scene(States::Game);
    } else if (mx >= 150 && mx <= 330 && my >= 240 && my <= 280) {
      // Help button
      help_on = true;
    }
  }
}

void Menu::draw() {
  // Scrolling background
  asw::draw::sprite(background1, asw::Vec2f(0, scroll_bg1));
  asw::draw::sprite(background2, asw::Vec2f(0, scroll_bg2));

  // Decorative cars
  for (const auto& car : cars) {
    asw::draw::sprite(car.image, asw::Vec2f(car.x, car.y));
  }

  // Menu overlay
  asw::draw::sprite(menu_img, asw::Vec2f(0, 0));
  asw::draw::sprite(button_start, asw::Vec2f(150, 160));
  asw::draw::sprite(button_help, asw::Vec2f(150, 240));

  if (help_on) {
    asw::draw::sprite(help_img, asw::Vec2f(0, 0));
  }
}
