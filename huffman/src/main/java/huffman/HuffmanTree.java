package huffman;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class HuffmanTree implements Serializable {

	private static final long serialVersionUID = 1L;

	private Node root;

	public Node getRoot() {
		return root;
	}

	public HuffmanTree() {
	}

	/**
	 * Create leaves from all entries in the hash table.
	 * 
	 * @param frequencyTable
	 *            - Map<ASCII code, frequency>
	 */
	public HuffmanTree(ConcurrentHashMap<Integer, AtomicLong> frequencyTable) {
		PriorityQueue<Node> treesOfLeaves = new PriorityQueue<Node>();
		for (Entry<Integer, AtomicLong> entry : frequencyTable.entrySet()) {
			Node leaf = new Node(entry.getKey(), entry.getValue().get(), null,
					null);
			treesOfLeaves.add(leaf);
		}
		constructTree(treesOfLeaves);
	}

	private void constructTree(PriorityQueue<Node> trees) {
		if (trees.size() == 1) {
			Node onlyNode = trees.poll();
			root = onlyNode;
		} else {
			Node unionTree = null;
			while (trees.size() > 1) {
				Node firstTree = trees.poll();
				Node secondTree = trees.poll();
				unionTree = new Node(null, firstTree.getFrequence()
						+ secondTree.getFrequence(), firstTree, secondTree);
				trees.add(unionTree);
			}
			root = unionTree;
		}
		/*
		 * Debugging purposes: printBinaryTree(root, 0); printTree(root);
		 */
	}

	/**
	 * create Hash<ASCII code, CodeSymbol(byte of the code, and the actual
	 * length of the 'needed' bits), e.g. <65,CodeSymbol(0000 0001, 2), if 'A'
	 * have code 01
	 * 
	 * @return
	 */
	public HashMap<Integer, CodeSymbol> codeTable() {
		HashMap<Integer, CodeSymbol> codeTable = new HashMap<>();
		Byte codedBits = 0;
		allPathsToLeaves(root, codeTable, new CodeSymbol(codedBits, 0));
		return codeTable;
	}

	private void allPathsToLeaves(Node node,
			HashMap<Integer, CodeSymbol> codeTable, CodeSymbol code) {
		if (node == null) {
			return;
		}
		if (node.getLeft() == null && node.getRight() == null) {
			// leaf node is reached
			codeTable.put(node.getSymbol(), code);
			return;
		}

		long left = (long) (code.code << 1);
		long right = (long) (left | 1);
		CodeSymbol leftCode = new CodeSymbol(left, code.length + 1);
		CodeSymbol rightCode = new CodeSymbol(right, code.length + 1);
		allPathsToLeaves(node.getLeft(), codeTable, leftCode);
		allPathsToLeaves(node.getRight(), codeTable, rightCode);
	}

	public void exportTree(String filename) {
		try (FileOutputStream fileOut = new FileOutputStream(filename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);) {
			out.writeObject(root);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void restoreTree(String filename) {
		try (FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn);) {
			root = (Node) in.readObject();
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "HuffmanTree [root=" + root + "]";
	}

	public void printTree(Node tmpRoot) {

		Queue<Node> currentLevel = new LinkedList<Node>();
		Queue<Node> nextLevel = new LinkedList<Node>();
		currentLevel.add(tmpRoot);
		while (!currentLevel.isEmpty()) {
			Iterator<Node> iter = currentLevel.iterator();
			while (iter.hasNext()) {
				Node currentNode = iter.next();
				if (currentNode.getLeft() != null) {
					nextLevel.add(currentNode.getLeft());
				}
				if (currentNode.getRight() != null) {
					nextLevel.add(currentNode.getRight());
				}
				System.out.print(currentNode.getSymbol() + " ");
			}
			System.out.println();
			currentLevel = nextLevel;
			nextLevel = new LinkedList<Node>();

		}

	}

	// test
	public void printBinaryTree(Node root, int level) {
		if (root == null)
			return;
		printBinaryTree(root.getRight(), level + 1);
		if (level != 0) {
			for (int i = 0; i < level - 1; i++)
				System.out.print("|\t");
			System.out.println("|-------" + root.getSymbol());
		} else
			System.out.println(root.getSymbol());
		printBinaryTree(root.getLeft(), level + 1);
	}

}
