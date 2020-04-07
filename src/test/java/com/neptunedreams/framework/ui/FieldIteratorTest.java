package com.neptunedreams.framework.ui;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/20
 * <p>Time: 11:22 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class FieldIteratorTest {
  @Test
  public void testFieldIterator() throws NoSuchFieldException, IllegalAccessException {
    List<JTextComponent> components = getTextComponents();
    System.out.println("setup complete");

    FieldIterator iterator = new FieldIterator(components, FieldIterator.Direction.FORWARD, "bravo", "foxtrot", "golf", "echo", "fox");
    System.out.println("Iterator Constructed");
    assertTrue(iterator.hasNext());
    assertFalse(iterator.hasPrevious());

    Field listIteratorField = FieldIterator.class.getDeclaredField("listIterator");
    listIteratorField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ListIterator<FieldIterator.SearchTermElement> privateIterator
        = (ListIterator<FieldIterator.SearchTermElement>) listIteratorField.get(iterator);
    if (privateIterator == null) {
      throw new NullPointerException("");
    }
    match(privateIterator.next(), "BRAVO", 6, 0);
    match(privateIterator.next(), "ECHO", 26, 0);
    match(privateIterator.next(), "FOX", 31, 0);
    match(privateIterator.next(), "FOXTROT", 31, 0);
    match(privateIterator.next(), "BRAVO", 0, 1);
    match(privateIterator.next(), "FOX", 14, 1);
    match(privateIterator.next(), "GOLF", 18, 1);
    match(privateIterator.next(), "ECHO", 6, 2);
    match(privateIterator.next(), "FOX", 11, 2);
    match(privateIterator.next(), "FOX", 20, 2);
    match(privateIterator.next(), "FOXTROT", 20, 2);
    match(privateIterator.next(), "FOX", 0, 3);
    assertFalse(privateIterator.hasNext());
//    while (privateIterator.hasNext()) {
//      FieldIterator.SearchTermElement next = privateIterator.next();
////      int componentIndex = next.getComponentIndex();
////      String source = components.get(componentIndex).getText();
////      System.out.printf("Found %8s in field %d at %2s: %s%n", next.getTermUpperCase(), componentIndex, next.getCharIndex(), source);
//      System.out.printf("match(privateIterator.next(), \"%s\", %d, %d);%n", next.getTermUpperCase(), next.getCharIndex(), next.getComponentIndex());
//    }
  }

  @NotNull
  private List<JTextComponent> getTextComponents() {
    JTextField f1 = new JTextField();
    JTextField f2 = new JTextField();
    JTextField f3 = new JTextField();
    JTextField f4 = new JTextField();
    f1.setText("Alpha bravo charlie delta echo foxtrot");
    f2.setText("Bravo Charlie Fox Golf Hotel Trot");
    f3.setText("delta echo fox trot foxtrot hotel indigo");
    f4.setText("fox indigo Juliet, Romeo");
    return new LinkedList<>(Arrays.asList(f1, f2, f3, f4));
  }

  private void match(FieldIterator.SearchTermElement element, String term, int chIndex, int cmpIndex) {
    assertEquals(element.getTermUpperCase(), term);
    assertEquals(element.getCharIndex(), chIndex);
    assertEquals(element.getComponentIndex(), cmpIndex);
  }
  
  @Test
  public void testEmptyIterator() {
    List<JTextComponent> componentList = getTextComponents();
    FieldIterator fieldIterator = new FieldIterator(componentList, FieldIterator.Direction.FORWARD, "");
    assertFalse(fieldIterator.hasPrevious());
    assertFalse(fieldIterator.hasNext());
  }
}