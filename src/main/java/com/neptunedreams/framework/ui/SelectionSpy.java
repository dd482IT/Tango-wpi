package com.neptunedreams.framework.ui;

import java.awt.DefaultKeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.FocusManager;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Keeps an eye on the selected text and the focused text component.
 * <p>
 *   This class also responds to listeners when the selection changes or the focus moves in or out of text components.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 4:18 PM
 *
 * @see FocusInTextFieldListener
 * @see SelectionExistsListener
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"Singleton", "UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "RedundantSuppression"})
public enum SelectionSpy implements CaretListener {
  /**
   * Singleton SelectionSpy instance
   */
  spy;

  @SuppressWarnings("NonFinalFieldInEnum")
  private @Nullable JTextComponent focusedTextComponent = null;
  @SuppressWarnings("NonFinalFieldInEnum")
  private String selectedText;
  
  private final List<SelectionExistsListener> selectionExistsListeners = new LinkedList<>();
  private final List<FocusInTextFieldListener> focusInTextFieldListeners = new LinkedList<>();

  SelectionSpy() {
    final DefaultKeyboardFocusManager focusManager = FocusManager.getCurrentManager();
    focusManager.addPropertyChangeListener("focusOwner", new FocusOwnerListener());
    selectedText=""; // Checker framework needs this initialized here instead of in declaration, probably because this is an enum.
  }
  
  @Override
  public void caretUpdate(final CaretEvent e) {
    if (e.getDot() == e.getMark()) {
      selectedText = "";
      fireSelectionExistsListeners(false);
    } else if (focusedTextComponent != null) { // if is always true, but required by the null checker
      selectedText = focusedTextComponent.getSelectedText();
      fireSelectionExistsListeners(true);
    }
  }
  
  private void fireSelectionExistsListeners(boolean value) {
    for (SelectionExistsListener listener : selectionExistsListeners) {
      listener.respond(value);
    }
  }
  
  private void fireFocusInTextFieldListeners() {
    boolean focusInTextField = focusedTextComponent != null;
    for (FocusInTextFieldListener listener : focusInTextFieldListeners) {
      listener.respond(focusInTextField);
    }
  }
  
  public boolean isFocusedTextFieldEditable() {
    return (focusedTextComponent != null) && focusedTextComponent.isEditable();
  }

  /**
   * Add a SelectionExistsListener to respond to changes in the selection
   * @param listener The listener
   */
  public void addSelectionExistsListener(SelectionExistsListener listener) {
    selectionExistsListeners.add(listener);
  }

  /**
   * Add a FocusInTextFieldListener to respond to changes in the focused text component.
   * @param listener The listener
   */
  public void addFocusInTextFieldListener(FocusInTextFieldListener listener) {
    focusInTextFieldListeners.add(listener);
  }

  /**
   * Removes the specified SelectionExistsListener.
   * @param listener The listener to remove
   */
  public void removeSelectionExistsListener(SelectionExistsListener listener) {
    selectionExistsListeners.remove(listener);
  }

  public String getSelectedText() {
    return selectedText;
  }

  /**
   * Replace the selected text in the FocusedTextComponent with the specified replacement text
   * @param replacement The replacement text
   */
  public void replaceSelectedText(String replacement) {
    JTextComponent focusedTextField = getFocusedTextComponent();
    if (focusedTextField != null) {
      int sStart = focusedTextField.getSelectionStart();
      int sEnd = focusedTextField.getSelectionEnd();
      int start;
      int end;
      if (sStart < sEnd) {
        start = sStart;
        end = sEnd;
      } else {
        end = sStart;
        start = sEnd;
      }
      StringBuilder text = new StringBuilder(focusedTextField.getText());
      text.replace(start, end, replacement);
      focusedTextField.setText(text.toString());
      int endOfReplacement =start + replacement.length();
      focusedTextField.setSelectionStart(endOfReplacement);
      focusedTextField.setSelectionEnd(endOfReplacement);
    }
  }

  /**
   * paste the clipboard text into the focused text component.
   */
  public void pasteIntoSelected() {
    if (focusedTextComponent != null) {
      focusedTextComponent.paste();
    }
  }

  /**
   * Respond to changes in the existence of selected text.
   */
  @FunctionalInterface
  public interface SelectionExistsListener {
    /**
     * Respond to changes if the selected text state.
     * @param exists true iff a selection exists and is not empty.
     */
    void respond(boolean exists);
  }

  /**
   * Respond to changes in the focus, when entering or leaving a JTextComponent. Ignores changes from one non-text component to another.
   */
  @FunctionalInterface
  public interface FocusInTextFieldListener {
    /**
     * Respond to changes in the focus in a JTextComponent
     * @param inTextField true iff the focus is in a JTextComponent
     */
    void respond(boolean inTextField);
  }
  
  private @Nullable JTextComponent getFocusedTextComponent() { return focusedTextComponent; }
  private void setFocusedTextComponent(@Nullable JTextComponent component) {
    focusedTextComponent = component;
  }
  
  private class FocusOwnerListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      assert SwingUtilities.isEventDispatchThread() 
          : String.format("Thread %s Daemon = %b", Thread.currentThread().getName(), Thread.currentThread().isDaemon());
      Object newValue = evt.getNewValue();
      boolean selectionExists;
      if (newValue instanceof JTextComponent) {
        JTextComponent textComponent = (JTextComponent) newValue;
        reassignCaretListener(textComponent);
        selectionExists = !selectedText.isEmpty();
      } else {
        reassignCaretListener(null);
        selectionExists = false;
      }
      spy.fireFocusInTextFieldListeners();
      spy.fireSelectionExistsListeners(selectionExists);
    }

    /**
     * This keeps the CaretListener on the component with the focus.
     * It removes the caret listener from a textComponent that just lost the focus. 
     * Also, it adds the CaretListener to the textComponent that gained focus.
     * It's careful to check if either of these exist.
     * @param textComponent The JTextComponent that gained the focus, or null if a JTextComponent did not gain the focus.
     */
    private void reassignCaretListener(@Nullable JTextComponent textComponent) {
      SelectionSpy sSpy = SelectionSpy.spy;
      JTextComponent focusedComponent = sSpy.getFocusedTextComponent();
      if (focusedComponent != null) {
        focusedComponent.removeCaretListener(sSpy);
      }
      sSpy.setFocusedTextComponent(textComponent);
      
      if (textComponent != null) {
        textComponent.addCaretListener(sSpy);
      }
    }
  }
}
