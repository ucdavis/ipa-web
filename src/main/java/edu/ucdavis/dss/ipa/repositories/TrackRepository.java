package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TrackRepository extends CrudRepository<Tag, Long> {

	Tag findOneByWorkgroupIdAndName(long workgroupId, String trackName);

    List<Tag> findByWorkgroupId(long workgroupId);
}
