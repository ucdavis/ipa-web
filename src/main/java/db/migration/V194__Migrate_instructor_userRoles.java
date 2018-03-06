package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
                    Long userRoleId = rsUsers.getLong("Id");
                    Long workgroupId = rsUsers.getLong("WorkgroupId");
                    Integer roleId = rsUsers.getInt("RoleId");

                    // Instructor-ish roles:
                    // federationInstructor: 8
                    // senateInstructor: 9
                    // lecturer: 14
                    if (roleId != 8 && roleId != 9 && roleId != 14) {
                        continue;
                    }

                    userRolesDTO.oldUserRoles.add(userRoleId);
                    userRolesDTO.workgroupIdsForUser.add(workgroupId);

                    RolesDTO rolesDTO = userRolesDTO.rolesForWorkgroup.get(workgroupId);

                    if (rolesDTO == null) {
                        userRolesDTO.rolesForWorkgroup.put(workgroupId, new RolesDTO);
                        rolesDTO = userRolesDTO.rolesForWorkgroup.get(workgroupId);
                    }

                    switch (roleId) {
                        case 8: // FederationInstructor role
                            rolesDTO.isFederation = true;
                            break;
                        case 9: // SenateInstructor role
                            rolesDTO.isSenate = true;
                            break;
                        case 14: // Lecturer role
                            rolesDTO.isLecturer = true;
                            break;
                        default;
                            break;
                    }
                }

                // If senate/federation and lecturer are present, ignore lecturer role as it is less precise.

                // TODO: Create new userRole with instructorType association

                // TODO: DELETE all 'old userRoles'
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
        public List<Long> workgroupIdsForUser = new ArrayList<>();
        public List<Long> oldUserRoles = new ArrayList<>();

        public HashMap<Long, RolesDTO> rolesForWorkgroup;
    }

    public class RolesDTO {
        public boolean isSenate = false;
        public boolean isFederation = false;
        public boolean isLecturer = false;
    }
}