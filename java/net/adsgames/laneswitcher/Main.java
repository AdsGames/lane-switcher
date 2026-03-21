package net.adsgames.laneswitcher;

import java.awt.Dimension;
import javax.swing.JFrame;
import java.applet.*;

public class Main {

	public static void main(String[] args) {

		// Create lane switcher applet
		Applet game = (Applet)(new Laneswitcher());

		// Create a window (JFrame) and make applet the content pane.
		JFrame window = new JFrame("Lane Switcher");
		
		// Set content
		window.setContentPane(game);
		
		// Set size
		window.setPreferredSize(new Dimension(482, 387));
		
		// Set exit operation
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Arrange the components.
		window.pack(); 
		
		// Make the window visible.
		window.setVisible(true); 
		
		
		// Init game
		game.init(); 
		
		// Begin game
		game.start();
	}

}
