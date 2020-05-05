package com.neptunedreams.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import com.neptunedreams.framework.ui.SwipeView;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 5/2/20
 * <p>Time: 10:25 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"JavaDoc", "HardcodedLineSeparator", "StringConcatenation", "HardCodedStringLiteral"})
public final class SwipeViewTest extends JPanel {
  private static final String text1 = "Demo of Swipe View.\n\nThe swipe button will toggle between two pages of text. It has a built-in " +
      "special effect, which is a swipe. When you hit the swipe button, it should flip between two pages of text. This worked fine on " +
      "the older displays, but for some reason, on a Retina display, the text briefly switches to low resolution as the swipe proceeds, " +
      "then switches back once it has finished. This code is written for retina displays. I don't know if it will work for the older, " +
      "low resolution displays.\n\nYou can watch it swipe by hitting the space bar or by clicking the swipe button.";
  private static final String text2 = "Demo of Swipe View.\n\nThis is the second page of the swipe-text demo. The change in resolution is " +
      "most easily noticed when watching the line at the top, which doesn't change as the swipe is performed.";
  private final SwipeView<TestView> swipeView;
  private final TestView testView;

  public static void main(String[] args) {
    JFrame frame = new JFrame("SwipeView demo");
    SwipeViewTest comp = new SwipeViewTest();
    comp.install();
    frame.add(comp);
    frame.setLocationByPlatform(true);
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }
  
  private boolean page1 = true;
  
  private SwipeViewTest() {
    super(new BorderLayout());
    testView = new TestView();
    swipeView = SwipeView.wrap(testView);
    add(BorderLayout.CENTER, swipeView.getLayer());
  }
  
  private void install() {
    JButton jButton = new JButton("Swipe");
    jButton.addActionListener(this::doSwipe);
    add(jButton, BorderLayout.PAGE_END);
    AncestorListener ancestorListener = new AncestorListener() {
      @Override
      public void ancestorAdded(final AncestorEvent event) {
        JComponent button = event.getComponent();
        button.requestFocus();
        button.removeAncestorListener(this);
      }

      @Override public void ancestorRemoved(final AncestorEvent event) { }
      @Override public void ancestorMoved(final AncestorEvent event) { }
    };
    jButton.addAncestorListener(ancestorListener);
  }

  private void doSwipe(ActionEvent ignored) {
    swipeView.swipeLeft(this::flipPage);
  }
  
  private void flipPage() {
    page1 = !page1;
    if (page1) {
      testView.setText(text1);
    } else {
      testView.setText(text2);
    }
  }
  
  private static class TestView extends JPanel {

    private final JTextArea textArea;

    TestView() {
      super(new BorderLayout());
      textArea = new JTextArea(20, 40);
      JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      textArea.setEditable(false);
      textArea.setText(text1);
      add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setText(String text) {
      textArea.setText(text);
    }
  }
}