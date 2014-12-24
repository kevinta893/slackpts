package command;

import command.SlackMessage.SlackAttachment;
import command.SlackMessage.SlackField;
import server.WorkStream;

public class ColorCmd extends Command{

	private static final String COMMAND = "/colorme";

	private static final String DEFAULT_COLOR = "#000000";
	
	private String logMessage;


	public ColorCmd() {
		super(COMMAND);

	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {

		String[] args = req.getArgs();

		String colorArg = args[0].replaceAll("%23f", "#");


		if (args.length >= 2){
			SlackMessage ms = new SlackMessage("", req.getChannelID());
			ms.setDisplayName(req.getUserName());
			SlackAttachment a = new SlackAttachment("");

			//append hashtag and correct color hex code.
			String color = colorArg;
			if (args[0].charAt(0) != '#'){
				color = "#" + args[0];
			}

			//if any errors then switch to black hex code
			for (int i = 1; i < color.length(); i++){
				
				//if any errors then default color
				if (validHex(color.charAt(i)) == false){
					color = DEFAULT_COLOR;
				}
			}

			if (color.length() != 7){
				//pad end with zero
				for (int i = color.length() ; i < 7; i++){
					color += "0";
				}
			}

			//create field with text.
			a.setColor(color);

			String full = "";
			for (int i = 1 ; i < args.length; i++){
				full += args[i] + " ";
			}

			SlackField f = new SlackField("", full);

			//send message
			a.addField(f);
			ms.addAttachment(a);
			ws.messageSlack(ms);
		}


		return 0;
	}

	private boolean validHex(char c){
		
		boolean valid = false;
		
		switch (c){
			case 'a': valid = true;
			case 'A': valid = true;
			case 'b': valid = true;
			case 'B': valid = true;
			case 'c': valid = true;
			case 'C': valid = true;
			case 'd': valid = true;
			case 'D': valid = true;
			case 'e': valid = true;
			case 'E': valid = true;
			case 'f': valid = true;
			case 'F': valid = true;
			case '0': valid = true;
			case '1': valid = true;
			case '2': valid = true;
			case '3': valid = true;
			case '4': valid = true;
			case '5': valid = true;
			case '6': valid = true;
			case '7': valid = true;
			case '8': valid = true;
			case '9': valid = true;
		}
		
		
		return valid;
	}
	

	@Override
	public String getLogMessage() {
		return logMessage;
	}
}
