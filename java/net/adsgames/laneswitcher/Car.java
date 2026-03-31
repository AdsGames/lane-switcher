package net.adsgames.laneswitcher;

import java.awt.*;

public class Car {
	private int x;
	private int y;
	private int speed;
	private Image image;
	private int type;

	// Create car
	public Car(int x, int y, int speed, Image image, int type) {
		setX(x);
		setY(y);
		setImage(image);
		setSpeed(speed);
		setType(type);
	}

	// Get car speed
	public int getSpeed() {
		return speed;
	}

	// Get car type
	public int getType() {
		return type;
	}

	// Set car type
	private void setType(int carType) {
		type = carType;
	}

	// Set car speed
	public void setSpeed(int newSpeed) {
		speed = newSpeed;
	}

	// Get image
	public Image getImage() {
		return image;
	}

	// Get x
	public int getX() {
		return x;
	}

	// Get y
	public int getY() {
		return y;
	}

	// Set x
	public void setX(int x) {
		this.x = x;
	}

	// Set y
	public void setY(int y) {
		this.y = y;
	}

	// Set image
	public void setImage(Image image) {
		this.image = image;
	}
}