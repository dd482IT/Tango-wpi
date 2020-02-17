package com.neptunedreams.util;

import java.util.Collections;
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

  public static String emptyIfNull(@Nullable String nullableString) {
    return (nullableString == null) ? "" : nullableString;
  }
  
  public static <T> Iterable<T> notNull(Iterable<T> iterable) {
    if (iterable == null) {
      return Collections.emptyList();
    }
    return iterable;
  }
}
