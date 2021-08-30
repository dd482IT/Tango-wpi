package com.neptunedreams.framework.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Bind an editor or display field to a property of a data model.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/6/17
 * <p>Time: 12:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused")
public abstract class FieldBinding<R, T, C extends Component> {
  private final Function<? super R, ? extends T> getter;
  private final C editor;
  private final boolean isEditable;
  private static final Color DISABLED_COLOR = new Color(245, 245, 245);

  /**
   * Construct a FieldBinding
   * 
   * @param aGetter The getter
   * @param aField The display component
   */
  protected FieldBinding(Function<? super R, ? extends T> aGetter, C aField) {
    getter = aGetter;
    editor = aField;
    isEditable = false;
  }

  /**
   * Determines if the property's value in the editor has changed from the value in the data model.
   * @param record The dataModel record
   * @return true if the the cleaned editor value is different from the dataModel's value, false otherwise
   */
  public boolean propertyHasChanged(R record) {
    return !Objects.equals(getValue(record), readFieldValue());
  }

  /**
   * Retrieves the data field value from the dataModel and loads it into the editor.
   * @param dataRecord The dataModel record.
   */
  public final void prepareEditor(R dataRecord) {
    T loadedValue = getTheValue(dataRecord);
    loadStringValue(getStringValue(loadedValue));
  }

  /**
   * Gets the editor, which may just be a display field like a JLabel, or may be an editor component like a JTextField.
   * @return The editor component.
   */
  C getEditor() { return editor; }

  /**
   * Calls the getter to get the value from the dataModel record, without doing any cleaning. For internal use only.
   * @param record The record with the data
   * @return The value
   */
  @SuppressWarnings("WeakerAccess")
  protected T getTheValue(R record) { return getter.apply(record); }

  /**
   * Gets the value from the editor or display field, without doing any cleaning. Subclasses should implement this for 
   * their particular editor component.
   * @return The uncleaned value from the editor component.
   */
  protected abstract T getFieldValue();
  
  protected T readFieldValue() {
    return clean(getFieldValue());
  }

  /**
   * Uses the getter to retrieve the value from the dataModel record, and cleans it.
   * @param record The dataModel record
   * @return The cleaned value from the dataModel
   */
  public T getValue(R record) {
    return clean(getTheValue(record));
  }

  /**
   * Prepare the editor by setting the editor or display component to the specified value. This is declared as a String
   * because that's how both text and numerical data is displayed, but this may be revisited later.
   * Subclasses should implement this for their particular editor component.
   * @param editorValue The editor value, expressed as a String.
   */
  protected abstract void loadStringValue(String editorValue);

  /**
   * Cleans the value. The default implementation returns the value unchanged, but subclasses should override this
   * to do whatever cleaning is needed for the particular type of data. Strings, for example, should call the trim()
   * method.
   * @param value The value to clean
   * @return The cleaned value
   */
  protected T clean(T value) { return value; }

  /**
   * Returns the value as a String. This is used for loading an editor that displays the value as a String. 
   * @param value The value, which may be null, to express as a String. Null values are expressed as an empty String.
   * @return The String that expresses the value.
   */
  String getStringValue(@Nullable T value) { return Objects.toString(value, ""); }

  /**
   * Determines if the Binding uses an editable component. 
   * @return true if editable, false otherwise
   */
  public boolean isEditable() {
    return isEditable;
  }

  /**
   * This method is so you never have to cast a FieldBinding to an EditableFieldBinding, which raises all sorts
   * of inspection warnings for raw types and unsafe casting. The default implementation throws an
   * IllegalStateException, but EditableFieldBindings return this.
   * @return this, if editable. Throws IllegalStateException if not.
   */
  public EditableFieldBinding<R, T, ?> getEditableBinding() {
    throw new IllegalStateException("Not implemented for non-editable binding");
  }
  
