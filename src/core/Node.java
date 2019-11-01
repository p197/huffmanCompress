package core;

/**
 * @author 12130
 * @date 2019/10/29
 * @time 20:51
 * <p>
 * Huffman树中的节点
 */
public class Node implements Comparable<Node> {
    /**
     * 当前节点代表的字符
     */
    public byte value;
    /**
     * 当前节点出现的次数
     */
    public int count;

    public Node left;
    public Node right;

    public Node(byte value, int count) {
        this.value = value;
        this.count = count;
    }

    public Node(int count) {
        this.count = count;
    }

    @Override
    public int compareTo(Node o) {
        if (this.count > o.count) {
            return 1;
        } else if (this.count < o.count) {
            return -1;
        }
        return 0;
    }

    public void setChildren(Node leftChild, Node rightChild) {
        this.left = leftChild;
        this.right = rightChild;
    }
}
