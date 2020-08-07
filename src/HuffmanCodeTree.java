import java.util.Map;
import java.util.TreeMap;

/*  Student information for assignment:
 *
 *  On our honor, Kaustub and Sumedh, this programming assignment is our own work
 *  and we have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1: Kaustub Navalady
 *  UTEID: kan2235
 *  email address: kaustub.nvd@gmail.com
 *  Grader name: Amir
 *
 *  Student 2: Sumedh Chilakamarri
 *  UTEID: ssc2536
 *  email address: sumedh.chilak@utexas.edu
 *
 */

public class HuffmanCodeTree implements IHuffConstants {
    private TreeNode root;
    private int size;
    private Map<Integer, String> map;
    private TreeNode pointer;

    public HuffmanCodeTree(HuffPriorityQueue<TreeNode> priorityQueue) {
        final int DUMMY_VALUE = -1;
        while (priorityQueue.size() > 1) {
            TreeNode leftNode = priorityQueue.dequeue();
            TreeNode rightNode = priorityQueue.dequeue();
            // new nodes weight is the sum of left and right node taken from priorityqueue
            priorityQueue.enqueue(new TreeNode(leftNode, DUMMY_VALUE, rightNode));
        }
        // last node dequeued is the root
        this.root = priorityQueue.dequeue();
        preOrderTraversal(); // Calculate size
        pointer = root;
    }

    public HuffmanCodeTree() {
        root = null;
    }

    // This method creates a map of bit-chunks to Huffman encodings
    public Map<Integer, String> encodedBits() {
        map = new TreeMap<>();
        mapHelper(map, root, "");
        return map;
    }

    private void mapHelper(Map<Integer, String> map, TreeNode node, String path) {
        final String LEFT = "0";
        final String RIGHT = "1";
        if (node.isLeaf()) {
            map.put(node.getValue(), path);
        } else {
            // "0" added for every left traversal and "1" added for every right traversal
            mapHelper(map, node.getLeft(), path + LEFT);
            mapHelper(map, node.getRight(), path + RIGHT);
        }
    }

    public int size() {
        return this.size;
    }

    public String preOrderTraversal() {
        return preOrderHelper(root);
    }

    // recursive helper for STF compression
    private String preOrderHelper(TreeNode node) {
        final String INTERNAL_NODE = "0";
        final String LEAF_NODE = "1";
        if (node != null) {
            size++;
            if (node.isLeaf()) {
                int bitLength = Integer.toBinaryString(node.getValue()).length();
                // 1 to signify leaf, followed by leading zeroes and the binary form of leaf node val
                return LEAF_NODE + leadingZeroes((BITS_PER_WORD + 1) - bitLength)
                        + Integer.toBinaryString(node.getValue());
            } else {
                String bits = INTERNAL_NODE;
                bits += preOrderHelper(node.getLeft());
                bits += preOrderHelper(node.getRight());
                return bits;
            }
        } else {
            return "";
        }
    }

    // leading zeroes for padding
    private String leadingZeroes(int n) {
        final String LEADING_ZERO = "0";
        String result = "";
        for (int i = 0; i < n; i++) {
            result += LEADING_ZERO;
        }
        return result;
    }

    public int uncompress(int bit) {
        final int NOT_LEAF = -1;
        final int DONE = -2;
        // pointer goes left when bit is 0 and right when 1
        if (bit == 0) {
            pointer = pointer.getLeft();
        } else {
            pointer = pointer.getRight();
        }
        if (pointer.isLeaf()) {
            int value = pointer.getValue();
            // pointer set back to root for next iteration
            pointer = root;
            // finishes when pseudo val reached
            if (value == PSEUDO_EOF) {
                return DONE;
            }
            return value;
        } else {
            return NOT_LEAF;
        }
    }

    // method for recreating huffman tree STF
    public void stfDecode(int bit, int val) {
        final int DUMMY_VALUE = -1;
        size++;
        if (root == null) {
            root = new TreeNode(DUMMY_VALUE, DUMMY_VALUE);
            pointer = root;
        } else {
            stfHelper(root, bit, val);
        }
    }

    private boolean stfHelper(TreeNode node, int bit, int val) {
        final int DUMMY_VALUE = -1;
        if (node.getValue() < 0) {
            if (node.getLeft() == null) {
                if (bit == 1) { // Leaf
                    node.setLeft(new TreeNode(val, DUMMY_VALUE));
                } else { // Internal Node
                    node.setLeft(new TreeNode(DUMMY_VALUE, DUMMY_VALUE));
                }
                return true;
            }
            boolean added = stfHelper(node.getLeft(), bit, val);
            // now checks right
            if (!added) {
                if (node.getRight() == null) {
                    if (bit == 1) {
                        // leaf node
                        node.setRight(new TreeNode(val, DUMMY_VALUE));
                        // internal node
                    } else {
                        node.setRight(new TreeNode(DUMMY_VALUE, DUMMY_VALUE));
                    }
                    return true;
                }
                if (stfHelper(node.getRight(), bit, val)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
}