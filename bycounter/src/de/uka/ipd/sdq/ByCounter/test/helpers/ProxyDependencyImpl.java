package de.uka.ipd.sdq.ByCounter.test.helpers;

public class ProxyDependencyImpl implements ProxyDependency {
    public void calculate() {
        System.out.println("Dpc.calculate; this.getClass(): " + this.getClass());
    }
}