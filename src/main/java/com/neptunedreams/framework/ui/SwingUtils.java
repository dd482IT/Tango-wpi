package com.neptunedreams.framework.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 3:15 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SwingUtils {
  ;

  /**
   * Wrap the specified component into the east side (Actually, the line-end side) of a new JPanel.
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapEast(JComponent component) {
    return wrap(component, BorderLayout.LINE_END);
  }

  /**
   * Wrap the specified component into the wast side (Actually, the line-start side) of a new JPanel.
   *
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapWest(JComponent component) {
    return wrap(component, BorderLayout.LINE_START);
  }

  /**
   * Wrap the specified component into the north side (Actually, the page-start side) of a new JPanel.
   *
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapNorth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_START);
  }

  /**
   * Wrap the specified component into the south side (Actually, the page-end side) of a new JPanel.
   *
   * @param component The component to wrap
   * @return The containing JPanel
   */
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

  /**
   * Configure a JTextArea for prose text and wrap it inside a JScrollPane that is set up for vertical scrolling and
   * horizontal text wrapping the the component's width. This also sets the wordWrapStyle and lineWrap properties.
   * @param wrappedField The JTextArea to configure and wrap
   * @return a JScrollPane wrapping the now configured text area.
   */
  public static JComponent scrollArea(JTextArea wrappedField) {
    wrappedField.setWrapStyleWord(true);
    wrappedField.setLineWrap(true);
    return verticalScroll(wrappedField);
  }

  /**
   * Wrap the specified component inside a JScrollPane, configuring it to have a vertical scrollbar but no horizontal 
   * scrollbar. This is the best configuration for multi-line text components like JTextArea, which are designed to 
   * wrap text to the current size under these conditions. To configure a JTextArea properly, you should really call 
   * scrollArea instead, which calls this one, but makes more useful changes.
   * @param wrapped The component to wrap.
   * @return the JScrollPane that wraps the component.
   * @see #scrollArea(JTextArea) 
   */
  public static JComponent verticalScroll(JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }

  /**
   * Removes the border on the specified component by calling {@code setBorder(null)}.
   * The JComponent.setBorder method is improperly annotated. It puts @NonNull on the parameter, which is wrong. This
   * method works around that problem. It has to be its own method because you can't annotate a x.setBorder() call.
   *
   * @param component The component
   */
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

  /**
   * On the Mac, the AquaCaret will get installed. This caret has an annoying feature of selecting all the text on a
   * focus-gained event. If this isn't bad enough, it also fails to check temporary vs permanent focus gain, so it
   * gets triggered on a focused JTextComponent whenever a menu is released! This method removes the Aqua Caret and
   * installs a standard caret. It's only needed on the Mac, but it's safe to use on any platform.
   *
   * @param components The components to repair. This is usually a JTextField or JTextArea.
   */
  public static void installStandardCaret(JTextComponent... components) {
    installCustomCaret(DefaultCaret::new, components);
  }

  /**
   * Install a custom Caret, provided by the supplier or constructor, into the specified JTextComponents. 
   * @param supplier the Constructor to instantiate each Caret
   * @param components The components to get the new Caret.
   */
  public static void installCustomCaret(Supplier<? extends Caret> supplier, JTextComponent... components) {
    for (JTextComponent component : components) {
      Caret caret = supplier.get();
      replaceCaret(component, caret);
    }
  }

  /**
   * Installs the specified Caret into the JTextComponent, using the same blink-rate as the previous caret.
   * @param component The text component to get the new Caret
   * @param caret The new Caret to install
   */
  public static void replaceCaret(final JTextComponent component, final Caret caret) {
    final Caret priorCaret = component.getCaret();
    int blinkRate = priorCaret.getBlinkRate();
    if (priorCaret instanceof PropertyChangeListener) {
      // com.apple.laf.AquaCaret, the troublemaker, installs this listener which doesn't get removed when the Caret 
      // gets uninstalled.
      component.removePropertyChangeListener((PropertyChangeListener) priorCaret);
    }
    component.setCaret(caret);
    caret.setBlinkRate(blinkRate); // Starts the new caret blinking.
  }
}
