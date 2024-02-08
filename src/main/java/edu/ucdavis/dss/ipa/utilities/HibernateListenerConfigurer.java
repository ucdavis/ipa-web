package edu.ucdavis.dss.ipa.utilities;

import jakarta.annotation.PostConstruct;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

@Component
public class HibernateListenerConfigurer {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private UpdateListener updateListener;
    private InsertListener insertListener;
    private DeleteListener deleteListener;

    @Autowired
    public HibernateListenerConfigurer(
            UpdateListener updateListener,
            InsertListener insertListener,
            DeleteListener deleteListener) {
        this.updateListener = updateListener;
        this.insertListener = insertListener;
        this.deleteListener = deleteListener;
    }

    @PostConstruct
    protected void init() {
        SessionFactoryImpl sessionFactory = emf.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(updateListener);
        registry.getEventListenerGroup(EventType.POST_COMMIT_INSERT).appendListener(insertListener);
        registry.getEventListenerGroup(EventType.POST_COMMIT_DELETE).appendListener(deleteListener);
    }
}
