
import java.io.*;
import java.util.*;

public class Crossword
{
  int n; // Board size n x n
  char[][] board;
  StringBuilder[] colStr;
  StringBuilder[] rowStr;
  char alphabet[] = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
  DictInterface dic;
  String dicType;
  int solutionCount = 0;

  public Crossword(String fileName, DictInterface dict, String dt) throws IOException
  {
    dic = dict;
    dicType = dt;

    // Initializes the board
    Scanner fileScan = new Scanner(new FileInputStream(fileName));
    fileScan.useDelimiter("");
    n = fileScan.nextInt();
    System.out.println("Board is " + n + " by " + n);
    fileScan.nextLine();
    board = new char[n][n];

    // Fills the board structure from the given file
    for(int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
      {
        String s = fileScan.next();
        if (s.equals("+") || s.equals("-") || Character.isLetter(s.charAt(0)))
          board[j][i] = s.charAt(0);
        else
          j--;
      }
    }
    fileScan.close();
    printBoard(board);
    
    // Create the StringBuilder arrays which will allow us to "build" the board and backtrack if needed
    colStr = new StringBuilder[n];
    rowStr = new StringBuilder[n];
    for (int i = 0; i < colStr.length; i++) 
    {
      colStr[i] = new StringBuilder("");
      rowStr[i] = new StringBuilder("");
    }

    // The first call to solve(), which begins solving the board
    boolean solution = solve(0, 0);

    // Prints out the number of solutions if DLB was selected and exits the program
    if (dicType.equals("DLB"))
    {
      System.out.println("Number of solutions: " + solutionCount);
    }
    System.out.println("Finished running, exiting...");
    System.exit(0);

  }

  // Simply prints the current board
  private void printBoard(char[][] board)
  {
    System.out.println();
    for (int i=0; i < n; i++)
    {
      for (int j=0; j < n; j++)
        System.out.print(board[j][i]);
      System.out.println();
    }
    System.out.println();
  }

  // Prints out the board in progress using one of the StringBuilder arrays
  private void printProgress(StringBuilder[] board)
  {
    System.out.println();
    for (int i = 0; i < board.length; i++)
      System.out.println(board[i]);
  }


  /*
  *  Finds the last occurrance of '-' or the beginning of the stringbuilder,
  *  and returns the index ahead of the '-' or 0
  */
  private int findStart(StringBuilder str)
  {
    if (str.length() == 0) // word hasn't been started
      return 0;
    for (int i = str.length()-1; i >= 0; i--)
    {
      if (str.charAt(i) == '-')
        return i+1; // Return the index ahead of the '-' to prevent prefix search with '-'
    }
    return 0; // No '-' occurs
  }

