package net.adsgames.laneswitcher;

import java.io.*;
import javazoom.jl.player.Player;

public class MP3Loader extends Thread {
	// MP3 Loader attributes
	private String filename;
	private Player player;
	private boolean loop;

	// Constructor
	public MP3Loader(String filename, boolean loop) {
		this.filename = filename;
		this.loop = loop;
		start();
	}

	// Close audio stream
	public void close() {
		loop = false;
		if (player != null)
			player.close();
	}

	// Play music
	public void run() {
		try {
			while (loop) {
				InputStream bis = getClass().getResourceAsStream(filename);
				player = new Player(bis);
				player.play();
			}
		} 
		catch (Exception e) {
			System.out.println("Problem playing file " + filename);
			System.out.println(e);
		}
	}
}