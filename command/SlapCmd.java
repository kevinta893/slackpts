package command;

import server.Config;



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
		if((args.length == 1) && (args[0].equals("") == false)){
			victim = args[0];
		}
		else{
			//no args means slap self.
			victim = req.getUserName();
		}
		
		
		String perp = req.getUserName();
		
		
		
		double[] probs = {0.7, 0.15, 0.04, 0.01};
		
		int index = Rand.randArray(probs);
		
		
		//randomly select message to send back.
		if (index == 0){
			returnMessage = perp + " slapped " + victim + " with a trout!";
		}
		else if (index == 1){
			returnMessage = perp + " slapped " + victim + " with a fat trout";
		}
		else if (index == 2){
			returnMessage = perp + " slapped " + victim + " with a rainbow trout, fabulous!";
		}
		else if (index == 3){
			returnMessage = Config.getBotName() + " slapped " + victim + " with a " + perp + "! Critical hit!";
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
