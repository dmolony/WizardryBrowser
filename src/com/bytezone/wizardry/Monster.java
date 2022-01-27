package com.bytezone.wizardry;

import java.util.List;

public class Monster implements Comparable
{
  String genericName;
  String realName;
  int monsterID;
  private Monster partner;

  byte[] data;
  static int counter = 0;

  private static String[] monsterClass = { "Fighter", "Mage", "Priest", "Thief", "Midget", "Giant",
      "Mythical", "Dragon", "Animal", "Were", "Undead", "Demon", "Insect", "Enchanted" };

  private static int[] experience = { //
      55, 235, 415, 230, 380, 620, 840, 520, 550, 350,                // 00-09
      475, 515, 920, 600, 735, 520, 795, 780, 990, 795,               // 10-19
      1360, 1320, 1275, 680, 960, 600, 755, 1120, 2070, 870,          // 20-29
      960, 1120, 1120, 2435, 1080, 2280, 975, 875, 1135, 1200,        // 30-39
      620, 740, 1460, 1245, 960, 1405, 1040, 1220, 1520, 995,         // 40-49
      665, 2340, 2160, 2395, 790, 1140, 1235, 1790, 1720, 2240,       // 50-59
      1475, 1540, 1720, 1900, 1240, 1220, 1020, 20435, 5100, 3515,    // 60-69
      2115, 2920, 2060, 2140, 1400, 1640, 1280, 4450, 42840, 3300,    // 70-79
      40875, 5000, 3300, 2395, 1935, 1600, 3330, 44090, 40840, 5200,  // 80-89
      4150, 3000, 9200, 3160, 7460, 0, 0, 0, 0, 1000, 0               // 90-100
  };

  public Monster (String name, byte[] buffer)
  {
    this.genericName = name;
    this.data = buffer;

    int len = data[32];
    this.realName = new String (data, 33, len);

    if (counter >= experience.length)
      counter = 0;
    this.monsterID = counter++;

    if (false)
    {
      // StringBuilder line = new StringBuilder
      // (HexFormatter.format2(counter++));
      // line.append (" " + realName);
      StringBuilder line = new StringBuilder (realName);
      while (line.length () < 16)
        line.append (" ");
      // for (int i = 132; i < 136; i += 2)
      // line.append (HexFormatter.format2 (buffer[i]) + " ");
      // for (int i = 144; i < 148; i += 2)
      // line.append (HexFormatter.format2 (buffer[i]) + " ");
      // for (int i = 150; i < 158; i += 2)
      // line.append (HexFormatter.format2 (buffer[i]) + " ");
      line.append (":" + experience[monsterID]);

      while (line.length () < 23)
        line.append (" ");
      line.append (":" + getByte72 ());
      int total = getByte72 ();

      while (line.length () < 29)
        line.append (" ");
      line.append (":" + getByte74 ());
      total += getByte74 ();

      while (line.length () < 34)
        line.append (" ");
      line.append (":" + getByte80 ());
      total += getByte80 ();

      while (line.length () < 39)
        line.append (" ");
      line.append (":" + getByte82 ());
      total += getByte82 ();

      while (line.length () < 44)
        line.append (" ");
      line.append (":" + getByte132 ());
      total += getByte132 ();

      while (line.length () < 50)
        line.append (" ");
      line.append (":" + getByte134 ());
      total += getByte134 ();

      while (line.length () < 56)
        line.append (" ");
      line.append (":" + getByte144 ());
      total += getByte144 ();

      while (line.length () < 62)
        line.append (" ");
      line.append (":" + getByte146 ());
      total += getByte146 ();

      while (line.length () < 67)
        line.append (" ");
      line.append (":");
      line.append (getByte1 ());
      total += getByte1 ();

      while (line.length () < 72)
        line.append (" ");
      line.append (":");
      line.append (getByte2 ());
      total += getByte2 ();

      while (line.length () < 79)
        line.append (" ");
      line.append (":");
      line.append (getByte3 ());
      total += getByte3 ();

      while (line.length () < 85)
        line.append (" ");
      line.append (":");
      line.append (getByte4 ());
      total += getByte4 ();

      while (line.length () < 90)
        line.append (" ");
      line.append (":");
      line.append (total);

      System.out.println (line.toString ());
    }
  }

  private int getByte72 ()
  {
    int value = data[72];
    return (value - 1) * 60 + 20;
  }

  private int getByte74 ()
  {
    int value = data[74];
    return (value - 3) * 20;
  }

  private int getByte80 ()
  {
    int value = getArmourClass ();
    return (12 - value) * 40;
  }

  private int getByte82 ()
  {
    int value = data[82];
    return 60 * (int) Math.pow (2, value - 2);
  }

