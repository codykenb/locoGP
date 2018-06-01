package de.uka.ipd.sdq.ByCounter.test.helpers;

public class TestSubjectLineNumbers {

	public void normalised(int i) {
		// this for header is split to multiple lines on purpose
		for (
				int k = 0;
				k < i;
				k++) {
			System.out.println(k);
		}
	}
	
	public void testLabelAndLineNumbers() {
		int a = 3;
		a++;
		for(long l = 0; l < 123; l++) {
			a += l;
		}
		a--;
	}
	
	public void testLabelAndLineNumbersNormalform() {
		int a = 3;
		long l = 0; 
		while(l < 123) {
			a += l;
			l++; 
		}
	}
	
	public void testNestedNormalisedLoops(int i) {
		int j = 0;
		int k = 0;
		while(i < 11) {
			k += 1;
			while(j < 12) {
				k *= 2;
				j++;
			}
			i++; 
		}
		//at the end, k=?
	}
	
	/** Input-Variante 1 [51-53],[54],[55],[57],[58],[59],[61],[63]
	 *  Input-Variante 2 [51-53],     [55],[57],    ,[59],[61],[63] //ignoring loop overhead and jumps....
	 * @param i	 */
	public void testNestedNormalisedLoopsWithExternalCalls(int i) {
		int j = 0;
		int k = 0;
		int m = 0;
		while(i < 11) { //mit dieser Zeile sind zwei Labelblocks markiert! --> "single goto"-labelblock schlagen wir dem range [51-53] in der ersten Variante zu!
			k += 1;
			extCall1();
			m++;
			while(j < 12) { //aehnlich Zeile 58
				k *= 2;
				extCall2();
				j++; 
			}
			i++; 
		}
		//at the end, k=?
	}

	/**
	 * 
	 * 
	 * 
	 * [[88-90], stimmt NICHT: [92-95] 
	 * 
	 * TODO: let Klaus create an RDSEFF with GAST-to-SEFF
	 * @param i
	 */
	public void testNestedNormalisedLoopsWithExternalCallsAndBranchesWithoutExternalCalls(int i) {
		int j = 0;
		int k = 0;
		int m = 0;
		while(i < 11) { //mit dieser Zeile sind zwei Labelblocks markiert! --> "single goto"-labelblocl schlagen wir dem range [78-80] in der ersten Variante zu!
			k += 1;
			extCall1();
			m++;
			while(j < 12) { //aehnlich Zeile 58
				k *= 2;
				extCall2();
				if(j%2==0){//[88-92] liegen NICHT in einem Range WENN extOrIntCall3 extern ist den Range unterbricht -> mehrere LabelBlocks werden zu einem Rangeblock
					j++; 
				}else{
					extOrIntCall3();
					j+=3;
				}
				k=k*k;
			}
			i++; 
		}
		//at the end, k=?
	}
	
	public int testForeach() {
		int x = 0;
		String[] names = {"A", "B", "C"};
		for(String name : names) { // many instructions and basic blocks, but only one linenumber
			x++;
			name += "";
		}
		return x;
	}

	private void extOrIntCall3() {
		// TODO Auto-generated method stub
		
	}

	private void extCall2() {
		// TODO Auto-generated method stub
		
	}

	private void extCall1() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
