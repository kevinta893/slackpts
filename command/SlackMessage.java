package command;

import java.util.LinkedList;
import java.util.List;

import server.Config;


/**
 * Constructs a JSON object that can be used to
 * send a message onto Slack.
 * 
 * Uses configuration Config for defaults
 * @author Kevin
 *
 */
public class SlackMessage {



	private String message;
	private String channel;
	private String username;
	private String user_icon;
	private String user_icon_key;
	private boolean unfurl_links = false;
	
	private LinkedList<SlackAttachment> attachments;


	public enum IconType{
		EMOJI,
		URL
	}
	
	
	/**
	 * Creates a plain text message to be sent to the channel
	 * @param message
	 * @param channel
	 */
	public SlackMessage(String message, String channel){
		setMessage(message);			//ensure message checked
		setChannel(channel);			//ensure channel checked
		this.attachments = new LinkedList<SlackAttachment>();
		
		//defaults
		this.username = Config.getBotName();
		setUserIcon(Config.getDefaultIconURL(), IconType.URL );
		this.unfurl_links = false;
	}

	
	/**
	 * Creates a message to be sent on the default channel
	 * as defined in server.Config
	 * @param message
	 */
	public SlackMessage(String message){
		this(message, Config.getDefaultChannel());
	}

	/**
	 * Sets the message. Applicable slack formatting
	 * can be added here. Use <> to subtend a url.
	 * 
	 * Example:"<http://www.google.ca|Click here!> for details"
	 * 
	 * Any whitespace characters entered here will be properly 
	 * escaped by this function.
	 * @param message
	 */
	public void setMessage(String message){
		this.message = message;
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
		if ((channel.charAt(0) == '#') || (channel.charAt(0) == '@')){
			this.channel = channel;
		}
		else{
			throw new IllegalArgumentException("Invalid channel specifier. Required # or @ prefix. channel=" + channel);
		}
	}
	

	/**
	 * Sets the icon for the message being sent. Default
	 * on creation is found in the configuration file.
	 * @param url Expected http:// or https:// url to an icon
	 * which no restriction. Set null for url and any type to 
	 * set the icon to default.
	 * An emoji or custom emoji can be used in place of a url.
	 */
	public void setUserIcon(String url, IconType type){
		
		if(type == IconType.URL){
			this.user_icon_key = "icon_url";
		}
		else if (type == IconType.EMOJI){
			this.user_icon_key = "icon_emoji";
		}
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
				makePair("text", message) + "," +
				makePair("channel", channel) + "," +
				makePair("username", username) + "," +
				makePair(user_icon_key, user_icon) + "," +
				makePair("unfurl_links", unfurl_links) + 
				(attachments.size() > 0 ? ("," + makeArray("attachments", attachments)) : "") +		//add attachments array if not empty
				"}";
	}
	

	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getMessage() {
		return message;
	}


	public String getChannel() {
		return channel;
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
		private String color = "#00B200";
		
		private LinkedList<SlackField> fields;

		/**
		 * The text to be displayed whenever message attachments cannot be
		 * shown. IE: mobile, desktop notifications, etc
		 * @param fallback
		 */
		public SlackAttachment(String fallback){
			this.fallback = fallback;
			this.fields = new LinkedList<SlackField>();
			
		}

		

		public void setFallback(String fallback) {
			this.fallback = fallback;
		}


		public void setPretext(String pretext){
			this.pretext = pretext;
		}


		public void setColor(String color){
			this.color = color;
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

		public void setValue(String value){
			this.value = value;
		}
		
		public void setTitle(String title){
			this.title = title;
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
		return "\"" + key + "\":" + quote(value) + "";
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
	 * Code taken from Jettison
	 * http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.jettison/jettison/1.3.3/org/codehaus/jettison/json/JSONObject.java#JSONObject.quote%28java.lang.String%29
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
//                if (b == '<') {
                    sb.append('\\');
//                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ') {
                    t = "000" + Integer.toHexString(c);
                    sb.append("\\u" + t.substring(t.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
	
	
	/**
	 * Produces a human readable version of a JSON 
	 * @param json A json string
	 * @return
	 */
	public static String dumpString(String json){
		String out = json.replaceAll(",", ",\n");
		out = out.replaceAll("\\{", "{\n");
		out = out.replaceAll("\\}", "\n}");
		return out;
	}
	

	
	public static void main(String[] args){
		SlackMessage m = new SlackMessage("hi\n\n", "#dufus");
		SlackAttachment a = new SlackAttachment("what");
		SlackField f = new SlackField("hi", "value");
		a.addField(f);
		a.addField(f);
		m.addAttachment(a);
		m.addAttachment(a);
		
		SlackMessage test = new SlackMessage("This is a test", "#botforge");
		System.out.println(dumpString(test.getJSON()));
	}
}
