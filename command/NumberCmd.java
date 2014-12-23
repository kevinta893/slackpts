package command;

import server.WorkStream;


/**
 * Command returns a random number between 1 and 10 inclusive.
 * @author Kevin
 *
 */
public class NumberCmd extends Command {
	
	private static final String COMMAND = "/number";

	private static final int MIN = 1;				//inclusive
	private static final int MAX = 10;				//inclusive
	

	private String logMessage;



	public NumberCmd() {
		super(COMMAND);

	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {
		String result = Integer.toString(Rand.randInt(MIN, MAX));

		String returnMessage = "Your number is: *" + result + "*";
		ws.messageSlack(new SlackMessage(returnMessage, req.getChannelID()));
		return 0;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}

}

