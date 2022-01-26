package com.bytezone.wizardry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.bytezone.diskbrowser.applefile.HiResImage;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

public class AppleDump
{
  private File saveFile;
  private int signature;
  private byte[] header;
  private byte[] buffer;

  private HiResImage image;
  private ArrayList<Item> items;
  private ArrayList<Character> characters;

  private static String[] facing = { "North", "East", "South", "West" };
  private static NumberFormat nf = NumberFormat.getInstance ();

  private static int[] experience = { 55, 235, 415, 230, 380, 620, 840, 520, 550, 350, // 00-09
      475, 515, 920, 600, 735, 520, 795, 780, 990, 795, // 10-19
      1360, 1320, 1275, 680, 960, 600, 755, 1120, 2070, 870, // 20-29
      960, 1120, 1120, 2435, 1080, 2280, 975, 875, 1135, 1200, // 30-39
      620, 740, 1460, 1245, 960, 1405, 1040, 1220, 1520, 995, // 40-49
      665, 2340, 2160, 2395, 790, 1140, 1235, 1790, 1720, 2240, // 50-59
      1475, 1540, 1720, 1900, 1240, 1220, 1020, 20435, 5100, 3515, // 60-69
      2115, 2920, 2060, 2140, 1400, 1640, 1280, 4450, 42840, 3300, // 70-79
      40875, 5000, 3300, 2395, 1935, 1600, 3330, 44090, 40840, 5200, // 80-89
      4150, 3000, 9200, 3160, 7460, 0, 0, 0, 0, 1000, 0 // 90-100
  };

  public AppleDump (File f)
  {
    if (!f.exists ())
    {
      System.out.println ("File not found : " + f.getAbsolutePath ());
      return;
    }

    saveFile = f;
    readFile ();
  }

  public byte[] getBuffer ()
  {
    return buffer;
  }

  public void setBuffer (byte[] buffer)
  {
    this.buffer = buffer;
  }

  public void setItems (ArrayList<Item> items)
  {
    this.items = items;
  }

  public void setCharacters (ArrayList<Character> characters)
  {
    this.characters = characters;
  }

  public String getLocation ()
  {
    int level = buffer[29484];
    if (level <= 0)
      return "Castle";

    StringBuilder text = new StringBuilder ();
    text.append ("Level " + level + " " + buffer[29486] + "N ");
    text.append (buffer[29488] + "E, facing " + facing[buffer[29482]]);

    return text.toString ();
  }

  public String getCharacters ()
  {
    StringBuilder text = new StringBuilder ();
    int totalCharacters = buffer[29442];

    for (int i = 0; i < totalCharacters; i++)
    {
      int ptr = 29584 + i * 208;
      int length = buffer[ptr];
      if (length <= 0)
        break;

      int level = buffer[ptr + 132] & 0xFF;
      StringBuilder line = new StringBuilder ("  " + level);

      if (level < 10)
        line.append ("  ");
      else
        line.append (" ");

      String name = HexFormatter.getString (buffer, ptr + 1, length);
      line.append (name);

      long nextLevel = 0;
      int exp = Utility.intValue (buffer[ptr + 124], buffer[ptr + 125])
          + Utility.intValue (buffer[ptr + 126], buffer[ptr + 127]) * 10000;

      for (Character character : characters)
        if (character.getName ().equals (name))
        {
          nextLevel = character.getNextLevel ();
          break;
        }

      String expNeeded = nf.format ((nextLevel - exp));
      while (line.length () < 28 - expNeeded.length ())
        line.append (" ");
      line.append (expNeeded);

      int totalItems = buffer[ptr + 58];
      for (int j = 0; j < totalItems; j++)
      {
        int itemNo = buffer[ptr + 66 + j * 8];
        if (buffer[ptr + 64 + j * 8] == 0) // item is unidentified
        {
          while (line.length () < 33)
            line.append (" ");
          if (items == null)
            line.append (itemNo);
          else
          {
            Item item = items.get (itemNo);
            String cost = nf.format (item.getCost ());
            line.append (item.getName ());
            while (line.length () < 60 - cost.length ())
              line.append (" ");
            line.append (cost);
          }
          text.append (line.toString () + "\n");
          line = new StringBuilder ();
        }
      }
      if (line.length () > 0)
        text.append (line.toString ());
      if (text.charAt (text.length () - 1) != '\n')
        text.append ("\n");
    }
    return text.toString ();
  }

