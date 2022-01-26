package com.bytezone.wizardry;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.disk.DOSDiskImage;
import com.bytezone.disk.DiskImage;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;
import com.bytezone.wizardry.Spell.SpellType;

public class ComponentFactory
{
  private int[] componentBlocksFrom = new int[4];
  private int[] componentBlocksTo = new int[4];
  private ArrayList<ScenarioData> data;
  private ArrayList<Message> messages;
  private ArrayList<BufferedImage> images;
  private ArrayList<Item> items;
  private ArrayList<Character> characters;
  private ArrayList<Spell> spells;
  private ArrayList<Monster> monsters;
  private ArrayList<MazeDataModel> levels;
  private ArrayList<ExperienceLevel> experiences;
  private long[][] experienceLevels;
  private int codeOffset;
  private int scenario;
  private DiskImage disk;

  private static final int DATA_BLOCK = 2;
  private static final int MSG_BLOCK = 3;

  private static final int HEADER_AREA = 0;
  private static final int MAZE_AREA = 1;
  private static final int MONSTER_AREA = 2;
  private static final int UNKNOWN_AREA = 3;
  private static final int ITEM_AREA = 4;
  private static final int CHARACTER_AREA = 5;
  private static final int IMAGE_AREA = 6;
  private static final int EXPERIENCE_AREA = 7;

  static String[] typeText =
      { "header", "maze", "monsters", "??", "items", "characters", "images", "char levels" };

  public ComponentFactory (File file) throws NotAWizardryDisk, NotAnAppleDisk
  {
    disk = DiskImage.createDiskImage (file);

    if (disk == null)
      throw new NotAnAppleDisk ();

    byte[] buffer = disk.getSector (0, 11);
    String text = HexFormatter.getString (buffer, 59, 13);
    if (!text.equals ("SCENARIO.DATA"))
      throw new NotAWizardryDisk (file.getAbsolutePath ());

    String version = HexFormatter.getString (buffer, 7, 3);
    scenario = 0;
    if (version.equals ("WSV"))
      scenario = 1;
    else if (version.equals ("KOD"))
    {
      switch (buffer[28] & 0xFF)
      {
        case 148:
          scenario = 2;
          break;
        case 160:
          scenario = 3;
          break;
      }
    }
    if (scenario == 0)
      System.out.println ("Unknown scenario : " + version);

    buffer = new byte[512];
    getBlock (2, buffer); // header record
    data = new ArrayList<ScenarioData> (8);

    for (int i = 0; i < 4; i++)
    {
      int ptr = i * 26;
      componentBlocksFrom[i] = Utility.intValue (buffer[ptr], buffer[ptr + 1]);
      componentBlocksTo[i] = Utility.intValue (buffer[ptr + 2], buffer[ptr + 3]);
      if (false)
      {
        int dunno = Utility.intValue (buffer[ptr + 4], buffer[ptr + 5]);
        int length = buffer[ptr + 6] & 0xFF;
        String header = HexFormatter.getString (buffer, ptr + 7, length);
        System.out.println (
            componentBlocksFrom[i] + " " + componentBlocksTo[i] + " " + dunno + " " + header);
      }
    }

    int blockOffset = componentBlocksFrom[DATA_BLOCK];
    getBlock (blockOffset, buffer);

    if (true)
    {
      int length = buffer[0] & 0xFF;
      String scenarioTitle = HexFormatter.getString (buffer, 1, length);
      System.out.println (scenarioTitle);
    }

    for (int i = 0; i < 8; i++)
      data.add (new ScenarioData (buffer, i));

    if (false)
      for (ScenarioData sd : data)
        System.out.println (sd);

    extractMessages ();
    extractCharacters ();
    extractItems ();
    extractSpells ();
    extractMonsters ();
    extractLevels ();
    extractExperienceLevels ();
    extractImages ();

    for (Character c : characters)
    {
      c.linkSpells (spells);
      c.linkItems (items);
      int type = c.getStatistics ().typeInt;
      c.linkExperience (experiences.get (type));
    }

    for (Monster m : monsters)
      m.linkMonsters (monsters);

    if (false)
    {
      buffer = disk.getSector (21, 1);
      buffer[150] = 0;
      buffer[152] = 96;
      buffer[154] = 0;
      buffer[156] = 0;
      // System.out.println (HexFormatter.format (buffer, 0, 256));
      ((DOSDiskImage) disk).putSector (21, 1, buffer);
      ((DOSDiskImage) disk).save ();
    }
  }

