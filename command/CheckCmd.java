package command;

import server.Config;
import server.User;
import server.UserDB;
import server.UserMapping;

public class CheckCmd extends Command {
	private static final String COMMAND = "";


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
					messageSlack(humanName + " has " + check.getPts() + Config.getCurrencyName() + ".", channelName);
				}
			}
			else{
				//no such user exists, report back
				messageSlack("No such user named " + userName + " exists. Have they registered yet?", channelName);
			}

		}
		else if ((args.length == 1) && (args[0].equals("") == true)){

			//get the id of the user
			String targetID = UserMapping.getID(userName);

			if(targetID != null){
				if (UserDB.hasUser(targetID)){
					//user exists, return their count.
					User check = UserDB.getUser(targetID);
					String humanName = UserMapping.getName(targetID);
					messageSlack(humanName + " has " + check.getPts() + Config.getCurrencyName() + ".", channelName);
				}
			}
			else{
				//no such user exists, report back
				messageSlack("I cannot find your record " + userName + ". Have you registered yet?", channelName);
			}
		}

		return CmdResult.SUCCESS;
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
