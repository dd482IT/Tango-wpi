package com.neptunedreams.framework.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

/**
 * <p>Implements Standard rules for extending the selection, consistent with the standard behavior for extending the 
 * selection in all word processors, browsers, and other text editing tools, on all platforms. Without this, Swing's
 * behavior on extending the selection is inconsistent with all other text editing tools.
 * </p><p>
 * Swing components don't handle selectByWord the way most UI text components do. If you double-click on a word, they
 * will all select the entire word. But if you do a click-and-drag, most components will (a) select the entire clicked
 * word, and (b) extend the selection a word at a time as the user drags across the text. And if you double- click on a
 * word and follow that with a shift-click, most components will also extend the selection a word at a time.  Swing 
 * components handle a double-clicked word the standard way, but do not handle click-and-drag or shift-click correctly.
 * This caret, which replaces the standard DefaultCaret, fixes this.</p>
 * <p>Created by IntelliJ IDEA.</p>
 * <p>Date: 2/23/20</p>
 * <p>Time: 10:58 PM</p>
 *
 * @author Miguel Mu\u00f1oz
 */
public class StandardCaret extends DefaultCaret {
  // In the event of a double-click, these are the positions of the low end and high end of the word that was clicked.
  private int highMark;
  private int lowMark;
  private boolean selectingByWord = false; // true when the last selection was done by word.
  private boolean selectingByRow = false; // true when the last selection was done by paragraph. 

  /**
   * Instantiate an EnhancedCaret.
   */
  public StandardCaret() {
    super();
  }

