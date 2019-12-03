package com.bytezone.wizardry;

import com.bytezone.disk.HexFormatter;
import com.bytezone.utilities.*;

public class Item implements Comparable
{
	private String name;
	private byte[] data;
	private int type;
	private long cost;
  public int partyOwns;
  static int counter = 0;
	
	public Item (String name, byte[] buffer)
	{
		this.name = name;
		this.data = buffer;
		type = data[32];
    cost = HexFormatter.intValue (data[44], data[45])
          + HexFormatter.intValue (data[46], data[47]) * 10000
          + HexFormatter.intValue (data[48], data[49]) * 100000000L;
    /*
    StringBuilder line = new StringBuilder (HexFormatter.format2(counter));
    line.append (" " + name);
    while (line.length () < 18)
      line.append (" ");
    for (int i = 32; i < 56; i++)
//    for (int i = 56; i < 78; i++)
//    for (int i = 150; i < 158; i++)
      line.append (" " + HexFormatter.format2(buffer[i]));
    System.out.println(line.toString());
    counter++;*/
	}
	
	public String getName ()
	{
		return name;
	}
	
	public int getType ()
	{
		return type;
	}
	
	public int getArmourClass ()
	{
		return data[62];
	}
	
	public String getDamage ()
	{
	  if (data[66] == 0)
	    return "";
	  String damage = HexFormatter.intValue(data[66], data[67]) + "d"
	  									+ HexFormatter.intValue(data[68], data[69]);
	  if (data[70] != 0)
	    damage += "+" + HexFormatter.intValue(data[70], data[71]);
	  return damage;
	}
	
	public int getSpeed ()
	{
	  return HexFormatter.intValue(data[72]);
	}
	
	public long getCost ()
	{
		return cost;
	}
	
	public boolean isCursed ()
	{
		return data[36] != 0;
	}
	
	public int getStockOnHand ()
	{
		if (data[50] == -1 && data[51] == -1)
			return -1;

		return HexFormatter.intValue(data[50], data[51]);
	}
  
  public boolean canUse (int type2)
  {
    int users = HexFormatter.intValue(data[54]);
    return ((users >>> type2) & 1) == 1;
  }
	
	public String toString ()
	{
		StringBuilder2 line = new StringBuilder2 (name);
		line.tabTo (16);
		if (data[36] == -1)
			line.append ("(c)");
		line.appendNumber (data[62], 22);
		line.appendNumber (HexFormatter.intValue(data[34]), 36);
		line.tabTo (38);
		
		if (data[50] == -1 && data[51] == -1)
			line.append ("*");
		else
			line.appendNumber (HexFormatter.intValue(data[50], data[51]), 38);
		line.tabTo (41);
		
		for (int i = 38; i < 44; i++)
			line.append (HexFormatter.format2 (data[i]) + " ");
		for (int i = 48; i < 50; i++)
			line.append (HexFormatter.format2 (data[i]) + " ");
		for (int i = 52; i < 62; i++)
			line.append (HexFormatter.format2 (data[i]) + " ");
		for (int i = 64; i < 78; i++)
			line.append (HexFormatter.format2 (data[i]) + " ");
		
		return line.toString ();
	}
	
	public int compareTo (Object otherItem)
	{
		Item item = (Item)otherItem;
    return this.type - item.type;
	}
}
