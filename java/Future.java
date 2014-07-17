import java.util.*;
import java.util.concurrent.Semaphore;

public class Future{
    private String[] data;
    private CorobotException error;
    private Thread running;
    private ArrayList<Callback> callbacks;
    private ArrayList<ErrorCallback> errors;
    private Semaphore awaitingIO;
    private boolean done;
    
    public Future(){
        data = null;
		error = null;
		done = false;
		awaitingIO = new Semaphore(0);

        running = new Thread(new responseWait());
		running.start();
		callbacks = new ArrayList<Callback>();
    }
    
    public Future pause() throws CorobotException{
    	try {
			running.join();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		if( error != null ){
			throw error;
		}
		return this;
	}
    public Future then(Callback call){
    	return then(call, null);
    }
    
    public Future then(Callback call, ErrorCallback error){
		if (call != null){
			callbacks.add(call);
		}		
		if (error != null){
			errors.add(error);
		}
		return this;
    }
    
    public String[] get(){
    	return data;
    }
    
    public boolean is_fufilled(){
    	return done;
    }
	protected void safe_call(Callback f){
		safe_call(f, null);
	}

	protected void safe_call(Callback f, String[] data){
		try{
			f.call(data);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	protected void safe_call(ErrorCallback f){
		safe_call(f, null);
	}
	protected void safe_call(ErrorCallback f, Exception e){
		try{
			f.call(e);
		}catch(Exception d){
			d.printStackTrace();
		}
	}
	protected void fulfilled(String[] data){
		this.data = data;
		Iterator<Callback> i = callbacks.iterator();
		while( i.hasNext() ){
			safe_call(i.next(), this.data);
		}
		awaitingIO.release();
	}
	protected void error_occured(CorobotException error){
		this.error = error;
		Iterator<ErrorCallback> i = errors.iterator();
		while (i.hasNext()){
			safe_call(i.next(), this.error);
		}
		awaitingIO.release();
	}
    private class responseWait implements Runnable{
    	public void run(){
    		try {
				awaitingIO.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			awaitingIO.release();
			done = true;
    	}
    }
}
