/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.accounts.backend.boundary;

import java.util.UUID;
import org.hawkular.accounts.backend.entity.HawkularUser;
import org.hawkular.accounts.backend.entity.Organization;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public class PermissionCheckerTest {

  @Test
  public void ownerBelongsToOrganization() {
    HawkularUser user = new HawkularUser(UUID.randomUUID().toString());
    Organization organization = new Organization(UUID.randomUUID().toString(), user);
    PermissionChecker checker = new PermissionChecker();
    assertTrue("Owner of an organization should be a member of it", checker.hasAccessTo(user, organization));
  }

  @Test
  public void memberBelongsToOrganization() {
    HawkularUser owner = new HawkularUser(UUID.randomUUID().toString());
    HawkularUser member = new HawkularUser(UUID.randomUUID().toString());
    Organization organization = new Organization(UUID.randomUUID().toString(), owner);
    organization.addMember(member);

    PermissionChecker checker = new PermissionChecker();
    assertTrue("Direct member of an organization should be a member of it", checker.hasAccessTo(member, organization));
  }

  @Test
  public void organizationBelongsToOrganization() {
    // case here:
    // acme owns emca
    // jdoe is the owner of acme
    // therefore, acme is a member of emca

    HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
    Organization acme = new Organization(UUID.randomUUID().toString(), jdoe);
    Organization emca = new Organization(UUID.randomUUID().toString(), acme);

    PermissionChecker checker = new PermissionChecker();
    assertTrue("Organization owner of an organization should be a member of it", checker.isMemberOf(acme, emca));
  }

  @Test
  public void ownerBelongsToInnerOrganization() {
    // case here:
    // acme owns emca
    // jdoe is the owner of acme
    // therefore, jdoe is a member of emca

    HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
    Organization acme = new Organization(UUID.randomUUID().toString(), jdoe);
    Organization emca = new Organization(UUID.randomUUID().toString(), acme);

    PermissionChecker checker = new PermissionChecker();
    assertTrue("Owner of parent organization should be a member of it", checker.hasAccessTo(jdoe, emca));
  }

  @Test
  public void memberBelongsToInnerOrganization() {
    // case here:
    // acme owns emca
    // jdoe is the owner of acme
    // jsmith is a member of acme
    // therefore, jsmith is a member of emca

    HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
    HawkularUser jsmith = new HawkularUser(UUID.randomUUID().toString());
    Organization acme = new Organization(UUID.randomUUID().toString(), jdoe);
    Organization emca = new Organization(UUID.randomUUID().toString(), acme);
    acme.addMember(jsmith);

    PermissionChecker checker = new PermissionChecker();
    assertTrue("Owner of parent organization should be a member of it", checker.hasAccessTo(jsmith, emca));
  }

  @Test
  public void siblingsDontBelong() {
    // case here:
    // acme owns marketing
    // acme owns finance
    // jdoe is the owner of acme
    // finance DOES NOT BELONGS to marketing

    HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
    Organization acme = new Organization(UUID.randomUUID().toString(), jdoe);
    Organization finance = new Organization(UUID.randomUUID().toString(), acme);
    Organization marketing = new Organization(UUID.randomUUID().toString(), acme);

    PermissionChecker checker = new PermissionChecker();
    assertTrue("Siblings are not a member of each other", !checker.isMemberOf(marketing, finance));
    assertTrue("Siblings are not a member of each other", !checker.isMemberOf(finance, marketing));
  }

}
