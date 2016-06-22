package corobot;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * The model for the Tour Guide Robot.
 *
 * Stores data concerning:
 * --different tour routes
 * --different voice actors
 * --current location on route
 * And also executes the //TODO audio
 *                      functionality.
 *
 * @author Dr. Zack Butler
 * @author Paul Galatic
 *
 */
public class TGR_Model extends Observable implements Runnable{

    //debuginfo
    private final String WAIT_MSG = "Waiting... ";
    //maximum number of stops (two less than this value)
    private final int TOTALSTOPS = 5;
    //number of audio clips to be given in
    //between stops
    private final int MISCAUDIO = 8;
    //voice actor IDs
    private final String VA_1 = "_alice.mp3";
    private final String VA_2 = "_daisy.mp3";
    //sleep time constants
    private final int SLEEP_SML = 1;
    private final int SLEEP_MED = 3;

    private final Media DEFAULT_MEDIA = new Media(Paths.get("resources/audio/null.mp3").toUri().toString());

    private boolean isTouring;
    private boolean isTravelling;
    private boolean isResetting;
    private boolean moveOn;
    private boolean hold;
    private boolean canPlayClip;
    private int stops;
    private int tourRoute;
    private int va;
    private int i;
    private String information;

    //holds audio specific to certain stops
    private TreeMap<Integer, String> audioMap;
    //holds audio to be given at any time
    private ArrayList<String> audioList;
    //plays audio
    private MediaPlayer player;
    //the Tour Guide Robot
    //private Robot TGR;
    //private Future TGR_STATUS;

    public TGR_Model(){

        hold = true;
        stops = 0;
        i = -1;
        information = "";
        audioMap = new TreeMap<Integer, String>();
        audioList = new ArrayList<String>();
        //TGR = new Robot();

        //TODO: MAKE THESE VARIABLES DYNAMIC
        //currently only one route / voice actor
        va = 1;
        tourRoute = 1;

        buildAudioMap();
        resetIterator();
        //buildAudioList();

    }

    /**Returns the robot!*/
    //public Robot getTGR() { return TGR; }

    /**Returns the 'current activity' of the robot.*/
    public String getInformation(){
        return information;
    }

    /**Returns whether the robot can play a clip.*/
    public boolean canPlayClip(){ return canPlayClip; }

    /**Returns whether or not the robot is
     * travelling between stops.*/
    public boolean isTravelling(){ return isTravelling; }

    /**Returns whether or not the robot has a
     * tour currently in progress.*/
    public boolean isTouring(){ return isTouring; }

    /**Reset flag; signals the model to reset.*/
    public void setResetting(boolean b){ isResetting = b; }

    /**Flag for robot to begin tour.*/
    public void setTouring(boolean b){ isTouring = b; }

    /**Flag for robot to /not/ move onto next
     * stop.*/
    public void setHold(boolean b){ hold = b; }

    /**Lets the robot know it can play a voice clip.*/
    public void setCanPlayClip(boolean b){ canPlayClip = b; }

    /**Sets the tour route. TODO: OPTIONS*/
    public void setTourRoute(int i){ tourRoute = i; }

    /**Sets the 'current activity' of the robot.*/
    public void setInfo(String s){ information = s; }

    /**Sets the 'current activity' of the robot,
     * relative to a specific stop.*/
    public void setInfoStop(int i){ information = "This is stop " + i + "."; }

    /**Returns the chosen voice actor.
     *
     * @param filename: whether or not to return
     *        the value as the end of a filename
     *        for usage in buildAudioMap() and
     *        buildAudioList()
     *
     * @return: String, determined by the current
     *        va value*/
    public String getVA(boolean filename) throws Exception{

        switch (va){
            case 0:
                throw new IndexOutOfBoundsException("Invalid Void Actor");
            case 1:
                if (filename){
                    return "_alice.mp3";
                }
                return "Alice";
            case 2:
                if (filename){
                    return "_daisy.mp3";
                }
                return "Daisy";
        }

        throw new UnexpectedException("Reached end of getVA()");

    }

