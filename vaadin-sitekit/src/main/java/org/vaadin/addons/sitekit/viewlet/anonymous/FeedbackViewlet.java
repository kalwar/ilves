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
package org.vaadin.addons.sitekit.viewlet.anonymous;

import com.vaadin.data.util.BeanItem;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;
import org.vaadin.addons.sitekit.grid.FieldDescriptor;
import org.vaadin.addons.sitekit.grid.FieldSetDescriptorRegister;
import org.vaadin.addons.sitekit.grid.ValidatingEditor;
import org.vaadin.addons.sitekit.grid.ValidatingEditorStateListener;
import org.vaadin.addons.sitekit.model.Company;
import org.vaadin.addons.sitekit.model.Feedback;
import org.vaadin.addons.sitekit.site.AbstractViewlet;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * Feedback Viewlet.
 *
 * @author Tommi S.E. Laukkanen
 */
public final class FeedbackViewlet extends AbstractViewlet {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(FeedbackViewlet.class);

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Feedback. */
    private Feedback feedback;
    /** Validating editor. */
    private ValidatingEditor editor;

    /**
     * Default constructor.
     */
    public FeedbackViewlet() {

        final List<FieldDescriptor> fieldDescriptors = FieldSetDescriptorRegister.getFieldSetDescriptor("feedback")
                .getFieldDescriptors();

        editor = new ValidatingEditor(fieldDescriptors);

        final Button submitButton = new Button(getSite().localize("button-submit"));
        submitButton.addListener(new ClickListener() {
            /** The default serial version ID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                editor.commit();

                final EntityManager entityManager = getSite().getSiteContext().getObject(EntityManager.class);
                final Company company = getSite().getSiteContext().getObject(Company.class);

                feedback.setCreated(new Date());
                feedback.setModified(feedback.getCreated());
                feedback.setOwner(company);

                try {

                    Notification.show(getSite().localize("message-feedback-submit-success"),
                            Notification.Type.HUMANIZED_MESSAGE);

                } catch (final Exception e) {
                    LOGGER.error("Error adding user.", e);
                    Notification.show(getSite().localize("message-feedback-submit-error"),
                            Notification.TYPE_WARNING_MESSAGE);
                }
                reset();
            }
        });

        editor.addListener(new ValidatingEditorStateListener() {
            @Override
            public void editorStateChanged(final ValidatingEditor source) {
                if (source.isValid()) {
                    submitButton.setEnabled(true);
                } else {
                    submitButton.setEnabled(false);
                }
            }
        });

        reset();


        final HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(new MarginInfo(true, false, true, false));
        titleLayout.setSpacing(true);
        final Embedded titleIcon = new Embedded(null, getSite().getIcon("view-icon-feedback"));
        titleIcon.setWidth(32, UNITS_PIXELS);
        titleIcon.setHeight(32, UNITS_PIXELS);
        titleLayout.addComponent(titleIcon);
        final Label titleLabel = new Label(
                "<h1>" + getSite().localize("view-feedback") + "</h1>", Label.CONTENT_XHTML);
        titleLayout.addComponent(titleLabel);

        final VerticalLayout panel = new VerticalLayout();
        panel.addComponent(titleLayout);
        panel.addComponent(editor);
        panel.addComponent(submitButton);
        panel.setSpacing(true);

        final HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.addComponent(panel);

        setCompositionRoot(mainLayout);

    }

    /**
     * Reset data.
     */
    public void reset() {
        feedback = new Feedback();
        final BeanItem<Feedback> customerItem = new BeanItem<Feedback>(feedback);
        editor.setItem(customerItem, true);
    }

    @Override
    public void enter(final String parameters) {
    }
}