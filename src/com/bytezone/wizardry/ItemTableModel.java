package com.bytezone.wizardry;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ItemTableModel extends AbstractTableModel
{
  private ArrayList<Item> items;
  private int[] ownedItems;
  private static String[] columnNames = { "Item Name", "Type", "Crs", "Cost",
          "SoH", "Own", "A/C", "Speed", "Damage", "Use" };
  private static String[] types = { "Weapon", "Armour", "Shield", "Helmet",
          "Gauntlets", "Magic", "Misc Item" };
  private static String[] classes = { "F", "M", "P", "T", "B", "S", "L", "N" };

  public ItemTableModel(ArrayList<Item> items)
  {
    this.items = items;
  }

  public int getRowCount ()
  {
    if (items == null)
      return 0;
    return items.size ();
  }

  public int getColumnCount ()
  {
    return columnNames.length;
  }

  public Object getValueAt (int rowIndex, int columnIndex)
  {
    Item item = (Item) items.get (rowIndex);
    switch (columnIndex)
    {
      case 0:
        return item.getName ();
      case 1:
        return types[item.getType ()];
      case 2:
        return (item.isCursed ()) ? "y" : "";
      case 3:
        return new Long (item.getCost ());
      case 4:
        int stock = item.getStockOnHand ();
        if (stock == -1)
          stock = 99;
        return new Integer (stock);
      case 6:
        return new Integer (item.getArmourClass ());
      case 7:
        return new Integer (item.getSpeed ());
      case 8:
        return item.getDamage ();
      case 9:
        StringBuilder text = new StringBuilder ();
        for (int userType = 0; userType < 8; userType++)
          if (item.canUse (userType))
            text.append (classes[userType]);
        return text.toString ();
      case 5:
        return new Integer (item.partyOwns);
      default:
        return "??";
    }
  }

  public String getColumnName (int columnIndex)
  {
    return columnNames[columnIndex];
  }
}
