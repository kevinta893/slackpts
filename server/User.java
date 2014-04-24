package server;


/**
 * The User class, has a name an the number of points.
 * @author Kevin
 *
 */
public class User {

	private String name;
	private long pts;
	
	
	
	public User(String name){
		this.name = name;
		this.pts = 0;
	}
	
	public User(String name, long pts){
		this.name = name;
		this.pts = pts;
	}
	
	/**
	 * Increments the number of points for this user by 1.
	 */
	public void increment(){
		pts++;
	}
	
	/**
	 * Increments the number of points for this user by given amount.
	 * Warning is printed for giving a non postive number
	 * @param amount
	 */
	public void increment(long amount){
		if (amount <= 0){
			//non postive increment, print error
			System.err.println("Warning! Attempted to add " + amount + " value to user: " + name);
		}
		
		pts += amount;
	}
	
	
	
	
	public long getPts(){
		return pts;
	}
	
	public String getName(){
		return name;
	}
}
