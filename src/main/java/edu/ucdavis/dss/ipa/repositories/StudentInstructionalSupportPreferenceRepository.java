package edu.ucdavis.dss.ipa.repositories;

        import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportPreference;
        import org.springframework.data.jpa.repository.Modifying;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.CrudRepository;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.List;

public interface StudentInstructionalSupportPreferenceRepository extends CrudRepository<StudentInstructionalSupportPreference, Long> {

    @Modifying
    @Transactional
    @Query(value="delete from StudentInstructionalSupportPreference sisp WHERE sisp.id = ?1")
    void deleteById(long studentInstructionalSupportPreferenceId);

    List<StudentInstructionalSupportPreference> findByInstructionalSupportStaffIdAndStudentInstructionalSupportCallId(long supportStaffId, long studentSupportCallId);
}
