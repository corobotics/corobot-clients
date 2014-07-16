/**
 * Robot library in Java
 * 
 * Sketched version for simple simulator testing
 * Could be used as starting point for "real" API
 *
 * @author Z. Butler, Jan 2013
 * @author E. Klei, Jul 2014
 */

/*
  Current questions:
  * How to be informed which robot to connect to?
  * What to do if robot connection can't be established?
  * Or if connection is lost?
  * Robot should detect user program finishing by socket
  being dropped - should make sure that status is updated
  to idle from running
*/

import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.Image;

public class Robot {

    // this should come from config file or something?
    private static int USER_PORT = 15001;

    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<Future> futures;
	private int msgId;

	public static final String CMD_NAVTOLOC = "NAVTOLOC",
								CMD_NAVTOXY = "NAVTOXY",
								CMD_GOTOLOC = "GOTOLOC",
								CMD_GOTOXY = "GOTOXY",
								CMD_GETPOS = "GETPOS",
								CMD_POS = "POS",
								CMD_SHOW_MSG = "SHOW_MSG",
								CMD_SHOW_MSG_CONFIRM = "SHOW_MSG_CONFIRM",
								CMD_CONFIRM = "CONFIRM";	

    /**
     * Constructor, starts connection to a robot...
     */
	public Robot(){
		msgId = 1;
		futures = new ArrayList<Future>();
		System.err.println("Connecting to robot...");
		openSocket("127.0.0.1");
	}
    public Robot(String address) {
        // offloaded to another function for now mostly so that
        // Javadocs can be hidden
        msgId = 1;
		futures = new ArrayList<Future>(); 
        System.err.println("Connecting to robot...");
        openSocket(address);
        // can't think of anything else to do here?
    }

