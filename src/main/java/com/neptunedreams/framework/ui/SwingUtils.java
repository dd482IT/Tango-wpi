package com.neptunedreams.framework.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 3:15 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SwingUtils {
  ;

  public static JPanel wrapEast(JComponent component) {
    return wrap(component, BorderLayout.LINE_END);
  }
  
  public static JPanel wrapWest(JComponent component) {
    return wrap(component, BorderLayout.LINE_START);
  }

  public static JPanel wrapNorth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_START);
  }

  public static JPanel wrapSouth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_END);
  }
  
  private static JPanel wrap(JComponent component, String direction) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(component, direction);
    return wrapper;
  }

  public static JPanel wrapCenter(JComponent component) {
    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    flowPanel.add(component);
    return flowPanel;
  }

  public static JComponent scrollArea(JTextArea wrappedField) {
    wrappedField.setWrapStyleWord(true);
    wrappedField.setLineWrap(true);
    return verticalScroll(wrappedField);
  }

  public static JComponent verticalScroll(JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }

  /**
   * Removes the border on the specified component by calling {@code setBorder(null)}.
   * The JComponent.setBorder method is improperly annotated. It puts @NonNull on the parameter, which is wrong. This
   * method works around that problem. It has to be its own method because you can't annoate a x.setBorder() call.
   *
   * @param component The component
   */
  @SuppressWarnings("argument.type.incompatible")
  public static void setNoBorder(JComponent component) {
    component.setBorder(null);
  }

  /**
   * Create a decorated JTextArea that cleans the clipboard before pasting data. "Cleaning the clipboard" means 
   * stripping out the extra blank lines inserted automatically when pasting html data into a plain-text object.
   * This works by calling ClipFix.htmlToText() before pasting any data. If the clipboard doesn't have html data,
   * this has no effect.
   * @param rows The number of rows
   * @param columns The number of columns
   * @return a decorated JTExtArea
   * @see ClipFix#htmlToText() 
   */
  public static JTextArea createClipboardCleaningTextArea(int rows, int columns) {
    return new JTextArea(rows, columns) {
      @Override
      public void paste() {
        // Fix html clipboard data
        ClipFix.htmlToText();
        super.paste();
      }
    };
  }

}
