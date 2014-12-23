package command;

import server.WorkStream;

public abstract class Command {

	public final String commandName;			//the command name as seen on slack. should be in lower case only.
	
	
	
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
	 * @param req
	 * @return 
	 * <0 for error
	 * >=0 for successful execution
	 * 
	 * Semantics of this error code is arbitarily defined
	 * and should be included whenever extending this class.
	 */
	public abstract int doRequest(WorkStream ws, RequestStruct req);
	
	
	/**
	 * Gets the comment on what work was done. Error messages or detailed
	 * runtime can be dumpped here.
	 * 
	 * Note that the error message here is the message that not need to be reported
	 * back to Slack or written to the log.
	 * 
	 * To report errors during runtime (preferred) use WorkStream to dump messages.
	 * 
	 * Return null if no errors.
	 * @return
	 */
	public abstract String getLogMessage();
	
	
	/**
	 * Checks if the command given is equal to the command.
	 * @param command
	 * @return
	 */
	public final boolean isCommand(String command){
		return commandName.equals(command);
	}
	
	/**
	 * Checks if the command given is equal to the command.
	 * @param cmd
	 * @return
	 */
	public final boolean isCommand(Command cmd){
		return commandName.equals(cmd.commandName);
	}
	
}
