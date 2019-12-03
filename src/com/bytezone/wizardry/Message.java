package com.bytezone.wizardry;

import java.util.ArrayList;
import java.util.List;

public class Message
{
	List<String> text = new ArrayList<String> ();
  int messageID;
	
	public Message (int ID, String message)
	{
		text.add (message);
    messageID = ID;
	}
  
  public boolean match (int messageNum)
  {
    if (messageID == messageNum)
      return true;
    
    // this code is to allow for a bug in scenario #1
    if (messageNum > messageID && messageNum < (messageID + text.size ()))
      return true;
    
    return false;
  }
  
  public Message (int ID, List<String> messages)
  {
    text.addAll (messages);
    messageID = ID;
    System.out.println ("adding " + ID);
    for (String s : messages)
      System.out.println ("    " + s);
  }
	
	public String toString ()
	{
    StringBuilder message = new StringBuilder ();
    for (String line : text)
      message.append (line + "\n");
    if (message.length() > 0)
      message.deleteCharAt(message.length () - 1);    // remove newline
		return message.toString ();
	}
  
  public String toHTMLString ()
  {
    StringBuilder message = new StringBuilder ();
    for (String line : text)
      message.append ("&nbsp;" + line + "&nbsp;<br>");
    if (message.length() > 0)
      for (int i = 0; i < 4; i++)
        message.deleteCharAt(message.length () - 1);  // remove <br> tag
    return message.toString ();
  }
}
