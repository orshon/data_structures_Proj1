


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

    /** pre: node in the heap. merge node into the root list.
     * complexity is O(1)
	 */
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
        else{ 
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
	 *  combine root nodes of same degree to consolidate the heap by creating an arr of binomial trees
	 *  complexity W.C O(n), amortized O(logn)
	 */
	public void consolidate() {
		HeapNode[] Arr = new HeapNode[(int)(Math.log(this.n) * 2 + 1)];
		if (this.roots_num == 0 || this.root_list == null) { return; }
		HeapNode current = this.root_list;
		int cnt = 0;
		int original_roots_num = this.roots_num;
		do {
    		HeapNode x = current;
    		int x_rank = x.rank;
			HeapNode saver = x.next;
			while (Arr[x_rank] != null) { // check if there is already a node with the same rank
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
			current = saver;
			cnt++;
			
    	} while (current != this.root_list && cnt < original_roots_num);
    //find the new minimum node
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
	 * complexity W.C and amortized O(logn) 
	 */
	public void make_children_roots(HeapNode node) { 
		if (node.child != null) {
			HeapNode firstChild = node.child;
			HeapNode currentChild = firstChild;
			do {
				HeapNode saver = currentChild.next;
				currentChild.parent = null;
				currentChild.mark = false;
				this.merge_root_list(currentChild);
				currentChild = saver;
			} while (currentChild != firstChild);
			node.child = null;
			node.rank = 0;
    	}
	}

/**
 * find new min in the heap
 * complexity W.C O(n) amortized O(logn)
 */
	public void findNewMin(){
		HeapNode current = this.root_list;
		HeapNode newMin = this.min;
		if (this.min.next == this.min){ return; }
		if (this.root_list != null) {
			do {
				if (current.key < newMin.key) {
					newMin = current;  // update minimum if a smaller node is found.
				}
				current = current.next;
			} while (current != this.root_list); 
			this.min = newMin;
    	}
	}

	/**
	 * Delete the minimal item
	 * complexity W.C O(n) amortized O(logn)
	 */
	public void deleteMin()
	{
        if (this.min != null) {
            if (this.min.child != null) {
				this.make_children_roots(this.min);
            }
			this.remove_from_root_list(this.min);
			if (this.min == this.min.next && this.min.child == null) { //min_node is the only root with no children
                this.min = null;
                this.root_list = null;
				this.roots_num = 0;
				this.n = 0;
            }
			else{
				this.min = this.min.next;
				this.findNewMin();
				this.consolidate();
				this.n--;
			}
            
        }
    }

/** 
 * cut node from parent and add to root list
 * complexity W.C O(n) amortized O(1)
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
		HeapNode saver = x.next;
        x.parent = null; //make it so x isnt parent's child
		x.prev.next = x.next;
		x.next.prev = x.prev;
        parent.rank = parent.rank - 1;
        this.merge_root_list(x);
        x.mark = false;
		if (x.next == x){ parent.child = null; }
		else { parent.child = saver;}
		
    }
	/**
	 * pre: 0<diff<x.key
	 * Decrease the key of x by diff and fix the heap.
     * complexity: W.C O(n) amortized O(1)
	 */
	public void decreaseKey(HeapNode x, int diff)
    {
		if (x == null) { return; }
    	x.key = x.key - diff;
		HeapNode parent = x.parent;
    	if (parent != null && x.key < parent.key) {
            this.cut_link(x, parent);
            this.cascading_cut(parent);
        }
        if (x.key <= this.min.key) { this.min = x; }
	}

	/**
	 * delete the x from the heap.
	 * complexity W.C O(n) and amortized O(logn)
	 */
	public void delete(HeapNode x)
	{
		if (x == null) { return; }
		if (x == this.min) {
			this.deleteMin(); //deletes min and consolidates
		}
		else {
			if (x.child != null) {
				this.make_children_roots(x);
				this.remove_from_root_list(x);
				this.n--;
			}
			else {
				if (x.parent == null){
					this.remove_from_root_list(x);
					this.n--;
				}
				else {
					HeapNode parent = x.parent;
					this.cut_link(x, x.parent);
					this.cascading_cut(parent);
					this.n--;
				}
			}
		}
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
	
	}
}