  private int getByte132 ()
  {
    int value = data[132];
    return 200 * (int) Math.pow (2, value - 1);
  }

  private int getByte134 ()
  {
    int value = data[134];
    return 90 * (int) Math.pow (2, value - 1);
  }

  private int getByte144 ()
  {
    int value = data[144];
    return 35 * (int) Math.pow (2, value - 1);
  }

  private int getByte146 ()
  {
    int value = data[146];
    return 35 * (int) Math.pow (2, value - 1);
  }

  private int getByte1 ()
  {
    if (data[150] > 0)
      return 60;
    return 0;
  }

  private int getByte2 ()
  {
    int val = data[152];
    int multiplier = 0;
    if (val > 0)
    {
      int tier = val / 10;
      if (tier > 9)
        tier = 9;
      multiplier = (int) Math.pow (2, tier);
      if (tier == 8)
        multiplier += 250;
      else if (tier == 9)
        multiplier += 500;
    }
    return 40 * multiplier;
  }

  private int getByte3 ()
  {
    int bits = countBits (data[154] & 0xFF, 1, 6);
    return 35 * (int) Math.pow (2, bits - 1);
  }

  private int getByte4 ()
  {
    int bits = countBits (data[156] & 0xFF, 0, 6);
    return 40 * (int) Math.pow (2, bits - 1);
  }

  private int countBits (int value)
  {
    return countBits (value, 0, 7); // count all bits
  }

  private int countBits (int value, int first, int last)
  {
    int totalBits = 0;
    for (int i = 0, weight = 1; i < 8; i++, weight *= 2)
    {
      if (i < first || i > last)
        continue;
      if ((value & weight) > 0)
        totalBits++;
    }
    return totalBits;
  }

  protected void linkMonsters (List<Monster> monsters)
  {
    int pct = getPartnerOdds ();
    if (pct > 0)
      partner = monsters.get (getPartnerID ());
  }

  public String getName ()
  {
    return genericName;
  }

  public String getRealName ()
  {
    return realName;
  }

  public String getPartnerList (int minimumOdds)
  {
    if (getPartnerOdds () >= minimumOdds && partner != null)
      return realName + "\n" + partner.getPartnerList (minimumOdds) + "\n";
    return realName + "\n";
  }

  public int getType ()
  {
    return data[78];
  }

  public int getImageID ()
  {
    return data[64];
  }

  public int getPartnerID ()
  {
    return data[140];
  }

  public int getPartnerOdds ()
  {
    return data[142];
  }

  public String getHitPoints ()
  {
    StringBuilder text = new StringBuilder ((data[72] & 0xFF) + "d" + (data[74] & 0xFF));
    if (data[76] != 0)
      text.append ("+" + (data[76] & 0xFF));
    return text.toString ();
  }

  public int getArmourClass ()
  {
    return data[80];
  }

  public int getExperience ()
  {
    return experience[monsterID];
  }

  public String getDamage ()
  {
    StringBuilder text = new StringBuilder ();
    for (int i = 0, ptr = 84; i < 8; i++, ptr += 6)
    {
      if (data[ptr] == 0)
        break;
      if (text.length () > 0)
        text.append (", ");
      text.append ((data[ptr] & 0xFF) + "d" + (data[ptr + 2] & 0xFF));
      if (data[ptr + 4] != 0)
        text.append ("+" + (data[ptr + 4] & 0xFF));
    }
    return text.toString ();
  }

  public String getGroupSize ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ((data[66] & 0xFF) + "d" + (data[68] & 0xFF));
    if (data[70] != 0)
      text.append ("+" + (data[70] & 0xFF));

    return text.toString ();
  }

  public int getSpeed ()
  {
    return data[82];
  }

  public String getMonsterClass ()
  {
    return monsterClass[data[78]];
  }

  public int getMageSpellLevel ()
  {
    return data[144];
  }

  public int getPriestSpellLevel ()
  {
    return data[146];
  }

  public int getTreasureType ()
  {
    return data[138];
  }

  @Override
  public int compareTo (Object otherMonster)
  {
    int otherType = ((Monster) otherMonster).getType ();
    int thisType = getType ();
    if (thisType == otherType)
      return 0;
    if (thisType < otherType)
      return -1;
    return 1;
  }

  @Override
  public String toString ()
  {
    StringBuilder2 line = new StringBuilder2 (genericName);

    line.tabTo (19);
    line.append (realName);
    line.appendNumber (data[72], 39);
    line.appendNumber (data[80], 45);
    line.appendNumber (data[142], 52);

    return line.toString ();
  }

  public static String getHeading ()
  {
    return new StringBuilder ().append ("Generic name       Real name")
        .append ("         Lvl   AC    HP?\n").toString ();
  }
}