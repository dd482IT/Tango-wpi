package com.neptunedreams.framework.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/2/22
 * <p>Time: 2:00 AM
 * 
 * TODO: Try this:
 * todo    1 Eliminate the Matte border
 * todo    2 Put the line border back
 * todo    3 Put the clear box in its own JPanel, with a BoxLayout, or maybe a Box, with vertical orientation. That way, it doesn't expand.
 * 
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"NumericCastThatLosesPrecision", "HardCodedStringLiteral", "MagicNumber", "UseOfSystemOutOrSystemErr", "RedundantSuppression"})
public class ClearableTextField extends JPanel {
  private static final LineBorder buttonBorder = new LineBorder(Color.lightGray, 1);
  @NonNull
  private final JTextField textField;
  private final JButton button = new JButton();

  /**
   * Wrap an existing JTextField inside a ClearableTextField. For now, this only works well with
   * newly constructed JTextFields that haven't been displayed yet.
   * @param textField The text field.
   */
  public ClearableTextField(@NotNull JTextField textField) {
    super(new BorderLayout());
    this.textField = textField;
    install(textField);
  }

  @NotNull
  public JTextField getTextField() {
    return textField;
  }

  @SuppressWarnings("method.invocation.invalid")
  private void install(@UnderInitialization ClearableTextField this, @NonNull JTextField tField) {
    add(BorderLayout.CENTER, tField);
    add(BorderLayout.LINE_END, makeClearBox(tField));
    final AncestorListener ancestorListener = new AncestorListener() {
      @Override
      public void ancestorAdded(final AncestorEvent event) {
        final Container ancestor = event.getAncestor();
        if (ancestor instanceof Window) {
          WindowListener windowListener = new WindowAdapter() {
            private void doRevalidate(Window w) {
              button.setOpaque(false);
              button.invalidate();
              w.revalidate();
            }
            @Override
            public void windowOpened(final WindowEvent e) {
              final Window window = e.getWindow();
              SwingUtilities.invokeLater(() -> doRevalidate(window));
            }
          };
          ((Window) ancestor).addWindowListener(windowListener);
          removeAncestorListener(this); // Generates method.invocation.invalid warning
        }
      }
      @Override public void ancestorRemoved(final AncestorEvent event) {}
      @Override public void ancestorMoved(final AncestorEvent event) {}
    
    };
    this.addAncestorListener(ancestorListener); // Generates method.invocation.invalid warning
    
    button.setToolTipText("Clear Search Box");
  }
  
  private JComponent makeClearBox(@UnderInitialization ClearableTextField this, @NonNull JTextField tField) {
    button.setBorder(buttonBorder);
    button.setIcon(makeXIcon(tField));
    button.setFocusable(false);
    DocumentListener documentListener = new DocumentListener() {
      @Override public void insertUpdate(final DocumentEvent e) { process(e); }
      @Override public void removeUpdate(final DocumentEvent e) { process(e); }
      @Override public void changedUpdate(final DocumentEvent e) { process(e); }
      
      private void process(DocumentEvent e) {
        button.setEnabled(e.getDocument().getLength() > 0);
      }
    };
    tField.getDocument().addDocumentListener(documentListener);
    button.addActionListener(e -> clear(tField));
    button.setEnabled(false);
    Box box = new Box(BoxLayout.Y_AXIS);
    box.add(Box.createVerticalGlue());
    box.add(button);
    box.add(Box.createVerticalGlue());
    return box;
  }
  
  private static void clear(JTextField tField) {
    tField.setText("");
    tField.requestFocus();
  }

  private Icon makeXIcon(@UnderInitialization ClearableTextField this, @NonNull final JTextField tField) {
    return new Icon() {
      private int size = -1;
      
      // Warning. Support for changing the font doesn't really work. 
      private final PropertyChangeListener pcl = evt -> {
        // replace previous clear box.
        add(BorderLayout.LINE_END, makeClearBox(tField));
      };
      {
        tField.addPropertyChangeListener("font", pcl);
      }

      private int calculateSize(String dim) {
        if (size <= 0) {
          Insets fi = tField.getBorder().getBorderInsets(tField); // field insets
          Insets bi = button.getBorder().getBorderInsets(button); // button insets
          final int newSize = tField.getHeight() - (fi.top - bi.top) - (fi.bottom - bi.bottom);
          if (newSize > 0) {
            size = newSize;
          }
        }
        return size;
      }

      @Override
      @Initialized
      public int getIconWidth() {
        return calculateSize("wd");
      }

      @Override
      @Initialized
      public int getIconHeight() {
        return calculateSize("ht");
      }

      @Override
      public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform savedTransform = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        float ht = getIconHeight();
        float half = ht/2.0f;
        g2.translate(x+half, y+half);
        g2.rotate(Math.PI/4.0);
        
        final float strokeWidth = Math.max(2.0f, ht * 0.1f);
        final float halfWidth = strokeWidth/2.0f;
        Rectangle2D horizontal = new Rectangle2D.Float(-half, -halfWidth, ht, strokeWidth);
        Rectangle2D vertical   = new Rectangle2D.Float(-halfWidth, -half, strokeWidth, ht);
        g2.setColor(c.isEnabled()? Color.black : Color.lightGray);
        g2.fill(horizontal);
        g2.fill(vertical);
        g2.setTransform(savedTransform);
        g2.dispose();
      }
    };
  }

  /**
   * For testing
   * @param args Args
   * @throws UnsupportedLookAndFeelException for L&F
   */
  public static void main(String[] args) throws UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(new MetalLookAndFeel());
    JTextField textField = new JTextField();
    ClearableTextField field = new ClearableTextField(textField);
    JFrame frame = new JFrame("test");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.add(field, BorderLayout.PAGE_START);
    frame.add(new JTextArea(20, 40), BorderLayout.CENTER);
    JButton button = new JButton("Change Font Size");
    button.addActionListener(e -> {
          Font font = textField.getFont();
          if (font.getSize() == 24) {
            font = font.deriveFont(12.0f);
          } else {
            font = font.deriveFont(24.0f);
          }
          textField.setFont(font);
          frame.getContentPane().revalidate();
        });
    frame.add(BorderLayout.PAGE_END, button);
    frame.pack();
    frame.setVisible(true);
  }
}
