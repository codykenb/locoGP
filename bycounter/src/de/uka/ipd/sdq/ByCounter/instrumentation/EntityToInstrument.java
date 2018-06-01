package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.Serializable;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Common base class for entities that can be instrumented using ByCounter.
 * @author Martin Krogmann
 */
public abstract class EntityToInstrument implements Serializable {
	
	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A unique identifier for tracking the entity.
	 */
	private UUID id;

	/**
	 * Initialise the id ({@link #getId()}.
	 */
	public EntityToInstrument() {
		id = UUID.randomUUID();
	}
	
	/**
	 * @return A unique identifier for tracking the entity.
	 */
	public UUID getId() {
		return this.id;
	}
	
	/**
	 * @return {@link MethodDescriptor}s for methods that are specified by this
	 * {@link EntityToInstrument}.
	 */
	public abstract MethodDescriptor[] getMethodsToInstrument();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
		EntityToInstrument other = (EntityToInstrument) obj;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		return true;
	}
}
