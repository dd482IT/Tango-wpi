package com.neptunedreams.framework.ui;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import com.neptunedreams.framework.task.RestartableTimer;

import static java.awt.event.KeyEvent.*;

/**
 * Automatically saves the data after one minute (or other interval) since the last character was typed or deleted.
 * Note: This works for keyboard events and paste events. I'm not yet sure how to best handle paste events. Right
 * now it watches for the control-v or command-v keystroke. This doesn't work if paste is done from the menu, and 
 * probably fails in many locales that don't use Engish.
 * I tried listening for TextEvents, but I didn't see any. I may be able to handle it by tracking the focussed
 * component and listening to DocumentChanged events instead of KeyEvents, but I need to test this. I may also
 * respond to CaretEvents instead of KeyTyped events, since they would get sent any time the text changes, but they
 * also get sent any time the caret moves. Also, the toolkit doesn't monitor CaretEvents. This is probably not a
 * big deal, since any paste is likely to be accompanied by a lot of other KeyTyped events.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 6/19/20
 * <p>Time: 1:10 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class AutoSave implements AWTEventListener {

  /** One Minute */
  public static final int ONE_MINUTE = 60;
  private final RestartableTimer timer;
  private static final int ON_MASK = toExMask(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
  private static final int ALL_MASK = ALT_DOWN_MASK | SHIFT_DOWN_MASK | META_DOWN_MASK | CTRL_DOWN_MASK;

  @SuppressWarnings("argument.type.incompatible")
  private AutoSave(Runnable runnable, int seconds) {
    timer = new RestartableTimer(seconds * 1000L, runnable);
    timer.start();
    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
  }

  @Override
  public void eventDispatched(final AWTEvent event) {
    KeyEvent keyEvent = (KeyEvent) event;
    // This ignores modifier keys, function keys, arrow keys and other navigation keys. 
    // It catches typed characters as well as the backspace and delete key.
    final boolean pasteKey = isPasteKey(event);
    if ((keyEvent.getID() == KEY_TYPED) || pasteKey) {
      timer.restart();
    }
  }

  /**
   * Note that this may not work in another locale.
   * @param awtEvent An AWTEvent, which should be a keystroke
   * @return true if this is the paste operation, false otherwise.
   */
  @SuppressWarnings("MagicCharacter")
  private static boolean isPasteKey(AWTEvent awtEvent) {
    KeyEvent keyEvent = (KeyEvent) awtEvent;
    return (keyEvent.getID() == KEY_PRESSED)
        && (keyEvent.getKeyChar() == 'v')
        && ((keyEvent.getModifiersEx() & ALL_MASK) == ON_MASK);
  }

  private static int toExMask(int mask) {
    switch (mask) {
      case CTRL_MASK:
        return CTRL_DOWN_MASK;
      case SHIFT_MASK:
        return SHIFT_DOWN_MASK;
      case ALT_MASK:
        return ALT_DOWN_MASK;
      case META_MASK:
        return META_DOWN_MASK;
      case InputEvent.CTRL_DOWN_MASK:
      case InputEvent.SHIFT_DOWN_MASK:
      case InputEvent.ALT_DOWN_MASK:
      case InputEvent.META_DOWN_MASK:
        return mask;
      default:
        throw new IllegalArgumentException(String.format("Unknown Mask: %d = 0x%08x", mask, mask));
    }
  }

  /**
   * Start the AutoSave, setting the timer delay to 60 seconds.
   * @param saveMethod The controller to save the records
   */
  public static void engage(Runnable saveMethod) {
    engage(saveMethod, ONE_MINUTE);
  }

  /**
   * Start the AutoSave, using the specified 
   * @param saveMethod The method used to save the changes
   * @param seconds The delay, in seconds, before saving.
   */
  public static void engage(Runnable saveMethod, int seconds) {
    //noinspection ResultOfObjectAllocationIgnored
    new AutoSave(saveMethod, seconds);
  }
}
