/*
 * Copyright (c) 2011, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     05/19/2010-2.1 ailitchev - Bug 244124 - Add Nested FetchGroup
package org.eclipse.persistence.testing.tests.jpa.fieldaccess.fetchgroups;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import junit.framework.TestSuite;
import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced.Employee;
import org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced.PhoneNumber;
import org.junit.Test;

import java.util.List;

/**
 * Simple tests to verify the functionality of single level FetchGroup usage
 *
 * @author dclarke
 * @since EclipseLink 2.1
 */
public class SimpleDefaultFetchGroupTests extends BaseFetchGroupTests {

    public SimpleDefaultFetchGroupTests() {
        super();
    }

    public SimpleDefaultFetchGroupTests(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("SimpleDefaultFetchGroupTests");

        suite.addTest(new SimpleDefaultFetchGroupTests("testSetup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("findDefaultFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("singleResultDefaultFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("resultListDefaultFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("resultListWithJoinFetchAddress"));
        suite.addTest(new SimpleDefaultFetchGroupTests("resultListWithJoinFetchAddress_AddressInFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("singleResultNoFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("resultListNoFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("emptyFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("managerFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("employeeNamesFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("namedEmptyFetchGroupUsingGetSingleResult"));
        suite.addTest(new SimpleDefaultFetchGroupTests("namedNamesFetchGroupUsingGetSingleResult"));
        suite.addTest(new SimpleDefaultFetchGroupTests("joinFetchEmployeeAddressWithDynamicFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("joinFetchEmployeeAddressPhoneWithDynamicFetchGroup"));
        suite.addTest(new SimpleDefaultFetchGroupTests("joinFetchEmployeeAddressPhoneWithDynamicFetchGroup_AddressInFetchGroup"));

        return suite;
    }

    /**
     * Set default fetch groups.
     *
     * @see EmployeeCustomizer
     * @see PhoneCustomizer
     */
    @Override
    public void setUp() {
        super.setUp();

        try {
            (new EmployeeCustomizer()).customize(employeeDescriptor);
            (new PhoneCustomizer()).customize(phoneDescriptor);
            // reprepare read queries after all fetch groups set into all descriptors.
            reprepareReadQueries(employeeDescriptor);
            reprepareReadQueries(phoneDescriptor);
        } catch (RuntimeException rtEx) {
            throw rtEx;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        FetchGroup defaultEmpFG = employeeDescriptor.getDefaultFetchGroup();
        FetchGroup defaultPhoneFG = phoneDescriptor.getDefaultFetchGroup();

        assertConfig(employeeDescriptor, defaultEmpFG, 0);
        assertConfig(phoneDescriptor, defaultPhoneFG, 0);

    }

    @Test
    public void findDefaultFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            Employee emp = minimumEmployee(em);
            assertNotNull(emp);
            assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertDefaultFetched(emp);

            assertNotFetchedAttribute(emp, "salary");
            emp.getSalary();

            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertFetchedAttribute(emp, "salary");

            assertNoFetchGroup(emp.getAddress());

            assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
            }

            assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            if (emp.getManager() != null) {
                assertDefaultFetched(emp.getManager());
                assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            } else {
                // If manager_id field is null then getManager() does not trigger an sql.
                assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void singleResultDefaultFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
            query.setParameter("ID", minimumEmployeeId(em));

            Employee emp = (Employee) query.getSingleResult();

            assertNotNull(emp);
            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            assertDefaultFetched(emp);

            emp.getSalary();

            assertFetchedAttribute(emp, "salary");
            assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            assertNoFetchGroup(emp.getAddress());

            assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
            }

            assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            if (emp.getManager() != null) {
                assertDefaultFetched(emp.getManager());
                assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            } else {
                // If manager_id field is null then getManager() does not trigger an sql.
                assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void resultListDefaultFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);
            TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID", Employee.class);
            query.setParameter("ID", minimumEmployeeId(em));

            List<Employee> emps = query.getResultList();

            assertNotNull(emps);
            assertEquals(1, emps.size());

            Employee emp = emps.get(0);

            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertDefaultFetched(emp);

            emp.getSalary();

            assertNoFetchGroup(emp);
            assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            assertNoFetchGroup(emp.getAddress());

            assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
            }
            assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            if (emp.getManager() != null) {
                assertDefaultFetched(emp.getManager());
                assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            } else {
                // If manager_id field is null then getManager() does not trigger an sql.
                assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void resultListWithJoinFetchAddress() {
        internalResultListWithJoinFetchAddress(false);
    }

    @Test
    public void resultListWithJoinFetchAddress_AddressInFetchGroup() {
        internalResultListWithJoinFetchAddress(true);
    }
    void internalResultListWithJoinFetchAddress(boolean addAddressToFetchGroup) {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);
            Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id = :ID");
            query.setParameter("ID", minimumEmployeeId(em));
            assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            FetchGroup fg = null;
            if(addAddressToFetchGroup) {
                // that returns clone of the default fetch group
                fg = employeeDescriptor.getFetchGroupManager().createDefaultFetchGroup();
                fg.addAttribute("address");
                query.setHint(QueryHints.FETCH_GROUP, fg);
            }
            Employee emp = (Employee)query.getSingleResult();
            int nSql = 2;
            if(!addAddressToFetchGroup) {
                // An extra sql to read employee's Address -
                // because address attribute is not in the fetch group the Address object was not built
                // by join fetch - though the db row for address was read in.
                //
                // yet another extra sql generated when the whole employee object is read when address is set.
                nSql = nSql + 2;
            }

            assertEquals(nSql, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            if(addAddressToFetchGroup) {
                assertFetched(emp, fg);
            } else {
                // the whole object has been instantiated when address has been set
                assertNoFetchGroup(emp);
            }

            // instantiates the whole object - unless already instantiated
            emp.getSalary();

            assertNoFetchGroup(emp);
            if(addAddressToFetchGroup) {
                nSql++;
            }
            assertEquals(nSql, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            assertNoFetchGroup(emp.getAddress());
            assertEquals(nSql, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
            }
            assertEquals(++nSql, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            if (emp.getManager() != null) {
                assertDefaultFetched(emp.getManager());
                assertEquals(++nSql, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            } else {
                // If manager_id field is null then getManager() does not trigger an sql.
                assertEquals(nSql, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void singleResultNoFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
            query.setParameter("ID", minimumEmployeeId(em));

            assertNotNull(employeeDescriptor.getFetchGroupManager().getDefaultFetchGroup());

            query.setHint(QueryHints.FETCH_GROUP_DEFAULT, "false");
            assertNull(getFetchGroup(query));

            Employee emp = (Employee) query.getSingleResult();

            assertNotNull(emp);
            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertNoFetchGroup(emp);
            assertNoFetchGroup(emp.getAddress());

            assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
            }

            assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void resultListNoFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID", Employee.class);
            query.setParameter("ID", minimumEmployeeId(em));
            query.setHint(QueryHints.FETCH_GROUP_DEFAULT, "false");

            List<Employee> emps = query.getResultList();

            assertNotNull(emps);
            assertEquals(1, emps.size());

            Employee emp = emps.get(0);

            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            assertNoFetchGroup(emp);
            assertNoFetchGroup(emp.getAddress());

            assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
            }
            assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void emptyFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            // Use q query since find will only use default fetch group
            Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
            query.setParameter("ID", minimumEmployeeId(em));
            FetchGroup emptyFG = new FetchGroup("empty@" + System.currentTimeMillis());
            query.setHint(QueryHints.FETCH_GROUP, emptyFG);

            assertEquals(emptyFG, getFetchGroup(query));

            Employee emp = (Employee) query.getSingleResult();

            assertFetched(emp, emptyFG);

            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);
                phone.getAreaCode();
                assertNoFetchGroup(phone);
            }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Override
    @Test
    public void managerFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            //Use q query since find will only use default fetch group
            //Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
            //query.setParameter("ID", minimumEmployeeId(em));

            //Complex where clause used to avoid triggering employees and their departments:
            //   Don't include employees who are managers themselves - otherwise if first selected as employee, then as e.manager the full read will be triggered;
            //   Don't include managers with departments - because there is no fetch group on e.manager its (non-null) department will trigger an extra sql
            Query query = em.createQuery("SELECT e FROM Employee e WHERE e.manager IS NOT NULL AND NOT EXISTS(SELECT e2 FROM Employee e2 WHERE e2.manager = e) AND e.manager.department IS NULL");
            FetchGroup managerFG = new FetchGroup();
            managerFG.addAttribute("manager");

            query.setHint(QueryHints.FETCH_GROUP, managerFG);
            query.setHint(QueryHints.LEFT_FETCH, "e.manager");

            assertEquals(managerFG, getFetchGroup(query));

            List employees = query.getResultList();
            Employee emp = (Employee)employees.get(0);

            assertFetched(emp, managerFG);
            assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            emp.getManager();

            assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertFetched(emp, managerFG);

            emp.getLastName();

            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertNoFetchGroup(emp);

            int numPhones = 0;
            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);

                phone.getAreaCode();

                assertNoFetchGroup(phone);
                numPhones++;
            }
            // 1 sql to read all the phones with a fetch group + 1 sql per phone to re-read each phone without fetch group
            assertEquals(3 + numPhones, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Override
    @Test
    public void employeeNamesFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            int minId = minimumEmployeeId(em);
            assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            // Use q query since find will only use default fetch group
            Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
            query.setParameter("ID", minId);

            FetchGroup namesFG = new FetchGroup();
            namesFG.addAttribute("firstName");
            namesFG.addAttribute("lastName");
            query.setHint(QueryHints.FETCH_GROUP, namesFG);

            assertNotNull(getFetchGroup(query));
            assertSame(namesFG, getFetchGroup(query));

            Employee emp = (Employee) query.getSingleResult();

            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertFetched(emp, namesFG);

            emp.getId();
            emp.getFirstName();
            emp.getLastName();
            emp.getVersion();

            assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertFetched(emp, namesFG);

            emp.getGender();
            emp.getSalary();

            assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            assertNoFetchGroup(emp);

            int numPhones = 0;
            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                assertDefaultFetched(phone);

                phone.getAreaCode();

                assertNoFetchGroup(phone);
                numPhones++;
            }
            // 1 sql to read all the phones with a fetch group + 1 sql per phone to re-read each phone without fetch group
            assertEquals(4 + numPhones, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

            if(emp.getManager() != null) {
                assertEquals(5 + numPhones, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
                assertDefaultFetched(emp.getManager());
            } else {
                // If manager_id field is null then getManager() does not trigger an sql.
                assertEquals(4 + numPhones, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    @Test
    public void namedEmptyFetchGroupUsingGetSingleResult() {
        FetchGroup fetchGroup = new FetchGroup("test");
        employeeDescriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertTrue(fetchGroup.getItems().isEmpty());
        assertEquals(1, employeeDescriptor.getFetchGroupManager().getFetchGroups().size());

        EntityManager em = createEntityManager("fieldaccess");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP_NAME, "test");

        Employee emp = (Employee) query.getSingleResult();
        assertNotNull(emp);
        assertFetched(emp, "test");
    }

    @Test
    public void namedNamesFetchGroupUsingGetSingleResult() {
        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");

        employeeDescriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertEquals(2, fetchGroup.getItems().size());

        assertEquals(1, employeeDescriptor.getFetchGroupManager().getFetchGroups().size());

        EntityManager em = createEntityManager("fieldaccess");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP_NAME, "names");

        Employee emp = (Employee) query.getSingleResult();
        assertNotNull(emp);

        FetchGroupTracker tracker = (FetchGroupTracker) emp;
        assertNotNull(tracker);

        FetchGroup usedFG = tracker._persistence_getFetchGroup();

        assertNotNull("No FetchGroup found on read Employee", fetchGroup);
        // No longer can make any assumptions about a name of FetchGroup on the entity
        // What's guaranteed is that attribute sets are the same
        assertEquals(fetchGroup.getAttributeNames(), usedFG.getAttributeNames());
//        assertEquals(fetchGroup.getName(), usedFG.getName());
//        assertSame(fetchGroup, ((EntityFetchGroup)usedFG).getParent());
        assertEquals(4, fetchGroup.getItems().size());
        assertTrue(tracker._persistence_isAttributeFetched("id"));
        assertTrue(tracker._persistence_isAttributeFetched("version"));
        assertFalse(tracker._persistence_isAttributeFetched("salary"));
        assertTrue(tracker._persistence_isAttributeFetched("firstName"));
        assertTrue(tracker._persistence_isAttributeFetched("lastName"));
        assertFalse(tracker._persistence_isAttributeFetched("department"));
    }

    @Override
    @Test
    public void joinFetchEmployeeAddressWithDynamicFetchGroup() {
        EntityManager em = createEntityManager("fieldaccess");

        Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, fetchGroup);

        List<?> emps = query.getResultList();

        assertNotNull(emps);
    }

    @Override
    @Test
    public void joinFetchEmployeeAddressPhoneWithDynamicFetchGroup() {
        internalJoinFetchEmployeeAddressPhoneWithDynamicFetchGroup(false);
    }
    @Test
    public void joinFetchEmployeeAddressPhoneWithDynamicFetchGroup_AddressInFetchGroup() {
        internalJoinFetchEmployeeAddressPhoneWithDynamicFetchGroup(true);
    }
    void internalJoinFetchEmployeeAddressPhoneWithDynamicFetchGroup(boolean addAddressToFetchGroup) {
        EntityManager em = createEntityManager("fieldaccess");
        try {
            beginTransaction(em);

            TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id IN (SELECT p.owner.id FROM PhoneNumber p)", Employee.class);

            FetchGroup fetchGroup = new FetchGroup("names");
            fetchGroup.addAttribute("firstName");
            fetchGroup.addAttribute("lastName");
            if(addAddressToFetchGroup) {
                fetchGroup.addAttribute("address");
            }
            query.setHint(QueryHints.FETCH_GROUP, fetchGroup);

            List<Employee> emps = query.getResultList();
            assertNotNull(emps);

            if(addAddressToFetchGroup) {
                assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            } else {
                assertEquals(1 + 2*emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
            }
            for (Employee emp : emps) {
                if(addAddressToFetchGroup) {
                    assertFetched(emp, fetchGroup);
                } else {
                    // the whole object has been instantiated when address has been set
                    assertNoFetchGroup(emp);
                }
        }
        } finally {
            if (isTransactionActive(em)){
                rollbackTransaction(em);
            }
            closeEntityManager(em);
        }
    }

    public static class EmployeeCustomizer implements DescriptorCustomizer {

        @Override
        public void customize(ClassDescriptor descriptor) {
            FetchGroup fg = new FetchGroup("Employee-default");
            fg.addAttribute("firstName");
            fg.addAttribute("lastName");
            descriptor.getFetchGroupManager().setDefaultFetchGroup(fg);
        }

    }

/*    public static class PhoneCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            FetchGroup fg = new FetchGroup("Phone-default");
            fg.addAttribute("number");
            if (!descriptor.hasFetchGroupManager()) {
                descriptor.setFetchGroupManager(new FetchGroupManager());
            }
            descriptor.getFetchGroupManager().setDefaultFetchGroup(fg);
        }

    }*/
}
