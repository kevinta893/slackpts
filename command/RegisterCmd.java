package command;

import server.Config;
import server.UserDB;
import server.UserMapping;

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

		String userName = req.getUserName();
		String userID = req.getUserID();
		
		//register command sent, update id of new user.

		if (UserMapping.registerPair(userName, userID)){
			UserMapping.saveAll();

			

			//create new user in database
			UserDB.registerUser(userID);
			UserDB.saveAll();

			logMessage = "Added " + userName + " as new ID: " + userID;

			returnMessage = "Welcome "+ userName + "! You have " + UserDB.getUser(userID).getPts() 
					+ Config.getCurrencyName() + ". Earn more by getting tips from friends.";
			return CmdResult.SUCCESS;
		}
		else{
			String oldName = UserMapping.getName(userID);
			if (UserMapping.updateName(oldName, userName)){
				//successful name update.

				UserMapping.saveAll();

				logMessage = "Updated " + oldName + " -> " + userName;

				returnMessage = "Gotcha! I'll remember you as " + userName + " from now on.";
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
