/*************
 * Starts a virtual robot
 * @author E. Klei Jul 2014
 */
public class VirtualRobotStart{
	public static void main(String args[]){
		if (args.length == 0) {
			VirtualRobot r = new VirtualRobot();
			r.startReading();
		}else if (args.length == 1){
			switch (args[0].toUpperCase()){
				case "T":
				case "TRUE":
					VirtualRobot r = new VirtualRobot(true);
					r.startReading();
			}
		}else{
			System.err.print("Could not recognize arguments.");
			System.out.println("USAGE: java VirtualRobotStart\nALT: java VirtualRobotStart T");
		}
	}
}
