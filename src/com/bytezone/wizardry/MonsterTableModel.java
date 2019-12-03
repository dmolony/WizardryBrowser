package com.bytezone.wizardry;

import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class MonsterTableModel extends AbstractTableModel
{
	private ArrayList<Monster> monsters;
	private static NumberFormat nf = NumberFormat.getInstance ();
	private static String[] columnNames = { "Generic Name", "Real Name",
			"Type", "Exp", "A/C", "Group", "Hit Pts", "Damage"};

	public MonsterTableModel (ArrayList<Monster> monsters)
	{
		this.monsters = monsters;
	}

	public int getRowCount ()
	{
	  if (monsters == null)
	    return 0;
		return monsters.size ();
	}

	public int getColumnCount ()
	{
		return columnNames.length;
	}

	public Object getValueAt (int rowIndex, int columnIndex)
	{
		Monster monster = monsters.get (rowIndex);
		switch (columnIndex)
		{
			case 0:
				return monster.getName ();
			case 1:
				return monster.getRealName ();
			case 2:
				return new Integer (monster.getType ());
			case 3:
				return new Integer (monster.getExperience ());
			case 4:
				return new Integer (monster.getArmourClass ());
			case 6:
				return monster.getHitPoints ();
			case 5:
				return monster.getGroupSize ();
			case 7:
				return monster.getDamage ();
			default:
				return "???";
		}
	}

	public String getColumnName (int columnIndex)
	{
		return columnNames[columnIndex];
	}
}
