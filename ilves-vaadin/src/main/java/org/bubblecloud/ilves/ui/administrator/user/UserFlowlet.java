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
package org.bubblecloud.ilves.ui.administrator.user;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import org.bubblecloud.ilves.component.flow.AbstractFlowlet;
import org.bubblecloud.ilves.component.grid.*;
import org.bubblecloud.ilves.model.GroupMember;
import org.bubblecloud.ilves.model.User;
import org.bubblecloud.ilves.security.DefaultRoles;
import org.bubblecloud.ilves.security.PasswordLoginUtil;
import org.bubblecloud.ilves.security.SecurityService;
import org.bubblecloud.ilves.security.UserDao;
import org.bubblecloud.ilves.site.SiteFields;
import org.bubblecloud.ilves.util.ContainerUtil;
import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import javax.persistence.EntityManager;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User edit Flowlet.
 *
 * @author Tommi S.E. Laukkanen
 */
public final class UserFlowlet extends AbstractFlowlet implements ValidatingEditorStateListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The entity manager. */
    private EntityManager entityManager;
    /** The user flow. */
    private User user;

    /** The entity form. */
    private ValidatingEditor editor;
    /** The save button. */
    private Button saveButton;
    /** The discard button. */
    private Button discardButton;

    /** The entity container. */
    private EntityContainer<GroupMember> childContainer;
    /** The list button layout. */
    private HorizontalLayout childListButtonLayout;
    /** The user element grid. */
    private Grid childGrid;

    @Override
    public String getFlowletKey() {
        return "user";
    }

    @Override
    public boolean isDirty() {
        return editor.isModified();
    }

    @Override
    public boolean isValid() {
        return editor.isValid();
    }

    @Override
    public void initialize() {
        entityManager = getSite().getSiteContext().getObject(EntityManager.class);

        final GridLayout layout = new GridLayout(2, 3);
        layout.setSizeFull();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setRowExpandRatio(1, 1f);
        layout.setColumnExpandRatio(1, 1f);
        setViewContent(layout);

        editor = new ValidatingEditor(SiteFields.getFieldDescriptors(User.class));
        editor.setCaption("User");
        editor.addListener((ValidatingEditorStateListener) this);
        editor.setWidth("480px");
        layout.addComponent(editor, 0, 1);


        final List<FieldDescriptor> childFieldDescriptors = SiteFields.getFieldDescriptors(GroupMember.class);
        final List<FilterDescriptor> childFilterDescriptors = new ArrayList<FilterDescriptor>();
        childContainer = new EntityContainer<GroupMember>(entityManager, GroupMember.class, "groupMemberId", 1000,
            true, false, false);
        childContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[] {"user.firstName", "user.lastName"}, new boolean[] {true, true});

        ContainerUtil.addContainerProperties(childContainer, childFieldDescriptors);

        final Table childTable = new FormattingTable();
        childGrid = new Grid(childTable, childContainer);
        childGrid.setFields(childFieldDescriptors);
        childGrid.setFilters(childFilterDescriptors);

        childTable.setColumnCollapsed("created", true);

        layout.addComponent(childGrid, 1, 1);

        final HorizontalLayout editorButtonLayout = new HorizontalLayout();
        editorButtonLayout.setSpacing(true);
        layout.addComponent(editorButtonLayout, 0, 2);

        saveButton = new Button("Save");
        saveButton.setImmediate(true);
        editorButtonLayout.addComponent(saveButton);
        saveButton.addClickListener(new ClickListener() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                editor.commit();
                try {
                    final boolean toBeAdded = user.getUserId() == null;
                    if (user.getPasswordHash() != null) {
                        final int hashSize = 64;
                        if (user.getPasswordHash().length() != hashSize) {
                            try {
                                PasswordLoginUtil.setUserPasswordHash(user.getOwner(), user, user.getPasswordHash());
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // UserLogic.updateUser(user,
                    // UserDao.getGroupMembers(entityManager, user));
                    if (toBeAdded) {
                        SecurityService.addUser(getSite().getSiteContext(), user,
                                UserDao.getGroup(entityManager, user.getOwner(), DefaultRoles.USER));
                        childGrid.refresh();
                    } else {
                        SecurityService.updateUser(getSite().getSiteContext(), user);
                    }

                    editor.setItem(new BeanItem<User>(user), false);
                    //entityManager.detach(user);
                } catch (final Throwable t) {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                    throw new RuntimeException("Failed to save entity: " + user, t);
                }
            }
        });

        discardButton = new Button("Discard");
        discardButton.setImmediate(true);
        editorButtonLayout.addComponent(discardButton);
        discardButton.addClickListener(new ClickListener() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                editor.discard();
            }
        });

        childListButtonLayout = new HorizontalLayout();
        childListButtonLayout.setSpacing(true);
        childListButtonLayout.setSizeUndefined();
        layout.addComponent(childListButtonLayout, 1, 0);

        final Button addButton = getSite().getButton("add");
        childListButtonLayout.addComponent(addButton);
        addButton.addClickListener(new ClickListener() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                final GroupMember userElement = new GroupMember();
                userElement.setUser(user);
                userElement.setCreated(new Date());
                final UserGroupMemberFlowlet userGroupMemberFlowlet = getFlow().forward(UserGroupMemberFlowlet.class);
                userGroupMemberFlowlet.edit(userElement, true);
            }
        });

        final Button removeButton = getSite().getButton("remove");
        childListButtonLayout.addComponent(removeButton);
        removeButton.addClickListener(new ClickListener() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                if (childGrid.getSelectedItemId() == null) {
                    return;
                }
                childContainer.removeItem(childGrid.getSelectedItemId());
                childContainer.commit();
            }
        });

    }

    /**
     * Edit an existing user.
     * @param entity entity to be edited.
     * @param newEntity true if entity to be edited is new.
     */
    public void edit(final User entity, final boolean newEntity) {
        this.user = entity;
        editor.setItem(new BeanItem<User>(entity), newEntity);
        childContainer.getQueryView().getQueryDefinition().removeDefaultFilters();
        childContainer.getQueryView().getQueryDefinition().addDefaultFilter(new Compare.Equal("user", user));
        childGrid.refresh();
    }

    @Override
    public void editorStateChanged(final ValidatingEditor source) {
        if (isDirty()) {
            if (isValid()) {
                saveButton.setEnabled(true);
            } else {
                saveButton.setEnabled(false);
            }
            discardButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
            discardButton.setEnabled(false);
        }
        childListButtonLayout.setEnabled(!editor.isNewItem());
    }

    @Override
    public void enter() {
        if (user.getUserId() != null) {
            entityManager.refresh(user);
        }
        childContainer.getQueryView().getQueryDefinition().removeDefaultFilters();
        childContainer.getQueryView().getQueryDefinition().addDefaultFilter(new Compare.Equal("user", user));
        childGrid.refresh();
    }

}
