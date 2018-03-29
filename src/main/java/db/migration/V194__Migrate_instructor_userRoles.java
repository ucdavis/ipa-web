package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class V194__Migrate_instructor_userRoles implements JdbcMigration {

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

                // Will store structured data about the user's current userRoles to aid in creation/deletion.
                UserRolesDTO userRolesDTO = new UserRolesDTO();

                // Find all userRoles for user
                while(rsUserRoles.next()) {
                    Long userRoleId = rsUserRoles.getLong("Id");
                    Long workgroupId = rsUserRoles.getLong("WorkgroupId");
                    Integer roleId = rsUserRoles.getInt("RoleId");

                    // Instructor-ish roles:
                    // federationInstructor: 8
                    // senateInstructor: 1
                    // lecturer: 14
                    if (roleId != 8 && roleId != 1 && roleId != 14) {
                        continue;
                    }

                    userRolesDTO.workgroupIdsForUser.add(workgroupId);

                    RolesDTO rolesDTO = userRolesDTO.rolesForWorkgroup.get(workgroupId);

                    if (rolesDTO == null) {
                        userRolesDTO.rolesForWorkgroup.put(workgroupId, new RolesDTO());
                        rolesDTO = userRolesDTO.rolesForWorkgroup.get(workgroupId);
                    }

                    switch (roleId) {
                        case 8: // FederationInstructor role
                            rolesDTO.isFederation = true;
                            break;
                        case 1: // SenateInstructor role
                            rolesDTO.isSenate = true;
                            break;
                        case 14: // Lecturer role
                            rolesDTO.isLecturer = true;
                            break;
                        default:
                            break;
                    }
                }

                // Create instructor userRoles based on the old userRoles
                for (Long workgroupId: userRolesDTO.workgroupIdsForUser) {
                    // We need to make one userRole for each workgroup the user is an instructor in
                    Long roleId = 15L; // New role 'instructor'

                    // Identify the instructorType to use in the new userRole
                    Long instructorTypeId = null;

                    RolesDTO rolesDTO = userRolesDTO.rolesForWorkgroup.get(workgroupId);

                    // If senate/federation and lecturer are present, ignore lecturer role as it is less precise.
                    // Cannot be senate and federation simultaneously for a workgroup.
                    if (rolesDTO.isSenate) {
                        instructorTypeId = 6L;
                    } else if (rolesDTO.isFederation) {
                        instructorTypeId = 4L;
                    } else if (rolesDTO.isLecturer) {
                        instructorTypeId = 7L;
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

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psUsers != null) {
                    psUsers.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public class UserRolesDTO {
        public Set<Long> workgroupIdsForUser = new HashSet<>();

        public HashMap<Long, RolesDTO> rolesForWorkgroup = new HashMap<Long, RolesDTO>();
    }

    public class RolesDTO {
        public boolean isSenate = false;
        public boolean isFederation = false;
        public boolean isLecturer = false;
    }
}