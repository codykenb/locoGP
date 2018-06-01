package de.uka.ipd.sdq.ByCounter.utils.wide;
import java.util.ArrayList;
import java.util.Collections;


public class WideVsNormalExampleExecutor {
	
	public static void main(String[] args) {
		WideVsNormalExample wvne = new WideVsNormalExample();
		ArrayList<Long> measurements = new ArrayList<Long>();
		int nrOfMeasurements = 1001;
		for(int i=0; i<nrOfMeasurements; i++){
			measurements.add(wvne.testWide_ISTORE_3Vars_4Iters_falseArrayExplicit_5ArraySize());
		}
		Collections.sort(measurements);
		//TODO compute standard deviation, etc.
		System.out.println("med: "+measurements.get(500)+", "+"min: "+measurements.get(0)+", "+"max: "+measurements.get(1000));
		System.out.println("5% : "+measurements.get(50) +", "+"10%: "+measurements.get(100)+", "+"25%: "+measurements.get(250));
		System.out.println("75%: "+measurements.get(750)+", "+"90%: "+measurements.get(900)+", "+"95%: "+measurements.get(950));
	}
}
