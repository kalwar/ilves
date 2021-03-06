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
package org.bubblecloud.ilves.ui.anonymous.login;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.log4j.Logger;
import org.bubblecloud.ilves.component.flow.AbstractFlowlet;
import org.bubblecloud.ilves.component.grid.FieldDescriptor;
import org.bubblecloud.ilves.component.grid.ValidatingEditor;
import org.bubblecloud.ilves.component.grid.ValidatingEditorStateListener;
import org.bubblecloud.ilves.component.validator.PasswordValidator;
import org.bubblecloud.ilves.component.validator.PasswordVerificationValidator;
import org.bubblecloud.ilves.model.Company;
import org.bubblecloud.ilves.model.Customer;
import org.bubblecloud.ilves.model.PostalAddress;
import org.bubblecloud.ilves.model.User;
import org.bubblecloud.ilves.module.customer.CustomerModule;
import org.bubblecloud.ilves.security.SecurityService;
import org.bubblecloud.ilves.security.UserDao;
import org.bubblecloud.ilves.site.SiteFields;
import org.bubblecloud.ilves.site.SiteModuleManager;
import org.bubblecloud.ilves.util.EmailUtil;
import org.bubblecloud.ilves.util.PropertiesUtil;
import org.bubblecloud.ilves.util.StringUtil;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Register Flowlet.
 *
 * @author Tommi S.E. Laukkanen
 */
public final class RegisterFlowlet extends AbstractFlowlet {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(RegisterFlowlet.class);

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Customer. */
    private Customer customer;
    /** Original password property. */
    private Property originalPasswordProperty;
    /** Verification password property. */
    private Property verifiedPasswordProperty;
    /** Validating editor. */
    private ValidatingEditor editor;

    @Override
    public String getFlowletKey() {
        return "register";
    }

    /**
     * Reset data.
     */
    public void reset() {
        customer = new Customer();
        final CompositeItem compositeItem = new CompositeItem();

        final PropertysetItem defaultItem = (PropertysetItem) compositeItem.getItem(CompositeItem.DEFAULT_ITEM_KEY);
        originalPasswordProperty.setValue("");
        verifiedPasswordProperty.setValue("");
        defaultItem.addItemProperty("password1", originalPasswordProperty);
        defaultItem.addItemProperty("password2", verifiedPasswordProperty);

        final BeanItem<Customer> customerItem = new BeanItem<Customer>(customer);
        compositeItem.addItem("customer", customerItem);

        originalPasswordProperty.setValue(null);
        verifiedPasswordProperty.setValue(null);
        editor.setItem(compositeItem, true);
    }

