package locoGP.problems;

public class Sort1Quick22387 {
	public static Integer[] sort(Integer[] a, Integer length) {
		return sort(a, 0, length - 1);
	}

	public static Integer[] sort(Integer[] a, Integer p, Integer r) {
		if (p < r) {
			int q = 0;
			int x = a[p];
			int i = p - 1;
			int j = r + 1;
			while (true) {
				i++;
				while (i < j && a[i] < x)
					i++;
				j--;
				while (a[j] > x)
					j--;
				if (i < j) {
					int temp = a[i];
					a[i] = a[j];
					a[j] = temp;
				} else {
						q = j;
						break;
				}
			}
			sort(a, p, q);
			sort(a, q + 1, r);
		}
		return a;
	}
}
