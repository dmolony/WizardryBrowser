package com.bytezone.wizardry;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class NumberRenderer extends DefaultTableCellRenderer
{
  protected NumberFormat nf = NumberFormat.getNumberInstance ();

  @Override
  public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected,
      boolean isFocused, int row, int column)
  {
    Component component =
        super.getTableCellRendererComponent (table, value, isSelected, isFocused, row, column);

    if (value != null && (value instanceof Integer || value instanceof Long))
    {
      ((JLabel) component).setText (nf.format (value));
    }
    else
      ((JLabel) component).setText (value == null ? "" : value.toString ());

    return component;
  }
}
