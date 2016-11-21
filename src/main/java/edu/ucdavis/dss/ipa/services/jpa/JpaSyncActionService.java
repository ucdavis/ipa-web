package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SyncAction;
import edu.ucdavis.dss.ipa.repositories.SyncActionRepository;
import edu.ucdavis.dss.ipa.services.SyncActionService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class JpaSyncActionService implements SyncActionService {

	@Inject SyncActionRepository syncActionRepository;

	@Override
	public SyncAction save(@NotNull @Valid SyncAction syncAction) {
		return syncActionRepository.save(syncAction);
	}

	@Override
	public SyncAction getOneById(Long id) {
		return syncActionRepository.findOne(id);
	}

}
