import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	// initialize socket and input stream 
	private Socket socket; 
	private ServerSocket server; 
	private DataInputStream input;

	// position vars
	private double x, y, z;

	// other vars
	private boolean isActive;
	private int tabChange;

	public static void main(String[] args) {
		new Server();
	}

	// constructor with port 
	public Server() { 
		// starts server and waits for a connection 
		try {
			int port = 9997; /////////////////////////////////////////
			isActive = true;
			tabChange = 0;

			server = new ServerSocket(port); 
			System.out.println("Server started\nWaiting for a client ..."); 

			// accept client
			socket = server.accept(); 
			System.out.println("Client accepted"); 

			// takes input from the client socket 
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

			// create input and scroll threads
			InputThread it = new InputThread();
			it.start();

			ScrollThread st = new ScrollThread();
			st.start();


		} catch(IOException i) { 
			System.out.println(i); 
		} 
	}

	private void closeConnections() {
		try {
			socket.close();
			input.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private class ScrollThread extends Thread {

		private Robot robot;
		private int scrollDelay = 30;
		private final double DELAY_FACTOR = 50;
		private final double DELAY_LOWER_BOUND = 20;
		private final double DELAY_HIGHER_BOUND = 300;
		private final double TAB_CHANGE_FACTOR = 0.5;

		public ScrollThread() {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			while (isActive) {
				scrollDelay = (int)(DELAY_FACTOR / Math.abs(x));

				//				System.out.println(scrollDelay);

				System.out.println(y + " " + scrollDelay);
				if (scrollDelay > DELAY_LOWER_BOUND && scrollDelay < DELAY_HIGHER_BOUND) {
					// System.out.println("rather neutral");
					if (x < 0) {
						robot.mouseWheel(-1);
					} else {
						robot.mouseWheel(1);
					}

					try {
						Thread.sleep(scrollDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (y < -TAB_CHANGE_FACTOR) {
					System.out.println("TO THE LEFTTTTT");
					tabChange = -1;
				} else if (y > TAB_CHANGE_FACTOR) {
					System.out.println("TO THE RIGHTTTT");
					tabChange = 1;
				} else if (tabChange == -1) {
					robot.keyPress(KeyEvent.VK_CONTROL);
					robot.keyPress(KeyEvent.VK_SHIFT);
					robot.keyPress(KeyEvent.VK_TAB);
					robot.keyRelease(KeyEvent.VK_CONTROL);
					robot.keyRelease(KeyEvent.VK_SHIFT);
					robot.keyRelease(KeyEvent.VK_TAB);
					tabChange = 0;
				} else if (tabChange == 1) {
					robot.keyPress(KeyEvent.VK_CONTROL);
					robot.keyPress(KeyEvent.VK_TAB);
					robot.keyRelease(KeyEvent.VK_CONTROL);
					robot.keyRelease(KeyEvent.VK_TAB);
					tabChange = 0;
				}

			}

		}
	}

	private class InputThread extends Thread{
		public void run() {
			while (isActive) { 
				try { 
					String line = input.readUTF();

					char c = line.charAt(0);

					if (c == 'x') {
						x = Double.parseDouble(line.substring(2));
					} else if (c == 'y') {
						y = Double.parseDouble(line.substring(2));
					} else if (c == 'z') {
						z = Double.parseDouble(line.substring(2));
//					} else if (c == 'a') {
//						iz = Double.parseDouble(line.substring(2));
						//					} else if (c == 'b') {
						//						gy = Double.parseDouble(line.substring(2));
					} else if (c == 'g') {
						//						gz = Double.parseDouble(line.substring(2));
					}
				} catch(IOException e) { 
					System.out.println(e); 
					break;
				}
			}
			System.out.println("end of reading");
			closeConnections();
			System.exit(0);
		}
	}

}
