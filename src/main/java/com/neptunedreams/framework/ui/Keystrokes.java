package com.neptunedreams.framework.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import static javax.swing.JComponent.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/5/20
 * <p>Time: 7:39 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum Keystrokes {
  ;

  /**
   * Finds the last ancestor of the provided component. This is the most distant ancestor that's still a subclass of JComponent, so it's
   * usually the main panel of the window.
   * @param component The component
   * @return The last ancestor of {@code component}.
   */
  public static JComponent getLastAncestorOf(JComponent component) {
    JComponent previousParent = component;
    Container parent = component.getParent();
    while (parent instanceof JComponent) {
      previousParent = (JComponent) parent;
      parent = previousParent.getParent();
    }
    return previousParent;
  }

  /**
   * Finds the first ancestor of the specified component whose class matches the specified ancestor class.
   * @param component The component whose ancestors get searched
   * @param ancestorClass The class of the ancestor to look for
   * @param <T> The type of the ancestor component
   * @return The instance of the ancestor class that holds the specified component
   * @throws IllegalArgumentException if no matching ancestor is found.
   */
  public static <T extends JComponent> T getSpecificAncestorOf(JComponent component, Class<T> ancestorClass) {
    JComponent previousParent = component;
    Container parent = previousParent.getParent();
    while (parent instanceof JComponent) {
      if (ancestorClass.isAssignableFrom(parent.getClass())) {
        @SuppressWarnings("unchecked")
        T ancestor = (T) parent;
        return ancestor;
      }
      previousParent = (JComponent) parent;
      parent = previousParent.getParent();
    }
    throw new IllegalArgumentException(String.format("Component of %s has no ancestor of %s", component.getClass(), ancestorClass));
  }

  /**
   * Install a restricted keystroke action on the specified component.<p>
   * For keystrokes that are already defined for JTextComponents, such as the arrow keys, use installRestrictedKeystrokeAction() 
   * @param view      The component to get the action
   * @param name      The name of the action, which should be unique.
   * @param key       The key constant defined in java.awt.event.KeyEvent
   * @param modifiers The modifiers used on the keystroke
   * @param theOp     The operation to perform when the key is pressed.
   */
  public static void installKeystrokeAction(JComponent view, final String name, int key, int modifiers, Runnable theOp) {
    KeystrokeAction keystrokeAction = new KeystrokeAction(name, theOp);
    KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
    InputMap inputMap = view.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = view.getActionMap();
    inputMap.put(keyStroke, name);
    actionMap.put(name, keystrokeAction);
  }

  /**
   * Install a restricted keystroke action on the specified component. It's "restricted" because it does not get invoked when the focus is
   * held by a JTextComponent or one of its subclasses. This is intended only for keystrokes that already have defined actions for
   * JTextComponents, such as the arrow keys.
   *
   * @param view      The component to get the action
   * @param name      The name of the action, which should be unique.
   * @param key       The key constant defined in java.awt.event.KeyEvent
   * @param modifiers The modifiers used on the keystroke
   * @param theOp     The operation to perform when the key is pressed.
   */
  public static void installRestrictedKeystrokeAction(JComponent view, final String name, int key, int modifiers, Runnable theOp) {
    RestrictedKeystrokeAction keystrokeAction = new RestrictedKeystrokeAction(name, theOp);
    KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
    InputMap inputMap = view.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = view.getActionMap();
    inputMap.put(keyStroke, name);
    actionMap.put(name, keystrokeAction);
  }

  /**
   * Prints the ancestry of the component. For debugging only
   * @param component The component
   */
  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void printAncestry(JComponent component) {
    System.out.printf("C: %s%n", component.getClass());
    Container parent = component.getParent();
    while (parent != null) {
      System.out.printf("-> %s%n", parent.getClass());
      parent = parent.getParent();
    }
  }

  private static final class KeystrokeAction extends AbstractAction {
    private final Runnable operation;

    private KeystrokeAction(final String name, final Runnable theOp) {
      super(name);
      operation = theOp;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      operation.run();
    }

    @Override
    public KeystrokeAction clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException("KeystrokeAction.clone() is not supported");
    }
  }

  private static final class RestrictedKeystrokeAction extends AbstractAction {
    private final Runnable operation;
    private FocusManager focusManager = FocusManager.getCurrentManager();


    private RestrictedKeystrokeAction(final String name, final Runnable theOp) {
      super(name);
      operation = theOp;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      final Component owner = focusManager.getPermanentFocusOwner();
      if ((!(owner instanceof JTextComponent)) || (((JTextComponent) owner).getText().isEmpty())) {
        operation.run();
      }
    }

    @Override
    public RestrictedKeystrokeAction clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException("KeystrokeAction.clone() is not supported");
    }
  }
}
