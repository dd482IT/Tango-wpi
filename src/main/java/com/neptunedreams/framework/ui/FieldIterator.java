package com.neptunedreams.framework.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Used to search for text in a series of text components. Search takes place within a single database result.
 * <p> Note that the {@code hasNext()}, {@code hasPrevious()}, {@code goToNext()} and {@code goToPrevious()} don't behave they way they do
 * in ListIterator. In ListIterator, if I say listIterator.next(), followed by listIterator.previous(), I will get the same item as last
 * time. To avoid this, when we change direction, we throw away the first item in the new direction.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/4/20
 * <p>Time: 7:42 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class FieldIterator {
//  @MonotonicNonNull
  private final List<JTextComponent> componentList;
  private final ListIterator<SearchTermElement> listIterator;
  private Direction direction;

  /**
   * Iterates through all found strings on the current on-screen result 
   * @param componentList List of components to search
   * @param direction Direction to search initially
   * @param searchTerms Terms to search for
   */
  public FieldIterator(Collection<JTextComponent> componentList, Direction direction, String... searchTerms) {
    this.componentList = new ArrayList<>(componentList);
    final List<SearchTermElement> searchTermElements = new LinkedList<>(assembleIterator(searchTerms));
    listIterator = searchTermElements.listIterator();
    this.direction = direction;
    if (direction == Direction.BACKWARD) {
      // start at the end
      while (listIterator.hasNext()) {
        listIterator.next();
      }
    }
  }

  @RequiresNonNull("componentList")
  private Set<SearchTermElement> assembleIterator(@UnderInitialization FieldIterator this, String... terms) {
    // pack all Strings into a TreeSet to eliminate duplicates and pre-sort them by length
    TreeSet<String> allTerms = Arrays.stream(terms)
        .map(String::toUpperCase)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(String::length).thenComparing(String::toString))));
    int componentIndex = 0;
    Set<SearchTermElement> searchTermElements = new TreeSet<>();
    if (allTerms.isEmpty()) {
      // skip the searching!
      return searchTermElements;
    }
