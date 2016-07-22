/********************
* A VirtualRobot for testing the API functionality without using a real robot
* Usable for checking connections and basic sending of data. Actual robot
* functionality should use a real robot
* @author E. Klei Jul 2014
* @author Z. Butler Jul 2016
********************/

import java.io.*;
import java.net.*;
import corobot.RobotMap;
import corobot.MapNode;

public class VirtualRobot {
    private ServerSocket mine;
    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;
    private RobotMap rmap;
    private double x, y;
    /**
     * Creates a new VirtualRobot
     */
    public VirtualRobot(){
        try{
            mine = new ServerSocket(15001);
            sock = mine.accept();
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        }catch( IOException e) {
            e.printStackTrace();
        }
        rmap = new RobotMap();
    }

    /**
     *  Makes the virtual robot pause a reasonable amount when "moving"
     * @args goSlow Should I go slow?
     */
    public VirtualRobot(boolean goSlow) {
        this();
        this.goSlow = goSlow;
    }
    
    /**
     * Simulates the act of robot movement. Waits, then continues
     * @args The id of the message telling the robot to go to a place
     */
    public void goPlaces(int msgId){
        try{
            if (goSlow) {
                dist = Math.sqrt((newx-x)*(newx-x) + (newy-y)*(newy-y));
                Thread.sleep(3000*dist);
            } else {
                Thread.sleep(1000);
            }
            x = newx;
            y = newy;
            sendPos(msgId);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Simulates a robot sending it's position
     * @args the message id to send with
     */
    public void sendPos(int msgId){
        out.println(msgId + " POS " + x + " " + y + " 0" );
    }
    /**
     * Simulates the result of a confirm dialog
     * @args the message id to send with
     */
    public void confirm(int msgId){
        out.println(msgId + " CONFIRM false");
    }
    /**
     * Simulates displaying a message
     */
    public void showMessage(){
        System.out.print("Message");
    }
    /**
     * Simulates displaying a confirmation
     * @args the id of the command sent
     */
    public void showConfirm(int msgId){
        System.out.print("Confirm");
        confirm(msgId);
    }
    /**
     * Start reading the input to the server
     */
    public void startReading(){
        
        new readerThread().start();
    }
    /**
     * Runs forever, reading input and calling the appropriate functions
     */
    private class readerThread extends Thread{
        public void run(){
            try{
                for(;;){
                    String given = in.readLine();
                    if (given == null) break;
                    
                    String[] parts = given.split(" ");
                    int id = Integer.parseInt(parts[0]);
                    String key = parts[1];
                    if(key.equals("NAVTOLOC") || key.equals("NAVTOXY") || key.equals("GOTOLOC") || key.equals("GOTOXY") ){
                        double newx,newy;
                        if (key.endsWith("XY")) {
                            newx = Double.parseDouble(parts[2]);
                            newy = Double.parseDouble(parts[3]);
                        } else {
                            MapNode mn = rmap.getNode(parts[2]);
                            newx = mn.x;
                            newy = mn.y;
                        }
                        goPlaces(id,newx,newy);
                    }else if(key.equals("SHOW_MSG")){
                        showMessage();
                    }else if(key.equals("SHOW_MSG_CONFIRM")){
                        showConfirm(id);
                    }else if (key.equals("GETPOS")) {
                        sendPos(id);
                    }else{
                        System.out.print("Unknown command");
                    }
                }
            }catch(Exception e){
                System.err.println(e);
            }
            finally{
                try{
                    sock.close();
                }catch(Exception e){}
            }
        }
    }
}