  /*
  *   The recursive method that solves the board square by square. [x,y] are coordinates on the given board
  *   and through this, it finds what occupies the space and appends it to the stringbuilders. 
  */
  private boolean solve(int x, int y)
  {
    int resCol = -1;
    int resRow = -1;

    if (board[x][y] == '+') // Any alphabetic character can be appended, check if prefix/word
    {
      // Try appending each letter of the alphabet
      for(char c: alphabet)
      {
        rowStr[y].append(c);
        colStr[x].append(c);

        // Get prefix/word results for the column and row
        if (rowStr[y].length() == 1)
          resRow = 3; // Single characters are words per the assignment sheet
        else
          resRow = dic.searchPrefix(rowStr[y], findStart(rowStr[y]), rowStr[y].length()-1);
        if (colStr[x].length() == 1)
          resCol = 3;
        else
          resCol = dic.searchPrefix(colStr[x], findStart(colStr[x]), colStr[x].length()-1);

        // After appending the letter, one of the words is not a prefix/word
        if ((resRow == 0) || (resCol == 0)) 
        {
          rowStr[y].deleteCharAt(rowStr[y].length()-1);
          colStr[x].deleteCharAt(colStr[x].length()-1);
          continue;
        }
        // Last square of board
        else if ((y==n-1) && (x==n-1))
        {
          if (((resRow == 3) || (resRow == 2)) && ((resCol == 3) || (resCol == 2))) // Horizontal & Vertical word, board done
          {
            if (dicType.equals("DLB"))
            {
              solutionCount++;
              if (solutionCount % 10000 == 0)
                printProgress(rowStr);
              else if (solutionCount == 1)
                System.out.println("First solution found! (Used for timing)");

              rowStr[y].deleteCharAt(rowStr[y].length()-1);
              colStr[x].deleteCharAt(colStr[x].length()-1);
              continue; // Print out the solution and continue
            }
            else
            {
              printProgress(rowStr);
              return true;  // Print out the first solution, end search
            }
          }
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            continue;
          }
        }
        // Bottom edge of board
        else if (y == n-1)
        {
          if (((resRow == 3) || (resRow == 1)) && ((resCol == 3) || (resCol == 2))) // Horizontal prefix, vertical word)
          {
            if (solve(x+1, y)) // Go to next square to the right
              return true;
            else
            {
              rowStr[y].deleteCharAt(rowStr[y].length()-1);
              colStr[x].deleteCharAt(colStr[x].length()-1);
              continue;
            }
          }
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            continue;
          }
        }
        // Right edge of board
        else if (x == n-1)
        {
          if (((resRow == 3) || (resRow==2)) && ((resCol == 1) || (resCol == 3))) // Horizontal word, Vertical Prefix
          {
            if (solve(0, y+1)) // Go to next row if a horizontal word
              return true;
            else
            {
              rowStr[y].deleteCharAt(rowStr[y].length()-1);
              colStr[x].deleteCharAt(colStr[x].length()-1);
              continue;
            }
          }
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            continue;
          }
        }
        // Not at edges
        else 
        {
          if (((resRow == 1) || (resRow == 3)) && ((resCol == 1) || (resCol == 3))) // Vertical and horizontal prefix
          {
            if (solve(x+1, y))
              return true;
            else
            {
              rowStr[y].deleteCharAt(rowStr[y].length()-1);
              colStr[x].deleteCharAt(colStr[x].length()-1);
              continue;
            }
          }
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            continue;
          }
        }
      }
      return false; // If alphabet has no valid characters for this space
    }

    else if (board[x][y] == '-') // Terminating char, check if word, return false if not
    {
      // If the '-' is to be the only character in the stringbuilder, let it be seen as a word
      // If is is not the only character, but the previous character is also '-', mark as a word
      if (rowStr[y].length() == 0)
        resRow = 3;
      else
        resRow = dic.searchPrefix(rowStr[y], findStart(rowStr[y]), rowStr[y].length()-1); 

      if (colStr[x].length() == 0)
        resCol = 3;
      else
        resCol = dic.searchPrefix(colStr[x], findStart(colStr[x]), colStr[x].length()-1); 

    
      if ((resRow == 0) || (resCol == 0) || (resRow == 1) || (resCol == 1)) // Not a word
      {
        return false;
      }
      else if ((y==n-1) && (x==n-1))
      {
        if (((resRow == 3) || (resRow == 2)) && ((resCol == 3) || (resCol == 2))) // Horizontal & Vertical word, board done
        {
          rowStr[y].append(board[x][y]);
          colStr[x].append(board[x][y]);

          if (dicType.equals("DLB"))
          {
            solutionCount++;
            if (solutionCount % 10000 == 0)
              printProgress(rowStr);
            else if (solutionCount == 1)
              System.out.println("First solution found!");
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false; // Print out the solution and continue
          }
          else
          {
            printProgress(rowStr);
            return true;  // Print out the first solution
          }
        }
        else
          return false;
      }
      else if (x == n-1) // Right edge of board
      {
        if (((resRow == 3) || (resRow == 2)) && ((resCol == 3) || (resCol == 2))) // Both are words
        {
          rowStr[y].append(board[x][y]);
          colStr[x].append(board[x][y]);
          if (solve(0, y+1))
            return true;
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false;
          }
        }
        else
        {
          return false;
        }
      }
      else  // Any non-right edge space
      {
        if (((resRow == 3) || (resRow == 2)) && ((resCol == 3) || (resCol == 2))) // Both are words
        {
          rowStr[y].append(board[x][y]);
          colStr[x].append(board[x][y]);
          if (solve(x+1, y))
            return true;
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false;
          }
        }
        else
        {
          return false;
        }
      }
    }

    else // Only the letter in this space can be appended
    {
      // Append the found letter
      rowStr[y].append(board[x][y]);
      colStr[x].append(board[x][y]);

      // Get prefix/word results for the column and row
        if (rowStr[y].length() == 1)
          resRow = 3; // Single characters are words per the assignment sheet
        else
          resRow = dic.searchPrefix(rowStr[y], findStart(rowStr[y]), rowStr[y].length()-1);
        if (colStr[x].length() == 1)
          resCol = 3;
        else
          resCol = dic.searchPrefix(colStr[x], findStart(colStr[x]), colStr[x].length()-1);

      // After appending the letter, one of the words is not a prefix/word
      if ((resRow == 0) || (resCol == 0)) 
      {
        rowStr[y].deleteCharAt(rowStr[y].length()-1);
        colStr[x].deleteCharAt(colStr[x].length()-1);
        return false;
      }
      // Last square of board
      else if ((y==n-1) && (x==n-1))
      {
        if (((resRow == 3) || (resRow == 2)) && ((resCol == 3) || (resCol == 2))) // Horizontal & Vertical word, board done
        {
          if (dicType.equals("DLB"))
          {
            solutionCount++;
            if (solutionCount % 10000 == 0)
              printProgress(rowStr);
            else if (solutionCount == 1)
              System.out.println("First solution found!");
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false; // Print out the solution and continue
          }
          else
          {
            printProgress(rowStr);
            return true; // Print first solution
          }
        }
        else
        {
          rowStr[y].deleteCharAt(rowStr[y].length()-1);
          colStr[x].deleteCharAt(colStr[x].length()-1);
          return false;
        }
      }
      // Bottom edge of board
      else if (y == n-1)
      {
        if (((resRow == 3) || (resRow == 1)) && ((resCol == 3) || (resCol == 2))) // Horizontal prefix, vertical word)
        {
          if (solve(x+1, y)) // Go to next square to the right
            return true;
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false;
          }
        }
        else
        {
          rowStr[y].deleteCharAt(rowStr[y].length()-1);
          colStr[x].deleteCharAt(colStr[x].length()-1);
          return false;
        }
      }
      // Right edge of board
      else if (x == n-1)
      {
        if (((resRow == 3) || (resRow==2)) && ((resCol == 1) || (resCol == 3))) // Horizontal word, Vertical Prefix
        {
          if (solve(0, y+1)) // Go to next row if a horizontal word
            return true;
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false;
          }
        }
        else
        {
          rowStr[y].deleteCharAt(rowStr[y].length()-1);
          colStr[x].deleteCharAt(colStr[x].length()-1);
          return false;
        }
      }
      // Not at edges
      else 
      {
        if (((resRow == 1) || (resRow == 3)) && ((resCol == 1) || (resCol == 3))) // Vertical and horizontal prefix
        {
          if (solve(x+1, y))
            return true;
          else
          {
            rowStr[y].deleteCharAt(rowStr[y].length()-1);
            colStr[x].deleteCharAt(colStr[x].length()-1);
            return false;
          }
        }
        else
        {
          rowStr[y].deleteCharAt(rowStr[y].length()-1);
          colStr[x].deleteCharAt(colStr[x].length()-1);
          return false;
        }
      }
    }
    
  }

  public static void main(String [] args) throws IOException
  {
    // Uses the command line arguments to decide what structure to store the dictionary in
    DictInterface dict;
    String dictType = args[0];
    if (dictType.equals("DLB"))
      dict = new DLB();
    else
      dict = new MyDictionary();

    // Load the dictionary into the structure
    String str;
    Scanner dicScan = new Scanner(new FileInputStream("dict8.txt"));
    while (dicScan.hasNext())
    {
      str = dicScan.nextLine();
      dict.add(str);
    }

    // Gets the board file from the user
    System.out.print("Enter the file name of a crossword board: ");
    Scanner reader = new Scanner(System.in);
    String file = reader.nextLine();
    reader.close();

    Crossword c = new Crossword(file, dict, dictType);

  }
}
