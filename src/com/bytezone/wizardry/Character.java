package com.bytezone.wizardry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;
import com.bytezone.wizardry.ComponentFactory.ExperienceLevel;

public class Character
{
  private String name;
  private byte[] data;
  private Attributes attributes;
  private Statistics stats;
  private ExperienceLevel experience;

  private Collection<Spell> spellBook = new ArrayList<Spell> ();
  private Collection<Baggage> baggageList = new ArrayList<Baggage> ();

  private static NumberFormat nf = NumberFormat.getInstance ();

  static String[] races = { "No race", "Human", "Elf", "Dwarf", "Gnome", "Hobbit" };
  static String[] alignments = { "Unalign", "Good", "Neutral", "Evil" };
  static String[] types =
      { "Fighter", "Mage", "Priest", "Thief", "Bishop", "Samurai", "Lord", "Ninja" };
  static String[] statuses =
      { "OK", "Afraid", "Asleep", "Paralyze", "Stoned", "Dead", "Ashes", "Lost" };

  public Character (byte[] buffer)
  {
    this.data = buffer;

    int nameLength = data[0] & 0xFF;
    String charName = new String (data, 1, nameLength);
    name = charName;

    attributes = new Attributes ();
    stats = new Statistics ();
    attributes.array = new int[6];

    stats.race = races[data[34] & 0xFF];
    stats.typeInt = data[36] & 0xFF;
    stats.type = types[stats.typeInt];
    stats.ageInWeeks = Utility.intValue (data[38], data[39]);
    stats.statusValue = data[40];
    stats.status = statuses[stats.statusValue];
    stats.alignment = alignments[data[42] & 0xFF];

    stats.gold =
        Utility.intValue (data[52], data[53]) + Utility.intValue (data[54], data[55]) * 10000;
    stats.experience =
        Utility.intValue (data[124], data[125]) + Utility.intValue (data[126], data[127]) * 10000;
    stats.level = Utility.intValue (data[132], data[133]);

    stats.hitsLeft = Utility.intValue (data[134], data[135]);
    stats.hitsMax = Utility.intValue (data[136], data[137]);
    stats.armourClass = data[176];

    attributes.strength = (data[44] & 0xFF) % 16;
    if (attributes.strength < 3)
      attributes.strength += 16;
    attributes.array[0] = attributes.strength;

    int i1 = (data[44] & 0xFF) / 16;
    int i2 = (data[45] & 0xFF) % 4;
    attributes.intelligence = i1 / 2 + i2 * 8;
    attributes.array[1] = attributes.intelligence;

    attributes.piety = (data[45] & 0xFF) / 4;
    attributes.array[2] = attributes.piety;

    attributes.vitality = (data[46] & 0xFF) % 16;
    if (attributes.vitality < 3)
      attributes.vitality += 16;
    attributes.array[3] = attributes.vitality;

    int a1 = (data[46] & 0xFF) / 16;
    int a2 = (data[47] & 0xFF) % 4;
    attributes.agility = a1 / 2 + a2 * 8;
    attributes.array[4] = attributes.agility;

    attributes.luck = (data[47] & 0xFF) / 4;
    attributes.array[5] = attributes.luck;
  }

  public void linkItems (List<Item> itemList)
  {
    boolean equipped;
    boolean identified;
    int totItems = data[58];
    stats.assetValue = 0;

    for (int ptr = 60; totItems > 0; ptr += 8, totItems--)
    {
      int itemID = data[ptr + 6] & 0xFF;
      Item item = itemList.get (itemID);
      equipped = (data[ptr] == 1);
      identified = (data[ptr + 4] == 1);
      baggageList.add (new Baggage (item, equipped, identified));
      stats.assetValue += item.getCost ();
      item.partyOwns++;
    }
  }

  public void linkSpells (List<Spell> spellList)
  {
    for (int i = 138; i < 145; i++)
    {
      for (int bit = 0; bit < 8; bit++)
      {
        byte b = data[i];
        if (((b >>> bit) & 1) == 1)
        {
          int index = (i - 138) * 8 + bit;
          spellBook.add (spellList.get (index - 1));
        }
      }
    }
  }

  public void linkExperience (ExperienceLevel exp)
  {
    this.experience = exp;
    stats.nextLevel = exp.getExperiencePoints (stats.level);
  }

  public int[] getMageSpellPoints ()
  {
    int[] spells = new int[7];

    for (int i = 0; i < 7; i++)
      spells[i] = data[146 + i * 2];

    return spells;
  }

  public int[] getPriestSpellPoints ()
  {
    int[] spells = new int[7];

    for (int i = 0; i < 7; i++)
      spells[i] = data[160 + i * 2];

    return spells;
  }

  public String getName ()
  {
    return name;
  }

  public Long getNextLevel ()
  {
    return stats.nextLevel;
  }

  public boolean isWinner ()
  {
    return (data[206] == 1);
  }

  public boolean isOut ()
  {
    return (data[32] == 1);
  }

  public String getType ()
  {
    return stats.type;
  }

  public String getRace ()
  {
    return stats.race;
  }

  public String getAlignment ()
  {
    return stats.alignment;
  }

  public Attributes getAttributes ()
  {
    return attributes;
  }

  public Statistics getStatistics ()
  {
    return stats;
  }

  public Iterator<Baggage> getBaggage ()
  {
    return baggageList.iterator ();
  }

  public Iterator<Spell> getSpells ()
  {
    return spellBook.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuffer line = new StringBuffer (name);
    while (line.length () < 17)
      line.append (" ");
    if (true)
    {
      for (int i = 182; i < 208; i++)
        line.append (HexFormatter.format2 (data[i]) + " ");
      return line.toString ();
    }
    return name + "\n" + HexFormatter.format (data, 32, 176);
  }
}

class Baggage
{
  public Item item;
  public boolean equipped;
  public boolean identified;

  public Baggage (Item item, boolean equipped, boolean identified)
  {
    this.item = item;
    this.equipped = equipped;
    this.identified = identified;
  }

  @Override
  public String toString ()
  {
    if (equipped)
      return "*" + item.getName ();
    return " " + item.getName ();
  }
}

class Statistics implements Cloneable
{
  public String race;
  public String type;
  public String alignment;
  public String status;
  public int typeInt;
  public int statusValue;
  public int gold;
  public int experience;
  public long nextLevel;
  public int level;
  public int ageInWeeks;
  public int hitsLeft;
  public int hitsMax;
  public int armourClass;
  public int assetValue;
}

class Attributes
{
  public int strength;
  public int intelligence;
  public int piety;
  public int vitality;
  public int agility;
  public int luck;
  public int[] array;
}