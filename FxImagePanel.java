package cloudtagger;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

/**
 *
 * @author Sascha Rasler
 */
public class FxImagePanel extends JFXPanel {

    protected ImageView imageView;
    protected JFXPanel tagPanel;
    protected FlowPane flowPane;
    protected ImageModel imageModel;
    protected Scene scene;
    protected ImageIcon icon;
    protected String url;

    public FxImagePanel(String url) {

        this.url = url;
        imageView = new ImageView(url);
        imageView.setPreserveRatio(true);
        this.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));

        Boolean portrait = imageView.getImage().getHeight() > imageView.getImage().getWidth();
        if (portrait) {
            imageView.setFitHeight(CloudUI.THUMBNAIL_MAX_DIM);
        } else {
            imageView.setFitWidth(CloudUI.THUMBNAIL_MAX_DIM);
        }
        tagPanel = new JFXPanel();
        flowPane = new FlowPane();
        flowPane.setPadding(new Insets(5, 5, 5, 5));
        flowPane.setStyle("-fx-background-color: #555555;");
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setVgap(5);
        flowPane.setHgap(5);
        flowPane.setPrefWrapLength(CloudUI.THUMBNAIL_MAX_DIM - 20);
        flowPane.getChildren().add(imageView);
        scene = new Scene(flowPane);

        setupDragAndDrop();
    }

    //See: http://stackoverflow.com/questions/24329395/app-hangs-up-or-not-on-fx-application-thread-occurs-during-app-activity
    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
        ArrayList<String> tags = imageModel.getTags();
        Platform.runLater(() -> {
            for (String tag : tags) {
                FxTag newTag = new FxTag(tag, true);
                newTag.setParentImagePanel(this);
                flowPane.getChildren().add(newTag);
            }
            this.setScene(scene);
        });
    }

    
    public String getImageUrl(){
        return this.url;
    }
    
    public boolean containsTag(String tag){
        return this.imageModel.getTags().contains(tag);
    }

    private void setupDragAndDrop() {
        scene.setOnDragOver((DragEvent event) -> {

            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != scene
                    && event.getDragboard().hasString()) {
                /* allow for moving */
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        scene.setOnDragDropped((DragEvent event) -> {
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                FxTag importTag = new FxTag(db.getString(), true);
                flowPane.getChildren().add(importTag);
                importTag.setParentImagePanel(this);
                CloudUI.imageTagger.addTagToImage(url, db.getString());
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
