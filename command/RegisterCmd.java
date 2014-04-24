package command;

public class RegisterCmd extends Command{

	private static final String COMMAND = "/register";


	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;


	public RegisterCmd() {
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
