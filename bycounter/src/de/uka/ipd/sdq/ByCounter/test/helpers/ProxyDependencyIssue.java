package de.uka.ipd.sdq.ByCounter.test.helpers;

/**
 * This class demonstrates execution of a dependency class method, 
 * i.e. a method on an unknown instance of a known class or a subclass.
 */
public class ProxyDependencyIssue implements ProxyDependencyIssueInterface {
    ProxyDependency dep;

    /* (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.test.helpers.ProxyDependencyIssueInterface#setDependency(de.uka.ipd.sdq.ByCounter.test.helpers.ProxyDependency)
	 */
    @Override
	public void setDependency(ProxyDependency dep) {
        this.dep = dep;
    }

    // This Method should be measured using ByCounter
    /* (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.test.helpers.ProxyDependencyIssueInterface#doSomething()
	 */
    @Override
	public void doSomething() {
        dep.calculate();
    }
}