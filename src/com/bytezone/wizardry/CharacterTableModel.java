package com.bytezone.wizardry;

import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class CharacterTableModel extends AbstractTableModel
{
  private ArrayList<Character> characters;
  private static NumberFormat nf = NumberFormat.getInstance ();
  private static String[] columnNames = { "Name", "Alignment", "Race", "Class", "Lvl", "AC", "Hits",
      "Gold", "Experience", "Next level", "Attr" };

  public CharacterTableModel (ArrayList<Character> characters)
  {
    this.characters = characters;
  }

  @Override
  public int getRowCount ()
  {
    if (characters == null)
      return 0;
    return characters.size ();
  }

  @Override
  public int getColumnCount ()
  {
    return columnNames.length;
  }

  @Override
  public Object getValueAt (int rowIndex, int columnIndex)
  {
    Character character = characters.get (rowIndex);
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
        return Integer.valueOf (stats.level);
      case 5:
        return Integer.valueOf (stats.armourClass);
      case 6:
        return Integer.valueOf (stats.hitsMax);
      case 7:
        return Integer.valueOf (stats.gold);
      case 8:
        return Long.valueOf (stats.experience);
      case 9:
        return Long.valueOf (stats.nextLevel - stats.experience);
      case 10:
        int totalAttributes = 0;
        for (int value : attributes.array)
          totalAttributes += value;
        return Integer.valueOf (totalAttributes);
      default:
        return "???";
    }
  }

  @Override
  public String getColumnName (int columnIndex)
  {
    return columnNames[columnIndex];
  }
}