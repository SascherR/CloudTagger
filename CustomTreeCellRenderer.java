/*
 * inspired by: http://stackoverflow.com/questions/13746880/making-a-jtree-leaf-appear-as-an-empty-directory-and-not-a-file
 * & : http://www.java2s.com/Code/Java/Swing-JFC/CheckBoxNodeTreeSample.htm
 */
package cloudtagger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

    private final JPanel imageBox = new JPanel(new BorderLayout());
    private final JPanel folderBox = new JPanel(new BorderLayout());
    private final JLabel folderLabel = new JLabel("", null, RIGHT);
    private final JLabel imageLabel = new JLabel();

    private final Font rootFont = imageLabel.getFont().deriveFont(Font.BOLD, 13);
    private final Font otherFont = imageLabel.getFont().deriveFont(Font.PLAIN, 24);
    
    private static final Color highlight = Color.ORANGE;

    public CustomTreeCellRenderer() {
        
        Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
        folderLabel.setIcon(UIManager.getIcon("FileView.directoryIcon"));

        folderLabel.setOpaque(false);
        imageLabel.setOpaque(false);

        folderLabel.setFont(otherFont);
        imageLabel.setFont(otherFont);

        folderBox.setOpaque(false);
        imageBox.setOpaque(false);

        folderBox.add(folderLabel, BorderLayout.AFTER_LINE_ENDS);

        imageBox.add(imageLabel, BorderLayout.AFTER_LINE_ENDS);

        folderBox.validate();
        imageBox.validate();

        imageBox.setFocusable(false);
        folderBox.setFocusable(false);

        //folderLabel.setFocusable(false);
        //imageLabel.setFocusable(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.isRoot()) {
                setIcon(null);
                setFont(rootFont);
                setBackgroundSelectionColor(highlight);
                setForeground(Color.BLACK);
                setBorderSelectionColor(null);
            }
            if (selected) {
                folderLabel.setBackground(highlight);
                imageLabel.setBackground(highlight);
            } else {
                folderLabel.setBackground(UIManager.getColor("Tree.textBackground"));
                imageLabel.setBackground(UIManager.getColor("Tree.textBackground"));
            }
            //this.setBorder(border);
           if (node.getUserObject() instanceof ImageModel) {
                ImageModel cloudImage = (ImageModel) node.getUserObject();
                //imageLabel.setText("" + fbImage.getInternalId());
                imageLabel.setIcon(cloudImage.getNodeicon());
                return imageBox;
            }
        }
        return this;
    }

}
