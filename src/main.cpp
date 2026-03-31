#include <asw/asw.h>

#include "./state/Game.h"
#include "./state/Lose.h"
#include "./state/Menu.h"
#include "./state/Splash.h"
#include "./state/States.h"

auto main() -> int {
  asw::core::init(482, 387);
  asw::core::print_info();

  asw::scene::SceneManager<States> app;
  app.register_scene<Splash>(States::Splash, app);
  app.register_scene<Menu>(States::Menu, app);
  app.register_scene<Game>(States::Game, app);
  app.register_scene<Lose>(States::Lose, app);
  app.set_next_scene(States::Splash);
  app.start();

  asw::core::shutdown();

  return 0;
}
