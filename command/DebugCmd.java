package command;



public class DebugCmd extends Command {
	private static final String COMMAND = "/debug";
	
	
	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;
	
	
	public DebugCmd() {
		super(COMMAND);
		
	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		returnChannel = req.getChannelName();
		
		//return the payload.
		returnMessage = req.getPayload();
		
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
