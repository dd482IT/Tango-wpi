package com.neptunedreams.framework;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:24 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum ErrorReport {
  ;

  private static final AtomicBoolean isOptionPaneOpen = new AtomicBoolean(false);

  /**
   * Reports an exception to the user, using JOptionPane
   * @param operation The name of the operation that caused the error.
   * @param t The Exception or Error
   */
  public static void reportException(String operation, Throwable t) {
    //noinspection HardCodedStringLiteral
    String message = String.format("Error during %s:%n%s", operation, t.getMessage());
    t.printStackTrace();
    // We test if the OptionPane is already open, because opening the pane triggers events that could throw the same exception again,
    // if the exception is in the event processing code. (Yes, this has happened).
    //noinspection IfStatementWithNegatedCondition
    if (!isOptionPaneOpen.get()) {
      isOptionPaneOpen.set(true);
      //noinspection HardCodedStringLiteral
      JOptionPane.showMessageDialog( null, message, "Error", JOptionPane.ERROR_MESSAGE);
      isOptionPaneOpen.set(false);
    } else {
      //noinspection UseOfSystemOutOrSystemErr
      System.err.println("Extra Exception triggered by error window:"); //NON-NLS
      t.printStackTrace();
    }
  }
}