  public File getFile ()
  {
    return disk.getFile ();
  }

  public ArrayList<Message> getMessages ()
  {
    return messages;
  }

  public ArrayList<Character> getCharacters ()
  {
    return characters;
  }

  public ArrayList<Item> getItems ()
  {
    return items;
  }

  public ArrayList<Spell> getSpells ()
  {
    return spells;
  }

  public ArrayList<Monster> getMonsters ()
  {
    return monsters;
  }

  public ArrayList<BufferedImage> getImages ()
  {
    return images;
  }

  public ArrayList<MazeDataModel> getLevels ()
  {
    return levels;
  }

  private void extractMessages ()
  {
    messages = new ArrayList<Message> ();
    int from = componentBlocksFrom[MSG_BLOCK];
    int to = componentBlocksTo[MSG_BLOCK];
    codeOffset = 185;
    byte[] buffer = new byte[512];
    int id = 0;
    List<String> lines = new ArrayList<String> ();

    for (int i = from; i < to; i++)
    {
      getBlock (i, buffer);
      int recLen = 42;
      byte[] translation = new byte[recLen];
      String text;

      for (int ptr = 0; ptr < 504; ptr += recLen)
      {
        int length = buffer[ptr] & 0xFF;
        codeOffset--;

        if (scenario == 1)
          text = HexFormatter.getString (buffer, ptr + 1, length);
        else
        // messages are in code
        {
          for (int j = 0; j < length; j++)
          {
            translation[j] = buffer[ptr + 1 + j];
            translation[j] -= codeOffset - j * 3;
          }
          text = HexFormatter.getString (translation, 0, length);
        }
        System.out.println (id + " : " + text);

        int lastLine = buffer[ptr + 40] & 0xFF;
        lines.add (text);

        if (lastLine == 1)
        {
          messages.add (new Message (id, lines));
          id += lines.size ();
          lines.clear ();
        }
      }
    }
  }

