package corobot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Observable;
import java.util.Observer;

/**
 * The display and view for the Tour Guide Robot.
 *
 * Designed to run on the robot itself.
 *
 * @author: Zack Butler
 * @author: Paul Galatic
 */
public class TGR_GUI extends Application implements Observer {

    private final String color_bg = "-fx-background-color: #F8F7ED;";
    private final String color_orange = "-fx-background-color: #F36E21;";

    private final double scene_pref_width = 1680.0;
    private final double scene_pref_height = 850.0;
    private final double largebtn_pref_width = 700.0;
    private final double largebtn_pref_height = 500.0;
    private final double smallbtn_pref_width = 250.0;
    private final double smallbtn_pref_height = 120.0;
    private final double displaycol_pref_width = 400.0;
    private final double displaycol_pref_height = 550.0;
    private final double default_hgap = 75.0;
    private final double default_vgap = 20.0;
    private final double small_font = 32.0;
    private final double medium_font = 72.0;
    private final double large_font = 96.0;

    private TGR_Model model;
    private StackPane sp;
    private String viewType;

    public TGR_GUI(){


        sp = new StackPane();
        viewType = "view_1";

    }

    /**Constructs an orange banner that goes
     * across the screen.*/
    private FlowPane getBanner(){

        FlowPane fp = new FlowPane();
        fp.setStyle(color_orange);
        fp.setMaxHeight(200);

        return fp;

    }

    /**Constructs the top of the screen, with:
     * --A Restart button
     * --A Next Stop button
     * --The title label*/
    private FlowPane getTop(){

        //TODO: REMOVE MAGIC NUMBERS

        FlowPane fp = new FlowPane();
        fp.getChildren().add(getTopLeftImage());

        Label l = new Label("Tour Guide Robot");

        if (model.isTouring()) {
            l.setFont(Font.font("Bold", medium_font));

            Button btn1 = new Button("Restart");
            btn1.setPrefSize(smallbtn_pref_width, smallbtn_pref_height);
            btn1.setFont(Font.font("Bold", small_font));
            btn1.setOnAction( e -> {
                btn1.setDisable(true);
                model.debugPrint("Firing reset method!");
                model.setResetting(true);
            } );

            Button btn2 = new Button("Next Stop");
            btn2.setPrefSize(smallbtn_pref_width, smallbtn_pref_height);
            btn2.setFont(Font.font("Bold", small_font));
            btn2.setOnAction( e -> {
                model.debugPrint("Firing next stop method!");
                model.moveOn();
            } );
            btn2.setDisable(false);
            if (model.isTravelling() || !model.canPlayClip()){
                btn2.setDisable(true);
            }

            VBox vb = new VBox(btn1, btn2);

            fp.getChildren().add(l);
            fp.getChildren().add(vb);
        }else{
            l.setFont(Font.font("Bold", large_font));
            fp.getChildren().add(l);
        }

        fp.setAlignment(Pos.TOP_CENTER);
        fp.setHgap(default_hgap);

        return fp;

    }

    /**Creates a large start button in the center
     * of the screen.*/
    private FlowPane getStartButton(){

        //TODO: MAGIC NUMBERS

        Button b1 = new Button("Start Tour");
        b1.setPrefSize(largebtn_pref_width, largebtn_pref_height);
        b1.setOnAction( e -> {
            b1.setDisable(true);
            model.init();
        } );
        b1.setFont(Font.font("Bold", large_font));

        FlowPane fp = new FlowPane(b1);
        fp.setAlignment(Pos.CENTER);

        return fp;

    }

    /**Creates an image in the top left of the
     * screen.*/
    private FlowPane getTopLeftImage(){

        Image img = new Image("/tiger.png.gif", 300, 200, false, false);

        ImageView iv = new ImageView(img);

        return new FlowPane(iv);

    }

    /**Assembles the majority of the GUI.*/
    private BorderPane assemble() {

        BorderPane bp = new BorderPane();
        bp.setStyle(color_bg);
        bp.setTop(getTop());
        bp.setCenter(getBanner());

        if (viewType.equals("view_2")){
            bp.setBottom(getDisplayGrid());
        }

        return bp;

    }

    /**Creates a display grid alligned toward the
     * bottom of the screen, with:
     * --A display slot for the live obstacle
     *   detection
     * --A display slot for audio captions
     * --A display slot for any relevant images*/
    private GridPane getDisplayGrid(){

        GridPane gp = new GridPane();

        Pane p1 = new Pane();
        p1.setPrefSize(displaycol_pref_width, displaycol_pref_height);
        p1.setStyle("-fx-border-color: black; -fx-background-color: white;");

        Pane p2 = new Pane();
        p2.setPrefSize(displaycol_pref_width, displaycol_pref_height);

        Pane p3 = new Pane();
        p3.setPrefSize(displaycol_pref_width, displaycol_pref_height);
        p3.setStyle("-fx-border-color: black; -fx-background-color: white;");

        gp.addRow(0, p1, p2, p3);

        Label l1 = new Label("Live Obstacle Detection");
        l1.setFont(Font.font("Bold", small_font));
        Label l2 = new Label(model.getInformation());
        l2.setFont(Font.font("Bold", small_font));
        Label l3 = new Label("TODO: Image related to stop");
        l3.setFont(Font.font("Bold", small_font));

        gp.addRow(1, l1, l2, l3);

        gp.addRow(2, getBanner());

        gp.setStyle(color_bg);
        gp.setAlignment(Pos.BOTTOM_CENTER);
        gp.setHgap(default_hgap);
        gp.setVgap(default_vgap);

        return gp;

    }

    /**Initializes the stage.*/
    public void start(Stage stage){

        model = new TGR_Model();
        model.addObserver(this);
        new Thread(model).start();

        sp.getChildren().add(0, assemble());
        sp.getChildren().add(1, getStartButton());

        sp.setPrefSize(scene_pref_width, scene_pref_height);

        Scene scene = new Scene(sp);

        stage.setTitle("Tour Guide Robot");
        stage.setScene(scene);
        //stage.setFullScreen(true);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                //model.getTGR().closeSocket();
                Platform.exit();
                System.exit(0);
            }
        });

    }

    @Override
    public void update(Observable obs, Object o){

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                    sp.getChildren().clear();
                    if (model.isTouring()) {
                        viewType = "view_2";
                        sp.getChildren().add(0, assemble());
                    }else{
                        viewType = "view_1";
                        sp.getChildren().add(0, assemble());
                        sp.getChildren().add(1, getStartButton());
                    }
                }
        });
    }



    public static void main(String[] args) {

        Application.launch();

    }


}
