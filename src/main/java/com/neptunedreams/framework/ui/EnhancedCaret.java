package com.neptunedreams.framework.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

/**
 * Swing components don't handle selectByWord the way most UI text components do. If you double-click on a word, they
 * will all select the entire word. But if you do a click-and-drag, most components will (a) select the entire clicked
 * word, and (b) extend the selection a word at a time as the user drags across the text. And if you double- click on a
 * word and follow that with a shift-click, most components will also extend the selection a word at a time.  Swing 
 * components handle a double-clicked word the standard way, but do not handle click-and-drag or shift-click correctly.
 * This caret, which replaces the standard DefaultCaret, fixes this.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/23/20
 * <p>Time: 10:58 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class EnhancedCaret extends DefaultCaret {
  // In the event of a double-click, these are the positions of the low end and high end of the word that was clicked.
  private int highMark;
  private int lowMark;
  private boolean selectingByWord = false; // true when the last selection was done by word.

  public EnhancedCaret() {
    super();
  }

  /**
   * Install this Caret into a JTextComponent. Carets may not be shared among multiple components.
   * @param component The component to use the EnhancedCaret.
   */
  public void installInto(JTextComponent component) {
    SwingUtils.replaceCaret(component, this);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (selectingByWord && isExtendSelection(e)) {
      // user is doing a shift-click. Construct a new MouseEvent that happened at one end of the word, and send that
      // to super.mousePressed().
      int newPos;
      int pos = getPos(e);
      try {
        if (pos > highMark) {
          newPos = Utilities.getWordEnd(getComponent(), pos);
          setDot(lowMark);
        } else if (pos < lowMark) {
          newPos = Utilities.getWordStart(getComponent(), pos);
          setDot(highMark);
        } else {
          if (getMark() == lowMark) {
            newPos = Utilities.getWordEnd(getComponent(), pos);
          } else {
            newPos = Utilities.getWordStart(getComponent(), pos);
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
      super.mousePressed(alternateEvent);
    } else  {
      selectingByWord = e.getClickCount() == 2;
      super.mousePressed(e); // let the system select the clicked word
      // save the low end of the selected word.
      lowMark = getMark();
      if (selectingByWord) {
        // User did a double-click...
        // They've selected the whole word. Record the high end.
        highMark = getDot();
      } else {
        // Not a double-click.
        highMark = lowMark;
      }
    }
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
    if (!selectingByWord) {
      super.mouseDragged(e);
    } else {
      // super.mouseDragged just calls moveDot() after getting the position. We can do the same thing...
      // There's no "setMark()" method. You can set the mark by calling setDot(). It sets both the mark and the dot to
      // the same place. Then you can call moveDot() to put the dot somewhere else.
      if ((!e.isConsumed()) && SwingUtilities.isLeftMouseButton(e)) {
        int pos = getPos(e);
        JTextComponent component = getComponent();
        try {
          if (pos > highMark) {
            int wordEnd = Utilities.getWordEnd(component, pos);
            setDot(lowMark);
            moveDot(wordEnd);
          } else if (pos < lowMark) {
            int wordStart = Utilities.getWordStart(component, pos);
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

  @SuppressWarnings({"CloneReturnsClassType", "UseOfClone"})
  @Override
  public Object clone() {
    return super.clone();
  }
}
