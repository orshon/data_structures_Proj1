


/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	public HeapNode min;
    public int n;
    public HeapNode root_list;
	public int roots_num;
	
	/**
	 *
	 * Constructor to initialize an empty heap.
	 *
	 */
	public FibonacciHeap()
    {
        this.min = null;
        this.n = 0;
        this.root_list = null; //points to first root
		this.roots_num = 0; //number of roots
	}

   /** pre: node in the heap. merge node into the root list. */
    //complexity is O(1)

	public void merge_root_list(HeapNode node){
		if (this.root_list == null){
			this.root_list = node;
		}
		else{
			node.next = this.root_list;
			node.prev = this.root_list.prev;
			this.root_list.prev.next = node;
			this.root_list.prev = node;
		}
		this.roots_num++;
	}

	/**
	 * merge a node with the doubly linked child list of a root node
	 * complexity is O(1)
	 */
	public void merge_child_list(HeapNode parent, HeapNode node) {
		if (parent.child == null) {
			parent.child = node;
			node.next = node;
            node.prev = node;
			}
        else{ //inserting based on rank as instructed
			node.next = parent.child.next;
			node.prev = parent.child;
			parent.child.next.prev = node;
			parent.child.next = node;
		}
	}

	/**
	 * pre: key > 0
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *complexity is O(1)
	 */
    public HeapNode insert(int key, String info)
	{
		HeapNode node = new HeapNode(key);
        node.prev = node;
        node.next = node;
        this.merge_root_list(node); //updates root_num too
        if (this.min == null || node.key <= this.min.key) {
            this.min = node;
        }
        this.n++;
        return node;
	}

	/**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 * complexity is O(1)
	 *
	 */
	public HeapNode findMin() { return this.min; }

	/**
	 * remove root from root list field
	 * complexity O(1)
	 */

    public void remove_from_root_list(HeapNode node) {
        if (node == this.root_list) { this.root_list = node.next; }
		//if (node == this.min) { this.min = node.next; }
        node.prev.next = node.next;
        node.next.prev = node.prev;
		this.roots_num--;
    }

	/**
	 * create link between two nodes of a heap
	 * complexity is O(1)
	 */
	public void create_link(HeapNode y, HeapNode x) {
		this.remove_from_root_list(y); //updates root_num too
		y.prev = y;
		y.next = y;
		this.merge_child_list(x, y);
		x.rank = x.rank + 1;
		y.parent = x;
		y.mark = false;
	}

  /**
	*   function to iterate through a doubly linked list
	*/
    public void iterate(HeapNode head) {
        HeapNode node = head;
        HeapNode stop = head;
        boolean flag = false;
        while (true) {
            if (node == stop && flag == true){ break; }
			else if (node == stop){ flag = true; }
            node = node.next;
		}
	}

	/**
	 *  combine root nodes of same degree to consolidate the heap by creating an arr of binomial trees
	 *  complexity W.C O(n), amortized O(logn)
	 */

	public void consolidate() {
		HeapNode[] Arr = new HeapNode[(int)(Math.log(this.n) * 2 + 1)];
		HeapNode[] nodes = new HeapNode[this.roots_num]; //W.C n roots
		if (this.roots_num == 0 || this.root_list == null) { return; }
		nodes[0] = this.root_list;
		HeapNode current = this.root_list;
		//for (int i = 0; i < this.roots_num; i++) {
        //    System.out.print("curr = " + current.key + " -> ");
        //    current = current.next;
        //}
		int index = 0;
		do {
    		nodes[index++] = current;
    		current = current.next;
		} while (current != this.root_list && index < this.roots_num);
		for (int i = 0; i < index; i++) {
				HeapNode x = nodes[i];
				int x_rank = x.rank;
				while (Arr[x_rank] != null) {// doesnt this make it nlogn? :(
					HeapNode y = Arr[x_rank];
					if (x.key > y.key) {
						HeapNode temp = x;
						x = y;
						y = temp;
					}
					create_link(y, x);
					Arr[x_rank] = null;
					x_rank++;
				}
				Arr[x_rank] = x;
			}
			//for (int i=0; i < Arr.length; i++) {
			//	if (Arr[i] != null) {
			//		if (Arr[i].key < this.min.key) { this.min = Arr[i]; }
			//	}
			//}
			 // Reconstruct root list and find new minimum
			this.min = null;
			for (HeapNode node : Arr) {
				if (node != null) {
					if (this.min == null || node.key <= this.min.key) {
						this.min = node;
					}
				}
			}
		}


	/**
	 * make all children of a node roots
	 * complexity W.C O(logn) amortized O(1)
	 */
	public void make_children_roots(HeapNode node) { //in fibonacci heap max children per node is logn
		if (node.child != null){
			boolean is_first_run = true;
			HeapNode first_child = node.child;
			HeapNode curr_child = node.child;
			while (curr_child.next != curr_child && (is_first_run || curr_child != first_child)) {
				is_first_run = false;
				HeapNode saver = curr_child.next;
				this.merge_root_list(curr_child);
				curr_child.parent = null;
				if (curr_child.next != null && saver != node && saver != first_child){ curr_child = saver; }
				else { break; }
			}
			if (curr_child.next == curr_child) {
				this.merge_root_list(curr_child); 
			}
		}
	}


	/**
	 * Delete the minimal item
	 * complexity O(logn)
	 */
	public void deleteMin()
	{
        if (this.min != null) {
            if (this.min.child != null) {
				this.make_children_roots(this.min);
            }
			this.remove_from_root_list(this.min);
			if (this.min == this.min.next && this.min.child == null) { //min_node is the only root wiht no children
                this.min = null;
                this.root_list = null;
				this.roots_num = 0;
				this.n = 0;
            }
			else{
                this.min = this.min.next;
				HeapNode current = this.root_list;
				int cnt = 0;
				while (cnt < roots_num && current.next != null && current.next != current) {
					cnt++;
					if (this.min.key >= current.key) {
						this.min = current;
					}
					current = current.next;
				}
                this.consolidate();
				this.n--;
            }
        }
    }

