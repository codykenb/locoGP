package de.uka.ipd.sdq.ByCounter.test.helpers;

/* Random.java based on Java Numerical Toolkit (JNT) Random.UniformSequence
 class.  We do not use Java's own java.util.Random so that we can compare
 results with equivalent C and Fortran coces.
 */

/**
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public class SciMarkRandom_MK {

	/*
	 * ------------------------------------------------------------------------------
	 * CLASS VARIABLES
	 * ------------------------------------------------------------------------------
	 */


	private boolean haveRange = false;
	private int i = 4;
	private int j = 16;

	private double left = 0.0;
	private int m[];

	/*
	 * For mdig = 32 : m1 = 2147483647, m2 = 65536 For mdig = 64 : m1 =
		 * 9223372036854775807, m2 = 4294967296
		 */
		private final int mdig = 32;

		private final int one = 1;
	private final int m1 = (this.one << this.mdig - 2) + ((this.one << this.mdig - 2) - this.one);
	private final int m2 = this.one << this.mdig / 2;
	private double dm1 = 1.0 / (double) this.m1;

	@SuppressWarnings("unused")
	private double right = 1.0;
	int seed = 0;
	private double width = 1.0;

	/*
	 * ------------------------------------------------------------------------------
	 * CONSTRUCTORS
	 * ------------------------------------------------------------------------------
	 */

	/**
	 * Initializes a sequence of uniformly distributed quasi random numbers with
	 * a seed based on the system clock.
	 */
	public SciMarkRandom_MK() {
		initialize((int) System.currentTimeMillis());
	}

	/**
	 * Initializes a sequence of uniformly distributed quasi random numbers on a
	 * given half-open interval [left,right) with a seed based on the system
	 * clock.
	 *
	 * @param left (double)<BR>
	 *
	 * The left endpoint of the half-open interval [left,right).
	 *
	 * @param right (double)<BR>
	 *
	 * The right endpoint of the half-open interval [left,right).
	 */
	public SciMarkRandom_MK(double left, double right) {
		initialize((int) System.currentTimeMillis());
		this.left = left;
		this.right = right;
		this.width = right - left;
		this.haveRange = true;
	}

	/**
	 * Initializes a sequence of uniformly distributed quasi random numbers with
	 * a given seed.
	 *
	 * @param seed (int)<BR>
	 *
	 * The seed of the random number generator. Two sequences with the same seed
	 * will be identical.
	 */
	public SciMarkRandom_MK(int seed) {
		initialize(seed);
	}

	/**
	 * Initializes a sequence of uniformly distributed quasi random numbers with
	 * a given seed on a given half-open interval [left,right).
	 *
	 * @param seed (int)<BR>
	 *
	 * The seed of the random number generator. Two sequences with the same seed
	 * will be identical.
	 *
	 * @param left (double)<BR>
	 *
	 * The left endpoint of the half-open interval [left,right).
	 *
	 * @param right (double)<BR>
	 *
	 * The right endpoint of the half-open interval [left,right).
	 */
	public SciMarkRandom_MK(int seed, double left, double right) {
		initialize(seed);
		this.left = left;
		this.right = right;
		this.width = right - left;
		this.haveRange = true;
	}

	/*
	 * ------------------------------------------------------------------------------
	 * PUBLIC METHODS
	 * ------------------------------------------------------------------------------
	 */

	private void initialize(int seed) {

		int jseed, k0, k1, j0, j1, iloop;

		this.seed = seed;

		this.m = new int[17];

		jseed = Math.min(Math.abs(seed), this.m1);
		if (jseed % 2 == 0)
			--jseed;
		k0 = 9069 % this.m2;
		k1 = 9069 / this.m2;
		j0 = jseed % this.m2;
		j1 = jseed / this.m2;
		for (iloop = 0; iloop < 17; ++iloop) {
			jseed = j0 * k0;
			j1 = (jseed / this.m2 + j0 * k1 + j1 * k0) % (this.m2 / 2);
			j0 = jseed % this.m2;
			this.m[iloop] = j0 + this.m2 * j1;
		}
		this.i = 4;
		this.j = 16;

	}

	/**
	 * Returns the next random number in the sequence.
	 */
	public final synchronized double nextDouble() {

		int k;
		@SuppressWarnings("unused")
		double nextValue;

		k = this.m[this.i] - this.m[this.j];
		if (k < 0)
			k += this.m1;
		this.m[this.j] = k;

		if (this.i == 0)
			this.i = 16;
		else
			this.i--;

		if (this.j == 0)
			this.j = 16;
		else
			this.j--;

		if (this.haveRange)
			return this.left + this.dm1 * (double) k * this.width;
		else
			return this.dm1 * (double) k;

	}

	/*----------------------------------------------------------------------------
	 PRIVATE METHODS
	 ------------------------------------------------------------------------ */

	/**
	 * Returns the next N random numbers in the sequence, as a vector.
	 */
	public final synchronized void nextDoubles(double x[]) {

		int N = x.length;
		int remainder = N & 3; // N mod 4

		if (this.haveRange) {
			for (int count = 0; count < N; count++) {
				int k = this.m[this.i] - this.m[this.j];

				if (this.i == 0)
					this.i = 16;
				else
					this.i--;

				if (k < 0)
					k += this.m1;
				this.m[this.j] = k;

				if (this.j == 0)
					this.j = 16;
				else
					this.j--;

				x[count] = this.left + this.dm1 * (double) k * this.width;
			}

		} else {

			for (int count = 0; count < remainder; count++) {
				int k = this.m[this.i] - this.m[this.j];

				if (this.i == 0)
					this.i = 16;
				else
					this.i--;

				if (k < 0)
					k += this.m1;
				this.m[this.j] = k;

				if (this.j == 0)
					this.j = 16;
				else
					this.j--;

				x[count] = this.dm1 * (double) k;
			}

			for (int count = remainder; count < N; count += 4) {
				int k = this.m[this.i] - this.m[this.j];
				if (this.i == 0)
					this.i = 16;
				else
					this.i--;
				if (k < 0)
					k += this.m1;
				this.m[this.j] = k;
				if (this.j == 0)
					this.j = 16;
				else
					this.j--;
				x[count] = this.dm1 * (double) k;

				k = this.m[this.i] - this.m[this.j];
				if (this.i == 0)
					i = 16;
				else
					i--;
				if (k < 0)
					k += m1;
				m[j] = k;
				if (j == 0)
					j = 16;
				else
					j--;
				x[count + 1] = dm1 * (double) k;

				k = m[i] - m[j];
				if (i == 0)
					i = 16;
				else
					i--;
				if (k < 0)
					k += m1;
				m[j] = k;
				if (j == 0)
					j = 16;
				else
					j--;
				x[count + 2] = dm1 * (double) k;

				k = m[i] - m[j];
				if (i == 0)
					i = 16;
				else
					i--;
				if (k < 0)
					k += m1;
				m[j] = k;
				if (j == 0)
					j = 16;
				else
					j--;
				x[count + 3] = dm1 * (double) k;
			}
		}
	}

}