    /**Sets / resets the model to an initial
     * state.*/
    public void init(){

        isTravelling = true;
        moveOn = true;

        setInfo("Getting my bearings...");
        setResetting(false);
        setTouring(true);
        setHold(false);

        setChanged();
        notifyObservers();

        playSound(audioMap.get(0));

    }

    /**The main loop for the tour. It waits
     * unless it has been flagged to proceed to
     * the next stop, or to begin a tour. Unless
     * it has been told to reset, it progresses
     * through each stop until there are none
     * left. At that point, it waits until it is
     * told to reset.
     *
     * //TODO: Return to default location?*/
    @Override
    public void run() {

        while(true){

            while (stops < TOTALSTOPS) {

                setChanged();
                notifyObservers();

                while (hold) {
                    checkReset();
                    debugPrint(WAIT_MSG);
                    try {
                        TimeUnit.SECONDS.sleep(SLEEP_SML);
                    } catch (InterruptedException e) {}
                }

                checkReset();
                nextStop();

                stops++;

            }

            information = "All stops have been visited!";

            while(!isResetting && isTouring) {

                try {
                    TimeUnit.SECONDS.sleep(SLEEP_SML);
                } catch (InterruptedException e) {}

                checkReset();
            }
        }

    }

    /**Currently simulates travelling to the
     * next stop, updating the GUI along the
     * way.*/
    private void nextStop(){

        try {

            isTravelling = true;
            setInfo("Travelling...");
            //TODO
            //TGR_STATUS = TGR.navigateToLocation("SWLounge");
            setChanged();
            notifyObservers();

            while (true) {
                try {
                    debugPrint("Not there yet!");
                    TimeUnit.SECONDS.sleep(SLEEP_SML);
                    break;
                } catch (InterruptedException e) {
                }
            }

            //EXECUTE UPON ARRIVAL
            isTravelling = false;
            setHold(true);
            setInfoStop(stops);
            debugPrint(information);
            if (canPlayClip){
                playSound(audioMap.get(iterator()));
            }
            setChanged();
            notifyObservers();

            //TODO mapexception
        }catch (NullPointerException n) {
            n.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**Flag for robot to move to the next stop.*/
    public void moveOn(){
        moveOn = true;
        hold = false;
    }

    /**Checks the resetting flag and executes
     * reset protocol if it's active.*/
    private void checkReset(){

        if (isResetting){
            stops = 0;
            setTouring(false);
            setResetting(false);
            player.stop();

            setChanged();
            notifyObservers();
        }

    }

    /**Builds the audio map based on what tour
     * route has been selected.*/
    private void buildAudioMap(){

        //TODO: COMPLETE STOP LIST
        //TODO: FILES AREN'T BEING FOUND

        try {
            switch (tourRoute) {
                case 0:
                    throw new IllegalArgumentException("Bad Tour Route");
                case 1:
                    //Hits all possible stops.
                    audioMap.put(iterator(), "resources/audio/intro_1" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3509" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3511" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3515" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3517" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3519" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3521" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3525" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3527" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3535" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3537" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3545" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3547" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3551" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3555" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3557" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3561" + getVA(true));
                    audioMap.put(iterator(), "resources/audio/off3573" + getVA(true));

            }

        }catch(Exception e){

            e.printStackTrace();
        }

    }

    /**Plays a given voice clip.
     *
     * If a clip is already playing, returns
     * false.*/
    private synchronized void playSound(String soundFile){

        setCanPlayClip(false);

        if (player == null){
            player = new MediaPlayer(DEFAULT_MEDIA);
        }

        Media sound = new Media(Paths.get(soundFile).toUri().toString());

        player = new MediaPlayer(sound);

        player.setOnEndOfMedia(new Runnable(){
            public void run(){
                setCanPlayClip(true);
                setChanged();
                notifyObservers();
            }
        });

        player.play();

    }

    /**Iterates a semi-static value.*/
    private int iterator(){
        i++;
        return i;
    }

    /**Resets the iterator.*/
    private void resetIterator(){
        i = 0;
    }

    /**Prints the message. For debugging
     * purposes in principle.*/
    public void debugPrint(String msg){
        System.out.println("DEBUG: " + msg);
    }

}
