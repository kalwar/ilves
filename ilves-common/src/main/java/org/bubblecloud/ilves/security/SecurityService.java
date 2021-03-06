package org.bubblecloud.ilves.security;

import org.bubblecloud.ilves.cache.PrivilegeCache;
import org.bubblecloud.ilves.exception.SiteException;
import org.bubblecloud.ilves.model.*;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by tlaukkan on 12/14/2014.
 */
public class SecurityService {

    public static void addCustomer(final SecurityContext context, final Customer customer, final User user) {
        addCustomer(context, customer);
        UserDao.addGroupMember(context.getEntityManager(), customer.getAdminGroup(), user);
        UserDao.addGroupMember(context.getEntityManager(), customer.getMemberGroup(), user);
    }

    /**
     * Adds new customer to database.
     * @param context the processing context
     * @param customer the customer
     */
    public static void addCustomer(final SecurityContext context, final Customer customer) {
        final Company company = context.getObject(Company.class);
        if (company.isSelfRegistration()) {
            requireRole("add-customer", context, DefaultRoles.ADMINISTRATOR, DefaultRoles.ANONYMOUS);
        } else {
            requireRole("add-customer", context, DefaultRoles.ADMINISTRATOR);
        }
        CustomerDao.addCustomer(context.getEntityManager(), customer);
        AuditService.log(context, "add", "customer", customer.getCustomerId(), customer.toString());
    }

