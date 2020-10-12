package edu.ucdavis.dss.ipa.utilities;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.awt.*;

@Component
public class HibernateListenerConfigurer {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private UpdateListener updateListener;
    private InsertListener insertListener;
    private CollectionUpdateEventListener collectionUpdateEventListener;

    @Autowired
    public HibernateListenerConfigurer(
            UpdateListener updateListener,
            InsertListener insertListener,
            CollectionUpdateEventListener collectionUpdateEventListener) {
        this.updateListener = updateListener;
        this.insertListener = insertListener;
        this.collectionUpdateEventListener = collectionUpdateEventListener;
    }

    @PostConstruct
    protected void init() {
        SessionFactoryImpl sessionFactory = emf.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(updateListener);
        registry.getEventListenerGroup(EventType.POST_COMMIT_INSERT).appendListener(insertListener);
        registry.getEventListenerGroup(EventType.POST_COLLECTION_UPDATE).appendListener(collectionUpdateEventListener);
    }
}
