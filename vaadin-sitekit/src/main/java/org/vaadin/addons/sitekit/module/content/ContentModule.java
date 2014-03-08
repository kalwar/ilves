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
package org.vaadin.addons.sitekit.module.content;

import org.vaadin.addons.sitekit.grid.FieldSetDescriptor;
import org.vaadin.addons.sitekit.grid.FieldSetDescriptorRegister;
import org.vaadin.addons.sitekit.model.Company;
import org.vaadin.addons.sitekit.module.SiteModule;
import org.vaadin.addons.sitekit.module.content.dao.ContentDao;
import org.vaadin.addons.sitekit.module.content.model.Content;
import org.vaadin.addons.sitekit.module.content.model.MarkupType;
import org.vaadin.addons.sitekit.module.content.view.ContentFlow;
import org.vaadin.addons.sitekit.module.content.view.MarkdownViewlet;
import org.vaadin.addons.sitekit.module.content.view.MarkupField;
import org.vaadin.addons.sitekit.module.content.view.MarkupTypeField;
import org.vaadin.addons.sitekit.site.*;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Content module adds support for Wiki content management.
 *
 * @author Tommi S.E. Laukkanen
 */
public class ContentModule implements SiteModule {

    @Override
    public void initialize() {
        final SiteDescriptor siteDescriptor = DefaultSiteUI.getContentProvider().getSiteDescriptor();

        final NavigationVersion navigationVersion = siteDescriptor.getNavigation().getProductionVersion();
        navigationVersion.addChildPage("configuration", 0, "content");

        // Describe content view.
        final ViewDescriptor viewDescriptor = new ViewDescriptor("content", "Content", DefaultView.class);
        viewDescriptor.setViewerRoles("administrator");
        viewDescriptor.setViewletClass("content", ContentFlow.class);
        siteDescriptor.getViewDescriptors().add(viewDescriptor);

        // Describe feedback view fields.
        final FieldSetDescriptor fieldSetDescriptor = new FieldSetDescriptor(Content.class);

        fieldSetDescriptor.setVisibleFieldIds(new String[]{
                "page", "title", "parentPage", "afterPage", "markupType", "markup", "created", "modified"
        });

        fieldSetDescriptor.getFieldDescriptor("created").setReadOnly(true);
        fieldSetDescriptor.getFieldDescriptor("created").setCollapsed(true);
        fieldSetDescriptor.getFieldDescriptor("modified").setReadOnly(true);
        fieldSetDescriptor.getFieldDescriptor("modified").setCollapsed(true);
        fieldSetDescriptor.getFieldDescriptor("page").setRequired(false);
        fieldSetDescriptor.getFieldDescriptor("parentPage").setRequired(false);
        fieldSetDescriptor.getFieldDescriptor("parentPage").setCollapsed(true);
        fieldSetDescriptor.getFieldDescriptor("afterPage").setRequired(false);
        fieldSetDescriptor.getFieldDescriptor("afterPage").setCollapsed(true);
        fieldSetDescriptor.getFieldDescriptor("markupType").setFieldClass(MarkupTypeField.class);
        fieldSetDescriptor.getFieldDescriptor("markupType").setConverter(null);
        fieldSetDescriptor.getFieldDescriptor("markup").setFieldClass(MarkupField.class);
        fieldSetDescriptor.getFieldDescriptor("markup").setWidth(800);
        fieldSetDescriptor.getFieldDescriptor("markup").getValidators().clear();
        fieldSetDescriptor.getFieldDescriptor("markup").setCollapsed(true);
        fieldSetDescriptor.getFieldDescriptor("title").setWidth(-1);

        FieldSetDescriptorRegister.registerFieldSetDescriptor(Content.class, fieldSetDescriptor);

    }

    @Override
    public void injectDynamicContent(final SiteDescriptor dynamicSiteDescriptor) {
        final Company company = Site.getCurrent().getSiteContext().getObject(Company.class);
        final EntityManager entityManager = Site.getCurrent().getSiteContext().getObject(EntityManager.class);
        final List<Content> contents = ContentDao.getContens(entityManager, company);

        final NavigationVersion navigationVersion = dynamicSiteDescriptor.getNavigation().getProductionVersion();

        for (final Content content : contents) {
            final String parentPage = content.getParentPage();
            final String afterPage = content.getAfterPage();
            final String page = content.getPage();
            final String title = content.getTitle();
            final MarkupType markupType = content.getMarkupType();
            final String markup = content.getMarkup();

            if (parentPage == null) {
                navigationVersion.addRootPage(0, page);
            } else {
                navigationVersion.addChildPage(parentPage, page);
            }

            // Describe content view.
            final ViewDescriptor viewDescriptor = new ViewDescriptor(page, title, DefaultView.class);
            viewDescriptor.getProductionVersion().setDynamic(true);
            viewDescriptor.setViewletClass("content", MarkdownViewlet.class, markup);
            dynamicSiteDescriptor.getViewDescriptors().add(viewDescriptor);
        }

    }
}