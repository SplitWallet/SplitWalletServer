package org.example.expensesuserservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expensesuserservice.controller.ExpenseUserController;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.dto.ExpenseUserDto;
import org.example.expensesuserservice.other.Group;
import org.example.expensesuserservice.request.UpdateExpenseParticipantRequest;
import org.example.expensesuserservice.request.UpdatePaidAmountRequest;
import org.example.expensesuserservice.service.ExpenseUserService;
import org.example.expensesuserservice.client.AuthServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExpenseUserControllerTest {

    @InjectMocks
    private ExpenseUserController expenseUserController;

    @Mock
    private ExpenseUserService expenseUserService;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private ModelMapper modelMapper;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userId;
    private Jwt jwt;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(expenseUserController).build();
        userId = "user123";
        jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(userId);

        auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
    }

    @Test
    void getExpenses_shouldReturnListOfExpenseUsers() throws Exception {
        ExpenseUser expenseUser = new ExpenseUser();
        ExpenseUserDto dto = new ExpenseUserDto();
        dto.setUserId("user456");
        dto.setAmount(BigDecimal.valueOf(100.0));

        when(expenseUserService.getExpenseUsers(1L, userId)).thenReturn(List.of(expenseUser));
        when(modelMapper.map(expenseUser, ExpenseUserDto.class)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/groups/{groupId}/expenses/{expenseId}", 1L, 1L)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("user456"))
                .andExpect(jsonPath("$[0].amount").value(100.0));

        verify(expenseUserService).getExpenseUsers(1L, userId);
        verifyNoMoreInteractions(expenseUserService);
    }

    @Test
    void updateExpense_shouldReturnUpdatedExpenseUsers() throws Exception {
        UpdateExpenseParticipantRequest updateRequest = new UpdateExpenseParticipantRequest();
        ExpenseUser expenseUser = new ExpenseUser();
        ExpenseUserDto dto = new ExpenseUserDto();
        dto.setUserId("user789");
        dto.setAmount(BigDecimal.valueOf(200.0));

        when(authServiceClient.getGroupById(anyString(), eq(1L))).thenReturn(new Group());
        when(expenseUserService.updateExpenseUser(eq(1L), anyList(), eq(userId), any())).thenReturn(List.of(expenseUser));
        when(modelMapper.map(expenseUser, ExpenseUserDto.class)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/groups/{groupId}/expenses/{expenseId}/users", 1L, 1L)
                        .principal(auth)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(updateRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("user789"))
                .andExpect(jsonPath("$[0].amount").value(200.0));

        verify(expenseUserService).updateExpenseUser(eq(1L), anyList(), eq(userId), any());
        verifyNoMoreInteractions(expenseUserService);
    }

    @Test
    void updatePaidAmount_shouldReturnUpdatedExpenseUser() throws Exception {
        UpdatePaidAmountRequest paidRequest = new UpdatePaidAmountRequest();
        paidRequest.setPaid(BigDecimal.valueOf(150.0));
        ExpenseUser expenseUser = new ExpenseUser();
        ExpenseUserDto dto = new ExpenseUserDto();
        dto.setAmount(BigDecimal.valueOf(150.0));
        dto.setPaid(BigDecimal.valueOf(150.0));

        when(expenseUserService.updatePaidAmount(1L, "user456", paidRequest, userId)).thenReturn(expenseUser);
        when(modelMapper.map(expenseUser, ExpenseUserDto.class)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/groups/{groupId}/expenses/{expenseId}/users/{userId}/paid", 1L, 1L, "user456")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paidRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paid").value(150.0));

        verify(expenseUserService).updatePaidAmount(1L, "user456", paidRequest, userId);
        verifyNoMoreInteractions(expenseUserService);
    }

    @Test
    void removeParticipant_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/groups/{groupId}/expenses/{expenseId}/users/{userId}", 1L, 1L, "user456")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success!!!"));

        verify(expenseUserService).removeExpense(1L, "user456", userId);
        verifyNoMoreInteractions(expenseUserService);
    }
}