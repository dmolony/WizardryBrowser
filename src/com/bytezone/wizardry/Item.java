package com.bytezone.wizardry;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

public class Item implements Comparable<Item>
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
    cost = Utility.intValue (data[44], data[45]) + Utility.intValue (data[46], data[47]) * 10000
        + Utility.intValue (data[48], data[49]) * 100000000L;
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
    String damage =
        Utility.intValue (data[66], data[67]) + "d" + Utility.intValue (data[68], data[69]);
    if (data[70] != 0)
      damage += "+" + Utility.intValue (data[70], data[71]);
    return damage;
  }

  public int getSpeed ()
  {
    return data[72] & 0xFF;
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

    return Utility.intValue (data[50], data[51]);
  }

  public boolean canUse (int type2)
  {
    int users = data[54] & 0xFF;
    return ((users >>> type2) & 1) == 1;
  }

  @Override
  public String toString ()
  {
    StringBuilder2 line = new StringBuilder2 (name);
    line.tabTo (16);
    if (data[36] == -1)
      line.append ("(c)");
    line.appendNumber (data[62], 22);
    line.appendNumber (data[34] & 0xFF, 36);
    line.tabTo (38);

    if (data[50] == -1 && data[51] == -1)
      line.append ("*");
    else
      line.appendNumber (Utility.intValue (data[50], data[51]), 38);
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

  @Override
  public int compareTo (Item item)
  {
    return this.type - item.type;
  }
}
