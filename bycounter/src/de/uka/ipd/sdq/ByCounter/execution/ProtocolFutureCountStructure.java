package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * Structure used to allocate {@link CountingResult}s in the 
 * {@link CountingResultCollector} before counts are known. This is used to
 * ensure the correct structure of threaded results.
 * 
 * <p>
 * The {@link #equals(Object)} and {@link #hashCode()} methods are specifically 
 * designed so that two {@link ProtocolFutureCountStructure} A and B equal if
 * A.ownID == B.ownID and A.observedEntityID = B.observedEntityID.
 * See {@link #identify(UUID, UUID)}.
 * </p>
 * @author Martin Krogmann
 *
 */
public class ProtocolFutureCountStructure {

	/** Fully qualified method name. */
	public String canonicalMethodName;

	/** This ID is a reference passed to the methods called by the method. */
	public UUID ownID;
	
	/** This ID is the ID of the {@link EntityToInstrument} that produced this 
	 * result. */
	public UUID observedEntityID;

	/** Threads (by id) spawned in the executed method. 
	 * If range blocks are used, every second value is the number of the 
	 * range block from which the thread was spawned. */
	public ArrayList<Long> spawnedThreads;

	/**
	 * Construct by initialising all fields with null.
	 */
	public ProtocolFutureCountStructure() {
		this.canonicalMethodName = null;
		this.ownID = null;
		this.observedEntityID = null;
		this.spawnedThreads = null;
	}
	
	/**
	 * Construct the structure with the given values.
	 * @param canonicalMethodName Fully qualified method name.
	 * @param ownID This ID is a reference passed to the methods called by the method.
	 * @param observedEntityID This ID is the ID of the {@link EntityToInstrument} that produced this 
	 * result.
	 * @param spawnedThreads Threads (by id) spawned in the executed method. 
	 * If range blocks are used, every second value is the number of the 
	 * range block from which the thread was spawned.
	 */
	public ProtocolFutureCountStructure(
			final String canonicalMethodName,
			final UUID ownID,
			final String observedEntityID,
			final ArrayList<Long> spawnedThreads) {
		this.canonicalMethodName = canonicalMethodName;
		this.ownID = ownID;
		this.observedEntityID = UUID.fromString(observedEntityID);
		this.spawnedThreads = spawnedThreads;
	}

	/**
	 * @param ownID {@link #ownID}
	 * @param observedEntityID {@link #observedEntityID}.
	 * @return True, when both parameters match the fields of this 
	 * {@link ProtocolFutureCountStructure}.
	 */
	public boolean identify(final UUID ownID, final UUID observedEntityID) {
		return this.ownID.equals(ownID) && this.observedEntityID.equals(observedEntityID);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.observedEntityID == null) ? 0 : this.observedEntityID
						.hashCode());
		result = prime * result
				+ ((this.ownID == null) ? 0 : this.ownID.hashCode());
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
		ProtocolFutureCountStructure other = (ProtocolFutureCountStructure) obj;
		if (this.observedEntityID == null) {
			if (other.observedEntityID != null)
				return false;
		} else if (!this.observedEntityID.equals(other.observedEntityID))
			return false;
		if (this.ownID == null) {
			if (other.ownID != null)
				return false;
		} else if (!this.ownID.equals(other.ownID))
			return false;
		return true;
	}
	
	
}
