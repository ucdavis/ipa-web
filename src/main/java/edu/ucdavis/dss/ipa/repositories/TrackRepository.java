package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.data.repository.CrudRepository;

public interface TrackRepository extends CrudRepository<Tag, Long> {

	Tag findOneByWorkgroupIdAndName(long workgroupId, String trackName);

}
