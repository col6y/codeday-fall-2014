package server;

import java.io.IOException;

import javax.swing.JOptionPane;

import server.logger.Logger;
import server.route.RouteServe;

public class LiteServer {
	public static void main(String[] args) throws IOException {
		new Thread() {
			public void run() {
				try {
					RouteServe.main(new String[0]);
				} catch (IOException e) {
					Logger.severe("Can't launch", e);
				}
			}
		}.start();
		MainThread.main(new String[] {"127.0.0.1"});
		JOptionPane.showMessageDialog(null, "LiteServer is now running locally on " + MainThread.getLocalAddress());
	}
}