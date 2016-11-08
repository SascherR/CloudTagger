package cloudtagger;

import ch.rakudave.suggest.JSuggestField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Sascha Rasler
 */
public class CloudUI extends JFrame {

    //Constants:
    public static final int THUMBNAIL_MAX_DIM = 200;
    private static final String MAIN_TITLE = "CloudTagger ALPHA";
    public static final int FOLDERICON_MAX_DIM = 32;
    private static final int FOLDER_TREE_ROW_HEIGHT = 20;
    private static final int THUMBNAIL_ROW_GAP = 40;
    private static final int THUMBNAIL_COLUMN_GAP = 40;
    private static final int DIVIDER_LOCATION = 200;
    private static final int SLIDE_BORDER_WIDTH = 2;
    private static final int DIA_BORDER_WIDTH = 10;
    private static final int NUMBER_COLUMNS = 4;
    private static final Color IMAGE_PANE_BGCOLOR = new Color(199, 199, 199);
    private static final int SCROLL_SPEED = 40;

    private static final int CLOUDPANEL_HEIGHT = 200;
    private static final int CLOUDPANEL_WIDTH = 1280;
    private int mainWindowHeight = 700;

    // UI models:
    public static HashMap<String, FxImagePanel> displayedPanels;
    public static HashMap<String, FxImagePanel> loadedPanels;

    public static ImageSorter imageSorter;

    private ArrayList<ImageModel> loadedImages;
    private ArrayList<String> imageURLs;
    private ArrayList<String> imageNames;
    private ArrayList<String> allTags;
    //private ArrayList<String> injectedTags;

    //UI elements:
    private final JPanel motherPanel = new JPanel();
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JTextField newTagField = new JTextField();
    private JSuggestField searchTagField = new JSuggestField(this, null);

    private JTree folderTree = new JTree();
    private DefaultMutableTreeNode top;

    private final JScrollPane treeScroller = new JScrollPane(folderTree);
    private static final JPanel imagesPanel = new JPanel(new MigLayout("wrap " + NUMBER_COLUMNS + ",gap " + THUMBNAIL_COLUMN_GAP + " " + THUMBNAIL_ROW_GAP, "", "align bottom"));
    private final JButton importButton = new JButton("Import images");
    private final JButton tagSubmitButton = new JButton("Add new tag + compute");
    private final JButton tagSearchZoomButton = new JButton("Zoom to tag");
    private final JButton tagSearchNewButton = new JButton("Open new cloud for tag");
    private final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir", "."));
    private final ImageIcon defaultImageIcon;

    private FxCloud cloud1;
    private FxCloud cloud2;

    public static ImageTagger imageTagger;

    Action closeAction = new AbstractAction("close") {
        @Override
        public void actionPerformed(ActionEvent e) {
            // ESC = Exit without saving
            dispose();
            System.exit(0);
        }
    };

    public CloudUI() {

        this.setTitle(MAIN_TITLE);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        defaultImageIcon = new ImageIcon();
        imageSorter = new ImageSorter();
        //injectedTags = new ArrayList<>();
    }

    public void init() throws IOException {
        this.addWindowListener(new CloudWindowListener());

        int screenHeight = getGraphicsConfiguration().getBounds().height;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int maxHeight = screenHeight - (screenInsets.top + screenInsets.bottom); // Reserve space for task bar
        if (mainWindowHeight > maxHeight) {
            mainWindowHeight = maxHeight;
        }
        // Make sure ESC closes window
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JComponent pane = getLayeredPane();
        pane.registerKeyboardAction(closeAction, "close", ks, JComponent.WHEN_IN_FOCUSED_WINDOW);

        displayedPanels = new HashMap<>();
        loadedPanels = new HashMap<>();
        imageURLs = new ArrayList<>();
        imageNames = new ArrayList<>();
        loadedImages = new ArrayList<>();

        motherPanel.add(mainPanel);

        setupUI();
        this.add(motherPanel);
        this.pack();
        this.setVisible(true);

        if (getHeight() > maxHeight) {
            setSize(getWidth(), maxHeight);
            validate();
        }
    }

    /*temporary:*/
    public void injectTags(ArrayList<String> tagList) {
        /*
         injectedTags = tagList;
         for (TagModel tag : injectedTags) {
         System.out.println("injected Tags: " + tag.getTagName());
         }
         */
    }

