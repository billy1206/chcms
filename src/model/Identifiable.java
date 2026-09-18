package model;

/**
 * Anything stored in a Repository must expose a unique id.
 * Keeping the contract to one method (Interface Segregation Principle) lets the
 * persistence layer depend on an abstraction instead of a concrete class.
 */
public interface Identifiable {
    String getId();
}
