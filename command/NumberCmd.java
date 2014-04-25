package command;


/**
 * Command returns a random number between 1 and 10 inclusive.
 * @author Kevin
 *
 */
public class NumberCmd extends Command {
	
	private static final String COMMAND = "/number";

	private static final int MIN = 1;				//inclusive
	private static final int MAX = 10;				//inclusive
	

	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;


	public NumberCmd() {
		super(COMMAND);

	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		returnChannel = req.getChannelName();

		String result = Integer.toString(Rand.randInt(MIN, MAX));

		returnMessage = "Your number is: *" + result + "*";

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

