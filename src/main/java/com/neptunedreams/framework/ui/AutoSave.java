package com.neptunedreams.framework.ui;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import com.neptunedreams.framework.task.RestartableTimer;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 6/19/20
 * <p>Time: 1:10 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class AutoSave implements AWTEventListener {

  private final RestartableTimer timer;

  @SuppressWarnings("argument.type.incompatible")
  private AutoSave(RecordController<?, ?, ?> controller) {
    timer = new RestartableTimer(60000L, controller::saveCurrentRecord);
    timer.start();
    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
  }

  @Override
  public void eventDispatched(final AWTEvent event) {
    KeyEvent keyEvent = (KeyEvent) event;
    if (!keyEvent.isActionKey()) {
      timer.restart();
    }
  }

  /**
   * Start the AutoSave
   * @param controller The controller to save the records
   */
  public static void engage(RecordController<?, ?, ?> controller) {
    //noinspection ResultOfObjectAllocationIgnored
    new AutoSave(controller);
  }
}
