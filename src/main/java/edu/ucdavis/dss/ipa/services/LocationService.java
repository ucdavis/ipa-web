package edu.ucdavis.dss.ipa.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.Workgroup;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Location;

@Validated
public interface LocationService {

    Location save(@NotNull @Valid Location location);

    Location findOneById(Long id);

    Location findOrCreateByWorkgroupAndDescription(Workgroup workgroup, String description);

    Location archiveById(Long locationId);
}
