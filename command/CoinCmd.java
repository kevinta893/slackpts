package command;


public class CoinCmd extends Command{
	
private static final String COMMAND = "/coin";
	
	
	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;
	
	
	public CoinCmd() {
		super(COMMAND);
		
	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		returnChannel = req.getChannelName();
		
		String result = (Rand.randBoolean() == true) ? "heads" : "tails";
		
		returnMessage = "Flipping a coin...\nIt's *" + result + "*";
				
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