  public abstract static class EditableFieldBinding<R, T, C extends JTextComponent> extends FieldBinding<R, T, C> {
    private final BiConsumer<? super R, ? super T> setter;
    private final boolean editableState = false;

    protected EditableFieldBinding(Function<? super R, ? extends T> aGetter, BiConsumer<? super R, ? super T> aSetter, C aField) {
      super(aGetter, aField);
      setter = aSetter;
      aField.setEditable(false);
    }

    @Override
    public boolean isEditable() {
      return true;
    }

    /**
     * Reads the value in the editor, cleans it, and loads it into the current record.
     *
     * @param record The record to receive the editor's value
     */
    public void saveEdit(R record) {
      setter.accept(record, readFieldValue());
    }

    /**
     * Uses the setter to set the specified value into the dataModel record, after cleaning it.
     *
     * @param record The dataModel record
     * @param value  The value to set
     */
    public void setValue(R record, T value) {
      assert record != null : "Null record";
      assert value != null : "Null value";
      setter.accept(record, clean(value));
    }
    
    public boolean getEditableState() {
      return editableState;
    }
    
    public void setEditableState(boolean editableState) {
      final C editor = getEditor();
      editor.setEditable(editableState);
      editor.setBackground(editableState? Color.white : DISABLED_COLOR);
    }

    @Override
    public final EditableFieldBinding<R, T, C> getEditableBinding() {
      return this;
    }
  }
  
  public static class StringBinding<D> extends FieldBinding<D, String, JLabel> {

    StringBinding(final Function<? super D, String> aGetter, final JLabel aField) {
      super(aGetter, aField);
    }

    @Override
    protected String getFieldValue() {
      return getEditor().getText();
    }

    @Override
    protected void loadStringValue(final String editorValue) {
      getEditor().setText(editorValue);
    }
  }

  /**
   * A Binding to edit a String value, which uses a subclass of JTextComponent (Usually a JTextField or JTextArea)
   * to display the editable value.
   * @param <D> The DataModel type.
   */
  public static class StringEditableBinding<D> extends EditableFieldBinding<D, String, JTextComponent> {
    StringEditableBinding(Function<? super D, String> aGetter, BiConsumer<? super D, ? super String> aSetter, JTextComponent aField) {
      super(aGetter, aSetter, aField);
    }

    @Override
    protected String getFieldValue() {
      return getEditor().getText();
    }

    @Override
    protected void loadStringValue(final String editorValue) {
      final JTextComponent editor = getEditor();
      editor.setText(clean(editorValue));
      
      // Don't scroll to the end on multi-line text components. This makes the swipe animation smoother.
      if (editor instanceof JTextArea) {
        editor.setSelectionStart(0);
        editor.setSelectionEnd(0);
      }
    }

    @Override
    protected String clean(final String value) { return value.trim(); }
  }

  /**
   * A binding to display a read-only integer value, which uses a JLabel to display the String value.
   * @param <D> The DataModel type
   */
  public static class IntegerBinding<D> extends FieldBinding<D, Integer, JLabel> {
    IntegerBinding(final Function<? super D, Integer> aGetter, final JLabel aField) {
      super(aGetter, aField);
      aField.setText("0"); // Default text to zero.
    }

    @Override
    protected void loadStringValue(final String editorValue) {
      getEditor().setText(editorValue);
    }

    @Override
    protected Integer getFieldValue() {
      return Integer.valueOf(getEditor().getText());
    }

  }
  
  public static <R> StringEditableBinding<R> bindEditableString(Function<? super R, String> getter, BiConsumer<? super R, ? super String> setter, JTextComponent field) {
    return new StringEditableBinding<>(getter, setter, field);
  }
  
  public static <R> StringBinding<R> bindConstantString(Function<? super R, String> getter, JLabel label) {
    return new StringBinding<>(getter, label);
  }

  public static <R> IntegerBinding<R> bindInteger(Function<? super R, Integer> getter, JLabel field) {
    return new IntegerBinding<>(getter, field);
  }
}