    @Override
    public void initialize() {
        originalPasswordProperty = new ObjectProperty<String>(null, String.class);
        verifiedPasswordProperty = new ObjectProperty<String>(null, String.class);

        final List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();

        final PasswordValidator passwordValidator = new PasswordValidator(getSite(), originalPasswordProperty, "password2");

        //fieldDescriptors.addAll(SiteFields.getFieldDescriptors(Customer.class));

        for (final FieldDescriptor fieldDescriptor : SiteFields.getFieldDescriptors(Customer.class)) {
            if (fieldDescriptor.getId().equals("adminGroup")) {
                continue;
            }
            if (fieldDescriptor.getId().equals("memberGroup")) {
                continue;
            }
            if (fieldDescriptor.getId().equals("created")) {
                continue;
            }
            if (fieldDescriptor.getId().equals("modified")) {
                continue;
            }
            fieldDescriptors.add(fieldDescriptor);
        }

        //fieldDescriptors.remove(fieldDescriptors.size() - 1);
        //fieldDescriptors.remove(fieldDescriptors.size() - 1);
        fieldDescriptors.add(new FieldDescriptor("password1", getSite().localize("input-password"),
                PasswordField.class, null, 150, null, String.class, null,
                false, true, true
                ).addValidator(passwordValidator));
        fieldDescriptors.add(new FieldDescriptor("password2", getSite().localize("input-password-verification"),
                PasswordField.class, null, 150, null,
                String.class, null, false, true,
                true).addValidator(new PasswordVerificationValidator(getSite(), originalPasswordProperty)));

        editor = new ValidatingEditor(fieldDescriptors);
        passwordValidator.setEditor(editor);

        final Button registerButton = new Button(getSite().localize("button-register"));
        registerButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        registerButton.addClickListener(new ClickListener() {
            /** The default serial version ID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                editor.commit();
                customer.setCreated(new Date());
                customer.setModified(customer.getCreated());
                final EntityManager entityManager = getSite().getSiteContext().getObject(EntityManager.class);
                final Company company = getSite().getSiteContext().getObject(Company.class);

                final PostalAddress invoicingAddress = new PostalAddress();
                invoicingAddress.setAddressLineOne("?");
                invoicingAddress.setAddressLineTwo("?");
                invoicingAddress.setAddressLineThree("?");
                invoicingAddress.setCity("?");
                invoicingAddress.setPostalCode("?");
                invoicingAddress.setCountry("?");
                final PostalAddress deliveryAddress = new PostalAddress();
                deliveryAddress.setAddressLineOne("?");
                deliveryAddress.setAddressLineTwo("?");
                deliveryAddress.setAddressLineThree("?");
                deliveryAddress.setCity("?");
                deliveryAddress.setPostalCode("?");
                deliveryAddress.setCountry("?");
                customer.setInvoicingAddress(invoicingAddress);
                customer.setDeliveryAddress(deliveryAddress);

                if (UserDao.getUser(entityManager, company, customer.getEmailAddress()) != null) {
                    Notification.show(getSite().localize("message-user-email-address-registered"),
                            Notification.Type.WARNING_MESSAGE);
                    return;
                }

                final HttpServletRequest request = ((VaadinServletRequest) VaadinService.getCurrentRequest())
                        .getHttpServletRequest();

                try {
                    final byte[] passwordAndSaltBytes = (customer.getEmailAddress()
                            + ":" + ((String) originalPasswordProperty.getValue()))
                            .getBytes("UTF-8");
                    final MessageDigest md = MessageDigest.getInstance("SHA-256");
                    final byte[] passwordAndSaltDigest = md.digest(passwordAndSaltBytes);

                    customer.setOwner(company);
                    final User user = new User(company, customer.getFirstName(), customer.getLastName(),
                            customer.getEmailAddress(), customer.getPhoneNumber(), StringUtil.toHexString(passwordAndSaltDigest));

                    SecurityService.addUser(getSite().getSiteContext(), user, UserDao.getGroup(entityManager, company, "user"));

                    if (SiteModuleManager.isModuleInitialized(CustomerModule.class)) {
                        SecurityService.addCustomer(getSite().getSiteContext(), customer, user);
                    }

                    final String url = company.getUrl() +
                            "#!validate/" + user.getUserId();

                    final Thread emailThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            EmailUtil.send(PropertiesUtil.getProperty("site", "smtp-host"),
                                    user.getEmailAddress(), company.getSupportEmailAddress(), "Email Validation",
                                    "Please validate your email by browsing to this URL: " + url);
                        }
                    });
                    emailThread.start();

                    LOGGER.info("User registered " + user.getEmailAddress()
                            + " (IP: " + request.getRemoteHost() + ":" + request.getRemotePort() + ")");
                    Notification.show(getSite().localize("message-registration-success"),
                            Notification.Type.HUMANIZED_MESSAGE);

                    getFlow().back();
                } catch (final Exception e) {
                    LOGGER.error("Error adding user. (IP: " + request.getRemoteHost()
                            + ":" + request.getRemotePort() + ")", e);
                    Notification.show(getSite().localize("message-registration-error"),
                            Notification.Type.WARNING_MESSAGE);
                }
                reset();
            }
        });

        editor.addListener(new ValidatingEditorStateListener() {
            @Override
            public void editorStateChanged(final ValidatingEditor source) {
                if (source.isValid()) {
                    registerButton.setEnabled(true);
                } else {
                    registerButton.setEnabled(false);
                }
            }
        });

        reset();

        final VerticalLayout panel = new VerticalLayout();
        panel.addComponent(editor);
        panel.addComponent(registerButton);
        panel.setSpacing(true);

        final HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setMargin(true);
        mainLayout.addComponent(panel);

        final Panel mainPanel = new Panel();
        mainPanel.setSizeUndefined();
        mainPanel.setContent(mainLayout);

        setViewContent(mainPanel);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void enter() {
    }

}
