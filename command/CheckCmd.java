package command;

import server.Config;
import server.User;
import server.UserDB;
import server.UserMapping;
import server.WorkStream;

/**
 * Command that checks how many points a user or self has
 * in their "wallet".
 * @author Kevin
 *
 */
public class CheckCmd extends Command {
	private static final String COMMAND = "/check";


	private String logMessage;


	public CheckCmd() {
		super(COMMAND);

	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {
		String[] args = req.getArgs();
		SlackMessage returnMessage = null;
		
		//check command sent, return current points.
	
		if (args.length == 1){
			//arg specified, check that user

			//get the id of the user
			String targetID = UserMapping.getID(args[0]);

			if(targetID != null){
				if (UserDB.hasUser(targetID)){
					//user exists, return their count.
					User check = UserDB.getUser(targetID);
					String humanName = UserMapping.getName(targetID);
					returnMessage = new SlackMessage(humanName + " has " + check.getPts() + Config.getCurrencyName() + ".", req.getChannelName());
					ws.messageSlack(returnMessage);
					return 0;
				}
			}
			else{
				//no such user exists, report back
				returnMessage = new SlackMessage("No such user named " + args[0] + " exists. Have they registered yet?", "#" + req.getChannelName());
				ws.messageSlack(returnMessage);
				return 0;
			}

		}
		else if (args.length == 0){
			//check on self
			
			//get the id of the user
			String targetID = UserMapping.getID(req.getUserName());

			if(targetID != null){
				if (UserDB.hasUser(targetID)){
					//user exists, return their count.
					User check = UserDB.getUser(targetID);
					String humanName = UserMapping.getName(targetID);
					returnMessage = new SlackMessage(humanName + " has " + check.getPts() + Config.getCurrencyName() + ".", "#" + req.getChannelName());
					ws.messageSlack(returnMessage);
					return 0;
				}
			}
			else{
				//no such user exists, report back
				returnMessage = new SlackMessage("I cannot find your record " + req.getUserName() + ". Have you registered yet?", "#" + req.getChannelName());
				ws.messageSlack(returnMessage);
				return 0;
			}
		}

		
		logMessage = "ERROR! Invalid arguements.";
		return -1;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}
}
