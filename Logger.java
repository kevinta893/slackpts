import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;



/**
 * The logger class, writes messages to a intantiated file name.
 * Each message is time stamped.
 * 
 * Maintains a timestamp named log in the .txt extension.
 * 
 * This Class is not thread safe, and must be locked before use in
 * a multi-threaded enviroment.
 * @author Kevin
 *
 */
public class Logger {

	private String filename;
	
	private BufferedWriter writer;
	
	
	
	
	public Logger(String filename){
		this.filename = filename;
		
		//create the log file, append to existing
		this.filename = filename + " - " + dateStamp() + ".txt";
		File logFile = new File(this.filename);
		
		try {
			boolean exists = logFile.exists();			//check if file already exists prior to creating
			
			writer = new BufferedWriter(new FileWriter(logFile, true));
			
			if(exists == true){
				this.writeLine("Log already exists, continuing log file...");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error! Cannot create the log file: " + filename);
		}
	}

	
	/**
	 * Writes a line to the file. Each message is time stamped
	 * and written on a single line.
	 * File is saved on write.
	 * @param name
	 * @param message
	 */
	public void writeLine(String message){
		try {
			writer.write(timeStamp() + message + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error! Cannot write line to the log file: " + filename);
		}
	}
	
	public void saveLog(){
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error! Cannot save the log file: " + filename);
		}
	}
	
	/**
	 * Closes the logger, does not save.
	 */
	public void close(){
		try {
			writer.close();
			writer = null;						//writer is dead.
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error! Cannot close the log file: " + filename);
		}
		
	}
	
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * Returns a time stamp for the log file. Also includes
	 * a terminating space for easy use.
	 * @return
	 */
	private static String timeStamp(){
		return "[" + (formatter.format(new Date(System.currentTimeMillis()))) + "]: ";
	}
	
	/**
	 * Returns the current date.
	 * @return
	 */
	private static String dateStamp(){
		return dateFormatter.format(new Date(System.currentTimeMillis()));
	}
	public String getFileName(){
		return filename;
	}
}
