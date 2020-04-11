package com.neptunedreams.framework.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static javax.swing.JComponent.*;

/**
 * SwipeView adds a swipe special effect to a Component. This draws a swipe-right or swipe-left effect on a chosen 
 * action. It also optionally supports a repeated action when the mouse is held down.
 * <p>
 * This class is very specific right now, but I hope to generalize it for other special effects later.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/18
 * <p>Time: 12:38 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class SwipeView<C extends JComponent> extends LayerUI<C> {
  /**
   * @param recordView The view to wrap with a swipe action
   * @param <J> The component type
   * @return A SwipeView that wraps the specified view
   */
  @SuppressWarnings("WeakerAccess")
  public static <J extends JComponent> SwipeView<J> wrap(J recordView) {
    JLayer<J> jLayer = new JLayer<>(recordView);
    final SwipeView<J> ui = new SwipeView<>(recordView, jLayer);
    jLayer.setUI(ui);
    return ui;
  }
  
  private final C liveComponent;
  private @Nullable Image priorScreen=null;
  private @Nullable Image upcomingScreen= null;
  private final JLayer<C> layer;
  
  private boolean isAnimating = false;
  private SwipeDirection swipeDirection = SwipeDirection.SWIPE_RIGHT;
  private static final int animationDurationMillis = 500;
  private static final int maxFrames = 15;
  // Calculated:
  @SuppressWarnings("FieldCanBeLocal")
  private static final int frameMillis = animationDurationMillis/maxFrames;
  private int frame = 0;
  
  private SwipeView(C view, JLayer<C> theLayer) {
    super();
    liveComponent = view;
    layer = theLayer;
  }

  @SuppressWarnings("WeakerAccess")
  public JLayer<C> getLayer() { return layer; }

  /**
   * Perform the specified operation with a swipe-right special effect. This is often used in an ActionListener:
   * <pre>
   *   first.addActionListener((e) -> swipeView.swipeRight(recordModel::goFirst));
   * </pre>
   * Here, the Action listener will perform a Swipe-right after executing the goFirst() method of recordModel.
   * @param operation The operation
   */
  @SuppressWarnings("WeakerAccess")
  public void swipeRight(Runnable operation) {
    swipe(operation,SwipeDirection.SWIPE_RIGHT);
  }

  /**
   * Perform the specified operation with a swipe-left special effect. This is often used in an ActionListener:
   * <pre>
   *   first.addActionListener((e) -> swipeView.swipeLeft(recordModel::goFirst));
   * </pre>
   * Here, the Action listener will perform a Swipe-Left after executing the goFirst() method of recordModel.
   *
   * @param operation The operation
   */
  @SuppressWarnings("WeakerAccess")
  public void swipeLeft(Runnable operation) {
    swipe(operation, SwipeDirection.SWIPE_LEFT);
  }

  private void swipe(Runnable operation, SwipeDirection swipeDirection) {
    prepareToAnimate(swipeDirection);
    operation.run();
    animate();
  }

  @Override
  public void paint(final Graphics g, final JComponent c) {
    if (isAnimating) {
      int xLimit = (c.getWidth() * frame) / maxFrames;
      if (swipeDirection == SwipeDirection.SWIPE_LEFT) {
        xLimit = c.getWidth() - xLimit;
      }
      int width = c.getWidth();
      int height = c.getHeight();

      assert upcomingScreen != null;
      assert priorScreen != null;
      Image pScreen = Objects.requireNonNull(priorScreen);
      Image uScreen = Objects.requireNonNull(upcomingScreen);
      if (swipeDirection == SwipeDirection.SWIPE_RIGHT) {
        g.drawImage(uScreen, 0, 0, xLimit, height, 0, 0, xLimit, height, c);
        g.drawImage(pScreen, xLimit, 0, width, height, xLimit, 0, width, height, c);
      } else {
        g.drawImage(uScreen, xLimit, 0, width, height, xLimit, 0, width, height, c);
        g.drawImage(pScreen, 0, 0, xLimit, height, 0, 0, xLimit, height, c);
      }
    } else {
      super.paint(g, c);
    }
  }
  
  private void prepareToAnimate(SwipeDirection swipeDirection) {
    this.swipeDirection = swipeDirection;
    isAnimating = true;
    frame = 0;

    // Save current state
    priorScreen = new BufferedImage(liveComponent.getWidth(), liveComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) priorScreen.getGraphics();
    liveComponent.paint(graphics2D);
    graphics2D.dispose();
  }

  private void animate() {
    Timer timer = new Timer(frameMillis, null);
    final ActionListener actionListener = (evt) -> {
      frame++;
      layer.repaint();
      if (frame == maxFrames) {
        frame = 0;
        isAnimating = false;
        timer.stop();
      }
    };
    timer.addActionListener(actionListener);
    upcomingScreen = new BufferedImage(liveComponent.getWidth(), liveComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) upcomingScreen.getGraphics();
    liveComponent.paint(graphics2D);
    graphics2D.dispose();
    
    timer.start();
  }

  /**
   * Assign a non-repeating action to the keystroke. The action will be performed on the keystroke, followed by a
   * swipe animation in the specified direction.<p>
   * For keystrokes that are already defined for JTextComponents, such as the arrow keys, use installRestrictedKeystrokeAction() 
   *
   * @param name           The name of the action. Must be unique
   * @param key            the key value, from KeyEvent, such as KeyEvent.VK_X
   * @param modifiers      The modifiers
   * @param operation      The operation to perform
   * @param swipeDirection The swipe direction
   * @see java.awt.event.KeyEvent
   */
  public void assignKeyStrokeAction(
      String name,
      int key,
      int modifiers,
      Runnable operation,
      SwipeDirection swipeDirection
  ) {
    Runnable fullOperation;
    fullOperation = createAnimatedAction(operation, swipeDirection);
    Keystrokes.installKeystrokeAction(Keystrokes.getLastAncestorOf(liveComponent), name, key, modifiers, fullOperation);
  }

  /**
   * Assign a non-repeating action to the keystroke. The action will be performed on the keystroke, followed by a 
   * swipe animation in the specified direction. It's "restricted" because it does not get invoked when the focus is
   * held by a JTextComponent or one of its subclasses. This is intended only for keystrokes that already have defined actions for
   * JTextComponents, such as the arrow keys.
   * @param name The name of the action. Must be unique
   * @param key the key value, from KeyEvent, such as KeyEvent.VK_X
   * @param modifiers The modifiers
   * @param operation The operation to perform
   * @param swipeDirection The swipe direction
   * @see java.awt.event.KeyEvent
   */
  public void assignRestrictedKeyStrokeAction(
      String name,
      int key,
      int modifiers,
      Runnable operation,
      SwipeDirection swipeDirection
  ) {
    Runnable fullOperation;
    fullOperation = createAnimatedAction(operation, swipeDirection);
    Keystrokes.installRestrictedKeystrokeAction(Keystrokes.getLastAncestorOf(liveComponent), name, key, modifiers, fullOperation);
  }

  /**
   * wraps an ordinary action with an animation, to create an animated action;
   * @param operation The operation to perform with the animation
   * @param swipeDirection The animation
   * @return An animated action
   */
  @NotNull
  public Runnable createAnimatedAction(final Runnable operation, final SwipeDirection swipeDirection) {
    final Runnable fullOperation;
    switch (swipeDirection) {
      case SWIPE_LEFT:
        fullOperation = () -> swipeLeft(operation);
        break;
      case SWIPE_RIGHT:
        fullOperation = () -> swipeRight(operation);
        break;
      default:
        throw new AssertionError(String.format("Unsupported Swipe Direction: %s", swipeDirection));
    }
    return fullOperation;
  }

  /**
   * Animate the specifIed action, using the specified swipe
   * @param view The view that is wrapped inside a SwipeView.
   * @param action The action to perform before the animation
   * @param direction the direction of the swipe
   * @throws IllegalArgumentException if the view was not first wrapped inside a SwipeView
   */
  public static void animateAction(JComponent view, Runnable action, SwipeDirection direction) {
    createAnimatedAction(view, action, direction).run();
  }

  /**
   * Animate the action for the specified view.
   * @param view the view that is wrapped inside a SwipeView
   * @param action The action to perform
   * @param direction The swipe direction
   * @throws IllegalArgumentException if the view was not first wrapped inside a SwipeView
   * @return A Runnable to perform the animated action.
   */
  @NotNull
  public static Runnable createAnimatedAction(final JComponent view, final Runnable action, final SwipeDirection direction) {
    JLayer<?> animatingAncestor = Keystrokes.getSpecificAncestorOf(view, JLayer.class);
    @SuppressWarnings("unchecked") SwipeView<JComponent> swipeView = (SwipeView<JComponent>) animatingAncestor.getUI();
    return swipeView.createAnimatedAction(action, direction);
  }

  /**
   * Assign a repeating action to the keystroke. It will repeat as long as the key is held down, with one swipe
   * per repeat. It's "restricted" because it does not get invoked when the focus is held by a JTextComponent or one of its subclasses. 
   * This is intended only for keystrokes that already have defined actions for JTextComponents, such as the arrow keys.
   * @param key The key value, from constants defined in KeyEvent, such as KeyEvent.VK_X
   * @param modifiers The modifiers
   * @param name The name, which should be unique
   * @param operation The operation to perform
   * @param swipeDirection the swipe direction
   * @see java.awt.event.KeyEvent
   */
  public void assignRestrictedRepeatingKeystrokeAction(
      String name,
      int key,
      int modifiers,
      Runnable operation,
      SwipeDirection swipeDirection
  ) {
    final KeyStrokeTracker keyStrokeTracker = new KeyStrokeTracker(operation, swipeDirection);
    KeyStroke pressedKeyStroke = KeyStroke.getKeyStroke(key, modifiers);
    KeyStroke releasedKeyStroke = KeyStroke.getKeyStroke(key, modifiers, true);
    JComponent lastAncestor = Keystrokes.getLastAncestorOf(liveComponent);
    InputMap inputMap = lastAncestor.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    @NonNls String pressedName = "pressed " + name;
    inputMap.put(pressedKeyStroke, pressedName);
    @NonNls String releasedName = "released " + name;
    inputMap.put(releasedKeyStroke, releasedName);
    ActionMap actionMap = lastAncestor.getActionMap();
    
    actionMap.put(pressedName, new AbstractAction(pressedName) {
      @Override
      public void actionPerformed(final ActionEvent e) {
        keyStrokeTracker.keyPressed();
      }

      @Override
      public AbstractAction clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone not supported for Action");
      }
    });
    
    actionMap.put(releasedName, new AbstractAction(releasedName) {
      @Override
      public void actionPerformed(final ActionEvent e) {
        keyStrokeTracker.keyReleased();
      }

      @Override
      public AbstractAction clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone not supported for Action");
      }
    });
  }

  /**
   * This lets you assign an action to a button that executes on mouseStillDown, but only when animation has completed.
   * This lets the user, say, hold an arrow button down and watch it page through the entries, animating each new page.
   * This method effectively replaces a call to addActionListener. Don't use that method if you're using this one.
   * <p>
   * Todo: Add Keystroke tracking
   * @param button The button to apply the mouseDown action to
   * @param operation The code to execute when the mouse is down.
   * @param swipeDirection The swipe direction
   */
  @SuppressWarnings("WeakerAccess")
  public void assignMouseDownAction(AbstractButton button, Runnable operation, SwipeDirection swipeDirection) {
    MouseTracker mouseTracker = new MouseTracker(operation, swipeDirection);
    button.addMouseListener(mouseTracker);
  }
  
  private class MouseTracker extends MouseAdapter {
    private boolean active = false;
    private boolean tracking = false;
    private final Timer timer = new Timer(frameMillis, null);

    MouseTracker(Runnable operation, SwipeDirection swipeDirection) {
      super();
      ActionListener listener = (e) -> {
        if (active && ! isAnimating) {
          swipe(operation, swipeDirection);
        }
      };
      timer.addActionListener(listener);
    }
    

    @Override
    public void mousePressed(final MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        active = true;
        tracking = true;
        timer.start();
      }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
      if (tracking) {
        active = false;
        timer.stop();
        tracking = false;
      }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
      if (tracking) {
        active = false;
      }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
      if (tracking) {
        active = true;
      }
    }
  }
  
  private class KeyStrokeTracker {
    private boolean active = false;
    private Timer timer = new Timer(frameMillis, null);
//    private final SwipeDirection direction;

    KeyStrokeTracker(Runnable operation, SwipeDirection swipeDirection) {
      ActionListener actionListener = (e) -> {
        if (active && !isAnimating) {
          swipe(operation, swipeDirection);
        }
      };
      timer.addActionListener(actionListener);
    }

    void keyPressed() {
      active = true;
      timer.start();
    }

    void keyReleased() {
      active = false;
      timer.stop();
    }
  }
}
