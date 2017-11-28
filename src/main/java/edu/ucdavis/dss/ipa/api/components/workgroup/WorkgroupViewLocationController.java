package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.entities.Location;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.LocationService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@CrossOrigin
public class WorkgroupViewLocationController {
    @Inject LocationService locationService;
    @Inject WorkgroupService workgroupService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/locations", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Location addLocation(@PathVariable Long workgroupId, @RequestBody Location location) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        return locationService.findOrCreateByWorkgroupAndDescription(workgroup, location.getDescription());
    }

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/locations/{locationId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Location updateLocation(@PathVariable Long workgroupId, @PathVariable long locationId,
                           @RequestBody Location location) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Location editedLocation = locationService.findOneById(locationId);
        editedLocation.setDescription(location.getDescription());
        return locationService.save(editedLocation);
    }

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/locations/{locationId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Location archiveLocation(@PathVariable Long workgroupId, @PathVariable long locationId) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        return locationService.archiveById(locationId);
    }

}
