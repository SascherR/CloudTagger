package cloudtagger;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javax.swing.BorderFactory;

/**
 *
 * @author Sascha Rasler
 */
public class FxCloud extends JFXPanel {

    private final static int CLOUD_WIDTH = 600;
    private final static int PANEL_HEIGHT = 190;
    private final static int BUTTONS_SPACING = 50;
    private final static int BUTTONS_WIDTH = 40;
    private final static int VBUTTONS_SPACING = 3;
    private final static int CLOUD_YOFFSET = 10;
    private final static int CLOUD_XOFFSET = 110;
    private final static int CENTER_TAGS_XOFFSET = 15;
    private final static int DEFAULT_CLOUD_TRANSFORM_DURATION = 400;

    private final static int NUM_TAGS = 96;
    private final static int LEVEL_LENGTH = 16;

    private double currentCloudTransformDuration;

    private HashMap<String, FxTag> loadedTags;
    private ArrayList<String> cornerTags;
    private List<String> tagsForLevel;

    private CloudSorter cloudSorter;
    private final CloudPositions tagPositions;

    private final Group parent;
    private final Group buttons;
    private Group cloud;
    private Group newZoom;
    private final Scene scene;

    private Text zoomFactorText;
    private Button clearBtn;
    private VBox zoomIndicator;
    private ArrayList<Rectangle> rAll;

    private String mainTag;
    private ArrayList<String> oldTags;
    //private int level = 0;
    private int zoomFactor = 1;
    private int index;
    private int newCloudIndex = 0;

    public FxCloud() {

        cloud = new Group();
        parent = new Group();
        scene = new Scene(parent, CLOUD_WIDTH + BUTTONS_SPACING, PANEL_HEIGHT, Color.LIGHTGRAY);
        buttons = new Group();
        //buttons.setS
        currentCloudTransformDuration = DEFAULT_CLOUD_TRANSFORM_DURATION;

        this.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
        tagPositions = new CloudPositions(CLOUD_WIDTH, PANEL_HEIGHT, LEVEL_LENGTH);

        scene.setOnDragOver((DragEvent event) -> {

            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != cloud
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
                FxTag importTag = new FxTag(db.getString(), false);
                try {
                    this.generate(importTag.getTagName(), this.newCloudIndex);
                } catch (IOException ex) {
                    Logger.getLogger(FxCloud.class.getName()).log(Level.SEVERE, null, ex);
                }
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);
            event.consume();
        });

