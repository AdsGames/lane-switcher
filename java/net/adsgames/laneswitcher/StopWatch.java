package net.adsgames.laneswitcher;

public class StopWatch {
	// Timing variables
	private long startTime = 0;
	private long stopTime = 0;
	private boolean running = false;

	// Start timer
	public void start() {
		this.startTime = System.currentTimeMillis();
		this.running = true;
	}

	// Stop timer
	public void stop() {
		this.stopTime = System.currentTimeMillis();
		this.running = false;
	}

	// Elapsed time in milliseconds
	public long getElapsedTime() {
		long elapsed;
		if (running) {
			elapsed = (System.currentTimeMillis() - startTime);
		} else {
			elapsed = (stopTime - startTime);
		}
		return elapsed;
	}

	// Elapsed time in seconds
	public long getElapsedTimeSecs() {
		long elapsed;
		if (running) {
			elapsed = ((System.currentTimeMillis() - startTime) / 1000);
		} else {
			elapsed = ((stopTime - startTime) / 1000);
		}
		return elapsed;
	}

	// Check if running
	public boolean isRunning() {
		return running;
	}

}