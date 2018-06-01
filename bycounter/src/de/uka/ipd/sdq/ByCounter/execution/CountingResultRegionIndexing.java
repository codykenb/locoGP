package de.uka.ipd.sdq.ByCounter.execution;

import java.util.HashMap;
import java.util.Map;

import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * Indexing for counting regions.
 * @author Martin Krogmann
 */
public class CountingResultRegionIndexing {
	
	Map<InstrumentedRegion, CountingResult> results;
	
	public CountingResultRegionIndexing() {
		this.results = new HashMap<InstrumentedRegion, CountingResult>();
	}

	/**
	 * @param res Partial counting result.
	 * @param currentRegion Region to which the result belongs.
	 */
	public void add(CountingResult res, InstrumentedRegion currentRegion) {
		CountingResult rs = this.results.get(currentRegion);
		if(rs == null) {
			// no entry for this region id yet
			this.results.put(currentRegion, res);
			res.setObservedElement(currentRegion);
		} else {
			// add up with the existing results
			rs.add(res);
		}
	}

	/**
	 * Clear the internal map.
	 */
	public void clearResults() {
		this.results.clear();
	}
}
