package com.neptunedreams.framework.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Document;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/2/22
 * <p>Time: 2:00 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"NumericCastThatLosesPrecision", "HardCodedStringLiteral", "MagicNumber"})
public class ClearableTextField extends JPanel {
  private static final LineBorder buttonBorder = new LineBorder(Color.black, 1);
  @NonNull
  private final JTextField textField = new JTextField();
  private final JButton button = new JButton();
  public ClearableTextField() {
    super(new BorderLayout());
    install(textField, textField.getBackground());
  }

  public ClearableTextField(int columns) {
    super(new BorderLayout());
    textField.setColumns(columns);
    install(textField, textField.getBackground());
  }

  public ClearableTextField(String text, int columns) {
    super(new BorderLayout());
    textField.setText(text);
    textField.setColumns(columns);
    install(textField, textField.getBackground());
  }

  public ClearableTextField(String text) {
    super(new BorderLayout());
    textField.setText(text);
    install(textField, textField.getBackground());
  }

  public ClearableTextField(Document doc, String text, int columns) {
    super(new BorderLayout());
    textField.setDocument(doc);
    textField.setText(text);
    textField.setColumns(columns);
    install(textField, textField.getBackground());
  }

  public JTextField getTextField() {
    return textField;
  }

  @SuppressWarnings("method.invocation.invalid")
  private void install(@UnderInitialization ClearableTextField this, @NonNull JTextField tField, Color bg) {
    add(BorderLayout.CENTER, tField);
    add(BorderLayout.LINE_END, makeClearBox(tField, bg));
    final AncestorListener ancestorListener = new AncestorListener() {
      @Override
      public void ancestorAdded(final AncestorEvent event) {
        final Container ancestor = event.getAncestor();
        if (ancestor instanceof Window) {
          WindowListener windowListener = new WindowAdapter() {
            private void doRevalidate(Window w) {
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
  }
  
  private JButton makeClearBox(@UnderInitialization ClearableTextField this, @NonNull JTextField tField, Color bg) {
    final Insets bi = tField.getBorder().getBorderInsets(tField);
    MatteBorder matteBorder = new MatteBorder(bi.top, bi.left, bi.bottom, bi.right, bg);
    CompoundBorder fullBorder = new CompoundBorder(matteBorder, buttonBorder);
    button.setBorder(fullBorder);
    button.setIcon(makeXIcon(tField));
    button.setFocusable(false);
    DocumentListener documentListener = new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        process(e);
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        process(e);
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        process(e);
      }
      
      private void process(DocumentEvent e) {
        button.setEnabled(e.getDocument().getLength() > 0);
      }
    };
    tField.getDocument().addDocumentListener(documentListener);
    button.addActionListener(e -> tField.setText(""));
    button.setEnabled(false);
    return button;
  }
  
  private Icon makeXIcon(@UnderInitialization ClearableTextField this, @NonNull final JTextField tField) {
    return new Icon() {
      private int size = -1;
      @Override @Initialized
      public int getIconWidth() {
        if (size < 0) {
          Insets fi = tField.getBorder().getBorderInsets(tField); // field insets
          Insets bi = buttonBorder.getBorderInsets(button);       // button insets
          final int width = tField.getHeight() - (fi.left - bi.left) - (fi.right - bi.right);
          final long round = Math.round(width * 0.9);
          size = (int) round;
        }
        return size;
      }

      @Override
      @Initialized
      public int getIconHeight() {
        if (size < 0) {
          Insets fi = tField.getBorder().getBorderInsets(tField); // field insets
          Insets bi = buttonBorder.getBorderInsets(button);       // button insets
          final int height = tField.getHeight() - (fi.top - bi.top) - (fi.bottom - bi.bottom);
          final long round = Math.round(height * 0.9);
          size = (int) round;
        }
        return size;
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

  public static void main(String[] args) throws UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(new MetalLookAndFeel());
    ClearableTextField field = new ClearableTextField();
    JFrame frame = new JFrame("test");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.add(field, BorderLayout.PAGE_START);
    frame.add(new JTextArea(20, 80), BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }
}
