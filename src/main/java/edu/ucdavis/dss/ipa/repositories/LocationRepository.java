package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Location;

import java.util.List;

public interface LocationRepository extends CrudRepository<Location, Long> {

    Location findOneByWorkgroupIdAndDescription(long id, String description);

    List<Location> findByWorkgroupId(long workgroupId);
}
