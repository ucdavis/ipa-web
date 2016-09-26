package edu.ucdavis.dss.ipa.entities.validation;

/**
 * This interface is required for loggable entities
 */
public interface Loggable {

    /**
     *
     * @return - [entityName]_[entityId]
     */
    String logTag();
}
