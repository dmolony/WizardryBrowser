package com.bytezone.wizardry;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class SortFilterModel extends AbstractTableModel
{
  private TableModel model;
  private int sortColumn;
  private Row[] rows;
  private JTable parent = null; // used to retain current selection
  public static final int ASCENDING = 0;
  public static final int DESCENDING = 1;
  private int currentDirection = ASCENDING;

  public SortFilterModel (TableModel m)
  {
    model = m;
    rows = new Row[model.getRowCount ()];
    for (int i = 0; i < rows.length; i++)
    {
      rows[i] = new Row ();
      rows[i].index = i;
    }
  }

  public void setTable (JTable t)
  {
    parent = t;
  }

  public void sort (int c, int direction)
  {
    int oldRow = 0;
    if (parent != null)
      oldRow = rows[parent.getSelectedRow ()].index;
    if (direction != DESCENDING)
      direction = ASCENDING;
    currentDirection = direction;

    sortColumn = c;
    Arrays.sort (rows);
    fireTableDataChanged ();

    if (parent != null)
    {
      for (int i = 0; i < rows.length; i++)
      {
        if (rows[i].index == oldRow)
        {
          parent.getSelectionModel ().setSelectionInterval (i, i);
          break;
        }
      }
    }
  }

  @Override
  public Object getValueAt (int r, int c)
  {
    return model.getValueAt (rows[r].index, c);
  }

  @Override
  public boolean isCellEditable (int r, int c)
  {
    return model.isCellEditable (rows[r].index, c);
  }

  @Override
  public void setValueAt (Object aValue, int r, int c)
  {
    model.setValueAt (aValue, rows[r].index, c);
  }

  public int translateRow (int row)
  {
    return rows[row].index;
  }

  @Override
  public int getRowCount ()
  {
    return model.getRowCount ();
  }

  @Override
  public int getColumnCount ()
  {
    return model.getColumnCount ();
  }

  @Override
  public String getColumnName (int c)
  {
    return model.getColumnName (c);
  }

  @Override
  public Class<?> getColumnClass (int c)
  {
    return model.getColumnClass (c);
  }

  private class Row implements Comparable<Row>
  {
    public int index;

    @Override
    public int compareTo (Row other)
    {
      Row otherRow = other;
      Object a = model.getValueAt (index, sortColumn);
      Object b = model.getValueAt (otherRow.index, sortColumn);

      if (a instanceof Comparable)
      {
        if (currentDirection == ASCENDING)
          return ((Comparable) a).compareTo (b);
        else
          return ((Comparable) b).compareTo (a);
      }
      else if (a instanceof GregorianCalendar)
      {
        Date d1 = ((GregorianCalendar) a).getTime ();
        Date d2 = ((GregorianCalendar) b).getTime ();
        if (currentDirection == ASCENDING)
          return d1.compareTo (d2);
        return d2.compareTo (d1);
      }
      else
      {
        if (currentDirection == ASCENDING)
          return a.toString ().compareTo (b.toString ());
        return b.toString ().compareTo (a.toString ());
      }
    }
  }
}
