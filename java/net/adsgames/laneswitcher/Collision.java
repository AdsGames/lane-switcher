package net.adsgames.laneswitcher;

public class Collision {
	boolean any(int xMin1, int xMax1, int xMin2, int xMax2, int yMin1, int yMax1, int yMin2, int yMax2) {
		if (xMin1 < xMax2 && yMin1 < yMax2 && xMin2 < xMax1 && yMin2 < yMax1) {
			return true;
		}
		return false;
	}

	boolean bottom(int yMin1, int yMax1, int yMin2, int yMax2) {
		if (yMin1 < yMax2 && yMax1 > yMax2) {
			return true;
		}
		return false;
	}

	boolean top(int yMin1, int yMax1, int yMin2, int yMax2) {
		if (yMin2 < yMax1 && yMin1 < yMin2) {
			return true;
		}
		return false;
	}

	boolean right(int xMin1, int xMax1, int xMin2, int xMax2) {
		if (xMin2 < xMax1 && xMin1 < xMin2) {
			return true;
		}
		return false;
	}

	boolean left(int xMin1, int xMax1, int xMin2, int xMax2) {
		if (xMin1 < xMax2 && xMax1 > xMax2) {
			return true;
		}
		return false;
	}
}