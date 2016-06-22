package edu.ucdavis.dss.ipa.security.authorization;

import org.springframework.stereotype.Service;

/**
 * Created by okadri on 6/22/16.
 */
public interface Authorizer<T> {
    public void authorize(T entity, Object... args);
}
