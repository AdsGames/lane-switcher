package net.adsgames.laneswitcher;

import java.applet.*;
import java.applet.AudioClip;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Laneswitcher extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 8712861426869530722L;

	int appletsize_x = 482;
	int appletsize_y = 387;

	double x;
	double y;

	int numberCars = 30;
	int characterSpeed = 2;
	int scrollBackground1;
	int scrollBackground2;
	int score;
	int game = 0;
	int mx = 0;
	int my = 0;

	double xspeed;
	double gasAmount;
	boolean helpOn = false;
	boolean startSpeech = false;
	boolean endSpeech = true;
	boolean firstRun = true;
	boolean canMoveLeft = true;
	boolean canMoveRight = true;
	boolean turbo = false;

	Image background1;
	Image background2;
	Image[] character;
	Image gasCanIcon;
	Image lose;
	Image intro;
	Image help;
	Image menu;
	Image start;
	Image button_start;
	Image button_help;
	Image[] exhaust;

	AudioClip crash;
	AudioClip rev;
	AudioClip brake;
	AudioClip horn;
	AudioClip pop;
	AudioClip fill;
	AudioClip warning;
	AudioClip late;
	AudioClip neverwork;

	private Image dbImage;
	private Graphics dbg;

	String scoreMessage;

	Font font;

	StopWatch coolDown;
	StopWatch endTime;

	Collision coll;

	ArrayList<Car> cars = new ArrayList<Car>();
	ArrayList<GasCan> gascans = new ArrayList<GasCan>();
	ArrayList<WarningBubble> warnings = new ArrayList<WarningBubble>();
	ArrayList<SpikeTrap> spiketraps = new ArrayList<SpikeTrap>();

	MP3Loader mp3;

	public void updateScores() {
		try {
			SAXBuilder builder = new SAXBuilder();
			InputStream xmlFile = getClass().getResourceAsStream("data/scores.xml");

			Document doc = (Document) builder.build(xmlFile);
			Element table = doc.getRootElement();

			Element scores = new Element("scores");
			table.addContent(scores);
			Element newScore = new Element("score").setText(Integer.toString(score));
			scores.addContent(newScore);
			Element name = new Element("name").setText("Name");
			scores.addContent(name);

			XMLOutputter xmlOutput = new XMLOutputter();

			// display
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter("data/scores.xml"));

			// xmlOutput.output(doc, System.out);

			System.out.println("File updated!");
		} catch (IOException io) {
			io.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		}
	}

	public void init() {
		repaint();

		helpOn = false;
		x = appletsize_x / 2 - 20;
		y = appletsize_y - 120;
		scrollBackground1 = 0;
		scrollBackground2 = -387;
		score = 0;
		xspeed = 0.0;
		gasAmount = 300;
		scoreMessage = "";

		// Clear array lists
		cars.clear();
		gascans.clear();
		warnings.clear();
		spiketraps.clear();

		if (firstRun) {
			exhaust = new Image[3];
			
			exhaust[0] = getImage(this.getClass().getResource("img/exhaust1.png"));
			exhaust[1] = getImage(this.getClass().getResource("img/exhaust2.png"));
			exhaust[2] = getImage(this.getClass().getResource("img/exhaust3.png"));

			character = new Image[3];
			character[0] = getImage(this.getClass().getResource("img/character.png"));
			character[1] = getImage(this.getClass().getResource("img/character_l.png"));
			character[2] = getImage(this.getClass().getResource("img/character_r.png"));

			help = getImage(this.getClass().getResource("img/help.png"));
			intro = getImage(this.getClass().getResource("img/intro.png"));
			background1 = getImage(this.getClass().getResource("img/background1.png"));
			background2 = getImage(this.getClass().getResource("img/background2.png"));
			gasCanIcon = getImage(this.getClass().getResource("img/gasCan.png"));
			lose = getImage(this.getClass().getResource("img/lose.png"));
			start = getImage(this.getClass().getResource("img/start.png"));
			menu = getImage(this.getClass().getResource("img/menu.png"));
			button_start = getImage(this.getClass().getResource("img/button_start.png"));
			button_help = getImage(this.getClass().getResource("img/button_help.png"));

			crash = getAudioClip(this.getClass().getResource("sound/crash.wav"));
			rev = getAudioClip(this.getClass().getResource("sound/rev.wav"));
			brake = getAudioClip(this.getClass().getResource("sound/brake.wav"));
			horn = getAudioClip(this.getClass().getResource("sound/horn.wav"));
			pop = getAudioClip(this.getClass().getResource("sound/pop.wav"));
			fill = getAudioClip(this.getClass().getResource("sound/fill.wav"));
			warning = getAudioClip(this.getClass().getResource("sound/warning.wav"));
			late = getAudioClip(this.getClass().getResource("sound/late.wav"));
			neverwork = getAudioClip(this.getClass().getResource("sound/neverwork.wav"));

			font = new Font("Eras Bold ITC", Font.BOLD, 16);

			coll = new Collision();
			coolDown = new StopWatch();

			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);

			firstRun = false;

			mp3 = new MP3Loader("/net/adsgames/laneswitcher/sound/music.mp3", true);
			
			resize(appletsize_x, appletsize_y);
		}
	}

	public void start() {
		Thread th = new Thread(this);
		th.start();
	}

	public void stop() {
	}

	public void destroy() {
		mp3.close();
	}

	// Mouse events
	public void mouseMoved(MouseEvent me) {
		mx = me.getX();
		my = me.getY();
		me.consume();
	}

	public void mouseReleased(MouseEvent me) {
		if (game == 1) {
			if (helpOn == true) {
				helpOn = false;
			} else if (coll.any(mx, mx, 150, 330, my, my, 160, 200)) {
				game = 3;
				startSpeech = true;
				late.play();
			} else if (coll.any(mx, mx, 150, 330, my, my, 240, 280)) {
				if (helpOn == false) {
					helpOn = true;
				}
			}
		}
		me.consume();
	}

	public void mouseClicked(MouseEvent me) {
	}

	public void mouseDragged(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		if (game == 3) {
			if (startSpeech == true) {
				startSpeech = false;
			}

			if (k == KeyEvent.VK_LEFT) {
				// Makes sure you dont run over cars
				if (canMoveLeft) {
					xspeed = -1.0 * ((characterSpeed + 1.0) / 2.0);
					if (!canMoveRight) {
						xspeed = -2.0;
					}
				}
			} else if (k == KeyEvent.VK_RIGHT) {
				if (canMoveRight) {
					xspeed = ((characterSpeed + 1.0) / 2.0);
					if (!canMoveLeft) {
						xspeed = 2.0;
					}
				}
			} else if (k == KeyEvent.VK_UP) {
				if (characterSpeed != 3) {
					characterSpeed = 3;
					rev.play();
				}
			} else if (k == KeyEvent.VK_DOWN) {
				if (characterSpeed != 0) {
					characterSpeed = 0;
					brake.play();
				}
			} else if (k == 32) {
				horn.play();
			} else if (k == 17) {
				turbo = true;
			} else {
				System.out.println(k);
			}
		}
		e.consume();
	}

	public void keyReleased(KeyEvent e) {
		int k = e.getKeyCode();
		if (game == 2) {
			if (k == 32) {
				game = 3;
			}
		} else if (game == 3) {
			if (k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN) {
				characterSpeed = 2;
			} else if (k == KeyEvent.VK_RIGHT) {
				xspeed = 0;
			} else if (k == KeyEvent.VK_LEFT) {
				xspeed = 0;
			} else if (k == 17) {
				turbo = false;
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	// Run main game
	public void run() {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		while (true) {
			repaint();

			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				// do nothing
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			// Loading
			if (game == -1) {
				game = 0;
			}

			// Splash
			if (game == 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
				}
				game = 1;
			}

			// Menu
			if (game == 1) {
				// Spawn car
				if ((int) (Math.random() * 20) == 1) {
					Image carImage;
					int carX;
					int carType;
					switch ((int) (Math.random() * 7)) {
					case 0:
						carImage = (getImage(this.getClass().getResource("img/car1.png")));
						carType = 0;
						break;
					case 1:
						carImage = (getImage(this.getClass().getResource("img/car2.png")));
						carType = 0;
						break;
					case 2:
						carImage = (getImage(this.getClass().getResource("img/car3.png")));
						carType = 0;
						break;
					case 3:
						carImage = (getImage(this.getClass().getResource("img/car4.png")));
						carType = 0;
						break;
					case 4:
						carImage = (getImage(this.getClass().getResource("img/car5.png")));
						carType = 0;
						break;
					case 5:
						carImage = (getImage(this.getClass().getResource("img/car6.png")));
						carType = 1;
						break;
					case 6:
						carImage = (getImage(this.getClass().getResource("img/car7.png")));
						carType = 1;
						break;
					default:
						carImage = (getImage(this.getClass().getResource("img/car1.png")));
						carType = 0;
						break;
					}
					switch ((int) (Math.random() * 4)) {
					case 0:
						carX = 70;
						break;
					case 1:
						carX = 175;
						break;
					case 2:
						carX = 270;
						break;
					case 3:
						carX = 370;
						break;
					default:
						carX = 70;
						break;
					}
					// Makes sure cars are not on top of eachother
					boolean canSpawn = true;
					for (int i = 0; i < cars.size(); i++) {
						if (coll.any(cars.get(i).getX() + 6, cars.get(i).getX() + 36, carX + 6, carX + 36,
								cars.get(i).getY() + 69, cars.get(i).getY() + 149, 0, 80)) {
							canSpawn = false;
						}
					}
					if (canSpawn) {
						cars.add(new Car(carX, -80, (int) (Math.random() * 2) + 2, carImage, carType));
					}
				}

				// Handle cars
				for (int i = 0; i < cars.size(); i++) {
					// Move car
					cars.get(i).setY(cars.get(i).getY() + (cars.get(i).getSpeed() + characterSpeed));
					if (cars.get(i).getY() > appletsize_y + 80) {
						cars.remove(i);
						break;
					}
					// Check collision from eachother
					for (int t = 0; t < cars.size(); t++) {
						if (coll.any(cars.get(i).getX() + 6, cars.get(i).getX() + 36, cars.get(t).getX() + 6,
								cars.get(t).getX() + 36, cars.get(i).getY() + 2, cars.get(i).getY() + 69,
								cars.get(t).getY() + 2, cars.get(t).getY() + 69)) {
							cars.get(i).setSpeed(cars.get(t).getSpeed());
							break;
						}
					}
				}

				// Scroll background
				if (scrollBackground1 >= 387 - characterSpeed) {
					scrollBackground1 = scrollBackground2 - 387;
					scrollBackground1 = scrollBackground1 + characterSpeed;
				} else {
					scrollBackground1 = scrollBackground1 + characterSpeed;
				}

				if (scrollBackground2 >= 387 - characterSpeed) {
					scrollBackground2 = scrollBackground1 - 387;
					scrollBackground2 = scrollBackground2 + characterSpeed;
				} else {
					scrollBackground2 = scrollBackground2 + characterSpeed;
				}
			}

			// Main game
			if (game == 3) {
				if (x > appletsize_x - 46) {
					if (xspeed > 0) {
						xspeed = 0;
					}
				}
				if (x < 0) {
					if (xspeed < 0) {
						xspeed = 0;
					}
				}
				if (turbo == true) {
					characterSpeed = 8;
				}

				// Move
				x = x + xspeed;

				canMoveLeft = true;
				canMoveRight = true;
				for (int i = 0; i < cars.size(); i++) {
					if (coll.left((int) (x) + 6, (int) (x) + 34, cars.get(i).getX() + 6, cars.get(i).getX() + 34)
							&& coll.any((int) (x) + 6, (int) (x) + 34, cars.get(i).getX() + 6, cars.get(i).getX() + 34,
									(int) (y) + 2, (int) (y) + 69, cars.get(i).getY() + 2, cars.get(i).getY() + 69)) {
						canMoveLeft = false;
						xspeed = 0;
						x = x + 0.2;
						break;
					}
				}
				for (int i = 0; i < cars.size(); i++) {
					if (coll.right((int) (x) + 6, (int) (x) + 34, cars.get(i).getX() + 6, cars.get(i).getX() + 34)
							&& coll.any((int) (x) + 6, (int) (x) + 34, cars.get(i).getX() + 6, cars.get(i).getX() + 34,
									(int) (y) + 2, (int) (y) + 69, cars.get(i).getY() + 2, cars.get(i).getY() + 69)) {
						canMoveRight = false;
						xspeed = 0;
						x = x - 0.2;
						break;
					}
				}

				// Spawn car
				if ((int) (Math.random() * 20) == 1) {
					Image carImage;
					int carX;
					int carType;
					switch ((int) (Math.random() * 7)) {
					case 0:
						carImage = (getImage(this.getClass().getResource("img/car1.png")));
						carType = 0;
						break;
					case 1:
						carImage = (getImage(this.getClass().getResource("img/car2.png")));
						carType = 0;
						break;
					case 2:
						carImage = (getImage(this.getClass().getResource("img/car3.png")));
						carType = 0;
						break;
					case 3:
						carImage = (getImage(this.getClass().getResource("img/car4.png")));
						carType = 0;
						break;
					case 4:
						carImage = (getImage(this.getClass().getResource("img/car5.png")));
						carType = 0;
						break;
					case 5:
						carImage = (getImage(this.getClass().getResource("img/car6.png")));
						carType = 1;
						break;
					case 6:
						carImage = (getImage(this.getClass().getResource("img/car7.png")));
						carType = 1;
						break;
					default:
						carImage = (getImage(this.getClass().getResource("img/car1.png")));
						carType = 0;
						break;
					}
					switch ((int) (Math.random() * 4)) {
					case 0:
						carX = 70;
						break;
					case 1:
						carX = 175;
						break;
					case 2:
						carX = 270;
						break;
					case 3:
						carX = 370;
						break;
					default:
						carX = 70;
						break;
					}
					// Makes sure cars are not on top of eachother
					boolean canSpawn = true;
					for (int i = 0; i < cars.size(); i++) {
						if (coll.any(cars.get(i).getX() + 6, cars.get(i).getX() + 36, carX + 6, carX + 36,
								cars.get(i).getY() + 69, cars.get(i).getY() + 149, 0, 80)) {
							canSpawn = false;
						}
					}
					if (canSpawn) {
						cars.add(new Car(carX, -80, (int) (Math.random() * 2) + 2, carImage, carType));
					}
				}

				// Spawn gas can
				if ((int) (Math.random() * 300) == 1) {
					Image gasImage = (getImage(this.getClass().getResource("img/gasCan.png")));
					int gasX;
					switch ((int) (Math.random() * 8)) {
					case 0:
						gasX = 30;
						break;
					case 1:
						gasX = 135;
						break;
					case 2:
						gasX = 230;
						break;
					case 3:
						gasX = 330;
						break;
					case 4:
						gasX = 430;
						break;
					default:
						gasX = 30;
						break;
					}
					// Makes sure cars are not on top of eachother
					boolean canSpawn = true;
					for (int i = 0; i < cars.size(); i++) {
						if (coll.any(cars.get(i).getX() + 6, cars.get(i).getX() + 36, gasX + 6, gasX + 36,
								cars.get(i).getY() + 69, cars.get(i).getY() + 149, 0, 80)) {
							canSpawn = false;
						}
					}
					if (canSpawn) {
						gascans.add(new GasCan(gasX, -40, gasImage));
					}
				}

				// Spawn spike trap
				if ((int) (Math.random() * (200 - score / 100)) == 1 && score > 1000) {
					Image warningInfo = getImage(this.getClass().getResource("img/warning.png"));
					int warningX;
					switch ((int) (Math.random() * 5)) {
					case 0:
						warningX = 30;
						break;
					case 1:
						warningX = 135;
						break;
					case 2:
						warningX = 230;
						break;
					case 3:
						warningX = 330;
						break;
					case 4:
						warningX = 430;
						break;
					default:
						warningX = 30;
						break;
					}
					warnings.add(new WarningBubble(warningX, 0, warningInfo));
				}

				// Handle cars
				for (int i = 0; i < cars.size(); i++) {
					// Checks if car is crashing from front
					if (coll.bottom((int) (y) + 6, (int) (y) + 69, cars.get(i).getY() + 6, cars.get(i).getY() + 69)
							&& coll.any((int) (x) + 6, (int) (x) + 36, cars.get(i).getX() + 6, cars.get(i).getX() + 36,
									(int) (y) + 2, (int) (y) + 69, cars.get(i).getY() + 2, cars.get(i).getY() + 69)) {
						characterSpeed = 0;
					}
					// Move car
					cars.get(i).setY(cars.get(i).getY() + (cars.get(i).getSpeed() + characterSpeed));
					if (cars.get(i).getY() > appletsize_y + 80) {
						cars.remove(i);
						break;
					}
					// Check collision from car
					if (cars.get(i).getSpeed() > 0
							&& coll.any((int) (x) + 6, (int) (x) + 36, cars.get(i).getX() + 6, cars.get(i).getX() + 36,
									(int) (y) + 2, (int) (y) + 69, cars.get(i).getY() + 2, cars.get(i).getY() + 69)) {
						cars.get(i).setSpeed(0);
						if (cars.get(i).getType() == 0) {
							cars.get(i).setImage(getImage(this.getClass().getResource("img/totaled_car.png")));
						} else if (cars.get(i).getType() == 1) {
							cars.get(i).setImage(getImage(this.getClass().getResource("img/totaled_truck.png")));
						}
						crash.play();
						coolDown.start();
					}

					// Check collision from eachother
					for (int t = 0; t < cars.size(); t++) {
						if (coll.any(cars.get(i).getX() + 6, cars.get(i).getX() + 36, cars.get(t).getX() + 6,
								cars.get(t).getX() + 36, cars.get(i).getY() + 2, cars.get(i).getY() + 69,
								cars.get(t).getY() + 2, cars.get(t).getY() + 69)) {
							cars.get(i).setSpeed(cars.get(t).getSpeed());
							break;
						}
					}
				}

				// Handle warnings
				for (int i = 0; i < warnings.size(); i++) {
					if (warnings.get(i).getTimer().isRunning() == false) {
						warnings.get(i).getTimer().start();
						warning.play();
					} else if (warnings.get(i).getTimer().isRunning() == true) {
						if (warnings.get(i).getTimer().getElapsedTimeSecs() >= 2) {
							spiketraps.add(new SpikeTrap(warnings.get(i).getX(), warnings.get(i).getY() - 40,
									getImage(this.getClass().getResource("img/spikeTrap.png"))));
							warnings.remove(i);
						}
					}
				}

				// Handle spike traps
				for (int i = 0; i < spiketraps.size(); i++) {
					spiketraps.get(i).setY(spiketraps.get(i).getY() + characterSpeed);
					if (spiketraps.get(i).getY() > appletsize_y + 80) {
						spiketraps.remove(i);
						break;
					}
					// Check collision from car
					if (coll.any((int) (x) + 6, (int) (x) + 36, spiketraps.get(i).getX(), spiketraps.get(i).getX() + 32,
							(int) (y) + 2, (int) (y) + 69, spiketraps.get(i).getY(), spiketraps.get(i).getY() + 5)) {
						spiketraps.remove(i);
						gasAmount = gasAmount - 100;
						pop.play();
					}
				}

				// Handle gas cans
				for (int i = 0; i < gascans.size(); i++) {
					gascans.get(i).setY(gascans.get(i).getY() + characterSpeed);
					if (gascans.get(i).getY() > appletsize_y + 80) {
						gascans.remove(i);
						break;
					}
					// Check collision from car
					if (coll.any((int) (x) + 6, (int) (x) + 36, gascans.get(i).getX() + 6, gascans.get(i).getX() + 36,
							(int) (y) + 2, (int) (y) + 69, gascans.get(i).getY() + 2, gascans.get(i).getY() + 69)) {
						gascans.remove(i);
						gasAmount = gasAmount + 100;
						if (gasAmount > 300) {
							gasAmount = 300;
						}
						fill.play();
					}
				}

				// Scroll background
				if (scrollBackground1 >= 387 - characterSpeed) {
					scrollBackground1 = scrollBackground2 - 387;
					scrollBackground1 = scrollBackground1 + characterSpeed;
				} else {
					scrollBackground1 = scrollBackground1 + characterSpeed;
				}

				if (scrollBackground2 >= 387 - characterSpeed) {
					scrollBackground2 = scrollBackground1 - 387;
					scrollBackground2 = scrollBackground2 + characterSpeed;
				} else {
					scrollBackground2 = scrollBackground2 + characterSpeed;
				}

				// Add 1 to score
				if (coolDown.isRunning() == false) {
					score++;
					scoreMessage = "Score:" + score;
				} else if (coolDown.isRunning() == true) {
					if (coolDown.getElapsedTimeSecs() > 5) {
						coolDown.stop();
					} else {
						scoreMessage = "No score for " + (5 - coolDown.getElapsedTimeSecs()) + " seconds";
					}
				}

				// Check if dead
				if (gasAmount <= 0) {
					// updateScores();
					game = 4;
					neverwork.play();
				}
				gasAmount = gasAmount - 0.1;
				if (turbo == true && characterSpeed == 8) {
					gasAmount = gasAmount - 0.1;
				}
			}

			// Losing screen
			if (game == 4) {
				repaint();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ex) {
				}
				game = 1;
				init();
			}
		}
	}

	public void update(Graphics g) {
		// Create buffer if not there
		if (dbImage == null) {
			dbImage = createImage(this.getSize().width, this.getSize().height);
			dbg = dbImage.getGraphics();
		}

		// Set color to the bacground color
		dbg.setColor(getBackground());
		dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);

		// Set canvas color and draw buffer
		dbg.setColor(getForeground());
		paint(dbg);

		// Draw buffer image
		g.drawImage(dbImage, 0, 0, this);
	}

	// Draw everything on screen
	public void paint(Graphics g) {
		if (game == -1) {
			g.setColor(Color.blue);
			g.fillRect(0, 0, 482, 387);
			g.setColor(Color.white);
			g.setFont(font);
			g.drawString("Loading", 160, 190);
		} else if (game == 0) {
			g.drawImage(intro, 0, 0, this);
		} else if (game == 3 || game == 1) {
			g.drawImage(background1, 0, scrollBackground1, this);
			g.drawImage(background2, 0, scrollBackground2, this);
			for (int i = 0; i < cars.size(); i++) {
				g.drawImage(cars.get(i).getImage(), cars.get(i).getX(), cars.get(i).getY(), this);
			}
			for (int i = 0; i < gascans.size(); i++) {
				g.drawImage(gascans.get(i).getImage(), gascans.get(i).getX(), gascans.get(i).getY(), this);
			}
			for (int i = 0; i < warnings.size(); i++) {
				g.drawImage(warnings.get(i).getImage(), warnings.get(i).getX(), warnings.get(i).getY(), this);
			}
			for (int i = 0; i < spiketraps.size(); i++) {
				g.drawImage(spiketraps.get(i).getImage(), spiketraps.get(i).getX(), spiketraps.get(i).getY(), this);
			}

			if (xspeed < 0) {
				g.drawImage(character[1], (int) (x), (int) (y), this);
			} else if (xspeed > 0) {
				g.drawImage(character[2], (int) (x), (int) (y), this);
			} else {
				g.drawImage(character[0], (int) (x), (int) (y), this);
			}

			if (characterSpeed <= 0) {
				g.drawImage(exhaust[0], (int) (x) + 4, (int) (y) + 69, this);
			} else if (characterSpeed > 0 && characterSpeed < 3) {
				g.drawImage(exhaust[1], (int) (x) + 4, (int) (y) + 69, this);
			} else if (characterSpeed > 2) {
				g.drawImage(exhaust[2], (int) (x) + 4, (int) (y) + 69, this);
			}

			if (startSpeech == true) {
				g.drawImage(start, (int) (x) - 100, (int) (y) - 80, this);
			}

			g.setColor(Color.white);
			g.setFont(font);
			g.drawString(scoreMessage, 20, 20);
			g.drawImage(gasCanIcon, 40, 340, this);
			g.setColor(Color.red);
			g.fillRoundRect(90, 350, (int) (gasAmount), 20, 15, 15);
			g.setColor(Color.black);
			g.drawRoundRect(90, 350, 300, 20, 15, 15);
		} else if (game == 4) {
			g.drawImage(lose, 0, 0, this);
			g.setColor(Color.white);
			g.setFont(font);
			g.drawString("Final Score: " + score, 160, 190);
		}

		if (game == 1) {
			g.drawImage(menu, 0, 0, this);
			g.drawImage(button_start, 150, 160, this);
			g.drawImage(button_help, 150, 240, this);

			if (helpOn == true) {
				g.drawImage(help, 0, 0, this);
			}
		}
	}
}