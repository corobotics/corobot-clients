
public class CorobotException extends Exception {
	public CorobotException(){
		super("Something is wrong with the corobot");
	}
	public CorobotException(String error){
		super(error);
	}
}
