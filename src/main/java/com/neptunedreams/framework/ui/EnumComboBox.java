package com.neptunedreams.framework.ui;

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 10:58 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class EnumComboBox<E extends Enum<E> & DisplayEnum> extends JComboBox<E> {

  public static <N extends Enum<N> & DisplayEnum> EnumComboBox<N> createComboBox(N[] values) {
    EnumComboBox<N> comboBox = new EnumComboBox<>();
    DefaultComboBoxModel<N> model = new DefaultComboBoxModel<>();
    comboBox.setModel(model);
    for (N value: values) {
      model.addElement(value);
    }
    model.setSelectedItem(values[0]);
    comboBox.setMaximumRowCount(values.length);
    comboBox.setEditable(false);
    ListCellRenderer<Object> r = new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        @SuppressWarnings("unchecked")
        N eValue = (N) value;

        return super.getListCellRendererComponent(list, eValue.getDisplay(), index, isSelected, cellHasFocus);
      }
    };
    comboBox.setRenderer(r);
    return comboBox;
  }

  private EnumComboBox() {
    super();
  }
  
  public E getSelected() {
    // Maybe I can get rid of this unchecked warning by extending ComboBoxModel to add two typed methods to match the
    // two current untyped methods. Then I could use the typed methods here.
    @SuppressWarnings("unchecked")
    final E selectedItem = (E) getModel().getSelectedItem();
    return selectedItem;
  }
}
