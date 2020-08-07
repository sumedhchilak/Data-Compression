public class HuffManTester {
    public static void main(String[] args) {
        HuffPriorityQueue<TreeNode> hf = new HuffPriorityQueue<>();
        hf.enqueue(new TreeNode(1021, 1));
        hf.enqueue(new TreeNode(1455, 1));
        hf.enqueue(new TreeNode(10321,1));
        hf.enqueue(new TreeNode(1234, 2));
        hf.enqueue(new TreeNode(1231, 2));
        hf.enqueue(new TreeNode(1051, 2));
        hf.enqueue(new TreeNode(1857, 4));
        hf.enqueue(new TreeNode(1341, 8));
        //System.out.println(hf);
        TreeNode iNode = hf.dequeue();
        TreeNode jNode = hf.dequeue();
        TreeNode kNode = new TreeNode(iNode, iNode.getFrequency() + jNode.getFrequency(), jNode);
        hf.enqueue(kNode);
        System.out.println(hf);


        HuffPriorityQueue<TreeNode> testPQ = new HuffPriorityQueue<>();
        testPQ.enqueue(new TreeNode(123, 1));
        testPQ.enqueue(new TreeNode(133, 1));
        testPQ.enqueue(new TreeNode(130, 1));
        testPQ.enqueue(new TreeNode(145, 1));
        testPQ.enqueue(new TreeNode(150, 1));
        
        // testPQ.enqueue(new TreeNode(1231, 2));
        // testPQ.enqueue(new TreeNode(1051, 2));
        // //-- 
        // testPQ.enqueue(new TreeNode(1857, 4));
        // testPQ.enqueue(new TreeNode(1341, 8));
        HuffmanCodeTree myTree = new HuffmanCodeTree(testPQ);
        System.out.println(myTree.encodedBits());
        // System.out.println(myTree.root.getFrequency() + "yo");
   
    }
}
