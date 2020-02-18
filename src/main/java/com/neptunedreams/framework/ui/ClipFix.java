package com.neptunedreams.framework.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import com.neptunedreams.util.StringStuff;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/15/20
 * <p>Time: 9:52 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum ClipFix {
  ;

  @SuppressWarnings("HardcodedLineSeparator")
  private static final char NEW_LINE = '\n';
  private static final String FALSE = "false";
  private static final String TRUE = "true";

  /**
   * Convert html data into String data, with extra line-breaks suppressed. The "html" data sent to this method
   * isn't always true html data. If this doesn't encounter a paragraph tag, it assumes this is not true html data
   * and returns null.
   * 
   * kludge: This is a kludgy approach. A more robust approach would be to add a "paste as text" button to let the user choose 
   * to use this. This may be more robust, but it's less convenient for the user.
   * 
   * @param text The html text to convert to raw text
   * @return the raw text, or null if it doesn't contain a paragraph tag
   */
  private static @Nullable String parseHtml(String text) {
    ParserDelegator delegator = new ParserDelegator();
    final StringBuilder rawText = new StringBuilder();
    // kludge (minor). This can't be a boolean because I need to make it final and change it later.
    final StringBuilder trueHtml = new StringBuilder(FALSE);
    HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
      @Override
      public void handleText(char[] data, int pos) {
        rawText.append(data);
      }

      @Override
      public void handleEndOfLineString(String eol) {
        rawText.append(NEW_LINE);
      }

      @Override
      public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        //noinspection ObjectEquality
        if (t == HTML.Tag.P) {
          rawText.append(NEW_LINE);
          trueHtml.replace(0, trueHtml.length(), TRUE);
        }
      }
    };
    try {
      delegator.parse(new StringReader(text), callback, true);
    } catch (IOException e) {
      // Not likely, since we're using a StringReader.
      throw new AssertionError(StringStuff.emptyIfNull(e.getLocalizedMessage()), e);
    }
    if (trueHtml.toString().equals(FALSE)) {
      return null;
    }
    text = rawText.toString();
    return text;
  }

  /**
   * Reads an html from the clipboard, parses it into text, and puts it back on the clipboard.
   * The purpose of this is to strip out double-blank lines from the text that get inserted by
   * pasting html text into a plain text document. If there is no html data on the clipboard,
   * this does nothing.
   * 
   * This should be done immediately before pasting text. This will be done automatically if the
   * JTextArea was created using SwingUtils.createClipboardCleaningTextArea()
   * @see SwingUtils#createClipboardCleaningTextArea(int, int) 
   */
  public static void htmlToText() {
    String htmlText = getHtmlAsText();
    if (htmlText != null) {
      StringSelection selection = new StringSelection(htmlText);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(selection, selection);
    }
  }
  
  @Nullable
  public static String getHtmlAsText() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    DataFlavor htmlFlavor = DataFlavor.selectionHtmlFlavor;
    if (clipboard.isDataFlavorAvailable(htmlFlavor)) {
      try {
        String data = clipboard.getData(htmlFlavor).toString();
        return parseHtml(data);
      } catch (UnsupportedFlavorException | IOException ignored) { }
    }
    return null;
  }
}
