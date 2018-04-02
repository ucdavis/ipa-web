package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class V194__Migrate_instructor_userRoles implements JdbcMigration {
    // Instructor Roles
    public static final int SENATE_INSTRUCTOR_ROLE = 1;
    public static final int FEDERATION_INSTRUCTOR_ROLE = 8;
    public static final int LECTURER_ROLE = 14;

    public static final long INSTRUCTOR_ROLE = 15;

    // Instructor Types
    public static final long UNIT_18_PRE_SIX = 4L;
    public static final long LADDER_FACULTY = 6L;
    public static final long INSTRUCTOR = 7L;

    /**
     * Converts any 'instructor'-ish userRoles into an instructor role with a relevant instructorType
     *
     * @param connection
     * @throws Exception
     */
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psUsers = connection.prepareStatement("SELECT * FROM `Users`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsUsers = psUsers.executeQuery();

            while (rsUsers.next()) {
                Long userId = rsUsers.getLong("Id");

                PreparedStatement psUserRoles = connection.prepareStatement("SELECT * FROM `UserRoles` WHERE `UserId` = ?");
                psUserRoles.setLong(1, userId);
                ResultSet rsUserRoles = psUserRoles.executeQuery();

                // Data about the user's current userRoles to aid in creation/deletion.
                Set<Long> workgroupIdsForUser = new HashSet<>();
                HashMap<Long, IdentifiedRoles> rolesForWorkgroup = new HashMap<Long, IdentifiedRoles>();

                // Find all userRoles for user
                while(rsUserRoles.next()) {
                    Long userRoleId = rsUserRoles.getLong("Id");
                    Long workgroupId = rsUserRoles.getLong("WorkgroupId");
                    Integer roleId = rsUserRoles.getInt("RoleId");

                    if (roleId != FEDERATION_INSTRUCTOR_ROLE && roleId != SENATE_INSTRUCTOR_ROLE && roleId != LECTURER_ROLE) {
                        continue;
                    }

                    workgroupIdsForUser.add(workgroupId);

                    IdentifiedRoles identifiedRoles = rolesForWorkgroup.get(workgroupId);

                    if (identifiedRoles == null) {
                        rolesForWorkgroup.put(workgroupId, new IdentifiedRoles());
                        identifiedRoles = rolesForWorkgroup.get(workgroupId);
                    }

                    switch (roleId) {
                        case FEDERATION_INSTRUCTOR_ROLE: // FederationInstructor role
                            identifiedRoles.isFederation = true;
                            break;
                        case SENATE_INSTRUCTOR_ROLE: // SenateInstructor role
                            identifiedRoles.isSenate = true;
                            break;
                        case LECTURER_ROLE: // Lecturer role
                            identifiedRoles.isLecturer = true;
                            break;
                        default:
                            break;
                    }
                }

                // Create instructor userRoles based on the old userRoles
                for (Long workgroupId: workgroupIdsForUser) {
                    // We need to make one userRole for each workgroup the user is an instructor in
                    Long roleId = INSTRUCTOR_ROLE; // New role 'instructor'

                    // Identify the instructorType to use in the new userRole
                    Long instructorTypeId = null;

                    IdentifiedRoles identifiedRoles = rolesForWorkgroup.get(workgroupId);

                    // If senate/federation and lecturer are present, ignore lecturer role as it is less precise.
                    // Cannot be senate and federation simultaneously for a workgroup.
                    if (identifiedRoles.isSenate) {
                        instructorTypeId = LADDER_FACULTY;
                    } else if (identifiedRoles.isFederation) {
                        instructorTypeId = UNIT_18_PRE_SIX;
                    } else if (identifiedRoles.isLecturer) {
                        instructorTypeId = INSTRUCTOR;
                    }

                    // Ensure UserRole does not already exist
                    Long existingUserRoleId = null;

                    PreparedStatement psExistingUserRole = connection.prepareStatement(
                            "SELECT * FROM UserRoles WHERE UserId = ? AND WorkgroupId = ? AND RoleId = ?;"
                    );
                    psExistingUserRole.setLong(1, userId);
                    psExistingUserRole.setLong(2, workgroupId);
                    psExistingUserRole.setLong(3, roleId);
                    ResultSet rsExistingUserRole = psExistingUserRole.executeQuery();

                    while(rsExistingUserRole.next()) {
                        existingUserRoleId = rsExistingUserRole.getLong("Id");
                    }

                    if (existingUserRoleId != null && existingUserRoleId > 0) { continue; }

                    // Create userRole
                    PreparedStatement psCreateUserRole = connection.prepareStatement(
                            "INSERT INTO `UserRoles` (UserId, WorkgroupId, RoleId, InstructorTypeId) " +
                                    "VALUES (?, ?, ?, ?);"
                            , Statement.RETURN_GENERATED_KEYS
                    );
                    psCreateUserRole.setLong(1, userId);
                    psCreateUserRole.setLong(2, workgroupId);
                    psCreateUserRole.setLong(3, roleId);
                    psCreateUserRole.setLong(4, instructorTypeId);
                    psCreateUserRole.execute();
                }
            }

            // Commit changes
            connection.commit();
        } finally {
            if (psUsers != null) {
                psUsers.close();
            }
        }
    }

    public class IdentifiedRoles {
        public boolean isSenate = false;
        public boolean isFederation = false;
        public boolean isLecturer = false;
    }
}