/** 
 * cut node from parent and add to root list
 * complexity W.C O(logn) amortized O(1)
 */
    public void cascading_cut(HeapNode node){
		HeapNode parent = node.parent;
        if (parent != null) { //is child of a node
            if (node.mark == false) {
                node.mark = true;
            }
            else {
                this.cut_link(node, parent);
                this.cascading_cut(parent);
            }
        }
    }

/**
 * cut link between x and parent
 * complexity is O(1)
 */
    public void cut_link(HeapNode x, HeapNode parent){
        x.parent = null; //make it so x isnt parent's child
		x.prev.next = x.next;
		x.next.prev = x.prev;
        parent.rank = parent.rank - 1;
        this.merge_root_list(x); //updates root_num too
        x.mark = false;
		if (x.next == x){ parent.child = null; }
		else { parent.child = x.next;}
		
    }
	/**
	 * pre: 0<diff<x.key
	 * Decrease the key of x by diff and fix the heap.
     * complexity: amortized O(1)
	 */
	public void decreaseKey(HeapNode x, int diff)
    {
		if (x == null) { return; }
    	x.key = x.key - diff;
		HeapNode parent = x.parent;
    	if (parent != null && x.key < parent.key) {
            this.cut_link(x, parent);
            this.cascading_cut(parent);
			System.out.println("enetereddddddddddddddd");
			this.printHeap();
        }
        if (x.key <= this.min.key) { this.min = x; }
	}

	/**
	 * delete the x from the heap.
	 * complexity W.C O(logn) amortized O(1)
	 */
	public void delete(HeapNode x)
	{
		if (x == null) { return; }
		if (x == this.min) {
			this.deleteMin(); //deletes min and consolidates
		}
		else {
			if (x.parent == null){ // is a root
				this.remove_from_root_list(x);
				if (x.child != null) {
					HeapNode first = x.child;
					while(x.child != null && x.child.next != first) {
						this.cascading_cut(x.child);
					}
				}
			}
			else { // not a root 
				while (x.child != null) { //has max logn children
					this.cascading_cut(x);
					this.cascading_cut(x.child);
				}
				this.remove_from_root_list(x);
			}
		}
    	this.n--;
	}

	/**
	 * Return the total number of links.
	 * 
	 */
	public int totalLinks()
	{
		return 0; // should be replaced by student code
	}


	/**
	 * 
	 * Return the total number of cuts.
	 * 
	 */
	public int totalCuts()
	{
		return 0; // should be replaced by student code
	}


	/**
	 * 
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2)
	{
		return; // should be replaced by student code   		
	}

	/**
	 * 
	 * Return the number of elements in the heap
	 *   complexity O(1)
	 */
	public int size() { return this.n; }


	/**
	 * 
	 * Return the number of trees in the heap.
	 * 
	 */
	public int numTrees()
	{
		return 0; // should be replaced by student code
	}

	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *  
	 */
	public static class HeapNode{
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank;
		public boolean mark;


        public HeapNode(int key){
            this.key = key;
            this.info = "";
            this.child = null;
            this.next = null;
            this.prev = null;
            this.rank = 0;
            this.mark = false;
        }

		public int getKey() { return this.key; }
	}
 
	public void printHeap() {
		if (this.root_list == null) {
			System.out.println("Heap is empty.");
			return;
		}
		System.out.println("Fibonacci Heap Structure:");
		HeapNode current = this.root_list;
		do {
			printNode(current, 0); // Start printing from the root list
			current = current.next;
		} while (current != this.root_list);
	}

	// Helper method to print a single node and its children recursively
	private void printNode(HeapNode node, int depth) {
		
		// Print indentation based on depth level
		for (int i = 0; i < depth; i++) {
			System.out.print("    "); // 4 spaces per depth level
		}
		System.out.println("Node key: " + node.key + ", Rank: " + node.rank + ", Mark: " + node.mark);
		if (node.child != null) {
			HeapNode child = node.child;
			do {
				printNode(child, depth + 1); // Recursive call for each child
				child = child.next;
			} while (child != node.child); // Traverse all siblings in the circular list
		}
	}

public int potential() {
		int t = 0; // Number of trees (roots)
		int m = 0; // Number of marked nodes
		HeapNode current = min;

		if (current != null) {
			// Traverse the circular linked list of roots
			do {
				t++; // Each root is a tree
				m += countMarkedNodes(current); // Count marked nodes in the tree
				current = current.next;
			} while (current != min);
		}

		return t + 2 * m; // Potential is t + 2 * m
	}

	// Helper method to count marked nodes in a tree
	private int countMarkedNodes(HeapNode node) {
		int count = 0;
		while (node != null) {
			if (node.mark) count++; // Increment if the node is marked
			node = node.child;
		}
		return count;
	}

	public int[] countersRep() {
		int[] counters = new int[calculateMaxRank()]; // Array to store the number of trees of each rank
		HeapNode current = min;

		// Traverse the root list and count trees by rank
		if (current != null) {
			do {
				int rank = current.rank;
				counters[rank]++; // Increment the count of trees of this rank
				current = current.next;
			} while (current != min);
		}

		return counters;
	}

	// Helper method to calculate the maximum possible rank (based on the number of nodes in the heap)
	private int calculateMaxRank() {
		return (int) Math.ceil(Math.log(this.n) / Math.log(2)) + 1;
	}
	public static void main(String[] args) {
        Test tester = new Test();
        tester.runAllTests();
    }
}