    private void setupCloud(JPanel container) throws IOException {

        cloud1 = new FxCloud();
        cloud2 = new FxCloud();
        cloud2.setBGColor(javafx.scene.paint.Color.rgb(201, 173, 162));
        newTagField.setBackground(new Color(231, 203, 192));
        tagSubmitButton.setBackground(new Color(231, 203, 192));
        //tagSubmitButton.setContentAreaFilled(false);
        //tagSubmitButton.setOpaque(true);
        Platform.runLater(() -> {
            try {
                cloud1.generate("frog", 0);
            } catch (IOException ex) {
                Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            cloud2.prepareEmptyCloud();
        });

        container.add(cloud1);
        container.add(cloud2);
    }

    public void renderThumbnails() throws MalformedURLException, IOException {

        BufferedImage img = null;
        imageTagger = new ImageTagger();

        for (String url : imageURLs) {

            try {
                img = ImageIO.read(new File(url));
            } catch (IOException e) {
            }

            if (img != null) {
                String newUrl = url.replace("\\", "/");
                newUrl = "file:///" + newUrl;
                String fileName = (String) url.subSequence(url.lastIndexOf("\\") + 1, url.length());
                FxImagePanel imagePanel = new FxImagePanel(newUrl);
                ImageModel imageModel = new ImageModel(url);

                ArrayList<String> imageTags = imageTagger.getTagsForImage(fileName);
                System.out.println("loaded tags from image: " + fileName + " = " + imageTags);
                if (imageTags != null) {
                    for (String imageTag : imageTags) {
                        imageModel.addTag(imageTag);
                    }
                } else {
                    System.out.println("image has no written history, adding: " + fileName);
                    imageTagger.addImage(fileName);
                }

                imagePanel.setImageModel(imageModel);
                loadedImages.add(imageModel);

                imagesPanel.add(imagePanel, "align center");
                displayedPanels.put(fileName, imagePanel);
                loadedPanels.put(fileName, imagePanel);
                imagesPanel.updateUI();
            }
        }
        createImageNodes();
    }

    public static void refreshPanels() {

        imagesPanel.removeAll();
        Iterator it = CloudUI.displayedPanels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            FxImagePanel panel = (FxImagePanel) pair.getValue();
            imagesPanel.add(panel);
            it.remove(); // avoids a ConcurrentModificationException
        }
        imagesPanel.updateUI();
    }

    private void setupBottomPanel() throws IOException {

        JPanel cloudPanel = new JPanel();
        cloudPanel.setPreferredSize(new Dimension(CLOUDPANEL_WIDTH, CLOUDPANEL_HEIGHT));
        setupCloud(cloudPanel);
        cloudPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        mainPanel.add(cloudPanel, BorderLayout.SOUTH);
    }

