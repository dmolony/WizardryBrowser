package com.bytezone.wizardry;

import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class CharacterTableModel extends AbstractTableModel
{
  private ArrayList<Character> characters;
  private static NumberFormat nf = NumberFormat.getInstance ();
  private static String[] columnNames = { "Name", "Alignment", "Race", "Class",
          "Lvl", "AC", "Hits", "Gold", "Experience", "Next level", "Attr" };

  public CharacterTableModel(ArrayList<Character> characters)
  {
    this.characters = characters;
  }

  public int getRowCount ()
  {
    if (characters == null)
      return 0;
    return characters.size ();
  }

  public int getColumnCount ()
  {
    return columnNames.length;
  }

  public Object getValueAt (int rowIndex, int columnIndex)
  {
    Character character = (Character) characters.get (rowIndex);
    Statistics stats = null;
    Attributes attributes = null;

    if (columnIndex >= 4)
      if (columnIndex == 10)
        attributes = character.getAttributes ();
      else
        stats = character.getStatistics ();

    switch (columnIndex)
    {
      case 0:
        return character.getName ();
      case 1:
        return character.getAlignment ();
      case 2:
        return character.getRace ();
      case 3:
        return character.getType ();
      case 4:
        return new Integer (stats.level);
      case 5:
        return new Integer (stats.armourClass);
      case 6:
        return new Integer (stats.hitsMax);
      case 7:
        return new Integer (stats.gold);
      case 8:
        return new Long (stats.experience);
      case 9:
        return new Long (stats.nextLevel - stats.experience);
      case 10:
        int totalAttributes = 0;
        for (int value : attributes.array)
          totalAttributes += value;
        return new Integer (totalAttributes);
      default:
        return "???";
    }
  }

  public String getColumnName (int columnIndex)
  {
    return columnNames[columnIndex];
  }
}