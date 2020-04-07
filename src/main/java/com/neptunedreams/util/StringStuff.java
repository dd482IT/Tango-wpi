package com.neptunedreams.util;

import java.util.Collections;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/17/20
 * <p>Time: 2:35 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum StringStuff {
  ;

  /**
   * Convert a possibly null String into a not-null String.
   * @param nullableString A String, which may be null
   * @return a not-null String, which will be empty if the input was null.
   */
  public static String emptyIfNull(@Nullable String nullableString) {
    return (nullableString == null) ? "" : nullableString;
  }

  /**
   * Returns the supplied Collection or other Iterable, converting null into an empty Collection. May be used like this:
   * <pre> for (String s: notNull(inputList)) { ... }</pre>
   * @param iterable The collection or other iterable. May be null.
   * @param <T> The type of the collection or iterable
   * @return A non-null collection or iterable. Returns the same collection or iterable if it is not null.
   */
  public static <T> Iterable<T> notNull(Iterable<T> iterable) {
    if (iterable == null) {
      return Collections.emptyList();
    }
    return iterable;
  }

  /**
   * Split a String, using any white-space as a delimiter. This treats multiple white-space characters as a single delimiter.
   * @param text text to Split
   * @return An array of strings, split on any white-space character
   */
  public static String[] splitText(@NonNull String text) {
    //noinspection EqualsReplaceableByObjectsCall
    assert text.trim().equals(text); // text should already be trimmed
    return text.split("\\s+");
  }
}