//    for (JTextComponent component: componentList) {
//      String cText = component.getText();
//      if (!cText.isEmpty()) {
//        System.out.printf("First text: <%s>%n", cText);
//        break;
//      }
//    }
    for (JTextComponent component: componentList) {
      String componentText = component.getText().toUpperCase();
      for (String term: allTerms) {
        int index = 0;
        while (index >= 0) {
          index = componentText.indexOf(term, index);
          if (index >= 0) {
            searchTermElements.add(new SearchTermElement(term, index, componentIndex));
            index++;
          }
        }
      }
      componentIndex++;
    }
    return searchTermElements;
  }

  /**
   * Returns true if another match is found in the current page. If the current direction is BACKWARD, sets it to Forward and skips past 
   * the first entry to avoid returning it twice in a row.  Note that this does not use the logic of a ListIterator.
   * @return true iff a next match exists on the current page.
   */
  public boolean hasNext() {
    if (direction == Direction.BACKWARD) {
      if (listIterator.hasNext()) {
        direction = Direction.FORWARD;
        if (listIterator.hasNext()) {
          listIterator.next();
        }
      }
    }
    return listIterator.hasNext();
  }

  /**
   * Returns true if a previous match is found in the current page. If the current direction is FORWARD, sets it to BACKWARD and skips past
   * the first entry to avoid returning it twice in a row. Note that this does not use the logic of a ListIterator.
   * @return true iff a previous match exists on the current page.
   */
  public boolean hasPrevious() {
    if (direction == Direction.FORWARD) {
      if (listIterator.hasPrevious()) {
        direction = Direction.BACKWARD;
        if (listIterator.hasPrevious()) {
          listIterator.previous();
        }
      }
    }
    return listIterator.hasPrevious();
  }

  /**
   * Goes to the next match on the page, selects it, and if necessary, scroll the field to the selected text. This should only be called
   * after a call to hasNext(), which will reverse the direction if necessary.
   */
  public void goToNext() {
    if (direction == Direction.BACKWARD) {
      listIterator.next();
      direction = Direction.FORWARD;
    }
    SearchTermElement nextElement = listIterator.next();
    selectText(nextElement);
  }

  /**
   * Goes to the previous match on the page, selects it, and if necessary, scroll the field to the selected text. This should only be called
   * after a call to hasPrevious(), which will reverse the direction if necessary.
   */
  public void goToPrevious() {
    // IMPLEMENTATION DETAIL: See the implementation detail in goToNext().
    if (direction == Direction.FORWARD) {
      listIterator.previous();
      direction = Direction.BACKWARD;
    }
    SearchTermElement previousElement = listIterator.previous();
    selectText(previousElement);
  }
  
  @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
  private void selectText(SearchTermElement element) {
    assert SwingUtilities.isEventDispatchThread();
    JTextComponent container = componentList.get(element.componentIndex);
    int index = element.charIndex;
    container.select(index, index + element.termUpperCase.length());
    SwingUtilities.invokeLater(container::requestFocus);
  }
  
  static class SearchTermElement implements Comparable<SearchTermElement> {
    private final int charIndex;
    private final String termUpperCase;
    private final int componentIndex;
    private final int hash;

    SearchTermElement(String term, int charIndex, int componentIndex) {
      termUpperCase = term.toUpperCase();
      this.charIndex = charIndex;
      this.componentIndex = componentIndex;
      hash = Objects.hash(charIndex, termUpperCase, componentIndex);
    }
    
    public int getComponentIndex() { return componentIndex; }

    public int getCharIndex() {
      return charIndex;
    }

    public String getTermUpperCase() {
      return termUpperCase;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
      if (!(obj instanceof SearchTermElement)) {
        return false;
      }
      SearchTermElement that = (SearchTermElement) obj;
      return (this.charIndex == that.charIndex) 
          && this.termUpperCase.equals(that.termUpperCase) 
          && (this.componentIndex == that.componentIndex);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    private static final Comparator<SearchTermElement> searchTermElementComparator = Comparator
        // Here's why we sort by length: If the terms are "light" and "lighthouse", and "light" is selected, we need the next search to
        // select "lighthouse". 
        .comparing(SearchTermElement::getComponentIndex)
        .thenComparing(SearchTermElement::getCharIndex)
        .thenComparing((SearchTermElement t) -> t.getTermUpperCase().length());

    @Override
    public int compareTo(@NotNull final SearchTermElement o) {
      return searchTermElementComparator
          .compare(this, o);
    }
  }

  public Direction getDirection() {
    return direction;
  }

  /**
   * Direction of next search.
   */
  @SuppressWarnings("JavaDoc")
  public enum Direction { BACKWARD, FORWARD }

//  public static void main(String[] args) { //throws NoSuchFieldException, IllegalAccessException {
//    JTextField f1 = new JTextField();
//    JTextField f2 = new JTextField();
//    JTextField f3 = new JTextField();
//    JTextField f4 = new JTextField();
//    f1.setText("Alpha bravo charlie delta echo foxtrot");
//    f2.setText("Bravo Charlie Fox Golf Hotel Trot");
//    f3.setText("delta echo fox trot foxtrot hotel indigo");
//    f4.setText("fox indigo Juliet, Romeo");
//    List<JTextComponent> components = new LinkedList<>(Arrays.asList(f1, f2, f3, f4));
//    System.out.println("setup complete");
//
//    FieldIterator iterator = new FieldIterator(components, Arrays.asList("bravo", "foxtrot", "golf", "echo", "fox"));
//    System.out.println("Iterator Constructed");
////    assertTrue(iterator.hasNext());
////    assertFalse(iterator.hasPrevious());
//
////    ListIterator<FieldIterator.SearchTermElement> privateIterator
////        = (ListIterator<FieldIterator.SearchTermElement>) FieldIterator.class.getField("listIterator").get(iterator);
//
//    ListIterator<FieldIterator.SearchTermElement> privateIterator = iterator.listIterator;
//
//    while (privateIterator != null && privateIterator.hasNext()) {
//      FieldIterator.SearchTermElement next = privateIterator.next();
//      int componentIndex = next.getComponentIndex();
//      String source = components.get(componentIndex).getText();
//      System.out.printf("Found %8s in field %d at %2s: %s%n", next.getTermUpperCase(), componentIndex, next.getCharIndex(), source);
//    }
//
//  }
}
