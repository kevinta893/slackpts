package command;

import command.SlackMessage.IconType;

import server.WorkStream;

public class ObjectionCmd extends Command {
	private static final String COMMAND = "/objection";
	

	private String logMessage;
	
	
	public ObjectionCmd() {
		super(COMMAND);
		
	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {
		SlackMessage ret = new SlackMessage("`Objection`<http://www.kyubeypawnshop.net/slack_server_icons/objection_vector.png|!>", req.getChannelID());
		ret.setUsername("Phoenix_Wright");
		ret.setUserIcon("http://www.kyubeypawnshop.net/slack_server_icons/Phoenix_Wright_icon.png", IconType.URL);
		
		ret.setUnfurlMedia(true);
		ws.messageSlack(ret);
		
		return 0;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}
}
