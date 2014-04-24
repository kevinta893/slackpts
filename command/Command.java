package command;

public abstract class Command {

	public final String commandName;			//the command name as seen on slack. should be in lower case only.
	
	
	
	public enum CmdResult{
		SUCCESS,					//request successful, write command to log if needed and return verbose.
		FAILED,						//request failed and should report error.
		SUCCESS_SILENT,				//request successful, but do not report back to slack
		FAILED_SILENT,				//request failed but, but do not report back to slack
		INVALID;					//request was an invalid command and should be ignored.
	}
	
	
	/**
	 * All subclasses must declare a command name
	 * This command name will be used to check
	 * @param command
	 */
	protected Command(String command){
		this.commandName = command;
	}
	
	
	/**
	 * Computes the request and creates the messages
	 * needed to be sent on the server.
	 * 
	 * Returns true if the resulting process was successful.
	 * Returns false if the resulting request could not
	 * be fulfilled and needs to report the error to the errorlog.
	 * @param req
	 * @return
	 */
	public abstract CmdResult doRequest(RequestStruct req);
	
	
	/**
	 * Gets the return message of the command. This message
	 * is the message that needs to be returned on slack. This method
	 * should be called after doRequest()
	 * 
	 * If the request could not be processed. Then the message or verbose
	 * should be returned here instead.
	 * @return
	 */
	public abstract String getReturnMessage();
	
	
	/**
	 * Gets the channel that the message needs to be reported on Slack.
	 * This method should be called after doRequest()
	 * @return
	 */
	public abstract String getReturnChannel();
	
	/**
	 * Get the log message needed to be recorded. This method
	 * should be called after doRequest();
	 * @return
	 */
	public abstract String getLogMessage();
	
	
	/**
	 * Get the error message needed to be recorded. This method
	 * should be called after doRequest.
	 * 
	 * Note that the error message here is the message that needs to be recorded
	 * onto the log and not the error to report back to Slack.
	 * 
	 * Return null if no errors.
	 * @return
	 */
	public abstract String getErrorMessage();
	
	
	
	public final boolean isCommand(String command){
		return commandName.equals(command);
	}
}
