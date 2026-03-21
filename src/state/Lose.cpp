#include "Lose.h"

#include <string>

#include "../globals.h"

void Lose::init() {
  lose_img = asw::assets::load_texture("assets/images/lose.png");
  font = asw::assets::load_font("assets/fonts/font.ttf", 16);
  timer = 0.0F;
}

void Lose::update(float dt) {
  Scene::update(dt);
  timer += dt;
  if (timer >= 3.0F) {
    manager.set_next_scene(States::Menu);
  }
}

void Lose::draw() {
  asw::draw::sprite(lose_img, asw::Vec2f(0, 0));
  asw::draw::text(font, "Final Score: " + std::to_string(g_final_score),
                  asw::Vec2f(160, 190), asw::Color(255, 255, 255));
}
