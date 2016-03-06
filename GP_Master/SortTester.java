

class SortTester{
	public static void main(String[] args){
		
		Integer[] unsorted = {23,457,243,76,7,2,7,9,346,75,7,34,568,34,568,68,69,346,235,865,5,534,3245,5684463};
		TestBubble.sort(unsorted, (Integer) unsorted.length);
		
		for(int i =0; i< unsorted.length; i++){
			System.out.print(unsorted[i]+",");
		}
		
	}		
}
