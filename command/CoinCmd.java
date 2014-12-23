package command;

import server.WorkStream;



/**
 * The command that flips a coin and says either
 * heads or tails. 50-50 chance.
 * @author Kevin
 *
 */
public class CoinCmd extends Command{

	private static final String COMMAND = "/coin";

	private String logMessage;



	public CoinCmd() {
		super(COMMAND);

	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {		

		String result = (Rand.randBoolean() == true) ? "heads" : "tails";

		String returnMessage = "Flipping a coin...\nIt's *" + result + "*";

		ws.messageSlack(new SlackMessage(returnMessage, req.getChannelID()));
		return 0;
	}





	@Override
	public String getLogMessage() {
		return logMessage;
	}

}
