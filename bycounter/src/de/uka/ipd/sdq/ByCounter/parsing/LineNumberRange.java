package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;

/**Denotes a range of source code line numbers which should be measured with {@link BytecodeCounter}.
 * Source code line numbers may have another order in the source code file than in the compiled Bytecode.
 * If there is more than one Bytecode block containing referencing a source code line, all blocks 
 * containing a reference will be counted. In general, LineNumberRangefrom x to y means that all 
 * Bytecode blocks B are counted for which { B | x <= ReferencedLineNumber(B) <= y } holds. 
 * Additionally, all blocks B_{hull} = {B_h | NoReferencedLineNumber(B_h) and \exists B_j \in B: JumpFromTo(B_j,B_h)}
 * are counted which do not reference a line number but for which a jump from any of the blocks in B exist. This enables
 * correct counting of for-each-loops.
 * @author Martin Krogmann
 * @author groenda
 */
public final class LineNumberRange implements Comparable<LineNumberRange>, Serializable{
	
	/**
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The first line number included in the range.
	 */
	public int firstLine;
	
	/**
	 * The last line number included in the range.
	 */
	public int lastLine;

	
	/**A new line number range with the given parameters.
	 * @param firstLine The first included source code line number.
	 * @param lastLine The last included source code line number.
	 */
	public LineNumberRange(int firstLine, int lastLine) {
		this.firstLine = firstLine;
		this.lastLine = lastLine;
	}


	/**
	 * @return A Comparator implementation for {@link LineNumberRange}s.
	 * The comparison is based on the first line only. E.g.: A range r1 is 
	 * smaller than r2 if r1.firstLine is smaller than r2.firstLine.
	 */
	public static Comparator<? super LineNumberRange> getComparator() {
		return new Comparator<LineNumberRange>() {
			public int compare(LineNumberRange lr1, LineNumberRange lr2) {
				if(lr1.firstLine < lr2.firstLine) {
					return -1;
				} else if(lr1.firstLine > lr2.firstLine) {
					return 1;
				} else {
					// firstLine equals
					return 0;
				}
			}
		};
	}


	/**Searches an array for a {@link LineNumberRange} starting at a given line.
	 * @param areasForMethod List of {@link InstrumentedCodeArea}s to search.
	 * @param l Line to analyse.
	 * @return When l is in the range of specified code areas: A code area that 
	 * includes line l. Null otherwise.
	 */
	public static LineNumberRange findLineInAreas(
			List<InstrumentedCodeArea> areasForMethod, int l) {
		for(InstrumentedCodeArea currentCodeArea : areasForMethod) {
			if(l >= currentCodeArea.getArea().firstLine
					&& l <= currentCodeArea.getArea().lastLine) {
				return currentCodeArea.getArea();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(LineNumberRange o) {
		if(this.firstLine<o.firstLine){
			return -1;
		}else if(this.firstLine>o.firstLine){
			return  1;
		}else if(this.lastLine<o.lastLine){
			return -1;
		}else if(this.lastLine>o.lastLine){
			return  1;
		}else{
			return 0;
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LineNumberRange [firstLine=" + this.firstLine + ", lastLine="
				+ this.lastLine + "]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.firstLine;
		result = prime * result + this.lastLine;
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineNumberRange other = (LineNumberRange) obj;
		if (this.firstLine != other.firstLine)
			return false;
		if (this.lastLine != other.lastLine)
			return false;
		return true;
	}
	
	
}
