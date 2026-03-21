package net.adsgames.laneswitcher;

import java.awt.*;

public class WarningBubble {
	private int x;
	private int y;
	private Image image;
	private StopWatch timer = new StopWatch();

	// Create warning
	public WarningBubble(int x, int y, Image image) {
		setX(x);
		setY(y);
		setImage(image);
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
	private void setX(int x) {
		this.x = x;
	}

	// Set y
	private void setY(int y) {
		this.y = y;
	}

	// Set image
	private void setImage(Image image) {
		this.image = image;
	}

	public StopWatch getTimer() {
		return timer;
	}
}