  public HiResImage getHiResScreen ()
  {
    if (image == null)
    {
      byte[] screen = new byte[8192];
      System.arraycopy (buffer, 8192, screen, 0, 8192);
      image = new HiResImage (screen);
    }
    return image;
  }

  public String getBiffo ()
  {
    String t = HexFormatter.getString (buffer, 28417, 9);
    if (t.equals ("NEW DELAY"))
      return "";

    for (int i = 0; i < 4; i++) // check four monster names
    {
      int length = buffer[28340 + i * 16];
      if (length <= 0 || length > 15)
        return "";
    }

    StringBuilder text = new StringBuilder ();
    String name;
    int totalCharacters = buffer[29442];
    int totalExperience = 0;

    for (int i = 0; i < 4; i++)
    {
      int ptr = 28206 + i * 292;

      int totalMonsters = buffer[ptr + 2] & 0xFF;
      int originalTotalMonsters = buffer[ptr + 4] & 0xFF;
      int monster = buffer[ptr + 6];
      if (totalMonsters < 0 || totalMonsters > 12 || monster == -1)
        break;

      int exp = experience[monster];
      totalExperience += exp * originalTotalMonsters;

      if (totalMonsters == 1)
      {
        int length = buffer[ptr + 166] & 0xFF;
        name = HexFormatter.getString (buffer, ptr + 167, length);
      }
      else
      {
        int length = buffer[ptr + 182] & 0xFF;
        name = HexFormatter.getString (buffer, ptr + 183, length);
      }

      // for (int j = 0; j < 8; j++)
      // text.append (HexFormatter.format2 (buffer[ptr + j]) + " ");

      text.append (totalMonsters + " " + name);
      text.append (" (" + exp + "/" + HexFormatter.format2 (buffer[ptr + 6]) + ")\n");

      for (int j = 0; j < totalMonsters; j++)
      {
        text.append ("   ");
        // for (int k = 0; k < 14; k++)
        // text.append (HexFormatter.format2 (buffer[ptr + 8 + j * 14 + k])
        // + " ");
        text.append ("#" + (j + 1) + " " + (buffer[ptr + 14 + j * 14] & 0xFF) + "HP");
        text.append ("\n");
      }
      text.append ("\n");
    }

    text.append ("Total experience : " + totalExperience / totalCharacters + " each");

    return text.toString ();
  }

  public String getTextScreen ()
  {
    StringBuilder text = new StringBuilder ();

    for (int j = 0; j < 3; j++)
      for (int i = 0; i < 8; i++)
      {
        int ptr = 1024 + i * 128 + j * 40;
        text.append (HexFormatter.getString (buffer, ptr, 40) + "\n");
      }

    return text.toString ();
  }

  @Override
  public String toString ()
  {
    if (buffer == null)
      return "No file";

    StringBuilder text = new StringBuilder ();
    int blocksSkipped = 0;
    for (int i = 0; i < buffer.length - 256; i += 256)
    {
      boolean nonZeroFound = false;
      for (int j = i; j < i + 256; j++)
      {
        if (buffer[j] != 0)
        {
          nonZeroFound = true;
          break;
        }
      }
      if (nonZeroFound)
      {
        if (blocksSkipped > 0)
        {
          text.append (blocksSkipped + " skipped\n");
          blocksSkipped = 0;
        }
        text.append (HexFormatter.format (buffer, i, 256) + "\n\n");
      }
      else
        blocksSkipped++;
    }
    if (blocksSkipped > 0)
      text.append (blocksSkipped + " skipped\n");

    return text.toString ();
  }

  private void readFile ()
  {
    try
    {
      RandomAccessFile in = new RandomAccessFile (saveFile, "r");
      signature = in.readInt ();
      if (signature != 1096242003)
        return;

      buffer = new byte[(int) saveFile.length () - 112];
      header = new byte[108];

      in.read (header);
      in.read (buffer);
      in.close ();
    }
    catch (IOException e)
    {
      System.out.println (e);
      return;
    }

    return;
  }

  public void writeFile ()
  {
    try
    {
      RandomAccessFile out = new RandomAccessFile (saveFile, "rw");
      out.writeInt (signature);
      out.write (header);
      out.write (buffer);
      out.close ();
    }
    catch (IOException e)
    {
      System.out.println (e);
      return;
    }

  }
}