    private void setupUI() throws IOException {
        imagesPanel.setBackground(IMAGE_PANE_BGCOLOR);
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(THUMBNAIL_ROW_GAP / 2, THUMBNAIL_COLUMN_GAP / 2, THUMBNAIL_ROW_GAP / 2, THUMBNAIL_COLUMN_GAP / 2));
        JScrollPane imageView = new JScrollPane(imagesPanel);
        imageView.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
        imageView.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, IMAGE_PANE_BGCOLOR));
        imageView.setPreferredSize(new Dimension((THUMBNAIL_COLUMN_GAP * (NUMBER_COLUMNS + 1) + DIA_BORDER_WIDTH * NUMBER_COLUMNS * 2 + SLIDE_BORDER_WIDTH * NUMBER_COLUMNS * 2 + THUMBNAIL_MAX_DIM * NUMBER_COLUMNS), mainWindowHeight));

        treeScroller.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, IMAGE_PANE_BGCOLOR));
        treeScroller.setPreferredSize(new Dimension(250, mainWindowHeight));

        setupBottomPanel();

        importButton.addActionListener((ActionEvent e) -> {

            FileFilter filter = new FileNameExtensionFilter("image files", "jpg", "png");
            fileChooser.setFileFilter(filter);
            fileChooser.setMultiSelectionEnabled(true);
            int returnVal = fileChooser.showOpenDialog(CloudUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                for (File file : files) {
                    imageURLs.add(file.getPath());
                    imageNames.add((String) file.getPath().subSequence(file.getPath().lastIndexOf("\\") + 1, file.getPath().length()));
                    //System.out.println("loaded: " + file.getPath().subSequence(file.getPath().lastIndexOf("\\") + 1, file.getPath().length()));
                }
                try {
                    renderThumbnails();
                } catch (MalformedURLException ex) {
                    Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                imageURLs.clear();
            }
        });

        tagSubmitButton.addActionListener((ActionEvent e) -> {
            //TODO:
            Platform.runLater(() -> {
                cloud2.clearCloud();
                try {
                    cloud2.generate(newTagField.getText(), 0);
                } catch (IOException ex) {
                    Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        });

        tagSearchZoomButton.addActionListener((ActionEvent e) -> {
            Platform.runLater(() -> {
                try {
                    if (allTags.contains(searchTagField.getText())) {
                        cloud1.zoomToTag(searchTagField.getText());
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Please enter an existing tag!", "Tag not found", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        });

        tagSearchNewButton.addActionListener((ActionEvent e) -> {
            Platform.runLater(() -> {
                try {
                    if (allTags.contains(searchTagField.getText())) {
                        cloud2.clearCloud();
                        cloud2.generate(searchTagField.getText(), 0);
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Please enter an existing tag!", "Tag not found", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        });

        JPanel folderView = new JPanel(new BorderLayout());
        folderView.add(treeScroller);
        setupFolderTree();
        folderView.add(importButton, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(imageView, c);

        JPanel textPanel = new JPanel(new FlowLayout());

        allTags = getAllTags();
        Vector v = new Vector<>(allTags);
        System.out.println(allTags);
        searchTagField = new JSuggestField(this, v);
        searchTagField.setPreferredSize(new Dimension(300, 24));
        newTagField.setPreferredSize(new Dimension(300, 24));
        TextPrompt newPrompt = new TextPrompt("Add a new tag: ", newTagField);
        TextPrompt searchPrompt = new TextPrompt("Search for tag: ", searchTagField);
        newPrompt.changeAlpha(0.5f);
        newPrompt.changeStyle(Font.ITALIC);
        searchPrompt.changeAlpha(0.5f);
        searchPrompt.changeStyle(Font.ITALIC);

        textPanel.add(searchTagField);
        tagSearchZoomButton.setPreferredSize(new Dimension(150, 24));
        tagSearchNewButton.setPreferredSize(new Dimension(150, 24));
        textPanel.add(tagSearchZoomButton);
        textPanel.add(tagSearchNewButton);
        textPanel.add(newTagField);
        tagSubmitButton.setPreferredSize(new Dimension(150, 24));
        textPanel.add(tagSubmitButton);
        JSplitPane vPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightPanel, textPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, folderView, vPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(DIVIDER_LOCATION);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.validate();
    }

    // see http://docs.oracle.com/javase/tutorial/uiswing/components/tree.html
    private void setupFolderTree() {
        top = new DefaultMutableTreeNode("Loaded Images");
        folderTree = new JTree(top);
        folderTree.setRowHeight(FOLDER_TREE_ROW_HEIGHT);
        folderTree.setCellRenderer(new CustomTreeCellRenderer());
        folderTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //folderTree.addTreeSelectionListener(this);
        folderTree.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        folderTree.setEditable(true);
        folderTree.setBackground(SystemColor.text);
        //folderTree.setSelectionPath(folderTree.getPathForRow(0));
        //folderTree.addKeyListener(new FacebookImporter.TreeKeyListener());
        folderTree.requestFocus();
        treeScroller.setViewportView(folderTree);
        //treeScroller.validate();
    }

    private void createImageNodes() {

        int i = 0;
        for (String url : imageURLs) {

            DefaultMutableTreeNode cloudImage = new DefaultMutableTreeNode(loadedImages.get(i));
            calcFolderIcon(url, loadedImages.get(i));
            top.add(cloudImage);
            i++;
        }
        folderTree.fireTreeCollapsed(folderTree.getPathForRow(0));
    }

    private void calcFolderIcon(String source, ImageModel cImage) {

        System.out.println(source);
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(source));
        } catch (IOException e) {
        }
        Image newThumbImg;
        if (img != null) {
            double aspectRatio = (double) img.getWidth() / (double) img.getHeight();
            if (aspectRatio > 1) {
                int newFolderIconHeight = (int) ((double) FOLDERICON_MAX_DIM / aspectRatio);
                newThumbImg = img.getScaledInstance(FOLDERICON_MAX_DIM, newFolderIconHeight, java.awt.Image.SCALE_FAST);
            } else {
                int newFolderIconWidth = (int) ((double) FOLDERICON_MAX_DIM * aspectRatio);
                newThumbImg = img.getScaledInstance(newFolderIconWidth, FOLDERICON_MAX_DIM, java.awt.Image.SCALE_FAST);
            }
            cImage.setNodeicon(new ImageIcon(newThumbImg));
        }
    }

    public ArrayList getLoadedImages() {
        return loadedImages;
    }

    private ArrayList<String> getAllTags() throws IOException {
        JSONParser jp = new JSONParser("distances.txt");
        return jp.getAllTagsNames();
    }

    private class CloudWindowListener extends WindowAdapter {

        /**
         * @source =
         * http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
         */
        @Override
        public void windowClosing(WindowEvent e) {

            if (JOptionPane.showConfirmDialog(CloudUI.this,
                    "Are you sure you want to close and save all changes?", "Save and quit?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                try {
                    if (imageTagger != null) {
                        imageTagger.writeAndClose();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                dispose();
                System.exit(0);
            }
        }
    }
}
