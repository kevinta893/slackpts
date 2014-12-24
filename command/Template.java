package command;

import server.WorkStream;


/**
 * Template class. Should be used as a template only.
 * @author Kevin
 *
 */
public final class Template extends Command {

	private static final String COMMAND = "/template";
	

	private String logMessage;
	
	
	public Template() {
		super(COMMAND);
		
	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {
		
		ws.messageSlack(new SlackMessage("Hello World", req.getChannelID()));
		logMessage = "Optional: Runtime errors or comments here. Leave null for nothing";
		return 0;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}


	

}
