package com.neptunedreams.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/5/20
 * <p>Time: 12:39 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class StringStuffTest {
  @Test
  public void testSplitter() {
    String input = "abc def\tghi    jkl\t  mno\f\n\r\t pqr \f \n \r \t  stu";
    String[] split = StringStuff.splitText(input);
    assertEquals(7, split.length);
    for (String s : split) {
      assertEquals(3, s.length());
    }
  }
}