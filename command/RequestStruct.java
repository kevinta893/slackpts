package command;

/**
 * Represents the slack information as presented in the slack commands.
 * @author Kevin
 *
 */
public class RequestStruct {


	//payload tags in Slack POST
	public static final String PAYLOAD_START = "token=";
	public static final String CMD_TAG = "command=";
	public static final String TEXT_TAG = "text=";
	public static final String CHANNEL_NAME_TAG = "channel_name=";
	public static final String CHANNEL_ID_TAG = "channel_id=";
	public static final String USER_NAME_TAG = "user_name=";
	public static final String USER_ID_TAG = "user_id=";


	private String token; 
	private String channelName; 
	private String channelID;
	private String userID; 
	private String userName; 
	private String command; 							//replace "%2f" with forward slash
	private String[] args;								//arguements of the command

	
	
	private RequestStruct(String payload) throws IllegalArgumentException{

	
			try {
				token = getTagArg(payload, PAYLOAD_START);
				channelName = getTagArg(payload, CHANNEL_NAME_TAG);
				channelID = getTagArg(payload, CHANNEL_ID_TAG);
				userID = getTagArg(payload, USER_ID_TAG);
				userName = getTagArg(payload, USER_NAME_TAG);
				command = getTagArg(payload, CMD_TAG).replaceAll("%2F", "/");			//replace "%2f" with forward slash
				args = getTextArgs(payload);
			} catch (Exception e) {
				throw new IllegalArgumentException("Bad payload");
			}											
		
		
	}
	
	
	
	
	
	
	
	

	/**
	 * Creates a structure of the payload.
	 * If its an invalid payload, then the returned class is null.
	 * @param payload
	 * @return Null if the instance cannot be crated with the given payload
	 */
	public static RequestStruct createInstance(String payload){
		
		RequestStruct attempt = null;
		
		//attempt to create an instance
		try {
			attempt = new RequestStruct(payload);
		} catch (IllegalArgumentException e) {
			attempt = null;
		}
		
		
		return attempt;
	}
	
	
	/**
	 * Gets the command argument of the Slack slash command of the
	 * specified tag in the request.
	 * Returned string is pre-trimmed.
	 * @param postRequest The request to parse
	 * @param tag The tag to extract the value of
	 * @return Empty string if tag is not found, Value of the tag otherwise.
	 */
	private static String getTagArg(String payload, String tag){

		int index = payload.indexOf(tag);

		if (index >= 0){
			String arg = payload.substring(index + tag.length(),payload.indexOf("&", index));
			return arg.trim();
		}

		return "";
	}
	
	
	/**
	 * Gets all the arguments seperated by spaces (the '+' symbol).
	 * Returns a String array of each argument in the order they are found in.
	 * 
	 * If there are no arguments, an empty array is returned.
	 * If the string does not contain the "
	 * @param payload
	 * @return
	 */
	private static String[] getTextArgs(String payload){
		int index = payload.indexOf(TEXT_TAG);

		if (index >=0){
			String raw = payload.substring(index + TEXT_TAG.length());

			return raw.split("\\+");
		}

		return (new String[0]);
	}
	
	
	
	
	
	//========================================================================
	//Getters and Setters
	
	public String getToken() {
		return token;
	}

	public String getChannelName() {
		return channelName;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getUserID() {
		return userID;
	}

	public String getUserName() {
		return userName;
	}

	public String getCommand() {
		return command;
	}

	public String[] getArgs() {
		return args;
	}
	
}