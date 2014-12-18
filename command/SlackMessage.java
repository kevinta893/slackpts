package command;

import java.util.LinkedList;
import java.util.List;

import server.Config;


/**
 * Constructs a JSON object that can be used to
 * send a message onto Slack.
 * @author Kevin
 *
 */
public class SlackMessage {



	private String text;
	private String channel;
	private String username;
	private String user_icon;
	private boolean unfurl_links = false;
	
	private LinkedList<SlackAttachment> attachments;


	/**
	 * Creates a plain text message to be sent to the channel
	 * @param message
	 * @param channel
	 */
	public SlackMessage(String message, String channel){
		this.text = message;
		this.channel = channel;
		this.attachments = new LinkedList<SlackAttachment>();
		
		//defaults
		this.username = Config.getBotName();
		this.user_icon = "null";
		this.unfurl_links = false;
	}


	/**
	 * Sets the username to display when posting
	 * @param name
	 */
	public void setDisplayName(String name){
		this.username = name;
	}


	/**
	 * Sets the channel for the text to be sent on.
	 * #channel for public channels 
	 * @channel for direct messages (posted as slackbot for that user)
	 * @param channel
	 */
	public void setChannel(String channel){
		if ((channel.charAt(0) != '#') || (channel.charAt(0) != '@')){
			throw new IllegalArgumentException("Invalid channel specifier. Required # or @ prefix.");
		}
		else{
			this.channel = channel;
		}
	}
	

	/**
	 * Sets the icon for the message being sent. Default
	 * on creation is found in the configuration file.
	 * @param url Expected http:// or https:// url to an icon
	 * which no restriction.
	 * An emoji or custom emoji can be used in place of a url.
	 */
	public void setIcon(String url){
		this.user_icon = url;
	}


	public void setUnfurlLinks(boolean yes){
		this.unfurl_links=yes;
	}
	
	
	public void addAttachment(SlackAttachment a){
		attachments.add(a);
	}
	
	
	public String getJSON(){
		return "{" + 
				makePair("text", text) + "," +
				makePair("channel", channel) + "," +
				makePair("username", username) + "," +
				makePair("usericon", user_icon) + "," +
				makePair("unfurl_links", unfurl_links) + 
				(attachments.size() > 0 ? ("," + makeArray("attachments", attachments)) : "") +		//add attachments array if not empty
				"}";
	}
	
	
	//=========================
	//message extras
	
	
	private interface JSONArrayable{
		
		/**
		 * Gets a JSON representation of a single item that
		 * can be stored in an array. Item returned is a valid
		 * JSON that is subtended by curly braces {}
		 * @return
		 */
		public String getJSON();
			
	}
	

	public static class SlackAttachment implements JSONArrayable{

		private String fallback;
		private String pretext;
		private String color;
		
		private LinkedList<SlackField> fields;

		/**
		 * The text to be displayed whenever message attachments cannot be
		 * shown. IE: mobile, desktop notifications, etc
		 * @param fallback
		 */
		public SlackAttachment(String fallback){
			this.fallback=fallback;

			this.fields = new LinkedList<SlackField>();

		}


		public void setPretext(String pretext){
			this.pretext=pretext;
		}


		public void setColor(String color){

		}

		public void addField(SlackField field){
			fields.add(field);
		}

		public void removeField(SlackField field){
			fields.remove(field);
		}

		public void removeField(int position){
			fields.remove(position);
		}

		


		public String getJSON(){
			return "{" +
					makePair("fallback", fallback) + "," + 
					makePair("pretext", pretext) + "," + 
					makePair("color", color) + 
					(fields.size() > 0 ? ("," + makeArray("fields", fields)) : "") + 
					"}";
					
		}
	}

	
	
	public static class SlackField implements JSONArrayable{

		private String title;
		private String value;
		private boolean shortField;


		public SlackField(String title, String value){
			this.title = title;
			this.value = value;

			this.shortField = false;
		}

		public void setShort(boolean yes){
			this.shortField = yes;
		}

		public String getJSON(){
			return "{" +
					makePair("title", title) + "," +
					makePair("value", value) + "," +
					makePair("short", shortField) +
					"}";
		}

	}

	
	
	
	//========================================
	//JSON utilities

	private static String makePair(String key, String value){
		return "\"" + key + "\":\"" + value + "\"";
	}

	private static String makePair(String key, boolean b){
		return "\"" + key + "\":" + (b == true ? "true" : "false");
	}

	
	/**
	 * Returns an array
	 * @param key
	 * @param items
	 * @return
	 */
	private static String makeArray(String key, List<? extends JSONArrayable> items){
		
		String collect = "\"" + key + "\":" + "[";
		
		for (int i = 0; i< items.size(); i++){
			collect += items.get(i).getJSON();
			
			if (i < (items.size()-1)){
				//last items does not need comma, comma everythign else
				collect += ",";
			}
		}
		
		
		collect += "]";
		
		return collect;
	}

	
	/**
	 * Produces a human readable version of the JSON message
	 * @return
	 */
	public static String dumpString(String d){
		return null;
	}
	
	public static void main(String[] args){
		SlackMessage m = new SlackMessage("hi", "dufus");
		SlackAttachment a = new SlackAttachment("what");
		SlackField f = new SlackField("hi", "value");
		a.addField(f);
		a.addField(f);
		m.addAttachment(a);
		m.addAttachment(a);
		
		String out = m.getJSON().replaceAll(",", ",\n");
		out = out.replaceAll("\\{", "{\n");
		out = out.replaceAll("\\}", "\n}");
		System.out.println(out);
	}
}
