package org.cateproject.repository.jpa.auth;
import java.util.Iterator;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.cateproject.Application;
import org.cateproject.domain.auth.UserAccount;
import org.cateproject.domain.auth.UserAccountDataOnDemand;
import org.cateproject.multitenant.MultitenantContextHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class UserAccountIntegrationTest {

    @Autowired
    UserAccountDataOnDemand dod;
    
    @Autowired
    UserAccountRepository userAccountRepository;
    
    @Before
    public void setUp() {
        MultitenantContextHolder.getContext().setTenantId(null);
    }

    @Test
    public void testCount() {
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", dod.getRandomUserAccount());
        long count = userAccountRepository.count();
        Assert.assertTrue("Counter for 'UserAccount' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void testFind() {
        UserAccount obj = dod.getRandomUserAccount();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to provide an identifier", id);
        obj = userAccountRepository.findOne(id);
        Assert.assertNotNull("Find method for 'UserAccount' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'UserAccount' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void testFindAll() {
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", dod.getRandomUserAccount());
        long count = userAccountRepository.count();
        Assert.assertTrue("Too expensive to perform a find all test for 'UserAccount', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<UserAccount> result = userAccountRepository.findAll();
        Assert.assertNotNull("Find all method for 'UserAccount' illegally returned null", result);
        Assert.assertTrue("Find all method for 'UserAccount' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void testFindEntries() {
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", dod.getRandomUserAccount());
        long count = userAccountRepository.count();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<UserAccount> result = userAccountRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
        Assert.assertNotNull("Find entries method for 'UserAccount' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'UserAccount' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void testFlush() {
        UserAccount obj = dod.getRandomUserAccount();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to provide an identifier", id);
        obj = userAccountRepository.findOne(id);
        Assert.assertNotNull("Find method for 'UserAccount' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyUserAccount(obj);
        Integer currentVersion = obj.getVersion();
        userAccountRepository.flush();
        Assert.assertTrue("Version for 'UserAccount' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void testSaveUpdate() {
        UserAccount obj = dod.getRandomUserAccount();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to provide an identifier", id);
        obj = userAccountRepository.findOne(id);
        boolean modified =  dod.modifyUserAccount(obj);
        Integer currentVersion = obj.getVersion();
        UserAccount merged = (UserAccount)userAccountRepository.save(obj);
        userAccountRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'UserAccount' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void testSave() {
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", dod.getRandomUserAccount());
        UserAccount obj = dod.getNewTransientUserAccount(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'UserAccount' identifier to be null", obj.getId());
        try {
            userAccountRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        userAccountRepository.flush();
        Assert.assertNotNull("Expected 'UserAccount' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void testDelete() {
        UserAccount obj = dod.getRandomUserAccount();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UserAccount' failed to provide an identifier", id);
        obj = userAccountRepository.findOne(id);
        userAccountRepository.delete(obj);
        userAccountRepository.flush();
        Assert.assertNull("Failed to remove 'UserAccount' with identifier '" + id + "'", userAccountRepository.findOne(id));
    }
}
