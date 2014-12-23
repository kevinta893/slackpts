package command;

import server.WorkStream;


/**
 * The debugging command that returns the payload
 * given to the server.
 * @author Kevin
 *
 */
public class DebugCmd extends Command {
	private static final String COMMAND = "/debug";
	
	
	private String logMessage;
	
	
	public DebugCmd() {
		super(COMMAND);
		
	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {

		
		//return the payload.
		String returnMessage = req.toString();
		
		ws.messageSlack(new SlackMessage(returnMessage, req.getChannelID()));
		return 0;
	}

	@Override
	public String getLogMessage() {
		return logMessage;
	}
}
