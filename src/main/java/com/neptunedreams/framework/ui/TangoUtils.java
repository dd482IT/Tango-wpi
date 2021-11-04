package com.neptunedreams.framework.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 3:15 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum TangoUtils {
  ;

  /**
   * Wrap the specified component into the east side (Actually, the line-end side) of a new JPanel.
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapEast(JComponent component) {
    return wrap(component, BorderLayout.LINE_END);
  }

  /**
   * Wrap the specified component into the wast side (Actually, the line-start side) of a new JPanel.
   *
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapWest(JComponent component) {
    return wrap(component, BorderLayout.LINE_START);
  }

  /**
   * Wrap the specified component into the north side (Actually, the page-start side) of a new JPanel.
   *
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapNorth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_START);
  }

  /**
   * Wrap the specified component into the south side (Actually, the page-end side) of a new JPanel.
   *
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapSouth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_END);
  }

  private static JPanel wrap(JComponent component, String direction) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(component, direction);
    return wrapper;
  }

  /**
   * Wrap the specified component into the center of a new JPanel.
   * @param component The component to wrap
   * @return The containing JPanel
   */
  public static JPanel wrapCenter(JComponent component) {
    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    flowPanel.add(component);
    return flowPanel;
  }

  /**
   * Configure a JTextArea for prose text and wrap it inside a JScrollPane that is set up for vertical scrolling and
   * horizontal text wrapping the the component's width. This also sets the wordWrapStyle and lineWrap properties.
   * @param wrappedField The JTextArea to configure and wrap
   * @return a JScrollPane wrapping the now configured text area.
   */
  public static JComponent scrollArea(JTextArea wrappedField) {
    wrappedField.setWrapStyleWord(true);
    wrappedField.setLineWrap(true);
    return verticalScroll(wrappedField);
  }

  /**
   * Wrap the JTextField in a JScrollPane and configure it for natural language text, wrapping lines on word
   * boundaries, with a vertical scroll bar, and using a proper platform-independent Carat.
   * @param wrappedField The JTextField to configure and wrap
   * @return a JScrollPane wrapping the newly configured text area.
   */
  public static JComponent prepareForNaturalText(JTextArea wrappedField) {
    return prepareForNaturalText(wrappedField, StandardCaret::new);
  }

  /**
   * Wrap the JTextField in a JScrollPane and configure it for natural language text, wrapping lines on word
   * boundaries, and using a proper platform-independent Carat.
   * @param caretSupplier The Supplier for a Carat of your choice.
   * @param wrappedField The JTextArea to configure and wrap
   * @return a JScrollPane wrapping the now configured text area.
   */
  public static JComponent prepareForNaturalText(JTextArea wrappedField, Supplier<? extends Caret> caretSupplier) {
    installCustomCaret(caretSupplier, wrappedField);
    return scrollArea(wrappedField);
  }

  /**
   * Wrap the specified component inside a JScrollPane, configuring it to have a vertical scrollbar but no horizontal 
   * scrollbar. This is the best configuration for multi-line text components like JTextArea, which are designed to 
   * wrap text to the current size under these conditions. To configure a JTextArea properly, you should really call 
   * scrollArea instead, which calls this one, but makes more useful changes.
   * @param wrapped The component to wrap.
   * @return the JScrollPane that wraps the component.
   * @see #scrollArea(JTextArea) 
   */
  public static JComponent verticalScroll(JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }

  /**
   * Removes the border on the specified component by calling {@code setBorder(null)}.
   * The JComponent.setBorder method is improperly annotated. It puts @NonNull on the parameter, which is wrong. This
   * method works around that problem. It has to be its own method because you can't annotate a x.setBorder() call.
   *
   * @param component The component
   */
  public static void setNoBorder(JComponent component) {
    component.setBorder(null);
  }

  /**
   * Create a decorated JTextArea that cleans the clipboard before pasting data. "Cleaning the clipboard" means 
   * stripping out the extra blank lines inserted automatically when pasting html data into a plain-text object.
   * This works by calling ClipFix.htmlToText() before pasting any data. If the clipboard doesn't have html data,
   * this has no effect.
   * @param rows The number of rows
   * @param columns The number of columns
   * @return a decorated JTExtArea
   * @see ClipFix#htmlToText() 
   */
  public static JTextArea createClipboardCleaningTextArea(int rows, int columns) {
    return new JTextArea(rows, columns) {
      @Override
      public void paste() {
        // Fix html clipboard data
        ClipFix.htmlToText();
        super.paste();
      }
    };
  }

  /**
   * On the Mac, the AquaCaret will get installed. This caret has an annoying feature of selecting all the text on a
   * focus-gained event. If this isn't bad enough, it also fails to check temporary vs permanent focus gain, so it
   * gets triggered on a focused JTextComponent whenever a menu is released, which will re-select all the text! This
   * method removes the Aqua Caret and installs a StandardCaret. It's only needed on the Mac, but it's safe to use 
   * on any platform.
   *
   * @param components The components to repair. This is usually a JTextField or JTextArea.
   */
  public static void installStandardCaret(JTextComponent... components) {
    installCustomCaret(StandardCaret::new, components);
  }

  /**
   * Install a custom Caret, provided by the supplier or constructor, into the specified JTextComponents. 
   * @param supplier the Constructor to instantiate each Caret
   * @param components The components to get the new Caret.
   */
  public static void installCustomCaret(Supplier<? extends Caret> supplier, JTextComponent... components) {
    for (JTextComponent component : components) {
      Caret caret = supplier.get();
      replaceCaret(component, caret);
    }
  }

  /**
   * Installs the specified Caret into the JTextComponent, using the same blink-rate as the previous caret.
   * @param component The text component to get the new Caret
   * @param caret The new Caret to install
   */
  public static void replaceCaret(final JTextComponent component, final Caret caret) {
    final Caret priorCaret = component.getCaret();
    int blinkRate = priorCaret.getBlinkRate();
    if (priorCaret instanceof PropertyChangeListener) {
      // For example, com.apple.laf.AquaCaret, the troublemaker, installs this listener which doesn't get removed when the Caret 
      // gets uninstalled.
      component.removePropertyChangeListener((PropertyChangeListener) priorCaret);
    }
    component.setCaret(caret);
    caret.setBlinkRate(blinkRate); // Starts the new caret blinking.
  }

  /**
   * Perform the specified action a single time, when the component is first displayed. This is useful, for example, for actions that
   * require a root pane, and so can't be done when the object is constructed.
   * @param component The component
   * @param action The action to preform when a component is first displayed.
   */
  public static void executeOnDisplay(JComponent component, Runnable action) {
    AncestorListener ancestorListener = new AncestorListener() {
      @Override
      public void ancestorAdded(final AncestorEvent event) {
        action.run();
        event.getComponent().removeAncestorListener(this);
      }

      @Override
      public void ancestorRemoved(final AncestorEvent event) { }

      @Override
      public void ancestorMoved(final AncestorEvent event) { }
    };
    component.addAncestorListener(ancestorListener);
  }

  /**
   * Recolor an Icon. This grays out the icon, then applies the given color to the different
   * gray levels to produce new colors of the same hue.
   *
   * @param rawIcon The icon to recolor
   * @param color   The color to give it.
   * @return A new Icon that looks like the old one, but is entirely of the new color.
   */
  public static ImageIcon recolor(ImageIcon rawIcon, final Color color) {
    @SuppressWarnings("UseOfClone") RGBImageFilter filter = new RGBImageFilter() {
      /**
       * Overrides {@code RGBImageFilter.filterRGB}.
       */
      @SuppressWarnings({"NumericCastThatLosesPrecision", "MagicNumber"})
      @Override
      public int filterRGB(int x, int y, int rgb) {
        // This is adapted from the javax.swing.GrayFilter class, which uses the NTSC formula to gray out a color.

        // The NTSC formula to gray out a color applies r*0.3, g*0.59, and b*0.11, to get
        // a new value that is used for R, G, and B. I raise those three constants by a
        // factor of 0.4 according to this formula: c' = (1-c)*r + c, where r is 0.4.
        // This reduces to c' = r - r*c + c. Applying that gives me the three values used below.
        int gray = (int) (((0.580 * ((rgb >> 16) & 0xff)) +
            (0.754 * ((rgb >> 8) & 0xff)) +
            (0.466 * (rgb & 0xff))) / 3);

        if (gray < 0) { gray = 0; }
        // Now that we have a gray, use the input color to produce a new color of the same hue.
        int red = (gray * color.getRed()) / 100;
        int green = (gray * color.getGreen()) / 100;
        int blue = (gray * color.getBlue()) / 100;
        if (red > 255) { red = 255; }
        if (green > 255) { green = 255; }
        if (blue > 255) { blue = 255; }
        //noinspection OverlyComplexBooleanExpression
        return (rgb & 0xff000000) | (red << 16) | (green << 8) | blue;
      }

      @SuppressWarnings("UseOfClone")
      @Override
      public RGBImageFilter clone() {
        return (RGBImageFilter) super.clone();
      }
    };
    ImageProducer prod = new FilteredImageSource(rawIcon.getImage().getSource(), filter);
    Image coloredImage = Toolkit.getDefaultToolkit().createImage(prod);
    return new ImageIcon(coloredImage);
  }

  /**
   * Shift the hue of an image by a specified amount. To shift from red to green or green to blue, use a shift of 85. 
   * To shift in the opposite direction, use 170.
   * @param rawIcon The original icon to recolor.
   * @param shift The amount to shift the hue, where 0 or 256 leave the hue unchanged. 
   * @return A recolored icon.
   */
  @SuppressWarnings("CloneableClassWithoutClone")
  public static ImageIcon shiftHue(ImageIcon rawIcon, final int shift) {
    float[] hsb = new float[3];
    RGBImageFilter filter = new RGBImageFilter() {
      @SuppressWarnings("MagicNumber")
      @Override
      public int filterRGB(int x, int y, int rgb) {
        int red = (0xff_0000 & rgb) >> 16;
        int grn = (0x00_ff00 & rgb) >> 8;
        int blu = (0x00_00ff & rgb);
        int alpha = 0xff000000 & rgb; 
        Color.RGBtoHSB(red, grn, blu, hsb);
        @SuppressWarnings("NumericCastThatLosesPrecision")
        int hue = (int) (hsb[0] * 256);
        float newHue = ((hue + shift) % 256) / 256.0F;
        // HSBtoRGB creates a color with an alpha of 0xFF
        final int newColor = Color.HSBtoRGB(newHue, hsb[1], hsb[2]) & 0x00ff_ffff; // strip out alpha value of 0xFF
        return alpha | newColor;
      }
    };
    ImageProducer prod = new FilteredImageSource(rawIcon.getImage().getSource(), filter);
    Image coloredImage = Toolkit.getDefaultToolkit().createImage(prod);
    return new ImageIcon(coloredImage);
  }

  /**
   * Prints out a distribution of the different hues of an image. This is an aid to determining the best hue to use in the recolor method.
   * It would never be used in an actual application, but it's useful during the development phase.
   * @param icon The icon to examine.
   */
  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "argument.type.incompatible", "MagicNumber", "NumericCastThatLosesPrecision"})
  public static void colorHistogram(ImageIcon icon) {
    int red = 0x00FF0000;
    int green = 0x0000FF00;
    int blue = 0x000000FF;
    final float redHue = getHue(new Color(red));
    final float grnHue = getHue(new Color(green));
    final float bluHue = getHue(new Color(blue));
    final int r = (int) (redHue * 256);
    final int g = (int) (grnHue * 256);
    final int b = (int) (bluHue * 256);
    System.out.printf("Red: %10.8f -> %3d%nGrn: %10.8f -> %3d%nBlu: %10.8f -> %3d%n%n", redHue, r, grnHue, g, bluHue, b); // NON-NLS
    
    int[] hues = new int[256];
    float[] hsb = new float[3];
    @SuppressWarnings("CloneableClassWithoutClone")
    RGBImageFilter filter = new RGBImageFilter() {
      @Override
      public int filterRGB(final int x, final int y, final int rgb) {
        final Color color = new Color(rgb);
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float fHue = getHue(color);
        int hue = (int) (fHue * 256);
        hues[hue] += (int) getWeight(color);
        return rgb;
      }
    };
    System.out.printf("Icon size: (%d x %d)%n", icon.getIconWidth(), icon.getIconHeight()); // NON-NLS
    ImageProducer prod = new FilteredImageSource(icon.getImage().getSource(), filter);

    // instantiates the image, but doesn't load it.
    Image result = Toolkit.getDefaultToolkit().createImage(prod);
    
    // Force the image to load
    Toolkit.getDefaultToolkit().prepareImage(result, icon.getIconWidth(), icon.getIconHeight(), null);

    System.out.printf("Histogram Data model:%n"); // NON-NLS
    for (int i=0; i<hues.length; ++i) {
      System.out.printf("%3d: %5d%n", i, hues[i]); // NON-NLS
    }
  }

  /**
   * Get the hue of a color as a float.
   * @param color The color
   * @return The color's hue, as a float with a range from 0.0 to 1.0
   */
  public static float getHue(Color color) {
    float[] hsb = new float[3];
    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
    return hsb[0];
  }

  /**
   * Get the weight of a color's hue, for statistical purposes, as a float in the range from 0.0 to 255.0
   * @param color The color to weight
   * @return The weight of the hue, which is the alpha channel, multiplied by the square of the brightness.
   */
  public static float getWeight(Color color) {
    float[] hsb = new float[3];
    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
    return color.getAlpha() * hsb[2] * hsb[2];
  }

}
