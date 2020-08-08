import java.util.Iterator;
import java.util.LinkedList;

public class HuffPriorityQueue<E extends Comparable<? super E>> { 

    private LinkedList<E> myCon;

    public HuffPriorityQueue() {
        this.myCon = new LinkedList<>();
    }

    public boolean enqueue(E item) {
        if(item == null){
            throw new IllegalArgumentException("Item cannot be null");
        }
        Iterator<E> descIterator = myCon.descendingIterator();
        int index = myCon.size();
        boolean added = false;
        if (this.isEmpty()) {
            myCon.add(item);
            return true;
        }
        // Iterating from the end
        while (descIterator.hasNext() && !added) {
            E queueElement = descIterator.next();
            if (item.compareTo(queueElement) >= 0) {
                myCon.add(index, item);
                // avoids duplicates
                added = true;
            }
            index--;
        }
        // Special Case
        if(index == 0 && !added) {
            myCon.add(index, item);
            added = true;
        }
        return added;
    }

    public E dequeue() {
        return myCon.removeFirst();
    }

    public int size() {
        return myCon.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public String toString() {
        return this.myCon.toString();
    }

}
