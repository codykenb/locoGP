package locoGP.problems;

public class Sort1Example { 
	/* 
	 * class Sort1Problem1614 { from GP48
	 * 
	 * When this is compiled at runtime during our testing, it causes the compiler to crash! why??
	 * 
	 * */
	public static void sort(  Integer[] a,  Integer length){
		for (int i=0; i < length; i++) {
			for (int j=0; j < length - 1; j++) {
				if (a[j] > a[j + 1]) {
					int k=a[j];
					a[j]=a[j + 1];
					a[j + 1]=k;
				}
			}
		}
		for (int i=0; i < length; i++) {
			for (int j=0; j < length - 1; j++) {
				for (; j < length - 1; j++) {
					if (a[j] > a[j + 1]) {
						int k=a[j];
						a[j + 1]=k;
					}
				}
				if (i > a[j + 1]) {
					int k=a[j];
					a[j + 1]=k;
				}
			}
		}
	}
}



