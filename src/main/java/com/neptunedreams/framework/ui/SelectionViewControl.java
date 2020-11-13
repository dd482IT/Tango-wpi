package com.neptunedreams.framework.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This allows you to show selected text in a field without giving that field the focus. It allows a single field at a time. When you
 * turn it on in a new field, it turns it off in the previous field. This is intended for a search feature that hilights the found text
 * in one field at a time.<br>
 * <strong>Note:</strong> This has a memory leak danger. When the component window goes away, be sure to call showNothing() to let go of the previously
 * specified field.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/20
 * <p>Time: 7:25 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SelectionViewControl {
  /**
   * // Single instance
   */
  instance;
  
  private final Color selectionColor = Color.orange;
  private final Color defaultColor = new JTextField().getSelectionColor();
  
//  private final CaretListener restoreSelectionColor = new CaretListener() {
//    // Making this a lambda or method pointer triggers a bug in the nullness checker, which prevents building.
//    @Override
//    public void caretUpdate(final CaretEvent e) {
//      SelectionViewControl.this.updateTheCaret(e);
//    }
//  };
  
  private final FocusListener selectionColorRestorer = new FocusListener() {
    @Override
    public void focusGained(final FocusEvent e) { SelectionViewControl.this.updateTheCaret((JTextComponent) e.getComponent());}

    @Override
    public void focusLost(final FocusEvent e) { }
  };
  
  @SuppressWarnings("NonFinalFieldInEnum")
  @Nullable
  private JTextComponent activeComponent = null;

  /**
   * Turn on the visibility of the selected text in the specified field. Also turns off the same thing in the previously selected field
   * @param showInField The field with the selection
   */
  public static void showInComponent(JTextComponent showInField) {
    if (instance.activeComponent != null) {
      hideSelection(instance.activeComponent);
    }
    showInField.getCaret().setSelectionVisible(true);
    if (showInField.hasFocus()) {
      showInField.setSelectionColor(instance.defaultColor);
    } else {
      showInField.setSelectionColor(instance.selectionColor);
      showInField.addFocusListener(instance.selectionColorRestorer);
    }
    instance.activeComponent = showInField;
  }

  /**
   * Turn off the visibility of the selected text in the previously selected field.
   */
  public static void showNothing() {
    if (instance.activeComponent != null) {
      hideSelection(instance.activeComponent);
      instance.activeComponent = null;
    }
  }

  private static void hideSelection(JTextComponent component) {
    component.getCaret().setSelectionVisible(false);
    component.setSelectionColor(instance.defaultColor);
    component.removeFocusListener(instance.selectionColorRestorer);
  }

  /**
   * Adds a FocusListener to the field to call showNothing when the search field loses the focus.
   * @param searchField The field that specifies the search text.
   */
  public static void prepareSearchField(JTextComponent searchField) {
    SwingUtilities.invokeLater(() -> {
      FocusListener focusListener = new FocusListener() {
        @Override
        public void focusGained(final FocusEvent e) { }

        @Override
        public void focusLost(final FocusEvent e) {
          showNothing();
        }
      };
      searchField.addFocusListener(focusListener);
    });
  }

  private void updateTheCaret(JTextComponent textComponent) {
    textComponent.setSelectionColor(defaultColor);
  }
}
