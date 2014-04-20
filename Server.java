import java.net.Socket;


public class Server {

	public static final String PTS_NAME = "peeg";
	public static final int INCREMENT = 1;
	
	
	public static final int SERVER_PORT = 80;
	
	public static Socket LISTEN_SOCK;
	
	public static void main(String[] args){
		System.out.println(UserDB.getInstance().getUserCount());
	}
}
