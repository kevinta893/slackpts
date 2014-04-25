package command;

import server.Config;


/**
 * The command that sends a random fish slapping message.
 * @author Kevin
 *
 */
public class SlapCmd extends Command{

private static final String COMMAND = "/slap";
	
	
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
