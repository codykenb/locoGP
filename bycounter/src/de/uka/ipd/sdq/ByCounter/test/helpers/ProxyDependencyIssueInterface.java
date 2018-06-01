package de.uka.ipd.sdq.ByCounter.test.helpers;

public interface ProxyDependencyIssueInterface {

	public abstract void setDependency(ProxyDependency dep);

	// This Method should be measured using ByCounter
	public abstract void doSomething();

}