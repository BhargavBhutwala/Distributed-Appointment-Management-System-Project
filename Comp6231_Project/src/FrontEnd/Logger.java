package FrontEnd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Logs the information in a file
 * @author S_K
 *
 */
public class Logger {

	
	/**
	 * logs the message in the server file
	 * @param msg message to log
	 */
	public  void log(String msg) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/Dell/Desktop/CONCORDIA/COMP 16/Final_Prroject/src/FrontEnd/log.txt", true));
			writer.append(msg);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
