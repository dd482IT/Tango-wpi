package com.neptunedreams.framework.ui;

import java.awt.DefaultKeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.FocusManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 4:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"Singleton", "UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "RedundantSuppression"})
public enum SelectionSpy implements CaretListener {
  spy;
  @SuppressWarnings("FieldCanBeLocal")
  private final DefaultKeyboardFocusManager focusManager = FocusManager.getCurrentManager();
  @SuppressWarnings("NonFinalFieldInEnum")
  private @Nullable JTextComponent focusedComponent = null;
  @SuppressWarnings("NonFinalFieldInEnum")
  private String selectedText;
  
  private final List<SelectionExistsListener> selectionExistsListeners = new LinkedList<>();
  private final List<FocusInTextFieldListener> focusInTextFieldListeners = new LinkedList<>();

  SelectionSpy() {
    focusManager.addPropertyChangeListener("focusOwner", new PListener());
    selectedText=""; // Checker framework needs this initialized here instead of in declaration.
  }
  
  @Override
  public void caretUpdate(final CaretEvent e) {
    if (e.getDot() == e.getMark()) {
      selectedText = "";
      fireSelectionExistsListeners(false);
    } else if (focusedComponent != null) { // if is always true, but required by the null checker
      selectedText = focusedComponent.getSelectedText();
      fireSelectionExistsListeners(true);
    }
  }
  
  private void fireSelectionExistsListeners(boolean value) {
    for (SelectionExistsListener listener : selectionExistsListeners) {
      listener.respond(value);
    }
  }
  
  private void fireFocusInTextFieldListeners(boolean value) {
    boolean focusInTextField = focusedComponent != null;
    for (FocusInTextFieldListener listener : focusInTextFieldListeners) {
      listener.respond(focusInTextField);
    }
  }

  public void addSelectionExistsListener(SelectionExistsListener listener) {
    selectionExistsListeners.add(listener);
  }
  
  public void addFocusInTextFieldListener(FocusInTextFieldListener listener) {
    focusInTextFieldListeners.add(listener);
  }
  
  public void removeBooleanListener(SelectionExistsListener listener) {
    selectionExistsListeners.remove(listener);
  }

  public String getSelectedText() {
    return selectedText;
  }
  
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

  public void pasteIntoSelected(String text) {
    if (focusedComponent != null) {
      focusedComponent.paste();
    }
  }

  @FunctionalInterface
  public interface SelectionExistsListener {
    void respond(boolean exists);
  }

  @FunctionalInterface
  public interface FocusInTextFieldListener {
    void respond(boolean inTextField);
  }
  
  private @Nullable JTextComponent getFocusedTextComponent() { return focusedComponent; }
  private void setFocusedComponent(@Nullable JTextComponent component) { focusedComponent = component; }
  
  private static class PListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      Object newValue = evt.getNewValue();
      if (newValue instanceof JTextComponent) {
        reassignListener((JTextComponent) newValue);
        spy.fireFocusInTextFieldListeners(true);
      } else {
        reassignListener(null);
        spy.fireFocusInTextFieldListeners(false);
        spy.fireSelectionExistsListeners(false);
      }
    }

    private void reassignListener(@Nullable JTextComponent component) {
      SelectionSpy sSpy = SelectionSpy.spy;
      JTextComponent focusedComponent = sSpy.getFocusedTextComponent();
      if (focusedComponent != null) {
        focusedComponent.removeCaretListener(sSpy);
      }
      sSpy.setFocusedComponent(component);
      
      if (component != null) {
        component.addCaretListener(sSpy);
      }
    }
  }
}
