package org.ff4j.sample;

import java.util.HashSet;
import java.util.Set;

import org.ff4j.security.AuthorizationsManager;

public class MockAuthorisationManager implements AuthorizationsManager {

    /** {@inheritDoc} */
    @Override
    public Set<String> getCurrentUserPermissions() {
        return new HashSet<String>();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> listAllPermissions() {
        Set<String> auths = new HashSet<String>();
        auths.add("Administrators");
        auths.add("Users");
        auths.add("SuperUsers");
        return auths;
    }

}
