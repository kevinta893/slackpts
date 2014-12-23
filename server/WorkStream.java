package server;

import command.SlackMessage;

public interface WorkStream {

	
	
	/**
	 * Dumps message into the output stream
	 * of the server
	 */
	public void outPrintln(String out);
	
	/**
	 * Dumps message into the error stream
	 * of the server
	 */
	public void errPrintln(String err);
	
	/**
	 * Dumps a file recorded message onto the server
	 */
	public void logPrintln(String out);
	
	/**
	 * Dumps a file recorded error message onto the server
	 */
	public void errLogPrintln(String err);
	
	/**
	 * Dumps an exception onto the log
	 */
	public void errLogPrintln(Exception e);
	
	/**
	 * Sends a message onto slack. Returns
	 * true if message was sent, false otherwise.
	 * @param message
	 * @boolean
	 */
	public boolean messageSlack(SlackMessage message);
}
