package huffman;

import java.io.Serializable;

public class Node implements Comparable<Node>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer symbol;
	private Long frequence;
	private Node left;
	private Node right;

	public Node(Integer symbol, Long frequency, Node left, Node right) {
		this.symbol = symbol;
		this.frequence = frequency;
		this.left = left;
		this.right = right;
	}

	@Override
	public int compareTo(Node o) {
		return frequence.compareTo(o.getFrequence());
	}

	@Override
	public String toString() {
		return "Node [symbol=" + symbol + ", frequence=" + frequence
				+ ", left=" + left + ", right=" + right + "]";
	}

	public Long getFrequence() {
		return frequence;
	}

	public Node getLeft() {
		return left;
	}

	public Integer getSymbol() {
		return symbol;
	}

	public Node getRight() {
		return right;
	}

}
