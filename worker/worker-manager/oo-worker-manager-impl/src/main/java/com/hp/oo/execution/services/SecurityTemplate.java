package com.hp.oo.execution.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Collection;
import java.util.Collections;

/**
 * User: Eyal Roth
 * Date: 3/18/13
 * Time: 4:26 PM
 */
public class SecurityTemplate {      //TO BE DELETED!!!! - MEIR

    /**
     * Performs some logic which include invocation of the given callback.
     * @param callback A callback to invoke.
     * @return
     */
    public <T> T invokeSecured(SecurityTemplateCallback<T> callback){
        T result;
        SecurityContext originalSecurityContext = fakeLogin();
        try {
            result = callback.doSecured();
        }
        finally {
            logout(originalSecurityContext);
        }
        return result;
    }

    public interface SecurityTemplateCallback<T> {
        public T doSecured();
    }

    private SecurityContext fakeLogin() {
        SecurityContext originalSecurityContext = SecurityContextHolder.getContext();

        SecurityContext secCtx = new SecurityContextImpl();
        secCtx.setAuthentication(new InternalProcessAuthentication());
        SecurityContextHolder.setContext(secCtx);

        return originalSecurityContext;
    }

    /*
     * Setting the given security context.
     */
    private void logout(SecurityContext securityContextToSet) {
        if (securityContextToSet == null) {
            SecurityContextHolder.clearContext();
        }
        else {
            SecurityContextHolder.setContext(securityContextToSet);
        }
    }


    public final class InternalProcessAuthentication implements Authentication {

        public static final String INTERNAL_PROCESS_PERMISSION_NAME = "InternalProcessPermissionName";

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public Object getCredentials() {
            return INTERNAL_PROCESS_PERMISSION_NAME;
        }

        @Override
        public Object getDetails() {
            return INTERNAL_PROCESS_PERMISSION_NAME;
        }

        @Override
        public Object getPrincipal() {
            return INTERNAL_PROCESS_PERMISSION_NAME;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {
        }

        @Override
        public String getName() {
            return INTERNAL_PROCESS_PERMISSION_NAME;
        }
    }
}