  private void extractExperienceLevels ()
  {
    ScenarioData sd = data.get (EXPERIENCE_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;
    byte[] buffer = new byte[1024];
    long[] levelsArray = new long[13];
    experiences = new ArrayList<ExperienceLevel> ();

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i * 2, buffer);

      int seq = 0;

      for (int ptr = 0; ptr + 6 <= buffer.length; ptr += 6)
      {
        if (buffer[ptr] == 0)
          break;

        long points = Utility.intValue (buffer[ptr], buffer[ptr + 1])
            + Utility.intValue (buffer[ptr + 2], buffer[ptr + 3]) * 10000
            + Utility.intValue (buffer[ptr + 4], buffer[ptr + 5]) * 100000000L;
        levelsArray[seq++] = points;
        if (seq == 13)
        {
          seq = 0;
          experiences.add (new ExperienceLevel (levelsArray));
        }
      }
    }
  }

  private void extractImages ()
  {
    ScenarioData sd = data.get (IMAGE_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks;
    byte[] buffer = new byte[512];
    images = new ArrayList<BufferedImage> ();

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i, buffer);

      if (buffer[0] == -61 && buffer[1] == -115)
        fixSlime (buffer);

      BufferedImage image = new BufferedImage (70, 50, BufferedImage.TYPE_BYTE_GRAY);
      DataBuffer db = image.getRaster ().getDataBuffer ();
      int element = 0;
      for (int j = 0; j < 500; j += 10)
      {
        for (int k = 0; k < 10; k++)
        {
          int bits = buffer[j + k] & 0xFF;
          for (int m = 0; m < 7; m++)
          {
            if ((bits & 1) == 1)
              db.setElem (element, 255);
            bits >>= 1;
            element++;
          }
        }
      }
      images.add (image);
    }
  }

  private void fixSlime (byte[] buffer)
  {
    for (int i = 0; i < 208; i++)
      buffer[i] = 0;
    buffer[124] = -108;
    buffer[134] = -43;
    buffer[135] = -128;
    buffer[144] = -44;
    buffer[145] = -126;
    buffer[154] = -48;
    buffer[155] = -118;
    buffer[164] = -64;
    buffer[165] = -86;
    buffer[174] = -64;
    buffer[175] = -86;
    buffer[184] = -63;
    buffer[185] = -86;
    buffer[194] = -44;
    buffer[195] = -86;
    buffer[204] = -44;
    buffer[205] = -126;
  }

  private void showClassLevels ()
  {
    ScenarioData sd = data.get (EXPERIENCE_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;
    byte[] buffer = new byte[1024];
    int counter = 0;
    NumberFormat nf = NumberFormat.getInstance ();

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i * 2, buffer);
      for (int ptr = 0; ptr + 6 <= buffer.length; ptr += 6)
      {
        if (buffer[ptr] == 0)
          break;
        long points = Utility.intValue (buffer[ptr], buffer[ptr + 1])
            + Utility.intValue (buffer[ptr + 2], buffer[ptr + 3]) * 10000
            + Utility.intValue (buffer[ptr + 4], buffer[ptr + 5]) * 100000000L;
        StringBuilder2 line = new StringBuilder2 (++counter + " ");
        while (line.length () < 4)
          line.append (" ");
        line.append (": ");
        for (int j = 0; j < 6; j++)
        {
          line.append (HexFormatter.format2 (buffer[ptr + j]) + " ");
        }
        System.out.println (line.toString () + " : " + nf.format (points));
        if (counter % 13 == 0)
          System.out.println ();
      }
    }
  }

  private void showMystery1 ()
  {
    ScenarioData sd = data.get (UNKNOWN_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;
    byte[] buffer = new byte[1024];

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i * 2, buffer);
      showBytes (buffer);
    }
  }

  private void showBytes (byte[] buffer)
  {
    int reclen = 168;
    for (int ptr = 0; ptr < 1008; ptr += reclen)
    {
      // System.out.println(HexFormatter.format(buffer, ptr, recLen));
      StringBuilder line = new StringBuilder ();
      for (int k = 0; k < 6; k++)
        line.append (HexFormatter.format2 (buffer[ptr + k]) + " ");
      System.out.println (line.toString ());
      for (int j = 6; j < reclen; j += 18)
      {
        line = new StringBuilder ();
        for (int k = 0; k < 18; k++)
          line.append (HexFormatter.format2 (buffer[ptr + j + k]) + " ");
        System.out.println (line.toString ());
      }
      System.out.println ();
    }
  }

  private void extractItems ()
  {
    items = new ArrayList<Item> ();
    ScenarioData sd = data.get (ITEM_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;
    byte[] buffer = new byte[1024];

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i * 2, buffer);
      addItems (buffer);
    }
  }

  private void addItems (byte[] buffer)
  {
    int recLen = 78;
    for (int ptr = 0; ptr < 1014; ptr += recLen)
    {
      int nameLength = buffer[ptr] & 0xFF;

      if (nameLength == 0)
        break;
      String itemName = HexFormatter.getString (buffer, ptr + 1, nameLength);

      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);

      items.add (new Item (itemName, data2));
    }
  }

  private void extractCharacters ()
  {
    characters = new ArrayList<Character> ();
    byte[] buffer = new byte[1024];
    ScenarioData sd = data.get (CHARACTER_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i * 2, buffer);
      addCharacters (buffer);
    }
  }

  private void addCharacters (byte[] buffer)
  {
    int recLen = 208;
    for (int ptr = 0; ptr < 832; ptr += recLen)
    {
      int nameLength = buffer[ptr] & 0xFF;
      if (nameLength == 0xC3 || buffer[ptr + 40] == 0x07)
        continue;

      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);

      characters.add (new Character (data2));
    }
  }

  private void extractLevels ()
  {
    levels = new ArrayList<MazeDataModel> ();
    ScenarioData sd = data.get (MAZE_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;
    byte[] buffer = new byte[1024];

    for (int level = 0; level < max; level++)
    {
      getBlock (firstBlock + level * 2, buffer);
      byte[] data2 = new byte[896];
      System.arraycopy (buffer, 0, data2, 0, 896);
      MazeDataModel model = new MazeDataModel (data2, level + 1);
      model.setMessages (messages);
      model.setMonsters (monsters);
      model.setItems (items);
      levels.add (model);
    }
  }

  private void extractMonsters ()
  {
    monsters = new ArrayList<Monster> ();
    ScenarioData sd = data.get (MONSTER_AREA);
    int firstBlock = sd.dataOffset + componentBlocksFrom[DATA_BLOCK];
    int max = sd.totalBlocks / 2;
    byte[] buffer = new byte[1024];

    for (int i = 0; i < max; i++)
    {
      getBlock (firstBlock + i * 2, buffer);
      addMonsters (buffer);
    }
  }

  private void addMonsters (byte[] buffer)
  {
    int recLen = 158;
    for (int ptr = 0; ptr < 948; ptr += recLen)
    {
      int nameLength = buffer[ptr] & 0xFF;
      if (nameLength == 0 || nameLength == 255)
        break;
      String itemName = HexFormatter.getString (buffer, ptr + 1, nameLength);

      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);
      monsters.add (new Monster (itemName, data2));
    }
  }

  private void extractSpells ()
  {
    spells = new ArrayList<Spell> ();
    byte[] buffer = new byte[512];

    switch (scenario)
    {
      case 1:
        getBlock (153, buffer);
        addSpells (buffer, SpellType.MAGE);
        getBlock (154, buffer);
        addSpells (buffer, SpellType.PRIEST);
        break;
      case 2:
        getBlock (152, buffer);
        addSpells (buffer, SpellType.MAGE);
        getBlock (153, buffer);
        addSpells (buffer, SpellType.PRIEST);
        break;
      case 3:
        getBlock (161, buffer);
        addSpells (buffer, SpellType.MAGE);
        getBlock (162, buffer);
        addSpells (buffer, SpellType.PRIEST);
        break;
    }
  }

  private void addSpells (byte[] buffer, SpellType type)
  {
    int level = 1;
    int ptr = -1;
    while (ptr < 255)
    {
      ptr++;
      int start = ptr;
      while (buffer[ptr] != 0x0D)
        ptr++;
      if (ptr == start)
        break;
      String spell = HexFormatter.getString (buffer, start, ptr - start);
      if (spell.startsWith ("*"))
      {
        spell = spell.substring (1);
        ++level;
      }

      Spell s = Spell.getSpell (spell, type, level);
      spells.add (s);
    }
  }

  private void getBlock (int block, byte[] buffer)
  {
    for (int offset = 0; offset + 512 <= buffer.length; offset += 512)
    {
      int track = block / 8;
      int index = block % 8;

      int sector = (index == 0) ? 0 : 15 - index * 2;
      disk.getSector (track, sector, buffer, offset);

      sector = (sector <= 1) ? sector + 14 : sector - 1;
      disk.getSector (track, sector, buffer, offset + 256);

      block++;
    }
  }

  class ExperienceLevel
  {
    private long[] expLevels;

    public ExperienceLevel (long[] data)
    {
      expLevels = new long[13];
      for (int i = 0; i < 13; i++)
        expLevels[i] = data[i];
    }

    public long getExperiencePoints (int level)
    {
      if (level < 13)
        return expLevels[level];
      return (level - 12) * expLevels[0] + expLevels[12];
    }

    @Override
    public String toString ()
    {
      StringBuilder line = new StringBuilder ();
      for (long exp : expLevels)
        line.append (exp + " ");
      return line.toString ();
    }
  }

  class ScenarioData
  {
    int dunno;
    int total;
    int totalBlocks;
    int dataOffset;
    int type;

    public ScenarioData (byte[] buffer, int seq)
    {
      int offset = 42 + seq * 2;
      dunno = buffer[offset] & 0xFF;
      total = buffer[offset + 16] & 0xFF;
      totalBlocks = buffer[offset + 32] & 0xFF;
      dataOffset = buffer[offset + 48] & 0xFF;
      type = seq;
    }

    @Override
    public String toString ()
    {
      StringBuilder line = new StringBuilder (typeText[type]);
      while (line.length () < 12)
        line.append (" ");
      line.append (dunno);
      while (line.length () < 16)
        line.append (" ");
      line.append (total);
      while (line.length () < 20)
        line.append (" ");
      line.append (totalBlocks);
      while (line.length () < 24)
        line.append (" ");
      line.append (dataOffset);

      return line.toString ();
    }
  }
}

class NotAWizardryDisk extends Exception
{
  NotAWizardryDisk (String s)
  {
    super (s);
  }
}

class NotAnAppleDisk extends Exception
{

}