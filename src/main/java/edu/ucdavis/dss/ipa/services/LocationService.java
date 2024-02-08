package edu.ucdavis.dss.ipa.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.Workgroup;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Location;

import java.util.List;

@Validated
public interface LocationService {

    Location save(@NotNull @Valid Location location);

    Location findOneById(Long id);

    Location findOrCreateByWorkgroupAndDescription(Workgroup workgroup, String description);

    Location archiveById(Long locationId);

    List<Location> findByWorkgroupId(long workgroupId);
}
