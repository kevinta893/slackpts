package command;


/**
 * Template class. Should be used as a template only.
 * @author Kevin
 *
 */
public final class Template extends Command {

	private static final String COMMAND = "";
	
	
	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;
	
	
	public Template() {
		super(COMMAND);
		
	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		returnChannel = req.getChannelName();
		
		return CmdResult.SUCCESS_NO_REPORT;
	}


	@Override
	public String getReturnMessage() {
		return returnMessage;
	}


	@Override
	public String getReturnChannel() {
		return returnChannel;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}


	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