        scene.setOnScroll((ScrollEvent t) -> {

            index += (t.getDeltaY() / 40) * zoomFactor;
            if (index <= NUM_TAGS - LEVEL_LENGTH && index >= 0) {
                try {
                    this.transformTo(index);
                } catch (IOException ex) {
                    Logger.getLogger(FxCloud.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (index < 0) {
                index = 0;
            } else if (index > NUM_TAGS - LEVEL_LENGTH) {
                index = NUM_TAGS - LEVEL_LENGTH;
            }
        });

        setupButtons();
        cloud.setTranslateX(CLOUD_XOFFSET);
        cloud.setTranslateY(CLOUD_YOFFSET);
        parent.getChildren().addAll(buttons, cloud);
        this.setScene(scene);
    }

    public final void generate(String mainTag, int index) throws IOException {

        long start = System.currentTimeMillis();
        this.mainTag = mainTag;
        this.index = index;
        cloudSorter = new CloudSorter(mainTag);
        cornerTags = new ArrayList<>();
        oldTags = new ArrayList<>();
        loadedTags = new HashMap<>();

        if (newZoom != null) {
            parent.getChildren().remove(newZoom);
            newZoom = null;
            zoomFactorText.setText(newCloudIndex + "/" + (NUM_TAGS - LEVEL_LENGTH));
        }

        adjustZoomIndicator();
        positionMainTag(mainTag);
        clearBtn.setVisible(true);
        //tagsForLevel = cloudSorter.getTagsForLevel(level);
        //System.out.println("main tag: " + mainTag + " has following tags for level " + level + "/5:" + tagsForLevel);
        tagsForLevel = cloudSorter.getTagsFromIndex(index);
        System.out.println("main tag: " + mainTag + " has following tags from index " + index + "/95:" + tagsForLevel);

        positionCenterTags(true);
        positionCornerTags(true);
        int i = 0;
        for (String cornerTag : cornerTags) {
            positionCloseToCornerTags(cornerTag, i++, true);
        }
        System.out.println("generated cloud in: " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * @param tag
     * @param pos= index of tag position in cloud (for cloudpositions)
     * @param initial= true if first generation / false for transform
     */
    public void addTag(String tag, int pos, boolean initial) {

        Platform.runLater(() -> {

            if (initial) {

                FxTag newTag = new FxTag(tag, false);
                newTag.fadeIn(DEFAULT_CLOUD_TRANSFORM_DURATION / 2);
                positionTag(newTag, pos, false);
                loadedTags.put(newTag.getTagName(), newTag);
                cloud.getChildren().add(newTag);

            } else {
                if (loadedTags.containsKey(tag)) {
                    positionTag(loadedTags.get(tag), pos, true);
                } else {
                    //System.out.println("adding new tag: " + tag);
                    FxTag newTag = new FxTag(tag, false);
                    positionTag(newTag, pos, false);
                    newTag.fadeIn(DEFAULT_CLOUD_TRANSFORM_DURATION);
                    loadedTags.put(newTag.getTagName(), newTag);
                    cloud.getChildren().add(newTag);
                }
            }
        });
    }

    private void positionMainTag(String tag) {

        Platform.runLater(() -> {

            FxTag mainFxTag = new FxTag(tag, false);
            mainFxTag.setAsMainTag();

            // setting Offset for positioning anchor:
            Point center = mainFxTag.getCenter();
            mainFxTag.setLayoutX(-center.x);

            mainFxTag.setTranslateX(tagPositions.getCenterTagPosition().x);
            mainFxTag.setTranslateY(tagPositions.getCenterTagPosition().y - mainFxTag.getHeight());
            loadedTags.put(mainFxTag.getTagName(), mainFxTag);
            cloud.getChildren().add(mainFxTag);
        });
    }

    private void positionCornerTags(boolean initial) throws IOException {

        int numCornerTags = 4;

        for (int counter = 0; counter < numCornerTags; counter++) {
            String cornerTag = tagsForLevel.get(0);
            this.addTag(cornerTag, counter, initial);
            tagsForLevel.remove(0);
            String oppositeCornerTag = cloudSorter.getFurthestTagFromSet(cornerTag, tagsForLevel);
            this.addTag(oppositeCornerTag, ++counter, initial);
            tagsForLevel.remove(oppositeCornerTag);
            cornerTags.add(cornerTag);
            cornerTags.add(oppositeCornerTag);
            //System.out.println("furthest tag for " + cornerTag + ": " + oppositeCornerTag);
        }
    }

    private void positionCenterTags(boolean initial) throws IOException {

        int numCenterTags = 4;
        for (int counter = 0; counter < numCenterTags; counter++) {
            String centerTag = tagsForLevel.get(tagsForLevel.size() - 1);
            this.addTag(centerTag, 12 + counter, initial);
            tagsForLevel.remove(tagsForLevel.size() - 1);
        }
    }

    private void positionCloseToCornerTags(String cornerTag, int pos, boolean initial) throws IOException {

        int order = pos + 4;

        String neighborTag = cloudSorter.getClosestTagFromSet(cornerTag, tagsForLevel);
         System.out.println("closest neighbors for " + cornerTag + " are: " + neighborTag);
        this.addTag(tagsForLevel.get(tagsForLevel.indexOf(neighborTag)), order, initial);
        tagsForLevel.remove(neighborTag);

        neighborTag = cloudSorter.getClosestTagFromSet(cornerTag, tagsForLevel);
        System.out.println("------- and: " + neighborTag);
        this.addTag(tagsForLevel.get(tagsForLevel.indexOf(neighborTag)), order + 4, initial);
        tagsForLevel.remove(neighborTag);
    }

    private void positionTag(FxTag tag, int order, boolean animate) {
        Point center = tag.getCenter();
        tag.setLayoutX(-center.x);
        tag.adjustColor(order);

        if (!animate) {
            if (order < 12) {
                tag.setTranslateX(tagPositions.getTagPostion(order).x);
                tag.setTranslateY(tagPositions.getTagPostion(order).y);
            } else if (order == 12) {
                // fourth closest Tag (above main tag)
                tag.setTranslateX(tagPositions.getTagPostion(order).x);
                tag.setTranslateY(tagPositions.getTagPostion(order).y + tag.getHeight());
            } else if (order == 13) {
                // left center Tag
                tag.setTranslateX(tagPositions.getTagPostion(order).x + CENTER_TAGS_XOFFSET);
                tag.setTranslateY(tagPositions.getTagPostion(order).y - tag.getHeight());
            } else if (order == 15) {
                // right center Tag
                tag.setTranslateX(tagPositions.getTagPostion(order).x - CENTER_TAGS_XOFFSET);
                tag.setTranslateY(tagPositions.getTagPostion(order).y - tag.getHeight());
            } else {
                tag.setTranslateX(tagPositions.getTagPostion(order).x);
                tag.setTranslateY(tagPositions.getTagPostion(order).y - tag.getHeight());
            }
        } else {
            Point newPos;
            if (order < 12) {
                newPos = new Point(tagPositions.getTagPostion(order).x, tagPositions.getTagPostion(order).y);
            } else if (order == 12) {
                // fourth closest Tag (above main tag)
                newPos = new Point(tagPositions.getTagPostion(order).x, (int) (tagPositions.getTagPostion(order).y + tag.getHeight()));
            } else if (order == 13) {
                // left center Tag
                newPos = new Point(tagPositions.getTagPostion(order).x + CENTER_TAGS_XOFFSET, (int) (tagPositions.getTagPostion(order).y - tag.getHeight()));
            } else if (order == 15) {
                // right center Tag
                newPos = new Point(tagPositions.getTagPostion(order).x - CENTER_TAGS_XOFFSET, (int) (tagPositions.getTagPostion(order).y - tag.getHeight()));
            } else {
                newPos = new Point(tagPositions.getTagPostion(order).x, (int) (tagPositions.getTagPostion(order).y - tag.getHeight()));
            }
            tag.animateTranslateTo(newPos, currentCloudTransformDuration);
        }
        //System.out.println("positioning: " + tag.getTagName() + " to x: " +tagPositions.getTagPostion(order).x );
    }

    private void setupButtons() {

        Button zoomIn = new Button("+");
        Button zoomOut = new Button("-");
        clearBtn = new Button("clear");
        clearBtn.setFont(new Font(11));
        zoomIn.setMaxWidth(Double.MAX_VALUE);
        zoomOut.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setPrefWidth(BUTTONS_WIDTH);

        final ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("x1");
        rb1.setScaleX(0.8);
        rb1.setScaleY(0.8);
        rb1.setToggleGroup(toggleGroup);
        rb1.setUserData(1);
        rb1.setSelected(true);
        RadioButton rb2 = new RadioButton("x2");
        rb2.setScaleX(0.8);
        rb2.setScaleY(0.8);
        rb2.setToggleGroup(toggleGroup);
        rb2.setUserData(2);
        RadioButton rb3 = new RadioButton("x4");
        rb3.setScaleX(0.8);
        rb3.setScaleY(0.8);
        rb3.setToggleGroup(toggleGroup);
        rb3.setUserData(4);

        toggleGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            if (toggleGroup.getSelectedToggle() != null) {
                zoomFactor = (int) toggleGroup.getSelectedToggle().getUserData();
                System.out.println("zoomfactor: " + zoomFactor);
            }
        });

        zoomIn.setOnAction((ActionEvent t) -> {
            index += zoomFactor;
            if (index <= NUM_TAGS - LEVEL_LENGTH) {
                try {
                    this.transformTo(index);
                } catch (IOException ex) {
                    Logger.getLogger(FxCloud.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                index -= zoomFactor;
            }
        });

        zoomOut.setOnAction((ActionEvent t) -> {
            index -= zoomFactor;
            if (index >= 0) {
                try {
                    this.transformTo(index);
                } catch (IOException ex) {
                    Logger.getLogger(FxCloud.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                index += zoomFactor;
            }
        });

        clearBtn.setOnAction((ActionEvent t) -> {
            cloud.getChildren().clear();
            clearBtn.setVisible(false);
            zoomFactorText.setText("0/" + (NUM_TAGS - LEVEL_LENGTH));
            if (newZoom == null) {
                prepareEmptyCloud();
            }
        });

        Text speed = new Text("speed:");
        speed.setTranslateX(3);
        speed.setFont(new Font(11));
        speed.setTextAlignment(TextAlignment.CENTER);
        zoomFactorText = new Text(index + "/" + (NUM_TAGS - LEVEL_LENGTH));
        zoomFactorText.setFont(new Font(10));
        zoomFactorText.setTranslateX(8);
        Text empty = new Text(" ");
        empty.setFont(new Font(2));
        Text empty2 = new Text(" ");
        empty2.setFont(new Font(2));

        VBox vbButtons = new VBox();
        vbButtons.setStyle("-fx-border-color: #999999;\n"
                + "    -fx-border-width: 1px;\n"
                + "-fx-background-color: whitesmoke;");
        vbButtons.setPrefHeight(PANEL_HEIGHT);
        vbButtons.setSpacing(VBUTTONS_SPACING);
        vbButtons.setTranslateX(-1);
        vbButtons.setTranslateY(-1);
        vbButtons.setPadding(new Insets(5, 5, 5, 5));

        zoomIndicator = setupZoomIndicator();
        adjustZoomIndicator();

        vbButtons.getChildren().addAll(speed, rb1, rb2, rb3, empty, zoomFactorText, zoomIndicator, empty2, clearBtn);
        buttons.getChildren().add(vbButtons);

    }

    private void transformTo(int index) throws IOException {

        adjustZoomIndicator();
        cornerTags.clear();
        oldTags.clear();

        tagsForLevel = cloudSorter.getTagsFromIndex(index);
        System.out.println("main tag: " + mainTag + " has following tags from index " + index + "/95:" + tagsForLevel);

        zoomFactorText.setText(index + "/" + (NUM_TAGS - LEVEL_LENGTH));
        compareLoadedTags();
        positionCenterTags(false);
        positionCornerTags(false);
        int i = 0;
        for (String cornerTag : cornerTags) {
            positionCloseToCornerTags(cornerTag, i++, false);
        }
        removeOldTags();
    }

    private void incrementalTransformTo(int toIndex, double duration) {

        final Animation animation = new Transition() {
            {
                setCycleDuration(new Duration(duration));
            }

            int currentIndex = 0;
            int startIndex = index;
            int stepsToTake = toIndex - index;
            double stepsPerIndex = 8;

            @Override
            protected void interpolate(double frac) {

                try {
                    //System.out.println("moving " + stepsToTake);
                    currentIndex = startIndex;
                    if (stepsToTake > 0) {
                        //stepsPerIndex = Math.ceil((double) stepsToTake / 3);
                        //System.out.println("steps per index: " + stepsPerIndex);
                        currentCloudTransformDuration = duration / ((double) stepsToTake / stepsPerIndex);
                        //System.out.println("++++++++++++current cylce duration: " + currentCloudTransformDuration);
                        int indexPerCyle = (int) Math.ceil(startIndex + (stepsToTake * (float) frac));
                        //System.out.println("indexPerCylce " + indexPerCyle);
                        if (startIndex + stepsPerIndex - 1 < indexPerCyle) {
                            currentIndex = indexPerCyle;
                            FxCloud.this.transformTo(currentIndex);
                            adjustZoomIndicator(currentIndex);
                        }
                    } else if (stepsToTake < 0) {
                        //stepsPerIndex = Math.ceil((double) -stepsToTake / 5);
                        //System.out.println("steps per index: " + stepsPerIndex);
                        currentCloudTransformDuration = duration / ((double) -stepsToTake / stepsPerIndex);
                        int indexPerCyle = (int) Math.ceil(startIndex + (stepsToTake * (float) frac));
                        //System.out.println("indexPerCylce " + indexPerCyle);
                        if (currentIndex + stepsPerIndex - 1 > indexPerCyle) {
                            currentIndex = indexPerCyle;
                            FxCloud.this.transformTo(currentIndex);
                            adjustZoomIndicator(currentIndex);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(FxCloud.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        System.out.println("Zooming to index: " + toIndex);
        animation.play();

        animation.setOnFinished((ActionEvent event) -> {
            currentCloudTransformDuration = DEFAULT_CLOUD_TRANSFORM_DURATION;
            index = toIndex;
            adjustZoomIndicator();
        });

    }

    private void removeOldTags() {

        for (String oldTag : oldTags) {
            FxTag tag = (FxTag) (cloud.getChildren().get(cloud.getChildren().indexOf(loadedTags.get(oldTag))));
            tag.removeMouseHandlers();
            //System.out.println("removing: " + oldTag);

            FadeTransition ft = new FadeTransition(Duration.millis(currentCloudTransformDuration), tag);
            ft.setFromValue(1.0);
            ft.setToValue(0);

            ScaleTransition st = new ScaleTransition(Duration.millis(currentCloudTransformDuration), tag);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(0.2);
            st.setToY(0.2);

            st.play();
            ft.play();
            ft.setOnFinished((ActionEvent event) -> {
                cloud.getChildren().remove(loadedTags.get(oldTag));
                loadedTags.remove(oldTag);
            });
        }
    }

    private void compareLoadedTags() {
        ObservableList<Node> allTags = cloud.getChildren();
        for (Node tag : allTags) {
            FxTag fTag = (FxTag) tag;
            if (!tagsForLevel.contains(fTag.getTagName())) {

                if (!fTag.getTagName().contains(mainTag)) {
                    oldTags.add(fTag.getTagName());
                }
            }
        }
    }

    public void prepareEmptyCloud() {

        newZoom = new Group();
        Text zoom = new Text("Select starting level: \n" + "(drag new tag here)");

        final ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("1/6");
        rb1.setToggleGroup(toggleGroup);
        rb1.setUserData(0);
        rb1.setSelected(true);
        RadioButton rb2 = new RadioButton("2/6");
        rb2.setToggleGroup(toggleGroup);
        rb2.setUserData(LEVEL_LENGTH);
        RadioButton rb3 = new RadioButton("3/6");
        rb3.setToggleGroup(toggleGroup);
        rb3.setUserData(2 * LEVEL_LENGTH);
        RadioButton rb4 = new RadioButton("4/6");
        rb4.setToggleGroup(toggleGroup);
        rb4.setUserData(3 * LEVEL_LENGTH);
        RadioButton rb5 = new RadioButton("5/6");
        rb5.setToggleGroup(toggleGroup);
        rb5.setUserData(4 * LEVEL_LENGTH);
        RadioButton rb6 = new RadioButton("6/6");
        rb6.setToggleGroup(toggleGroup);
        rb6.setUserData(5 * LEVEL_LENGTH);

        VBox vbButtons = new VBox();
        vbButtons.setSpacing(VBUTTONS_SPACING);
        vbButtons.setPadding(new Insets(5, 5, 5, 5));
        vbButtons.getChildren().addAll(rb1, rb2, rb3, rb4, rb5, rb6);
        HBox hZoom = new HBox();
        hZoom.setSpacing(VBUTTONS_SPACING);
        zoom.setTranslateY(5);
        hZoom.getChildren().addAll(zoom, vbButtons);
        newZoom.getChildren().addAll(hZoom);
        newZoom.setTranslateX(CLOUD_WIDTH / 3);
        newZoom.setTranslateY(CLOUD_YOFFSET * 3);
        parent.getChildren().addAll(newZoom);

        toggleGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            if (toggleGroup.getSelectedToggle() != null) {
                this.newCloudIndex = (int) toggleGroup.getSelectedToggle().getUserData();
            }
        });
    }

    private VBox setupZoomIndicator() {
        VBox v = new VBox();
        Color color = Color.LIGHTGRAY;
        v.setSpacing(3);
        v.setPadding(new Insets(2));
        double height = 3;
        Rectangle z1 = new Rectangle(4, height, color);
        Rectangle z2 = new Rectangle(6, height, color);
        Rectangle z3 = new Rectangle(8, height, color);
        Rectangle z4 = new Rectangle(10, height, color);
        Rectangle z5 = new Rectangle(12, height, color);
        Rectangle z6 = new Rectangle(14, height, color);
        Rectangle z7 = new Rectangle(18, height, color);
        rAll = new ArrayList<>();
        rAll.add(z1);
        rAll.add(z2);
        rAll.add(z3);
        rAll.add(z4);
        rAll.add(z5);
        rAll.add(z6);
        rAll.add(z7);
        v.getChildren().addAll(z7, z6, z5, z4, z3, z2, z1);
        v.setAlignment(Pos.CENTER);
        return v;
    }

    private void adjustZoomIndicator(int... currentindex) {
        int currentLevel = 0;
        if (index > 0) {
            if (currentindex.length>0) {
                currentLevel = (int) Math.floor((double) currentindex[0] / (double) LEVEL_LENGTH) + 1;
            } else {
                currentLevel = (int) Math.floor((double) index / (double) LEVEL_LENGTH) + 1;
            }
        }
        for (Rectangle r : rAll) {
            r.setFill(Color.LIGHTGRAY);
        }
        Rectangle r = rAll.get(currentLevel);
        r.setFill(Color.ORANGERED);
    }

    //default scope: package private
    void clearCloud() {
        cloud.getChildren().clear();
    }

    //default scope: package private
    void zoomToTag(String tag) throws IOException {
        int zoomToIndex = cloudSorter.getIndexOfTag(tag);
        if (zoomToIndex != -1) {
            if (zoomToIndex > NUM_TAGS - LEVEL_LENGTH) {
                zoomToIndex = NUM_TAGS - LEVEL_LENGTH;
            }
            this.incrementalTransformTo(zoomToIndex, 500);
        }
    }

    public void setBGColor(Color bg) {
        scene.setFill(bg);
    }
}
