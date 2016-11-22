package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SyncAction;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
public interface SyncActionService {

	SyncAction save(@NotNull @Valid SyncAction syncAction);

	SyncAction getOneById(Long id);

	void delete(Long id);

}
