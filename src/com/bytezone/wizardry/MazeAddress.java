package com.bytezone.wizardry;

public class MazeAddress
{
  public final int level;
  public final int row;
  public final int column;
  
  public MazeAddress (int level, int row, int column)
  {
    this.level = level;
    this.row = row;
    this.column = column;
  }
  
  public String toString ()
  {
    return level + "/" + row + "/" + column;
  }
}
