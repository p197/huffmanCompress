package core;

import java.util.PriorityQueue;

/**
 * @author 12130
 * @date 2019/10/29
 * @time 20:47
 */
public class HuffmanTree {

    /**
     * 构造huffman树，返回根节点
     *
     * @param queue
     * @return
     */
    public static Node getHuffmanTree(PriorityQueue<Node> queue) {
        while (queue.size() > 1) {
            Node leftChild = queue.poll();
            Node rightChild = queue.poll();
            Node parent = new Node(leftChild.count + rightChild.count);
            parent.setChildren(leftChild, rightChild);
            queue.add(parent);
        }
        if (queue.size() > 0) {
            return queue.poll();
        } else {
            return null;
        }
    }
}