    /**
     * Updates new customer to database.
     * @param context the processing context
     * @param customer the customer
     */
    public static void updateCustomer(final SecurityContext context, final Customer customer) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, "customer", customer.getCustomerId(), customer.toString(), context, DefaultRoles.ADMINISTRATOR);
        CustomerDao.updateCustomer(context.getEntityManager(), customer);
        AuditService.log(context, "update", "customer", customer.getCustomerId(), customer.toString());
    }

    /**
     * Removes customer from database.
     * @param context the processing context
     * @param customer the customer
     */
    public static void removeCustomer(final SecurityContext context, final Customer customer) {
        requireRole("remove-customer", context, DefaultRoles.ADMINISTRATOR);
        CustomerDao.removeCustomer(context.getEntityManager(), customer);
        AuditService.log(context, "remove", "customer", customer.getCustomerId(), customer.toString());
    }

    /**
     * Adds new userDirectory to database.
     * @param context the processing context
     * @param userDirectory the userDirectory
     */
    public static void addUserDirectory(final SecurityContext context, final UserDirectory userDirectory) {
        requireRole("add-user-directory", context, DefaultRoles.ADMINISTRATOR);
        UserDirectoryDao.addUserDirectory(context.getEntityManager(), userDirectory);
        AuditService.log(context, "add", "user-directory", userDirectory.getUserDirectoryId(), userDirectory.getAddress());
    }

    /**
     * Updates new userDirectory to database.
     * @param context the processing context
     * @param userDirectory the userDirectory
     */
    public static void updateUserDirectory(final SecurityContext context, final UserDirectory userDirectory) {
        requireRole("update-user-directory", context, DefaultRoles.ADMINISTRATOR);
        UserDirectoryDao.updateUserDirectory(context.getEntityManager(), userDirectory);
        AuditService.log(context, "update", "user-directory", userDirectory.getUserDirectoryId(), userDirectory.getAddress());
    }

    /**
     * Removes userDirectory from database.
     * @param context the processing context
     * @param userDirectory the userDirectory
     */
    public static void removeUserDirectory(final SecurityContext context, final UserDirectory userDirectory) {
        requireRole("remove-user-directory", context, DefaultRoles.ADMINISTRATOR);
        UserDirectoryDao.removeUserDirectory(context.getEntityManager(), userDirectory);
        AuditService.log(context, "remove", "user-directory", userDirectory.getUserDirectoryId(), userDirectory.getAddress());
    }

    /**
     * Adds new company to database.
     * @param context the processing context
     * @param company the company
     */
    public static void addCompany(final SecurityContext context, final Company company) {
        requireRole("add-company", context, DefaultRoles.ADMINISTRATOR);
        CompanyDao.addCompany(context.getEntityManager(), company);
        AuditService.log(context, "add", "company", company.getCompanyId(), company.getCompanyName());
    }

    /**
     * Updates new company to database.
     * @param context the processing context
     * @param company the company
     */
    public static void updateCompany(final SecurityContext context, final Company company) {
        requireRole("update-company", context, DefaultRoles.ADMINISTRATOR);
        CompanyDao.updateCompany(context.getEntityManager(), company);
        AuditService.log(context, "update", "company", company.getCompanyId(), company.getCompanyName());
    }

    /**
     * Removes company from database.
     * @param context the processing context
     * @param company the company
     */
    public static void removeCompany(final SecurityContext context, final Company company) {
        requireRole("remove-company", context, DefaultRoles.ADMINISTRATOR);
        CompanyDao.removeCompany(context.getEntityManager(), company);
        AuditService.log(context, "remove", "company", company.getCompanyId(), company.getCompanyName());
    }

    /**
     * Adds user to database.
     * @param context the processing context
     * @param user the user
     * @param defaultGroup the default group
     */
    public static final void addUser(final SecurityContext context, final User user, final Group defaultGroup) {
        final Company company = context.getObject(Company.class);
        if (company.isSelfRegistration()) {
            requireRole("add-user", context, DefaultRoles.ADMINISTRATOR, DefaultRoles.ANONYMOUS);
        } else {
            requireRole("add-user", context, DefaultRoles.ADMINISTRATOR);
        }
        UserDao.addUser(context.getEntityManager(), user, defaultGroup);
        AuditService.log(context, "add", "user", user.getUserId(), user.getEmailAddress());
    }

    /**
     * Updates user to database.
     * @param context the processing context
     * @param user the user
     */
    public static final void updateUser(final SecurityContext context, final User user) {
        if (user.getUserId().equals(context.getUserId())) {
            requireRole("update-user", context, DefaultRoles.ADMINISTRATOR, DefaultRoles.USER);
        } else {
            requireRole("update-user", context, DefaultRoles.ADMINISTRATOR);
        }
        UserDao.updateUser(context.getEntityManager(), user);
        AuditService.log(context, "update", "user", user.getUserId(), user.getEmailAddress());
    }

    /**
     * Removes user from database.
     * @param context the processing context
     * @param user the user
     */
    public static final void removeUser(final SecurityContext context, final User user) {
        requireRole("remove-user", context, DefaultRoles.ADMINISTRATOR);
        UserDao.removeUser(context.getEntityManager(), user);
        AuditService.log(context, "remove", "user", user.getUserId(), user.getEmailAddress());
    }

    /**
     * Adds new group to database.
     * @param context the processing context
     * @param group the group
     */
    public static void addGroup(final SecurityContext context, final Group group) {
        requireRole("add-group", context, DefaultRoles.ADMINISTRATOR);
        UserDao.addGroup(context.getEntityManager(), group);
        AuditService.log(context, "add", "group", group.getGroupId(), group.getName());
    }

    /**
     * Updates new group to database.
     * @param context the processing context
     * @param group the group
     */
    public static void updateGroup(final SecurityContext context, final Group group) {
        requireRole("update-group", context, DefaultRoles.ADMINISTRATOR);
        UserDao.updateGroup(context.getEntityManager(), group);
        AuditService.log(context, "update", "group", group.getGroupId(), group.getName());
    }

    /**
     * Removes group from database.
     * @param context the processing context
     * @param group the group
     */
    public static void removeGroup(final SecurityContext context, final Group group) {
        requireRole("remove-group", context, DefaultRoles.ADMINISTRATOR);
        UserDao.removeGroup(context.getEntityManager(), group);
        AuditService.log(context, "remove", "group", group.getGroupId(), group.getName());
    }

    /**
     * Adds new group member to database.
     * @param context the processing context
     * @param group the group
     * @param user the user
     */
    public static void addGroupMember(final SecurityContext context, final Group group, final User user) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, "group", group.getGroupId(), group.getName(), context, DefaultRoles.ADMINISTRATOR);
        UserDao.addGroupMember(context.getEntityManager(), group, user);
        AuditService.log(context, group.getName() + " member add", "user", user.getUserId(), user.getEmailAddress());
    }

    /**
     * Removes group member from database.
     * @param context the processing context
     * @param group the group
     * @param user the user
     */
    public static void removeGroupMember(final SecurityContext context, final Group group, final User user) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, "group", group.getGroupId(), group.getName(), context, DefaultRoles.ADMINISTRATOR);
        UserDao.removeGroupMember(context.getEntityManager(), group, user);
        AuditService.log(context, group.getName() + " member remove", "user", user.getUserId(), user.getEmailAddress());
    }

    /**
     * Adds new user privilege to database.
     * @param context the processing context
     * @param user the user
     * @param privilegeKey the privilegeKey
     * @param dataType the data type
     * @param dataId the data ID
     * @param dataLabel the data label
     */
    public static void addUserPrivilege(final SecurityContext context, final User user, final String privilegeKey,
                                        final String dataType, final String dataId, final String dataLabel) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, dataType, dataId, dataLabel, context, DefaultRoles.ADMINISTRATOR);
        UserDao.addUserPrivilege(context.getEntityManager(), user, privilegeKey, dataId);
        AuditService.log(context, user.getEmailAddress() + " had " + privilegeKey + " granted", dataType, dataId, dataLabel);
    }

    /**
     * Adds new group privilege to database.
     * @param context the processing context
     * @param group the group
     * @param privilegeKey the privilegeKey
     * @param dataType the data type
     * @param dataId the data ID
     * @param dataLabel the data label
     */
    public static void addGroupPrivilege(final SecurityContext context,
                                         final Group group, final String privilegeKey,
                                         final String dataType, final String dataId, final String dataLabel) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, dataType, dataId, dataLabel, context, DefaultRoles.ADMINISTRATOR);
        UserDao.addGroupPrivilege(context.getEntityManager(), group, privilegeKey, dataId);
        AuditService.log(context, group.getName() + " had " + privilegeKey + " granted", dataType, dataId, dataLabel);
    }

    /**
     * Removes user privilege from database.
     * @param context the processing context
     * @param user the user
     * @param privilegeKey the privilegeKey
     * @param dataType the data type
     * @param dataId the data ID
     * @param dataLabel the data label
     */
    public static void removeUserPrivilege(final SecurityContext context,
                                           final User user, final String privilegeKey,
                                           final String dataType, final String dataId, final String dataLabel) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, dataType, dataId, dataLabel, context, DefaultRoles.ADMINISTRATOR);
        UserDao.removeUserPrivilege(context.getEntityManager(), user, privilegeKey, dataId);
        AuditService.log(context, user.getEmailAddress() + " had " + privilegeKey + " revoked", dataType, dataId, dataLabel);
    }

    /**
     * Removes group privilege from database.
     * @param context the processing context
     * @param group the group
     * @param privilegeKey the privilegeKey
     * @param dataType the data type
     * @param dataId the data ID
     * @param dataLabel the data label
     */
    public static void removeGroupPrivilege(final SecurityContext context,
                                            final Group group, final String privilegeKey,
                                            final String dataType, final String dataId, final String dataLabel) {
        requirePrivilege(DefaultPrivileges.ADMINISTER, dataType, dataId, dataLabel, context, DefaultRoles.ADMINISTRATOR);
        UserDao.removeGroupPrivilege(context.getEntityManager(), group, privilegeKey, dataId);
        AuditService.log(context, group.getName()  + " had " + privilegeKey + " revoked", dataType, dataId, dataLabel);
    }

    /**
     * Require privilege to given data or one of the listed roles.
     * @param key the privilege key
     * @param dataType the data type
     * @param dataId the data ID
     * @param dataLabel the data label
     * @param context the processing context
     * @param roles the privileged roles
     */
    private static synchronized void requirePrivilege(final String key,
                                                      final String dataType, final String dataId, final String dataLabel,
                                                      final SecurityContext context, final String... roles) {
        for (final String role : roles) {
            if (context.getRoles().contains(role)) {
                AuditService.log(context, key + " access granted based on role " + role);
                return;
            }
        }
        if (!hasPrivilege(key, dataId, context)) {
            AuditService.log(context, key + " access denied", dataType, dataId, dataLabel);
            throw new SiteException("Access denied.");
        }
        AuditService.log(context, key + " access granted based on privilege", dataType, dataId, dataLabel);
    }

    /**
     * Require one of the following roles for privilege identified by privilege key.
     * @param key the privilege key
     * @param context the processing context
     * @param roles the roles
     */
    public static final void requireRole(final String key,
                                         final SecurityContext context, final String... roles) {
        for (final String role : roles) {
            if (context.getRoles().contains(role)) {
                AuditService.log(context, key + " access granted based on role " + role);
                return;
            }
        }
        AuditService.log(context, key + " access denied");
        throw new SiteException("Access denied.");
    }

    /**
     * Check if processing context has privilege to access given data.
     * @param key the privilege key
     * @param context the processing context
     * @param dataId the data ID
     * @return true if privilege exists on given data.
     */
    private static synchronized boolean hasPrivilege(final String key, final String dataId, final SecurityContext context) {
        final EntityManager entityManager = context.getEntityManager();
        final Company company = context.getObject(Company.class);

        if (context.getUserId() != null && context.getObject(PrivilegeCache.USER_FOR_PRIVILEGE_CHECK) == null) {
            context.putObject(PrivilegeCache.USER_FOR_PRIVILEGE_CHECK, entityManager.getReference(User.class, context.getUserId()));
        }
        final User user = context.getObject(PrivilegeCache.USER_FOR_PRIVILEGE_CHECK);

        if (context.getObject(PrivilegeCache.USER_GROUPS_FOR_PRIVILEGE_CHECK) == null) {
            context.putObject(PrivilegeCache.USER_GROUPS_FOR_PRIVILEGE_CHECK, UserDao.getUserGroups(entityManager, company, user));;
        }
        final List<Group> groups = context.getObject(PrivilegeCache.USER_GROUPS_FOR_PRIVILEGE_CHECK);

        if (!PrivilegeCache.hasPrivilege(entityManager, company, user, groups, key, dataId)) {
            return false;
        } else {
            return true;
        }
    }
}
