package cloudtagger;

/**
 *
 * @author Sascha Rasler
 */
import java.awt.Point;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class FxTag extends Group {

    private final static int HEIGHT = 25;
    private final static int IMAGEVIEW_HEIGHT = 18;
    private final static int FONT_SIZE = 14;
    private final static int IMAGEVIEW_FONT_SIZE = 12;
    private final static int H_BORDER = 20;
    private final static int IMAGEVIEW_H_BORDER = 15;
    private final static int H_ARC = 30;
    private final static int V_ARC = 20;
    private final static int FADEIN_DURATION = 30;
    private final static int FADEOUT_DURATION = 20;

    private final Text tagText;
    private final Rectangle tag;
    private final String name;
    private int width = 60;

    private final Color farTagColor = Color.rgb(255, 255, 102);
    private final Color midTagColor = Color.rgb(128, 255, 0);
    private final Color closeTagColor = Color.rgb(0, 204, 0);
    private Color currentColor;

    private EventHandler mouseEnter;
    private EventHandler mouseExit;
    private FxImagePanel parantPanel;

    private boolean isSelected = false;
    private boolean isMainTag = false;

    public FxTag(String name, boolean isImageView) {

        this.name = name;
        tag = new Rectangle();
        tagText = new Text();
        setup(isImageView);

        // for JavaFx Elements (i.e. other Cloud or Image)
        this.setOnDragDetected((MouseEvent event) -> {
            /* allow MOVE transfer mode */
            Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();
            content.putString(name);
            db.setContent(content);
            event.consume();
        });
    }

    private void setup(boolean isImageView) {

        tag.setStroke(Color.BLACK);
        tag.setArcHeight(H_ARC);
        tag.setArcWidth(V_ARC);

        if (isImageView) {

            tag.setHeight(IMAGEVIEW_HEIGHT);
            tag.setFill(Color.DARKORANGE);
            tagText.setText(name);
            tagText.setFont(new Font(IMAGEVIEW_FONT_SIZE));
            tagText.setTextAlignment(TextAlignment.CENTER);
            //tagText.setFill(Color.WHITE);
            tagText.setY(tagText.getBaselineOffset());
            this.setWidth((int) tagText.getLayoutBounds().getWidth() + IMAGEVIEW_H_BORDER);

            setupContextMenu();

        } else {

            tag.setHeight(HEIGHT);
            tagText.setFont(new Font(FONT_SIZE));
            tagText.setText(name);
            tagText.setTextAlignment(TextAlignment.CENTER);
            tagText.setY(tagText.getBaselineOffset() * 1.1);
            this.setWidth((int) tagText.getLayoutBounds().getWidth() + H_BORDER);

            setupMouseOver();
            this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                if (!isSelected) {
                    isSelected = true;
                    CloudUI.imageSorter.displayImagesForTags(true, name);
                    tag.setFill(Color.DARKORANGE);
                    this.removeMouseHandlers();
                } else {
                    isSelected = false;
                    CloudUI.imageSorter.removeSelectedTag(name);
                    if (isMainTag) {
                        tag.setFill(Color.GREEN);
                        tagText.setFill(Color.WHITE);
                    } else {
                        tag.setFill(currentColor);
                        setupMouseOver();
                    }
                }
            });
        }
        this.getChildren().add(tag);
        this.getChildren().add(tagText);
    }

    private void setWidth(int width) {
        this.width = width;
        tagText.setWrappingWidth(width);
        tag.setWidth(width);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public String getTagName() {
        return name;
    }

    public Point getCenter() {
        Point center = new Point();
        center.x = this.getWidth() / 2;
        center.y = HEIGHT / 2;
        return center;
    }

    public void setAsMainTag() {

        this.isMainTag = true;
        this.removeMouseHandlers();
        this.tag.setFill(Color.GREEN);
        this.tagText.setFill(Color.WHITE);
    }

    public void fadeIn(double duration) {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.opacityProperty(), 0),
                        new KeyValue(this.scaleXProperty(), 2),
                        new KeyValue(this.scaleYProperty(), 2)
                ),
                new KeyFrame(new Duration(duration),
                        new KeyValue(this.opacityProperty(), 1),
                        new KeyValue(this.scaleXProperty(), 1),
                        new KeyValue(this.scaleYProperty(), 1)
                ));
        timeline.play();
    }

    public void animateTranslateTo(Point pos, double duration) {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.translateXProperty(), this.getTranslateX()),
                        new KeyValue(this.translateYProperty(), this.getTranslateY())
                ),
                new KeyFrame(new Duration(duration),
                        new KeyValue(this.translateXProperty(), pos.x),
                        new KeyValue(this.translateYProperty(), pos.y)
                ));
        timeline.play();
    }

    public void adjustColor(int pos) {

        if (!isSelected) {
            switch (pos) {
                case 0:
                case 1:
                case 2:
                case 3:
                    tag.setFill(farTagColor);
                    currentColor = farTagColor;
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                    tag.setFill(midTagColor);
                    currentColor = midTagColor;
                    break;
                case 12:
                case 13:
                case 14:
                case 15:
                    tag.setFill(closeTagColor);
                    currentColor = closeTagColor;
                    break;
            }
        }
    }

    /**
     * to avoid reaction to zoom processes
     */
    public void removeMouseHandlers() {

        this.removeEventFilter(MouseEvent.MOUSE_ENTERED, mouseEnter);
        this.removeEventFilter(MouseEvent.MOUSE_EXITED, mouseExit);
    }

    public void setParentImagePanel(FxImagePanel parent) {
        this.parantPanel = parent;
    }

    private void setupMouseOver() {

        mouseEnter = (EventHandler<InputEvent>) (InputEvent event) -> {
            Timeline timeline = new Timeline();
            this.toFront();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(this.scaleXProperty(), 1.0),
                            new KeyValue(this.scaleYProperty(), 1.0)
                    ),
                    new KeyFrame(new Duration(FADEIN_DURATION),
                            new KeyValue(this.scaleXProperty(), 1.1),
                            new KeyValue(this.scaleYProperty(), 1.1)
                    ));
            timeline.play();
            event.consume();
        };

        mouseExit = (EventHandler<InputEvent>) (InputEvent event) -> {
            Timeline timeline = new Timeline();
            this.toFront();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(this.scaleXProperty(), 1.1),
                            new KeyValue(this.scaleYProperty(), 1.1)
                    ),
                    new KeyFrame(new Duration(FADEOUT_DURATION),
                            new KeyValue(this.scaleXProperty(), 1.0),
                            new KeyValue(this.scaleYProperty(), 1.0)
                    ));
            timeline.play();
            event.consume();
        };
        this.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEnter);
        this.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExit);
    }

    private void setupContextMenu() {
        final ContextMenu cm = new ContextMenu();
        MenuItem cmItem1 = new MenuItem("Remove Tag");

        cmItem1.setOnAction((ActionEvent e) -> {
            FlowPane parent = (FlowPane) this.getParent();
            parent.getChildren().remove(this);
            CloudUI.imageTagger.removeTagFromImage(this.parantPanel.getImageUrl(), this.name);
        });

        cm.getItems().add(cmItem1);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                cm.show(this, e.getScreenX(), e.getScreenY());
            }
        });
    }
}
