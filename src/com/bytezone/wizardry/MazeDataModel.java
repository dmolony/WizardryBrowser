package com.bytezone.wizardry;

import java.util.List;

import com.bytezone.disk.HexFormatter;

public class MazeDataModel
{
  byte[] data;
  int level;
  private List<Message> messages;
  private List<Monster> monsters;
  private List<Item> items;

  public MazeDataModel(byte[] buffer, int level)
  {
    this.data = buffer;
    this.level = level;
  }

  public int getRows ()
  {
    return 20;
  }

  public int getColumns ()
  {
    return 20;
  }

  public int getLevel ()
  {
    return level;
  }

  public void setMessages (List<Message> messages)
  {
    this.messages = messages;
  }

  public void setMonsters (List<Monster> monsters)
  {
    this.monsters = monsters;
  }

  public void setItems (List<Item> items)
  {
    this.items = items;
  }

  public MazeCell getLocation (int row, int column)
  {
    MazeAddress address = new MazeAddress (level, row, column);
    MazeCell cell = new MazeCell (address);

    // doors and walls

    int offset = column * 6 + row / 4; // 6 bytes/column

    int value = HexFormatter.intValue (data[offset]);
    value >>>= (row % 4) * 2;
    cell.westWall = ((value & 1) == 1);
    value >>>= 1;
    cell.westDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (data[offset + 120]);
    value >>>= (row % 4) * 2;
    cell.southWall = ((value & 1) == 1);
    value >>>= 1;
    cell.southDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (data[offset + 240]);
    value >>>= (row % 4) * 2;
    cell.eastWall = ((value & 1) == 1);
    value >>>= 1;
    cell.eastDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (data[offset + 360]);
    value >>>= (row % 4) * 2;
    cell.northWall = ((value & 1) == 1);
    value >>>= 1;
    cell.northDoor = ((value & 1) == 1);

    // monster table

    offset = column * 4 + row / 8; // 4 bytes/column, 1 bit/row
    value = HexFormatter.intValue (data[offset + 480]);
    value >>>= row % 8;
    cell.monsterLair = ((value & 1) == 1);

    // stairs, pits, darkness etc.

    offset = column * 10 + row / 2; // 10 bytes/column, 4 bits/row
    value = HexFormatter.intValue (data[offset + 560]);
    int b = (row % 2 == 0) ? value % 16 : value / 16;
    int c = HexFormatter.intValue (data[760 + b / 2]);
    int d = (b % 2 == 0) ? c % 16 : c / 16;

    switch (d)
    {
      case 1:
        cell.stairs = true;
        cell.addressTo = getAddress (b);
        break;
      case 2:
        cell.pit = true;
        break;
      case 3:
        cell.chute = true;
        cell.addressTo = getAddress (b);
        break;
      case 4:
        cell.spinner = true;
        break;
      case 5:
        cell.darkness = true;
        break;
      case 6:
        cell.teleport = true;
        cell.addressTo = getAddress (b);
        break;
      case 8:
        cell.elevator = true;
        cell.elevatorTo = HexFormatter.intValue (data[800 + b * 2],
                data[801 + b * 2]);
        cell.elevatorFrom = HexFormatter.intValue (data[832 + b * 2],
                data[833 + b * 2]);
        break;
      case 9:
        cell.rock = true;
        break;
      case 10:
        cell.spellsBlocked = true;
        break;
      case 11:
        int messageNum = HexFormatter.intValue (data[800 + b * 2],
                data[801 + b * 2]);
        for (Message m : messages)
          if (m.match (messageNum))
          {
            cell.message = m;
            break;
          }
        if (cell.message == null)
          System.out.println ("message not found : " + messageNum);
        cell.messageType = HexFormatter.intValue (data[832 + b * 2],
                data[833 + b * 2]);
        
        int itemID = -1;

        if (cell.messageType == 2) // obtain Item
        {
          itemID = HexFormatter.intValue (data[768 + b * 2],
                  data[769 + b * 2]);
          cell.itemObtained = items.get (itemID);
        }
        if (cell.messageType == 5) // requires Item
        {
          itemID = HexFormatter.intValue (data[768 + b * 2],
                  data[769 + b * 2]);
          cell.itemRequired = items.get (itemID);
        }
        if (cell.messageType == 4)
        {
          value = HexFormatter
                  .intValue (data[768 + b * 2], data[769 + b * 2]);
          if (value <= 100)
          {
            cell.monsterID = value;
            cell.monsters = monsters;
          }
          else
            cell.itemObtained = items.get ((value - 64536) * -1); // check this
        }
        break;
      case 12:
        cell.monsterID = HexFormatter.intValue (data[832 + b * 2],
                data[833 + b * 2]);
        cell.monsters = monsters;
        break;
      default:
        cell.unknown = d;
        break;
    }

    return cell;
  }

  private MazeAddress getAddress (int a)
  {
    int b = a * 2;
    return new MazeAddress (HexFormatter.intValue (data[768 + b],
            data[769 + b]), HexFormatter.intValue (data[800 + b],
            data[801 + b]), HexFormatter.intValue (data[832 + b],
            data[833 + b]));
  }
}
