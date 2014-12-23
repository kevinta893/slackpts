package command;

import server.Config;
import server.UserDB;
import server.UserMapping;
import server.WorkStream;


/**
 * The command that registers a user's name
 * to the particular ID. Updates user mappings database.
 * @author Kevin
 *
 */
public class RegisterCmd extends Command{

	private static final String COMMAND = "/register";


	private String logMessage;


	public RegisterCmd() {
		super(COMMAND);

	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {
		String returnMessage = null;

		String userName = req.getUserName();
		String userID = req.getUserID();
		
		//register command sent, update id of new user.

		if (UserMapping.registerPair(userName, userID)){
			UserMapping.saveAll();

			

			//create new user in database
			UserDB.registerUser(userID);
			UserDB.saveAll();

			logMessage = "Added " + userName + " as new ID: " + userID;
			ws.logPrintln(logMessage);
			
			returnMessage = "Welcome "+ userName + "! You have " + UserDB.getUser(userID).getPts() 
					+ Config.getCurrencyName() + ". Earn more by getting tips from friends.";
			ws.messageSlack(new SlackMessage(returnMessage, req.getChannelID()));
			return 0;
		}
		else{
			String oldName = UserMapping.getName(userID);
			if (UserMapping.updateName(oldName, userName)){
				//successful name update.

				UserMapping.saveAll();

				logMessage = "Updated " + oldName + " -> " + userName;
				ws.logPrintln(logMessage);
				
				returnMessage = "Gotcha! I'll remember you as " + userName + " from now on.";
				ws.messageSlack(new SlackMessage(returnMessage, req.getChannelID()));
				return 0;
			}
		}
		
		
		return -1;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}

}
