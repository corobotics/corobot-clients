import java.io.*;
import java.net.*;
public class VirtualRobot {
	private ServerSocket mine;
	private Socket sock;
	private PrintWriter out;
	private BufferedReader in;
	public VirtualRobot(){
		try{
			mine = new ServerSocket(15001);
			sock = mine.accept();
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}catch( IOException e) {
			e.printStackTrace();
		}
	}
	public void goPlaces(int msgId){
		try{
			Thread.sleep(1000);
			sendPos(msgId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendPos(int msgId){
		out.println(msgId + " POS 0 0 0" );
	}
	public void confirm(int msgId){
		out.println(msgId + " CONFIRM false");
	}
	public void showMessage(int msgId){
		System.out.print("Message");
	}
	public void showConfirm(int msgId){
		System.out.print("Confirm");
		confirm(msgId);
	}
	public void startReading(){

		new readerThread().start();
	}
	private class readerThread extends Thread{
		public void run(){
			for(;;){
				try{
					String given = in.readLine();
					int id = Integer.parseInt(given.split(" ")[0]);
					String key = given.split(" ")[1];
					if(key.equals("NAVTOLOC") || key.equals("NAVTOXY") || key.equals("GOTOLOC") || key.equals("GOTOXY") ){
						goPlaces(id);
					}else if(key.equals("SHOW_MSG")){
						showMessage(id);
					}else if(key.equals("SHOW_MSG_CONFIRM")){
						showConfirm(id);
					}else{
						System.out.print("Unknown command");
					}
				}catch(Exception e){
				}
				finally{
					try{
						sock.close();
					}catch(Exception e){}
				}
			}
		}
	}
}
