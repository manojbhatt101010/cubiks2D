-*/import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.Color;

class Solver {
	JButton[][] face; 
	JLabel message, lastRandomScramble;
	char[][] c = new char[6][9]; 
	Color[] colors;
	char[] colorsChar = {'a', 'b', 'c', 'd', 'e', 'f', 'g'}; 
	Map<char[], char[]> leftLayer = new HashMap<>(); 
	Map<char[], char[]> rightLayer = new HashMap<>();  
	char[] front, back, left, right, up, down;  
	Map<Integer, char[]> above = new HashMap<>();  
	Map<Integer, char[]> below = new HashMap<>(); 
	String[] f2lAlgorithms = new String[41];
	String[] ollAlgorithms = new String[57];
	String[] pllAlgorithms = new String[21];
	List<String> solution = new ArrayList<>(); 
	boolean loaded = true;

	Solver(JButton[][] face, JLabel lastRandomScramble, JLabel message, Color[] colors) {
		this.face = face;
		this.message = message;
		this.lastRandomScramble = lastRandomScramble;
		this.colors = colors;
		initialize();
	}

	void initialize() {
		up = c[0];
		down = c[1];
		front = c[2];
		back = c[3];
		left = c[4];
		right = c[5];

		rightLayer.put(front, right);
		rightLayer.put(right, back);
		rightLayer.put(back, left);
		rightLayer.put(left, front);

		leftLayer.put(right, front);
		leftLayer.put(back, right);
		leftLayer.put(left, back);
		leftLayer.put(front, left);

		above.put(1, front);
		above.put(5, right);
		above.put(7, back);
		above.put(3, left);

		below.put(1, back);
		below.put(5, right);
		below.put(7, front);
		below.put(3, left);

		solved = new boolean[8];
		
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(Solver.class.getResourceAsStream("F2L.txt")));
			for(int i = 0; i < 41; i++) 
				f2lAlgorithms[i] = read.readLine();

			read = new BufferedReader(new InputStreamReader(Solver.class.getResourceAsStream("OLL.txt")));
			for(int i = 0; i < 57; i++) 
				ollAlgorithms[i] = read.readLine();

