package unluac.decompile;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import unluac.Version;
import unluac.parse.LBoolean;
import unluac.parse.LNil;
import unluac.parse.LNumber;
import unluac.parse.LObject;
import unluac.parse.LString;

public class Constant {

  private static final Set<String> reservedWords = new HashSet<String>();
  
  static {
    reservedWords.add("and");
    reservedWords.add("break");
    reservedWords.add("do");
    reservedWords.add("else");
    reservedWords.add("elseif");
    reservedWords.add("end");
    reservedWords.add("false");
    reservedWords.add("for");
    reservedWords.add("function");
    reservedWords.add("if");
    reservedWords.add("in");
    reservedWords.add("local");
    reservedWords.add("nil");
    reservedWords.add("not");
    reservedWords.add("or");
    reservedWords.add("repeat");
    reservedWords.add("return");
    reservedWords.add("then");
    reservedWords.add("true");
    reservedWords.add("until");
    reservedWords.add("while");
  }
  
  private final int type;
  
  private final boolean bool;
  private final LNumber number;
  private final String string;

  public Constant(int constant) {
    type = 2;
    bool = false;
    number = LNumber.makeInteger(constant);
    string = null;
  }
  
  public Constant(LObject constant) {
    if(constant instanceof LNil) {
      type = 0;
      bool = false;
      number = null;
      string = null;
    } else if(constant instanceof LBoolean) {
      type = 1;
      bool = constant == LBoolean.LTRUE;
      number = null;
      string = null;
    } else if(constant instanceof LNumber) {
      type = 2;
      bool = false;
      number = (LNumber) constant;
      string = null;
    } else if(constant instanceof LString) {
      type = 3;
      bool = false;
      number = null;
      string = ((LString) constant).deref();
    } else {
      throw new IllegalArgumentException("Illegal constant type: " + constant.toString());
    }
  }

  public static int isUTF8(String buff, int startIndex) {
    int utf8length;
    int c = buff.charAt(startIndex) & 0xFF;
    if (c < 0x80) {
      utf8length = 1;
    } else if (c < 0xC) {
      utf8length = 0;
      return utf8length;
    } else if (c < 0xE0) {
      utf8length = 2;
    } else if (c < 0xF0) {
      utf8length = 3;
    } else if (c < 0xF8) {
      utf8length = 4;
    } else if (c < 0xFC) {
      utf8length = 5;
    } else if (c < 0xFE) {
      utf8length = 6;
    } else {
      utf8length = 0;
      return utf8length;
    }

    // Out of index.
    if (utf8length > (buff.length() - startIndex)) {
      utf8length = 0;
      return utf8length;
    }

    for (int i=startIndex+1;i < startIndex + utf8length; i++) {
      c = buff.charAt(i) & 0xFF;
      if ((c & 0xC0) != 0x80) {
        utf8length = 0;
        return utf8length;
      }
    }

    return utf8length;
  }
  
  public void print(Decompiler d, Output out, boolean braced) {

    switch(type) {
      case 0:
        out.print("nil");
        break;
      case 1:
        out.print(bool ? "true" : "false");
        break;
      case 2:
        out.print(number.toString());
        break;
      case 3:
        out.print("\"");
        if (true) {
          int utf8length = 0;
          for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == 7) {
              out.print("\\a");
            } else if (c == 8) {
              out.print("\\b");
            } else if (c == 12) {
              out.print("\\f");
            } else if (c == 10) {
              out.print("\\n");
            } else if (c == 13) {
              out.print("\\r");
            } else if (c == 9) {
              out.print("\\t");
            } else if (c == 11) {
              out.print("\\v");
            } else if (c == 34) {
              out.print("\\\"");
            } else if (c == 92) {
              out.print("\\\\");
            } else {
              if (c >= 0x20 && c <= 0x7F) {
                out.print(Character.toString(c));
              } else if ((utf8length = isUTF8(string, i)) > 1) {
                for (int j=i; j<i+utf8length; j++) {
                  char cc = string.charAt(j);
                  out.print((byte)cc);
                }
                i += (utf8length - 1);
              } else {
                String str = String.format("\\%02X", c & 0xFF);
                out.print(str);
              }
            }
          }
        }
        out.print("\"");
        break;
      default:
        throw new IllegalStateException();
    }
  }
  
  public boolean isNil() {
    return type == 0;
  }
  
  public boolean isBoolean() {
    return type == 1;
  }
  
  public boolean isNumber() {
    return type == 2;
  }
  
  public boolean isInteger() {
    return number.value() == Math.round(number.value());
  }
  
  public int asInteger() {
    if(!isInteger()) {
      throw new IllegalStateException();
    }
    return (int) number.value();
  }
  
  public boolean isString() {
    return type == 3;
  }
  
  public boolean isIdentifier() {
    if(!isString()) {
      return false;
    }
    if(reservedWords.contains(string)) {
      return false;
    }
    if(string.length() == 0) {
      return false;
    }
    char start = string.charAt(0);
    if(start != '_' && !Character.isLetter(start)) {
      return false;
    }
    for(int i = 1; i < string.length(); i++) {
      char next = string.charAt(i);
      if(Character.isLetter(next)) {
        continue;
      }
      if(Character.isDigit(next)) {
        continue;
      }
      if(next == '_') {
        continue;
      }
      return false;
    }
    return true;
  }
  
  public String asName() {
    if(type != 3) {
      throw new IllegalStateException();
    }
    return string;
  }
  
}
