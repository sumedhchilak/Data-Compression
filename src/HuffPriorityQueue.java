import java.util.Iterator;
import java.util.LinkedList;

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