			read = new BufferedReader(new InputStreamReader(Solver.class.getResourceAsStream("PLL.txt")));
			for(int i = 0; i < 21; i++) 
				pllAlgorithms[i] = read.readLine();
		} catch(Exception e) {
			message.setForeground(Color.red);
			message.setText("Couldn't load files. Solution will not work.");
			loaded = false;
		}
	}

	void solve() {
		if(!loaded || !checkForCorrectInput())
			return;

		char[][] copy = new char[6][9];
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++)
				copy[i][j] = c[i][j];
		}

		solution.clear();
		try {
			solution.add("<html><b><u><font color = black>Solved:)<br></font></u><br><font color = blue>Cross:</font>");
			solveCross();
			solution.add("<br><font color = blue>F2L:</font>");
			F2L();
			solution.add("<br><font color = blue>OLL:</font>");
			OLL();
			solution.add("<br><font color = blue>PLL:</font>");
			PLL();
			solution.add("</b></html>");
		} catch(Exception e) {
			message.setForeground(Color.red);
			restore(copy);
			message.setText("Incorrect color combination.");
			update();
			return;
		}

		if(!isSolved()) {
			message.setForeground(Color.red);
			restore(copy);
			message.setText("Incorrect color combination.");
			update();
			return;
		}

		restore(copy);
		update();
		message.setForeground(new Color(0, 77, 26));
		clean(solution);
		StringBuilder sol = new StringBuilder();
		for(int i = 0; i < solution.size(); i++)
			sol.append(solution.get(i) + " ");

		message.setText(sol.toString());
	}

	boolean isSolved() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				if(c[i][j] != c[i][4])
					return false;
			}
		}
		return true;
	}

	boolean checkForCorrectInput() {
		message.setForeground(Color.red);
		HashMap<Character, Integer> count = new HashMap<>();
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				if(c[i][j] == 'g') {
					message.setText("Fill whole cube.");
					return false;
				}
				
				if(count.containsKey(c[i][j])) {
					if(count.get(c[i][j]) == 9) {
						message.setText("Cube must have 9 faces of each color.");
						return false;
					}
					count.put(c[i][j], count.get(c[i][j]) + 1);
				}
				else
					count.put(c[i][j], 1);
			}
		}

		Set<Character> centers = new HashSet<>();
		for(int i = 0; i < 6; i++) {
			if(centers.contains(c[i][4])) {
				message.setText("Cube must have distinct centers.");
				return false;
			}
			centers.add(c[i][4]);
		}
		return true;
	}

	void test() {
		for(int i = 0; i < 100; i++) {
			generateRandomScramble();
			solve();
		}
	}

	int[] getPosition() {
		for(int i = 0; i < 6; i++) {
			for(int j = 1; j < 8; j += 2) {
				if(c[i][j] == c[1][4]) {
					if(i == 1 && solved[j])
						continue;

					return new int[] {i, j};
				}
			}
		}
		return null;
	}

	void markSolved(char[] temp) {
		for(int i = 1; i < 8; i += 2) {
			if(temp == above.get(i)) {
				solved[i] = true;
				break;
			}
		}
	}

	void middleLeft(char[] temp, int i) {
		if(i == 0) {
			rotateClockWise(temp);
			while(temp[4] != temp[7]) {
				d();
				solution.add("D");

				temp = rightLayer.get(temp);
			}
		}

		else {
			int rotatationCount = 0, targetColor = temp[5];
			while(temp[4] != targetColor) {
				temp = rightLayer.get(temp);
				rotatationCount++;
			}
			
			if(rotatationCount == 0) 
				rotateClockWise(temp);

			else if(rotatationCount == 1) {
				dPrime();
				solution.add("D'");

				rotateClockWise(leftLayer.get(temp));
				d();
				solution.add("D");
			}

			else if(rotatationCount == 2) {
				rotateTwice(leftLayer.get(temp));
				rotateCounterClockWise(temp);
				rotateTwice(leftLayer.get(temp));
			}

			else {
				d();
				solution.add("D");

				rotateClockWise(rightLayer.get(temp));
				dPrime();
				solution.add("D'");
			}
		}
		markSolved(temp);
	}

	void middleRight(char[] temp, int i) {
		if(i == 0) {
			rotateCounterClockWise(temp);
			while(temp[4] != temp[7]) {
				d();
				solution.add("D");

				temp = rightLayer.get(temp);
			}
		}

		else {
			int rotatationCount = 0, targetColor = temp[3];
			while(temp[4] != targetColor) {
				temp = leftLayer.get(temp);
				rotatationCount++;
			}

			if(rotatationCount == 0) 
				rotateCounterClockWise(temp);

			else if(rotatationCount == 1) {
				d();
				solution.add("D");

				rotateCounterClockWise(rightLayer.get(temp));
				dPrime();
				solution.add("D'");
			}
			
			else if(rotatationCount == 2) {
				rotateTwice(rightLayer.get(temp));
				rotateClockWise(temp);
				rotateTwice(rightLayer.get(temp));
			}

			else {
				dPrime();
				solution.add("D'");

				rotateCounterClockWise(leftLayer.get(temp));
				d();
				solution.add("D");
			}
		}
		markSolved(temp);
	}

	boolean[] solved = new boolean[8]; 
	void solveCross() throws Exception { 
		solved = new boolean[8];
		for(int i = 0; i < 4; i++) {
			int[] p = getPosition();
			if(p == null)
				return;

			if(p[0] == 0) {
				char[] temp = below.get(p[1]);
				while(temp[1] != temp[4]) {
					u();
					solution.add("U");

					temp = leftLayer.get(temp);
				}
				rotateTwice(temp);
				markSolved(temp);
			}

			else if(p[0] == 1) {
				char[] temp = above.get(p[1]);
				if(temp[4] == temp[7])
					markSolved(temp);
				else {
					rotateClockWise(temp);
					middleRight(temp, i);
				}
			}

			else if(p[1] == 3) 
				middleLeft(leftLayer.get(c[p[0]]), i);

			else if(p[1] == 5) 
				middleRight(rightLayer.get(c[p[0]]), i);

			else if(p[1] == 1) {
				char color = 'g';
				char[] temp = c[p[0]];
				for(int j = 1; j < 8; j += 2) {
					if(below.get(j) == temp) {
						color = up[j];
						break;
					}
				}

				while(leftLayer.get(temp)[4] != color) {
					u();
					solution.add("U");

					temp = leftLayer.get(temp);
				}

				rotateCounterClockWise(temp);
				rotateClockWise(leftLayer.get(temp));
				rotateClockWise(temp);
				markSolved(leftLayer.get(temp));
			}

			else {
				rotateClockWise(c[p[0]]);
				middleLeft(leftLayer.get(c[p[0]]), i);
			}
		}
	}

	int[] getCornerPosition(char a, char b) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j += 2) {
				if(j == 4) 
					continue;

				if(c[i][j] == down[4]) {
					char c = 'g', d = 'g';
					if(i == 0) {
						if(j == 0) {
							c = left[0];
							d = back[2];
						} else if(j == 2) {
							c = right[2];
							d = back[0];
						} else if(j == 6) {
							c = front[0];
							d = left[2];
						} else if(j == 8) {
							c = front[2];
							d = right[0];
						}
					}
					else if(i == 1) {
						if(j == 0) {
							c = front[6];
							d = left[8];
						} else if(j == 2) {
							c = right[6];
							d = front[8];
						} else if(j == 6) {
							c = left[6];
							d = back[8];
						} else if(j == 8) {
							c = right[8];
							d = back[6];
						}
					}
					else if(i == 2) {
						if(j == 0) {
							c = up[6];
							d = left[2];
						} else if(j == 2) {
							c = up[8];
							d = right[0];
						} else if(j == 6) {
							c = left[8];
							d = down[0];
						} else if(j == 8) {
							c = right[6];
							d = down[2];
						}
					}
					else if(i == 3) {
						if(j == 0) {
							c = up[2];
							d = right[2];
						} else if(j == 2) {
							c = up[0];
							d = left[0];
						} else if(j == 6) {
							c = right[8];
							d = down[8];
						} else if(j == 8) {
							c = left[6];
							d = down[6];
						}
					}
					else if(i == 4) {
						if(j == 0) {
							c = up[0];
							d = back[2];
						} else if(j == 2) {
							c = up[6];
							d = front[0];
						} else if(j == 6) {
							c = down[6];
							d = back[8];
						} else if(j == 8) {
							c = front[6];
							d = down[0];
						}
					}
					else if(i == 5) {
						if(j == 0) {
							c = up[8];
							d = front[2];
						} else if(j == 2) {
							c = up[2];
							d = back[0];
						} else if(j == 6) {
							c = front[8];
							d = down[2];
						} else if(j == 8) {
							c = back[6];
							d = down[8];
						}
					}
					if((a == c && b == d) || (a == d && b == c))
						return new int[] {i, j};
				}
			}
		}
		return null;
	}

	int[] getEdgePosition(char a, char b) {
		for(int i = 0; i < 6; i++) {
			if(i == 1) 
				continue;
			for(int j = 1; j < 8; j += 2) {
				if(i != 0 && j == 7) 
					continue;
				
				if(c[i][j] == a) {
					if(i == 0) {
						if(below.get(j)[1] == b)
							return new int[] {i, j};
					} else if(j == 1) {
						if((c[i] == front && up[7] == b) || (c[i] == left && up[3] == b) || (c[i] == back && up[1] == b) || (c[i] == right && up[5] == b))
							return new int[] {i, j};
					} else if(j == 3) {
						if(leftLayer.get(c[i])[5] == b)
							return new int[] {i, j};
					} else if(j == 5) {
						if(rightLayer.get(c[i])[3] == b)
							return new int[] {i, j};
					}
				}
			}
		}
		return null;
	}

	boolean isPairSolved() {
		if(front[4] == front[5] && front[4] == front[8] && right[3] == right[4] && right[6] == right[4] && down[2] == down[4])
			return true;
		return false;
	}

	void restore(char[][] temp) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++)
				c[i][j] = temp[i][j];
		}
	}

	void F2L() throws Exception {
		if(isSolved())
			return;

		for(int k = 0; k < 4; k++) {
			if(!isPairSolved()) {
				int[] p = getCornerPosition(front[4], right[4]);
				if(p == null) 
					return;

				int i = p[0], j = p[1];

				if((i == 0 && j == 0) || (i == 4 && j == 0) || (i == 3 && j == 2)) {
					u2();
					solution.add("U2");
				} else if((i == 0 && j == 2) || (i == 3 && j == 0) || (i == 5 && j == 2)) {
					u();
					solution.add("U");
				} else if((i == 0 && j == 6) || (i == 4 && j == 2) || (i == 2 && j == 0)) {
					uPrime();
					solution.add("U'");
				} else if((i == 1 && j == 6) || (i == 4 && j == 6) || (i == 3 && j == 8)) {
					l(); u2(); lPrime();
					solution.add("L");
					solution.add("U2");
					solution.add("L'");
				} else if((i == 1 && j == 8) || (i == 5 && j == 8) || (i == 3 && j == 6)) {
					rPrime(); u2(); r(); uPrime();
					solution.add("R'");
					solution.add("U2");
					solution.add("R");
					solution.add("U'");
				} else if((i == 1 && j == 0) || (i == 4 && j == 8) || (i == 2 && j == 6)) {
					lPrime(); uPrime(); l();
					solution.add("L'");
					solution.add("U'");
					solution.add("L");
				}

				boolean flag = ((i == 2 && j == 8) || (i == 5 && j == 6) || (i == 1 && j == 2));

				int[] q = getEdgePosition(front[4], right[4]);
				i = q[0]; j = q[1];

				flag = flag && !((i == 2 && j == 5) || (i == 5 && j == 3));

				if((i == 2 && j == 3) || (i == 4 && j == 5)) {
					lPrime(); uPrime(); l(); u();
					solution.add("L'");
					solution.add("U'");
					solution.add("L");
					solution.add("U");
				} else if((i == 4 && j == 3) || (i == 3 && j == 5)) {
					l(); uPrime(); lPrime(); u();
					solution.add("L");
					solution.add("U'");
					solution.add("L'");
					solution.add("U");
				} else if((i == 5 && j == 5) || (i == 3 && j == 3)) {
					rPrime(); u2(); r();
					solution.add("R'");
					solution.add("U2");
					solution.add("R");
				}

				if(flag) {
					q = getEdgePosition(front[4], right[4]);
					i = q[0]; j = q[1];
					
					if(i == 0) {
						char[] temp = below.get(j);
						while(temp[1] != temp[4]) {
							temp = leftLayer.get(temp);
							u();
							solution.add("U");
						}
					}
					else {
						char[] temp = c[i];
						while(temp[1] != temp[4]) {
							temp = leftLayer.get(temp);
							u();
							solution.add("U");
						}
					}
				}

				char[][] temp = new char[6][9];
				for(i = 0; i < 6; i++) {
					for(j = 0; j < 9; j++)
						temp[i][j] = c[i][j];
				}

				for(i = 0; i < 41; i++) {
					perform(f2lAlgorithms[i]);
					if(isPairSolved()) {
						for(String s: f2lAlgorithms[i].split(" "))
							solution.add(s);
						break;
					}
					restore(temp);
				}
			}
			if(k != 3) {
				y();
				solution.add("y");
			}
		}
	}

	boolean isOriented() {
		for(int i = 0; i < 9; i++) {
			if(up[i] != up[4])
				return false;
		}
		return true;
	}

	void OLL() throws Exception {
		if(isOriented())
			return;

		for(int i = 0; i < 4; i++) {
			char[][] temp = new char[6][9];
			for(int k = 0; k < 6; k++) {
				for(int l = 0; l < 9; l++)
					temp[k][l] = c[k][l];
			}

			for(int j = 0; j < 57; j++) {
				perform(ollAlgorithms[j]);
				if(isOriented()) {
					for(String a: ollAlgorithms[j].split(" "))
						solution.add(a);
					return;
				}	
				restore(temp);
			}
			u();
			solution.add("U");
		}
	}

	boolean isPermuted() {
		for(int i = 2; i < 6; i++) {
			if(c[i][0] != c[i][1] || c[i][1] != c[i][2])
				return false;
		}
		return true;
	}

	void PLL() throws Exception {
		if(!isPermuted()) {
			outer:
			for(int i = 0; i < 4; i++) {
				char[][] temp = new char[6][9];
				for(int k = 0; k < 6; k++) {
					for(int l = 0; l < 9; l++)
						temp[k][l] = c[k][l];
				}

				for(int j = 0; j < 21; j++) {
					perform(pllAlgorithms[j]);
					if(isPermuted()) {
						for(String a: pllAlgorithms[j].split(" "))
							solution.add(a);
						break outer;
					}
					restore(temp);
				}
				u();
				solution.add("U");
			}
		}
		for(int i = 0; i < 4 && front[1] != front[4]; i++){
			u();
			solution.add("U");
		}
	}

	void perform(String s) {
		String[] input = s.split(" ");
		for(int i = 0; i < input.length; i++) {
			if(input[i].equals("U")) {
				u();
			} else if(input[i].equals("D")) {
				d();
			} else if(input[i].equals("F")) {
				f();
			} else if(input[i].equals("B")) {
				b();
			} else if(input[i].equals("L")) {
				l();
			} else if(input[i].equals("R")) {
				r();
			} else if(input[i].equals("M")) {
				m();
			} else if(input[i].equals("E")) {
				e();
			} else if(input[i].equals("S")) {
				s();
			}

			else if(input[i].equals("U'")) {
				uPrime();
			} else if(input[i].equals("D'")) {
				dPrime();
			} else if(input[i].equals("F'")) {
				fPrime();
			} else if(input[i].equals("B'")) {
				bPrime();
			} else if(input[i].equals("L'")) {
				lPrime();
			} else if(input[i].equals("R'")) {
				rPrime();
			} else if(input[i].equals("M'")) {
				mPrime();
			} else if(input[i].equals("E'")) {
				ePrime();
			} else if(input[i].equals("S'")) {
				sPrime();
			}

			else if(input[i].equals("U2")) {
				u2();
			} else if(input[i].equals("D2")) {
				d2();
			} else if(input[i].equals("F2")) {
				f2();
			} else if(input[i].equals("B2")) {
				b2();
			} else if(input[i].equals("L2")) {
				l2();
			} else if(input[i].equals("R2")) {
				r2();
			} else if(input[i].equals("M2")) {
				m2();
			} else if(input[i].equals("E2")) {
				e2();
			} else if(input[i].equals("S2")) {
				s2();
			}

			else if(input[i].equals("u")) {
				wideU();
			} else if(input[i].equals("u'")) {
				wideUPrime();
			} else if(input[i].equals("d")) {
				wideD();
			} else if(input[i].equals("d'")) {
				wideDPrime();
			} else if(input[i].equals("f")) {
				wideF();
			} else if(input[i].equals("f'")) {
				wideFPrime();
			} else if(input[i].equals("b")) {
				wideB();
			} else if(input[i].equals("b'")) {
				wideBPrime();
			} else if(input[i].equals("l")) {
				wideL();
			} else if(input[i].equals("l'")) {
				wideLPrime();
			} else if(input[i].equals("r")) {
			 	wideR();
			} else if(input[i].equals("r'")) {
				wideRPrime();
			} else if(input[i].equals("u2")) {
				wideU2();
			} else if(input[i].equals("d2")) {
				wideD2();
			} else if(input[i].equals("f2")) {
				wideF2();
			} else if(input[i].equals("b2")) {
				wideB2();
			} else if(input[i].equals("l2")) {
				wideL2();
			} else if(input[i].equals("r2")) {
				wideR2();
			}

			else if(input[i].equals("x")) {
				x();
			} else if(input[i].equals("x'")) {
				xPrime();
			} else if(input[i].equals("x2")) {
				x2();
			} else if(input[i].equals("y")) {
				y();
			} else if(input[i].equals("y'")) {
				yPrime();
			} else if(input[i].equals("y2")) {
				y2();
			} else if(input[i].equals("z")) {
				z();
			} else if(input[i].equals("z'")) {
				zPrime();
			} else if(input[i].equals("z2")) {
				z2();
			}
		}
	}

	void fill() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				c[i][j] = colorsChar[i];
			}
		}
		update();
	}

	void update() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				for(int k = 0; k < 7; k++) {
					if(c[i][j] == colorsChar[k]) {
						face[i][j].setBackground(colors[k]);
						break;
					}
				}
			}
		}
	}

	void update2() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				Color color = face[i][j].getBackground();
				for(int k = 0; k < 7; k++) {
					if(color == colors[k]) {
						c[i][j] = colorsChar[k];
						break;
					}
				}
			}
		}
	}

	Random rand = new Random();
	void generateRandomScramble() {
		fill();
		List<String> scramble = new ArrayList<>(); 
		String[] moves = {"U", "U'", "U2", "D", "D'", "D2", "F", "F'", "F2", "B", "B'", "B2", "L", "L'", "L2", "R", "R'", "R2"};

		for(int i = 0; i < 23; i++) 
			scramble.add(moves[rand.nextInt(18)]);
		
		clean(scramble);
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < scramble.size(); i++)
			s.append(scramble.get(i) + " ");

		perform(s.toString());
		lastRandomScramble.setText("Last Random Scramble: " + s);
		update2();
	}

	void clean(List<String> list) {
		for(int i = 0; i < list.size() - 1;) {
			String a = list.get(i), b = list.get(i + 1);
			if(a.charAt(0) == b.charAt(0)) {
				if(a.length() == 1 && b.length() == 1) {
					list.remove(i);
					list.set(i, a.charAt(0) + "2");
					i = i == 0 ? 0 : i - 1;
				}
				else if(a.length() == 1 && b.length() == 2) {
					if(b.charAt(1) == '\'') {
						list.remove(i);
						list.remove(i);
						i = i == 0 ? 0 : i - 1;	
					}
					else if(b.charAt(1) == '2') {
						list.remove(i);
						list.set(i, a.charAt(0) + "'");
						i = i == 0 ? 0 : i - 1;	
					}
				}
				else if(a.length() == 2 && b.length() == 1) {
					if(a.charAt(1) == '\'') {
						list.remove(i);
						list.remove(i);
						i = i == 0 ? 0 : i - 1;	
					}
					else if(a.charAt(1) == '2') {
						list.remove(i);
						list.set(i, a.charAt(0) + "'");
						i = i == 0 ? 0 : i - 1;	
					}
				}
				else if(a.length() == 2 && b.length() == 2) {
					if(a.charAt(1) == '\'' && b.charAt(1) == '\'') {
						list.remove(i);
						list.set(i, a.charAt(0) + "2");
						i = i == 0 ? 0 : i - 1;	
					}
					else if(a.charAt(1) == '2' && b.charAt(1) == '2') {
						list.remove(i);
						list.remove(i);
						i = i == 0 ? 0 : i - 1;	
					}
					else {
						list.remove(i);
						list.set(i, Character.toString(a.charAt(0)));
						i = i == 0 ? 0 : i - 1;	
					}
				}
				else 
					i++;
			}
			else
				i++;
		}
	}

	void clear() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				face[i][j].setBackground(Color.lightGray);
				c[i][j] = 'g';
			}
		}
	}

	void swap(char[] arr, int a, int b, int c, int d) {
		char temp = arr[a];
		arr[a] = arr[d];
		arr[d] = arr[c];
		arr[c] = arr[b];
		arr[b] = temp;
	}

	void swap(char[] arr, char[] brr, char[] crr, char[] drr, int a, int b, int c, int d) {
		char temp = arr[a];
		arr[a] = drr[d];
		drr[d] = crr[c];
		crr[c] = brr[b];
		brr[b] = temp;
	}

	void u() {
		swap(up, 0, 2, 8, 6);
		swap(up, 1, 5, 7, 3);
		swap(back, right, front, left, 2, 2, 2, 2);
		swap(back, right, front, left, 1, 1, 1, 1);
		swap(back, right, front, left, 0, 0, 0, 0);
		update();
	}

	void uPrime() {
		swap(up, 0, 6, 8, 2);
		swap(up, 1, 3, 7, 5);
		swap(back, left, front, right, 2, 2, 2, 2);
		swap(back, left, front, right, 1, 1, 1, 1);
		swap(back, left, front, right, 0, 0, 0, 0);
		update();
	}

	void u2() {
		u(); u();
	}

	void wideU() {
		ePrime(); u();
	}

	void wideUPrime() {
		e(); uPrime();
	}

	void wideU2() {
		u2(); e2();
	}

	void d() {
		swap(down, 0, 2, 8, 6);
		swap(down, 1, 5, 7, 3);
		swap(front, right, back, left, 6, 6, 6, 6);
		swap(front, right, back, left, 7, 7, 7, 7);
		swap(front, right, back, left, 8, 8, 8, 8);
		update();
	}

	void dPrime() {
		swap(down, 0, 6, 8, 2);
		swap(down, 1, 3, 7, 5);
		swap(front, left, back, right, 6, 6, 6, 6);
		swap(front, left, back, right, 7, 7, 7, 7);
		swap(front, left, back, right, 8, 8, 8, 8);
		update();
	}

	void d2() {
		d(); d();
	}

	void wideD() {  
		d(); e();
	}

	void wideDPrime() {  
		dPrime(); ePrime();
	}

	void wideD2() { 
		d2(); e2();
	}

	void f() {
		swap(front, 0, 2, 8, 6);
		swap(front, 1, 5, 7, 3);
		swap(up, right, down, left, 6, 0, 2, 8);
		swap(up, right, down, left, 7, 3, 1, 5);
		swap(up, right, down, left, 8, 6, 0, 2);
		update();
	}

	void fPrime() {
		swap(front, 0, 6, 8, 2);
		swap(front, 1, 3, 7, 5);
		swap(up, left, down, right, 6, 8, 2, 0);
		swap(up, left, down, right, 7, 5, 1, 3);
		swap(up, left, down, right, 8, 2, 0, 6);
		update();
	}

	void f2() {
		f(); f();
	}

	void wideF() { 
		f(); s();
	}

	void wideFPrime() { 
		fPrime(); sPrime();
	}

	void wideF2() { 
		f2(); s2();
	}

	void b() {
		swap(back, 0, 2, 8, 6);
		swap(back, 1, 5, 7, 3);
		swap(up, left, down, right, 2, 0, 6, 8);
		swap(up, left, down, right, 1, 3, 7, 5);
		swap(up, left, down, right, 0, 6, 8, 2);
		update();
	}

	void bPrime() {
		swap(back, 0, 6, 8, 2);
		swap(back, 1, 3, 7, 5);
		swap(up, right, down, left, 2, 8, 6, 0);
		swap(up, right, down, left, 1, 5, 7, 3);
		swap(up, right, down, left, 0, 2, 8, 6);
		update();
	}

	void b2() {  
		b(); b();
	}

	void wideB() { 
		b(); sPrime();
	}

	void wideBPrime() {  
		bPrime(); s();
	}

	void wideB2() {
		b2(); s2();
	}

	void l() {
		swap(left, 0, 2, 8, 6);
		swap(left, 1, 5, 7, 3);
		swap(up, front, down, back, 0, 0, 0, 8);
		swap(up, front, down, back, 3, 3, 3, 5);
		swap(up, front, down, back, 6, 6, 6, 2);
		update();
	}

	void lPrime() {
		swap(left, 0, 6, 8, 2);
		swap(left, 1, 3, 7, 5);
		swap(up, back, down, front, 0, 8, 0, 0);
		swap(up, back, down, front, 3, 5, 3, 3);
		swap(up, back, down, front, 6, 2, 6, 6);
		update();
	}

	void l2() {
		l(); l();
	}

	void wideL() {  
		l(); m();
	}

	void wideLPrime() {  
		lPrime(); mPrime();
	}

	void wideL2() { 
		l2(); m2();
	}

	void r() {
		swap(right, 0, 2, 8, 6);
		swap(right, 1, 5, 7, 3);
		swap(up, back, down, front, 8, 0, 8, 8);
		swap(up, back, down, front, 5, 3, 5, 5);
		swap(up, back, down, front, 2, 6, 2, 2);
		update();
	}

	void rPrime() {
		swap(right, 0, 6, 8, 2);
		swap(right, 1, 3, 7, 5);
		swap(up, front, down, back, 8, 8, 8, 0);
		swap(up, front, down, back, 5, 5, 5, 3);
		swap(up, front, down, back, 2, 2, 2, 6);
		update();
	}

	void r2() {
		r(); r();
	}

	void wideR() {
		r(); mPrime();
	}

	void wideRPrime() {
		rPrime(); m();
	}

	void wideR2() {
		r2(); m2();
	}

	void m() {
		swap(up, front, down, back, 1, 1, 1, 7);
		swap(up, front, down, back, 4, 4, 4, 4);
		swap(up, front, down, back, 7, 7, 7, 1);
		update();
	}

	void mPrime() {
		swap(up, back, down, front, 1, 7, 1, 1);
		swap(up, back, down, front, 4, 4, 4, 4);
		swap(up, back, down, front, 7, 1, 7, 7);
		update();
	}

	void m2() {
		m(); m();
	}

	void e() {
		swap(front, right, back, left, 3, 3, 3, 3);
		swap(front, right, back, left, 4, 4, 4, 4);
		swap(front, right, back, left, 5, 5, 5, 5);
		update();
	}

	void ePrime() {
		swap(front, left, back, right, 3, 3, 3, 3);
		swap(front, left, back, right, 4, 4, 4, 4);
		swap(front, left, back, right, 5, 5, 5, 5);
		update();
	}

	void e2() {
		e(); e();
	}

	void s() {  
		swap(up, right, down, left, 3, 1, 5, 7);
		swap(up, right, down, left, 4, 4, 4, 4);
		swap(up, right, down, left, 5, 7, 3, 1);
		update();
	}

	void sPrime() {
		swap(up, left, down, right, 3, 7, 5, 1);
		swap(up, left, down, right, 4, 4, 4, 4);
		swap(up, left, down, right, 5, 1, 3, 7);
		update();
	}

	void s2() { 
		s(); s();
	}

	void rotateClockWise(char[] arr) {
		if(arr == front) {
			f();
			solution.add("F");
		} else if(arr == back) {
			b();
			solution.add("B");
		} else if(arr == up) {
			u();
			solution.add("U");
		} else if(arr == down) {
			d();
			solution.add("D");
		} else if(arr == left) {
			l();
			solution.add("L");
		} else {
			r();
			solution.add("R");
		}
	}

	void rotateCounterClockWise(char[] arr) {
		if(arr == front) {
			fPrime();
			solution.add("F'");
		} else if(arr == back) {
			bPrime();
			solution.add("B'");
		} else if(arr == up) {
			uPrime();
			solution.add("U'");
		} else if(arr == down) {
			dPrime();
			solution.add("D'");
		} else if(arr == left) {
			lPrime();
			solution.add("L'");
		} else {
			rPrime();
			solution.add("R'");
		}
	}

	void rotateTwice(char[] arr) {
		if(arr == front) {
			f2();
			solution.add("F2");
		} else if(arr == back) {
			b2();
			solution.add("B2");
		} else if(arr == up) {
			u2();
			solution.add("U2");
		} else if(arr == down) {
			d2();
			solution.add("D2");
		} else if(arr == left) {
			l2();
			solution.add("L2");
		} else {
			r2();
			solution.add("R2");
		}
	}

	void x() {
		wideR(); lPrime();
	}

	void xPrime() {
		wideRPrime(); l();
	}

	void x2() {
		wideR2(); l2();
	}

	void y() {
		wideU(); dPrime();
	}

	void yPrime() {
		wideUPrime(); d();
	}

	void y2() {
		wideU2(); d2();
	}

	void z() { 
		wideF(); bPrime();
	}

	void zPrime() {
		wideFPrime(); b();
	}

	void z2() { 
		wideF2(); b2();
	}	
}