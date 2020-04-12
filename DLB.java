
public class DLB implements DictInterface 
{
	private char termChar = '^';
	Node rootNode;

	public DLB()
	{
		rootNode = new Node();
	}

	// Adds a string to the DLB structure character by character
	public boolean add(String s)	
	{
		s += termChar;
		Node curr = rootNode;

		boolean added = false;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			Result result = addChild(curr, c);
			curr = result.node;
			added = result.added; 
		}
		
		return added;
	}

	// Searches for a StringBuilder within the tree. If no start and end are defined, it uses 0 and the length of the SB
	public int searchPrefix(StringBuilder s)
	{
		return (searchPrefix(s, 0, s.length()-1));
	}

	// Same as above, but uses a start and end index in the case of a split word
	public int searchPrefix(StringBuilder s, int start, int end)
	{
		end++; // The DLB implementation needs the full length of the search key

		Node curr = rootNode;
		for (int i = start; i < end; i++)
		{
			char c = s.charAt(i);
			curr = getChild(curr, c);

			if (curr == null)	// Not a word or prefix
			{
				return 0;
			}
		}

		Node termNode = getChild(curr, termChar);

		if (termNode == null)
			return 1;	// Child doesn't have a terminating character. Is a prefix
		else if (termNode.peer == null)
			return 2;	// Has a terminating node, but it has no peers. Is a word, not a prefix.
		else
			return 3;	// Has a terminating node with peers. Word and prefix
	}

	// Used to add a character to the DLB
	private Result addChild(Node parent, char c)
	{
		if (parent.child == null)
		{
			parent.child = new Node(c);
			return new Result(parent.child, true);
		}
		else
			return addPeer(parent.child, c);
	}

	// If the letter will not be a child, it is added as a peer
	private Result addPeer(Node start, char c)
	{
		if (start == null)
		{
			start = new Node(c);
			return new Result(start, true);
		}
		else
		{
			Node next = start;
			while (next.peer != null)
			{
				if (next.value == c)
				{
					break;
				}
				next = next.peer;
			}
			if (next.value == c)
				return new Result (next, false);	// Already exists, no need to add
			else
			{
				next.peer = new Node(c);
				return new Result(next.peer, true);
			}
		}
	}

	// Returns the current node or moves to the peers of the node
	private Node getPeer(Node start, char c)
	{
		Node next = start;

		while (next != null)
		{
			if (next.value == c)
			{	// If the character is the same, break
				break;
			}
			next = next.peer;	// If the letter was not the same, go to peer nodes
		}
		return next;
	}

	/** This is a pass through getPeer, which will check if the 
	* 	character in the node is what we are searching for. 
	*/
	private Node getChild(Node curr, char c)
	{
		return getPeer(curr.child, c);	
	}
}


// Node class is used for the nodes of the DLB. Each node has a value, a child, and a peer to build the trie structure
class Node
{
	Node peer;
	Node child;
	char value;
	
	public Node() {}
	
	public Node(char value)
	{
		this(value, null, null);
	}
	
	public Node(char value, Node peer, Node child) // Each node can have a value, peer and a child (can be null)
	{
		this.value = value;
		this.peer = peer;
		this.child = child;
	}
}

// Result class tells us if a node was added and gives us that node for use in the traversal
class Result
{
	Node node;
	boolean added;
	
	public Result(Node node, boolean added)
	{
		this.node = node;
		this.added = added;
	}
}