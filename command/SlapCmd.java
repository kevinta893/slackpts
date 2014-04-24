package command;


public class SlapCmd extends Command{

private static final String COMMAND = "";
	
	
	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;
	
	
	public SlapCmd() {
		super(COMMAND);
		
	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		returnChannel = req.getChannelName();
		
		return CmdResult.INVALID;
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
