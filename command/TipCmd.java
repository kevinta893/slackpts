package command;

import server.Config;
import server.UserDB;
import server.UserMapping;


/**
 * Command that supports the tipping of other users.
 * Updates user database of the new value.
 * @author Kevin
 *
 */
public class TipCmd extends Command{

	public static final String COMMAND= "/tip";

	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;

	private int tipAmount = 1;

	/**
	 * Creates a default tip command with tip increments of 1
	 */
	public TipCmd(){
		this(1);
	}

	public TipCmd(int tipAmount){
		super(COMMAND);
		this.tipAmount = 1;
	}

	
	@Override
	public CmdResult doRequest(RequestStruct req) {
		String[] args = req.getArgs();
		returnChannel = req.getChannelName();


		//increment only if there is a user that exists.
		if (args.length == 1){

			String targetID = UserMapping.getID(args[0]);

			if (targetID != null){
				if (UserDB.hasUser(targetID)){


					if (targetID.equals(req.getUserID()) == false){
						//not self, do tipping

						UserDB.increment(targetID, tipAmount);
						returnMessage = args[0] + " gained " + tipAmount + Config.getCurrencyName();
						logMessage = req.getUserName() + " gave " + args[0] + " " + tipAmount + Config.getCurrencyName();
						
						return CmdResult.SUCCESS;
					}
					else{
						//error, cannot tip self.
						returnMessage = "You cannot tip yourself " + req.getUserName() + "!";
						return CmdResult.SUCCESS;
					}
				}
			}
			else{
				//no mapping found, return error
				returnMessage = "I do not recognize who " + args[0] + " is! Ask that user to enter /register to get an account. Register again if you have changed your username.";
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

