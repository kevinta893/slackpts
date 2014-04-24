package command;

import server.Config;
import server.User;
import server.UserDB;
import server.UserMapping;

public class CheckCmd extends Command {
	private static final String COMMAND = "/check";


	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;


	public CheckCmd() {
		super(COMMAND);

	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		String[] args = req.getArgs();
		returnChannel = req.getChannelName();
		
		//check command sent, return current points.

		if ((args.length == 1) && (args[0].equals("") == false)){


			//get the id of the user
			String targetID = UserMapping.getID(args[0]);

			if(targetID != null){
				if (UserDB.hasUser(targetID)){
					//user exists, return their count.
					User check = UserDB.getUser(targetID);
					String humanName = UserMapping.getName(targetID);
					returnMessage = humanName + " has " + check.getPts() + Config.getCurrencyName() + ".";
					return CmdResult.SUCCESS;
				}
			}
			else{
				//no such user exists, report back
				returnMessage = "No such user named " + req.getUserName() + " exists. Have they registered yet?";
				return CmdResult.SUCCESS;
			}

		}
		else if ((args.length == 1) && (args[0].equals("") == true)){

			//get the id of the user
			String targetID = UserMapping.getID(req.getUserName());

			if(targetID != null){
				if (UserDB.hasUser(targetID)){
					//user exists, return their count.
					User check = UserDB.getUser(targetID);
					String humanName = UserMapping.getName(targetID);
					returnMessage = humanName + " has " + check.getPts() + Config.getCurrencyName() + ".";
					return CmdResult.SUCCESS;
				}
			}
			else{
				//no such user exists, report back
				returnMessage = "I cannot find your record " + req.getUserName() + ". Have you registered yet?";
				return CmdResult.SUCCESS;
			}
		}

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
