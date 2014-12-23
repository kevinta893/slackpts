package command;

import server.Config;
import server.WorkStream;


/**
 * The command that sends a random fish slapping message.
 * @author Kevin
 *
 */
public class SlapCmd extends Command{

private static final String COMMAND = "/slap";
	
	
	private String logMessage;

	
	
	
	
	public SlapCmd() {
		super(COMMAND);
		
	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {
		String returnMessage = null;
		
		
		String[] args = req.getArgs();
		
		// assign the victim
		String victim;
		if(args.length == 1){
			victim = args[0];
		}
		else{
			//no args means slap self.
			victim = req.getUserName();
		}
		
		
		String perp = req.getUserName();
		
		
		
		double[] probs = {0.8, 0.15, 0.04, 0.01};
		
		int index = Rand.randArray(probs);
		
		
		//randomly select message to send back.
		if (index == 0){
			returnMessage = perp + " slaps " + victim + " around a bit with a large trout.";
		}
		else if (index == 1){
			returnMessage = perp + " slaps " + victim + " around a bit with a large fat trout.";
		}
		else if (index == 2){
			returnMessage = perp + " slaps " + victim + " around a bit with a rainbow trout! Fabulous!";
		}
		else if (index == 3){
			returnMessage = Config.getBotName() + " slaps " + victim + " with a " + perp + "! Critical hit! 5x Combo!";
		}
		
		ws.messageSlack(new SlackMessage(returnMessage, req.getChannelID()));
		return 0;
	}



	@Override
	public String getLogMessage() {
		return logMessage;
	}

}
