#include "Splash.h"

void Splash::init() {
  intro = asw::assets::load_texture("assets/images/intro.png");
  timer = 0.0F;
}

void Splash::update(float dt) {
  Scene::update(dt);
  timer += dt;
  if (timer >= 2.0F) {
    manager.set_next_scene(States::Menu);
  }
}

void Splash::draw() {
  asw::draw::sprite(intro, asw::Vec2f(0, 0));
}
