package com.bytezone.wizardry;

import java.text.NumberFormat;

public class StringBuilder2
{
  StringBuilder string;
  private static String[] hex =
      { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
  private static NumberFormat nf = NumberFormat.getInstance ();

  public StringBuilder2 ()
  {
    string = new StringBuilder ();
  }

  public StringBuilder2 (String text)
  {
    string = new StringBuilder (text);
  }

  public void append (String text)
  {
    string.append (text);
  }

  public void append (byte b)
  {
    int value = intValue (b);
    if (value < 0)
      value += 256;
    string.append (hex[value / 16] + hex[value % 16]);
  }

  public static int intValue (byte b1)
  {
    int i1 = b1;
    if (i1 < 0)
      i1 += 256;

    return i1;
  }

  public void appendNumber (int value, int column)
  {
    if (nf == null)
      nf = NumberFormat.getInstance ();

    String text = nf.format (value);
    tabTo (column - text.length ());
    string.append (text);
  }

  public void tabTo (int column)
  {
    while (string.length () < column)
      string.append (" ");
  }

  @Override
  public String toString ()
  {
    return string.toString ();
  }

  public int length ()
  {
    return string.length ();
  }
}