  /**
   * Install this Caret into a JTextComponent. Carets may not be shared among multiple components.
   * @param component The component to use the EnhancedCaret.
   */
  public void installInto(JTextComponent component) {
    TangoUtils.replaceCaret(component, this);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    // if user is doing a shift-click. Construct a new MouseEvent that happened at one end of the word, and send that
    // to super.mousePressed().
    boolean isExtended = isExtendSelection(e);
    if (selectingByWord && isExtended) {
      MouseEvent alternateEvent = getRevisedMouseEvent(e, Utilities::getWordStart, Utilities::getWordEnd);
      super.mousePressed(alternateEvent);
    } else if (selectingByRow && isExtended) {
      MouseEvent alternateEvent = getRevisedMouseEvent(e, Utilities::getRowStart, Utilities::getRowEnd);
      super.mousePressed(alternateEvent);
    } else  {
      if (!isExtended) {
        int clickCount = e.getClickCount();
        selectingByWord = clickCount == 2;
        selectingByRow = clickCount == 3;
      }
      super.mousePressed(e); // let the system select the clicked word
      // save the low end of the selected word.
      lowMark = getMark();
      if (selectingByWord || selectingByRow) {
        // User did a double- or triple-click...
        // They've selected the whole word. Record the high end.
        highMark = getDot();
      } else {
        // Not a double-click.
        highMark = lowMark;
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    super.mouseClicked(e);
    if (selectingByRow) {
      int mark = getMark();
      int dot = getDot();
      lowMark = Math.min(mark, dot);
      highMark = Math.max(mark, dot);
    }
  }

  private MouseEvent getRevisedMouseEvent(final MouseEvent e, final BiTextFunction getStart, final BiTextFunction getEnd) {
    int newPos;
    int pos = getPos(e);
    final JTextComponent textComponent = getComponent();
    try {
      if (pos > highMark) {
        newPos = getEnd.loc(textComponent, pos);
        setDot(lowMark);
      } else if (pos < lowMark) {
        newPos = getStart.loc(textComponent, pos);
        setDot(highMark);
      } else {
        if (getMark() == lowMark) {
          newPos = getEnd.loc(textComponent, pos);
        } else {
          newPos = getStart.loc(textComponent, pos);
        }
        pos = -1; // ensure we make a new event
      }
    } catch (BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
    MouseEvent alternateEvent;
    if (newPos == pos) {
      alternateEvent = e;
    } else {
      alternateEvent = makeNewEvent(e, newPos);
    }
    return alternateEvent;
  }

  private boolean isExtendSelection(MouseEvent e) {
    // We extend the selection when the shift is down but control is not. Other modifiers don't matter.
    int modifiers = e.getModifiersEx();
    int shiftAndControlDownMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
    return (modifiers & shiftAndControlDownMask) == MouseEvent.SHIFT_DOWN_MASK;
  }

  @Override
  public void setDot(final int dot, final Position.Bias dotBias) {
    super.setDot(dot, dotBias);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if (!selectingByWord && !selectingByRow) {
      super.mouseDragged(e);
    } else {
      BiTextFunction getStart;
      BiTextFunction getEnd;
      if (selectingByWord) {
        getStart = Utilities::getWordStart;
        getEnd = Utilities::getWordEnd;
      } else {
        // selecting by paragraph
        getStart = Utilities::getRowStart;
        getEnd = Utilities::getRowEnd;
      }
      // super.mouseDragged just calls moveDot() after getting the position. We can do the same thing...
      // There's no "setMark()" method. You can set the mark by calling setDot(). It sets both the mark and the dot to
      // the same place. Then you can call moveDot() to put the dot somewhere else.
      if ((!e.isConsumed()) && SwingUtilities.isLeftMouseButton(e)) {
        int pos = getPos(e);
        JTextComponent component = getComponent();
        try {
          if (pos > highMark) {
            int wordEnd = getEnd.loc(component, pos);
            setDot(lowMark);
            moveDot(wordEnd);
          } else if (pos < lowMark) {
            int wordStart = getStart.loc(component, pos);
            setDot(wordStart); // Sets the mark, too
            moveDot(highMark);
          } else {
            setDot(lowMark);
            moveDot(highMark);
          }
        } catch (BadLocationException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private int getPos(final MouseEvent e) {
    JTextComponent component = getComponent();
    Point pt = new Point(e.getX(), e.getY());
    Position.Bias[] biasRet = new Position.Bias[1];
    return component.getUI().viewToModel(component, pt, biasRet);
  }
  
  private MouseEvent makeNewEvent(MouseEvent e, int pos) {
    JTextComponent component = getComponent();
    try {
      Rectangle rect = component.getUI().modelToView(component, pos);
      return new MouseEvent(
          component,
          e.getID(),
          e.getWhen(),
          e.getModifiers(),
          rect.x,
          rect.y,
          e.getClickCount(),
          e.isPopupTrigger(),
          e.getButton()
      );
    } catch (BadLocationException ev) {
      ev.printStackTrace();
      throw new IllegalStateException(ev);
    }
  }

  /**
   * Install a new StandardCaret into a JTextComponent, such as a JTextField or JTextArea, and starts the Caret blinking using the same
   * blink-rate as the previous Caret.
   * @param component The JTextComponent subclass
   */
  public static void installStandardCaret(JTextComponent component) {
    replaceCaret(component, new StandardCaret());
  }

  /**
   * Installs the specified Caret into the JTextComponent, and starts the Caret blinking using the same blink-rate as the previous Caret.
   *
   * @param component The text component to get the new Caret
   * @param caret     The new Caret to install
   */
  public static void replaceCaret(final JTextComponent component, final Caret caret) {
    final Caret priorCaret = component.getCaret();
    int blinkRate = priorCaret.getBlinkRate();
    if (priorCaret instanceof PropertyChangeListener) {
      // For example, com.apple.laf.AquaCaret, the troublemaker, installs this listener which doesn't get removed when the Caret 
      // gets uninstalled.
      component.removePropertyChangeListener((PropertyChangeListener) priorCaret);
    }
    component.setCaret(caret);
    caret.setBlinkRate(blinkRate); // Starts the new caret blinking.
  }

// For eventual use by a "select paragraph" feature:
//  private static final char NEW_LINE = '\n';
//  private static int getParagraphStart(JTextComponent component, int position) {
//    return component.getText().substring(0, position).lastIndexOf(NEW_LINE);
//  }
//  
//  private static int getParagraphEnd(JTextComponent component, int position) {
//    return component.getText().indexOf(NEW_LINE, position);
//  }

  @SuppressWarnings({"CloneReturnsClassType", "UseOfClone"})
  @Override
  public Object clone() {
    return super.clone();
  }
  
  @FunctionalInterface
  private interface BiTextFunction {
    int loc(JTextComponent component, int position) throws BadLocationException;
  }
}
