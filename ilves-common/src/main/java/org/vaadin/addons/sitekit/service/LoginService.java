/**
 * Copyright 2013 Tommi S.E. Laukkanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.addons.sitekit.service;

import org.vaadin.addons.sitekit.model.Company;
import org.vaadin.addons.sitekit.model.User;
import org.vaadin.addons.sitekit.module.audit.AuditService;
import org.vaadin.addons.sitekit.site.ProcessingContext;
import org.vaadin.addons.sitekit.util.PasswordLoginUtil;

import java.util.List;

/**
 * Login service.
 *
 * @author Tommi S.E. Laukkanen
 */
public class LoginService {

    public static String login(final ProcessingContext context, final Company company, final User user, final String emailAddress, final String password) {
        final String errorKey = PasswordLoginUtil.login(emailAddress, context.getRemoteHost(),
                context.getRemoteIpAddress(), context.getRemotePort(),
                context.getEntityManager(), company, user, password);
        if (errorKey == null) {
            AuditService.log(context, "password login success", "User", user.getUserId(), user.getEmailAddress());
        } else {
            AuditService.log(context, "password login failure", "User", user != null ? user.getUserId() : null, emailAddress);
        }
        return errorKey;
    }

    public static void logout(final ProcessingContext context) {
        AuditService.log(context, " logout");
    }
}