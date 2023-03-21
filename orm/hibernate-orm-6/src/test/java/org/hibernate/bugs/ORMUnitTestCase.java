/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * Although ORMStandaloneTestCase is perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing your reproducer using this method
 * simplifies the process.
 *
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
public class ORMUnitTestCase extends BaseCoreFunctionalTestCase {

	// Add your entities here.
	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[]{
				FederatedIdentityEntity.class,
				UserEntity.class
		};
	}

	// If you use *.hbm.xml mappings, instead of annotations, add the mappings here.
	@Override
	protected String[] getMappings() {
		return new String[] {
//				"Foo.hbm.xml",
//				"Bar.hbm.xml"
		};
	}
	// If those mappings reside somewhere other than resources/org/hibernate/test, change this.
	@Override
	protected String getBaseForMappings() {
		return "org/hibernate/test/";
	}

	// Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
	@Override
	protected void configure(Configuration configuration) {
		super.configure( configuration );

		configuration.setProperty( AvailableSettings.SHOW_SQL, Boolean.TRUE.toString() );
		configuration.setProperty( AvailableSettings.FORMAT_SQL, Boolean.TRUE.toString() );
		//configuration.setProperty( AvailableSettings.GENERATE_STATISTICS, "true" );
	}

	// Add your tests, using standard JUnit.
	@Test
	public void hhh123Test() throws Exception {
		final String someProviderId = "44442222";
		Long id;

		// Create a User entity
		{
			Session s = openSession();
			s.getTransaction().begin();

			UserEntity userEntity = new UserEntity();
			userEntity.setUsername("John");

			s.persist(userEntity);
			s.flush();

			id = userEntity.getId();

			s.getTransaction().commit();
			s.close();
		}

		assertNotNull(id);

		// Create a Federated Identity entity
		{
			Session s = openSession();
			s.getTransaction().begin();

			FederatedIdentityEntity federatedIdentity = new FederatedIdentityEntity();
			federatedIdentity.setIdentityProvider(someProviderId);

			UserEntity userEntity = s.getReference(UserEntity.class, id);
			assertNotNull(userEntity);

			federatedIdentity.setUser(userEntity);

			s.persist(federatedIdentity);
			s.flush();

			s.getTransaction().commit();
			s.close();
		}

		// Update federated identity
		{
			Session s = openSession();
			s.getTransaction().begin();

			FederatedIdentityEntity federatedIdentity = findFederatedIdentity(s, id, someProviderId);

			assertNotNull(federatedIdentity);
			Assert.assertEquals(federatedIdentity.getUser().getId(), id);

			federatedIdentity.setUserName("ChangedUsername");
			s.persist(federatedIdentity);
			s.flush();

			s.getTransaction().commit();
			s.close();
		}
	}

	private FederatedIdentityEntity findFederatedIdentity(Session s, Long userId, String identityProvider) {
		TypedQuery<FederatedIdentityEntity> query = s.createNamedQuery("findFederatedIdentityByUserAndProvider", FederatedIdentityEntity.class);

		UserEntity userEntity = s.getReference(UserEntity.class, userId);

		query.setParameter("user", userEntity);
		query.setParameter("identityProvider", identityProvider);

		List<FederatedIdentityEntity> results = query.getResultList();
		return results.size() > 0 ? results.get(0) : null;
	}
}
