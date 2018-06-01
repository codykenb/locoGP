package de.uka.ipd.sdq.ByCounter.utils;

/**
 * Barrier helper object for encapsulated synchronization.
 * Used in the GUI (classes ByLoaderGUI and ByClassFileTransformer - see Javadocs).
 * 
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.9
 * @version 1.2
 *
 */
public final class Barrier
{
	/**
	 * This blocks the current thread until {@link #release()} 
	 * is called.
	 * @throws InterruptedException
	 */
    public synchronized void block() throws InterruptedException
    {
        wait();
    }

	/**
	 * This unblocks the thread that was blocked with {@link #block()}.
	 */
    public synchronized void release()
    {
        notify();
    }

	/**
	 * This unblocks all threads that were blocked with 
	 * {@link #block()}.
	 */
    public synchronized void releaseAll()
    {
        notifyAll();
    }
 
}
