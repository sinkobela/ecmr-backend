/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCreationCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupUpdateCommand;
import org.openlogisticsfoundation.ecmr.web.mappers.GroupWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.GroupCreationModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupFlatModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupParentUpdateModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.openlogisticsfoundation.ecmr.domain.models.*;
import org.openlogisticsfoundation.ecmr.domain.services.*;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EcmrShareService ecmrShareService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private GroupWebMapper groupWebMapper;

    private AuthenticatedUser authenticatedUser;
    private Group group;
    private User user;

    @BeforeEach
    public void setup() {

        user = new User(
            1L,
            "John",
            "Doe",
            CountryCode.DE,
            "john.doe@example.com",
            "123456789",
            UserRole.User,
            123L,
            false,
            false
        );

        authenticatedUser = new AuthenticatedUser(user);

        group = new Group();
        group.setId(1L);
        group.setName("Name");
        group.setDescription("Description");
        group.setChildren(List.of());
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testGetAllGroups_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser(true)).thenReturn(authenticatedUser);
        when(groupService.getAllGroups()).thenReturn(Collections.singletonList(group));

        // Act
        mockMvc.perform(get("/group")
                .param("currentUserGroupsOnly", "false"))
            .andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser(true);
        verify(groupService, times(1)).getAllGroups();
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testGetAllGroupsAsFlatList_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(groupService.getAllGroups()).thenReturn(Collections.singletonList(group));
        when(groupWebMapper.toFlatModel(any(Group.class))).thenReturn(new GroupFlatModel( 1, "GROUP", "DESCRIPTION" ));

        // Act
        mockMvc.perform(get("/group/flat-list")
                .param("currentUserGroupsOnly", "false"))
            .andExpect(status().isOk());

        // Assert
        verify(groupService, times(1)).getAllGroups();
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testGetGroup_Success() throws Exception {
        // Arrange
        when(groupService.getGroup(1L)).thenReturn(group);

        // Act
        mockMvc.perform(get("/group/{id}", 1L))
            .andExpect(status().isOk());

        // Assert
        verify(groupService, times(1)).getGroup(1L);
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testCreateGroup_Success() throws Exception {
        // Arrange
        GroupCreationModel groupCreationModel  =new GroupCreationModel("Name","Created Description", 1L);
        GroupCreationCommand groupCreationCommand = new GroupCreationCommand("Name","Created Description", 1L);
        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(groupWebMapper.toCommand(any(GroupCreationModel.class))).thenReturn(groupCreationCommand);
        when(groupService.createGroup(any(AuthenticatedUser.class), any(GroupCreationCommand.class))).thenReturn(group);

        // Act
        mockMvc.perform(post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(groupCreationModel)))
            .andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(groupWebMapper, times(1)).toCommand(any(GroupCreationModel.class));
        verify(groupService, times(1)).createGroup(any(AuthenticatedUser.class), any(GroupCreationCommand.class));
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testUpdateGroup_Success() throws Exception {
        // Arrange
        GroupUpdateModel groupUpdateModel = new GroupUpdateModel("Name","Updated Description");
        GroupUpdateCommand groupUpdateCommand = new GroupUpdateCommand("Name", "Updated Description");
        when(groupWebMapper.toCommand(any(GroupUpdateModel.class))).thenReturn(groupUpdateCommand);
        when(groupService.updateGroup(eq(1L), any(GroupUpdateCommand.class))).thenReturn(group);

        // Act
        mockMvc.perform(post("/group/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(groupUpdateModel)))
            .andExpect(status().isOk());

        // Assert
        verify(groupWebMapper, times(1)).toCommand(any(GroupUpdateModel.class));
        verify(groupService, times(1)).updateGroup(eq(1L), any(GroupUpdateCommand.class));
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testDeleteGroup_Success() throws Exception {
        // Arrange
        when(groupService.deleteGroup(1L)).thenReturn(true);

        // Act
        mockMvc.perform(delete("/group/{id}", 1L))
            .andExpect(status().isOk());

        // Assert
        verify(groupService, times(1)).deleteGroup(1L);
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testUpdateGroupParent_Success() throws Exception {
        // Arrange
        GroupParentUpdateModel groupParentUpdateModel = new GroupParentUpdateModel(1L);
        when(groupService.updateGroupParent(eq(1L), any(Long.class))).thenReturn(group);
        when(groupService.getGroup(anyLong())).thenReturn(group);

        // Act
        mockMvc.perform(post("/group/{id}/update-parent", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(groupParentUpdateModel)))
            .andExpect(status().isOk());

        // Assert
        verify(groupService, times(1)).updateGroupParent(eq(1L), any(Long.class));
    }

    @Test
    @WithMockUser(roles = "Admin")
    public void testGetUsersForGroup_Success() throws Exception {
        // Arrange
        List<User> users = Collections.singletonList(user);
        when(userService.getUsersByGroupId(1L)).thenReturn(users);

        // Act
        mockMvc.perform(get("/group/{id}/users", 1L))
            .andExpect(status().isOk());

        // Assert
        verify(userService, times(1)).getUsersByGroupId(1L);
    }
}
