package com.getflourish.stt;

/**
 * Example 10-5: Object-oriented timer
 * http://www.learningprocessing.com
 * @author Daniel Shiffman
 */

public class Timer {

	int savedTime; // When Timer started
	int totalTime; // How long Timer should last

	public Timer(int tempTotalTime) {
		totalTime = tempTotalTime;
	}

	// Starting the timer
	public void start() {
		// When the timer starts it stores the current time in milliseconds.
		savedTime = (int) System.currentTimeMillis();
	}

	// The function isFinished() returns true if 5,000 ms have passed.
	// The work of the timer is farmed out to this method.
	public boolean isFinished() {
		// Check how much time has passed
		int passedTime = (int) (System.currentTimeMillis() - savedTime);
		if (passedTime > totalTime) {
			return true;
		} else {
			return false;
		}
	}
}