	/**
	 * Overriding Object.finalize() method in which we close the socket connection.
	 * Finalize is not guaranteed to be called immediately or might not get called at all 
	 * if the robot object is still referenced in code.
	 */
	protected void finalize() throws Throwable {
     try {
         this.closeSocket();
     } finally {
         super.finalize();
     }
 }
    /**
     * crap!  how does the server tell us which robot 
     * without making the user code do something?  
     * Environment var maybe?
     */
    private void openSocket(String address) throws RobotConnectionException{
        try {
            String robotName = "corobot2.rit.edu";//System.getenv("ROBOT");
            sock = new Socket(address, USER_PORT);
            out = new PrintWriter(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error connecting to assigned robot.  Please try again.");
            throw new RobotConnectionException("in constructor");
        }
    }

	/**
	 * This method is provided in order to close the socket connection manually.
	 * It should be called by the users of the Robot object after they are done using it.
	 */
	public void closeSocket(){
		try{
			if(sock != null && !sock.isClosed())
			sock.close(); // closes the in and out streams too
		} catch(Exception e){
			System.err.println("IOException thrown in Robot.closeSocket()");
		}
	}

	/**
	 * This method can send commands (messages) to the robot as per the specified format in API.md
	 *
	 * @param args: String vararg parameters to take the command and its options and send it to the 
	 * robot over the socket connection.
	 */
	public Future sendMsgToRobot(String... args){
		StringBuilder msgToSend = new StringBuilder((msgId++) + "");
		for (String arg : args )
			msgToSend.append(" " + arg);
		out.println(msgToSend.toString());
		out.flush();
		Future future = new Future();
		futures.add(future);
		return future;
	}

	/**
	 * This checks whether the response from the robot contains the word 'arrived'
	 * @return : True or False
	 */
	private boolean checkArrivedResponse(String methodName){
		String response = null;
		try {
			response = in.readLine();
			String[] msgs = response.split(" ");
			int id = Integer.parseInt(msgs[0]);
			Future future = futures.remove(id);
			String[] data = null;			
			if( msgs[1] == "POS"){
				data = Arrays.copyOfRange(msgs, 2, msgs.length);
			}else if(msgs[1] == "CONFIRM"){
				data = Arrays.copyOfRange(msgs, 2, msgs.length);
			}
			if( msgs[1] != "ERROR"){
				future.fulfilled(data);
			}else{
				future.error_occured(new CorobotException(response.substring(2)));
			}
		} catch (IOException e) {
			System.err.println("Lost connection with robot!");
			throw new RobotConnectionException("in " + methodName + "()");
		}
		if(response.toLowerCase().contains("arrived"))
			return true;
		return false;
	}

    /**
     * Plans and executes a path to the given location.  Planning is done by the robot.
     *
     * @param location Name (as on map) 
     * @param block specifies whether this call blocks until location reached or some failure condition.
     * @return return whether location has been reached (if blocking)
     */
    public Future navigateToLocation(String location) throws MapException{
        location = location.toUpperCase();
        if (RobotMap.isNode(location)) {
			return sendMsgToRobot(CMD_NAVTOLOC, location.toUpperCase());
        }
        else {
            throw new MapException("Location does not exist");
        }
    }
    

	/**
     * Plans and executes a path to the given coordinates.  Planning is done by the robot.
     *
     * @param x : x-coordinate in the map
     * @param y: y-coordinate in the map
     * @return True or False based on whether the robot succeeded in reaching the coordinates specified.
     */
	public Future navigateToXY(double x, double y){
	   return sendMsgToRobot(CMD_NAVTOXY, x+"", y+"");
	}

    /**
     * Attempts to move in a straight line to the given location.
     *
     * Currently not implemented, waiting for map.
     * @param location Name (as on map) 
     * @param block specifies whether this call blocks until location reached or some failure condition.
     * @return return whether location has been reached (if blocking)
     */
    public Future goToLocation(String location) throws MapException{
        location = location.toUpperCase();
        if (RobotMap.isNode(location)) {
			return sendMsgToRobot(CMD_GOTOLOC, location.toUpperCase());
		}
        else {
            throw new MapException("Location does not exist");
        }
    }

    /**
     * Attempts to move in a straight line to the given X,Y location
     * @param x X coordinate of destination (in map coordinate system) 
     * @param y Y coordinate of destination (in map coordinate system) 
     * @param block specifies whether this call blocks until location reached or some failure condition.
     * @return return whether location has been reached (if blocking)
     */
    public Future goToXY(double x, double y) {
       	return sendMsgToRobot(CMD_GOTOXY, x+"", y+"");
    }

    /**
     * Used by goto functions to wait for ack from robot
     */
    /*private boolean queryArrive() {
        out.println("QUERY_ARRIVE");
        out.flush();
        String line = "";
        try {
            do {
                line = in.readLine();
            } while (!line.equals("ARRIVE") && !line.equals("GOTOFAIL"));
        } catch (IOException e) {
            System.err.println("Lost connection with robot!");
            throw new RobotConnectionException("while waiting for robot to reach destination");
        }
        return line.equals("ARRIVE");
    }*/

    /**
     * Queries the robot for its current position in map coordinates
     * @return position
     */
    public Future getPos() {
		return sendMsgToRobot(CMD_GETPOS);
       
    }

    /** 
     * Gives the named location closest to the robot's current position.
     *
     * @return Name of location
     */
    public String getClosestLoc() {
        String[] s = getPos().get();
        Point p = new Point(Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        return RobotMap.getClosestNode(p.getX(),p.getY());
    }

    /**
     * Returns all named locations close to the robot's current position.
     * not sure how to define "close" here, but likely to be useful
     *
     * Currently not implemented.
     *
     * @return list of nearby location names
	//TODO
    public List<String> getAllCloseLocs() {
        throw new UnsupportedOperationException();
    }
     */
    /**
     * Sends a message for display on the local (robot) GUI
     * @param msg Message to display (&lt; 256 chars suggested)
	 * @param timeout : timeout duration 
     */
    // show a message on the laptop GUI
    public Future showMessage(String msg, int timeout) {
        if (msg.length() > 255)
            msg = msg.substring(0,255);
		if (timeout > 120)
            timeout = 120;
        return sendMsgToRobot(CMD_SHOW_MSG, timeout+"", msg);
    }

	/**
     * Sends a message for display on the local (robot) GUI
     * @param msg Message to display (&lt; 256 chars suggested)
	 * @param timeout : timeout duration 
     */
    // show a message on the laptop GUI
	public Future showMessageWithConfirmation(String msg, int timeout) {
         if (msg.length() > 255)
            msg = msg.substring(0,255);
		 if (timeout > 120)
            timeout = 120;
		 return sendMsgToRobot(CMD_SHOW_MSG_CONFIRM, timeout+"", msg);
    }
    
    /**
     * pops up an OK button on the laptop GUI.
     * Note that any timeout longer than 2 minutes will be set to 2 minutes
     * Currently unsupported by simulated or real robots
     * @param timeout Amount of time to wait for a response (in seconds) 
     * @return whether confirmed (true) or timed out (false)
     */
    /*public boolean waitForConfirm(int timeout) {
        if (timeout > 120)
            timeout = 120;
        out.println("CONFIRM " + timeout);
        out.flush();
        return true;
    }*/
        
    // RobotMap class contains a dictionary of String->MapNode
    // MapNode contains String name, double x,y, List<String> neighbors(?)

    /**
     * Obtain a picture from one of the robot's cameras
     * Extremely not implemented at present.
     * @param whichCamera Which camera to use: 0 = left, 1 = fwd, 2 = right
     * @return some image in some format?
     */
    public Image getImage(int whichCamera) {    
        throw new UnsupportedOperationException();
    }

    // may want other access to robot data but not sure what yet.
}
