package org.example.expensesservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expensesservice.client.AuthServiceClient;
import org.example.expensesservice.controller.ExpenseController;
import org.example.expensesservice.db.Expense;
import org.example.expensesservice.dto.ExpenseDto;
import org.example.expensesservice.other.ExpenseUser;
import org.example.expensesservice.other.Group;
import org.example.expensesservice.request.CreateExpenseRequest;
import org.example.expensesservice.request.UpdateExpenseRequest;
import org.example.expensesservice.service.ExpenseService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @InjectMocks
    private ExpenseController expenseController;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private ModelMapper modelMapper;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .findAndRegisterModules();

    private String userId;
    private Jwt jwt;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();
        userId = "user123";
        jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(userId);

        auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);


    }

    @Test
    void getExpenses_shouldReturnListOfExpenses() throws Exception {
        Long groupId = 1L;

        Expense expense1 = new Expense();
        Expense expense2 = new Expense();
        ExpenseUser expenseUser1 = new ExpenseUser();
        ExpenseUser expenseUser2 = new ExpenseUser();
        expenseUser1.setId(1L);
        expenseUser2.setId(2L);
        expenseUser1.setAmount(BigDecimal.valueOf(100));
        expenseUser2.setAmount(BigDecimal.valueOf(200));
        expense1.setExpenseUsers(List.of(expenseUser1));
        expense2.setExpenseUsers(List.of(expenseUser2));

        ExpenseDto expenseDto1 = new ExpenseDto();
        expenseDto1.setCurrentUserPaid(BigDecimal.valueOf(100));
        ExpenseDto expenseDto2 = new ExpenseDto();
        expenseDto2.setCurrentUserPaid(BigDecimal.valueOf(200));

        when(expenseService.getExpenses(userId, groupId)).thenReturn(List.of(expense1, expense2));
        when(modelMapper.map(eq(expense1), eq(ExpenseDto.class))).thenReturn(expenseDto1);
        when(modelMapper.map(eq(expense2), eq(ExpenseDto.class))).thenReturn(expenseDto2);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/groups/{groupId}/expenses", groupId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));

        verify(expenseService).getExpenses(userId, groupId);
        verifyNoMoreInteractions(expenseService);
    }

    @Test
    void createExpense_shouldReturnCreatedExpense() throws Exception {
        Long groupId = 1L;
        CreateExpenseRequest createRequest = new CreateExpenseRequest();
        createRequest.setAmount(BigDecimal.valueOf(500));
        createRequest.setName("Lunch");
        createRequest.setDate(LocalDate.now());
        createRequest.setDescription("Team lunch");
        createRequest.setCurrency("USD");

        Expense createdExpense = new Expense();
        createdExpense.setExpenseUsers(List.of(new ExpenseUser()));

        ExpenseDto expenseDto = new ExpenseDto();

        Group group = new Group();
        group.setId(groupId);

        when(authServiceClient.getGroupById(anyString(), eq(groupId))).thenReturn(group);
        when(expenseService.createExpense(any(CreateExpenseRequest.class), eq(userId), eq(group))).thenReturn(createdExpense);
        when(modelMapper.map(eq(createdExpense), eq(ExpenseDto.class))).thenReturn(expenseDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/groups/{groupId}/expenses", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .principal(auth)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        verify(expenseService).createExpense(any(CreateExpenseRequest.class), eq(userId), eq(group));
        verifyNoMoreInteractions(expenseService);
    }


    @Test
    void deleteExpense_shouldReturnSuccessMessage() throws Exception {
        Long groupId = 1L;
        Long expenseId = 1L;

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/groups/{groupId}/expenses/{expenseId}", groupId, expenseId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success!"));

        verify(expenseService).deleteExpense(expenseId, userId, groupId);
        verifyNoMoreInteractions(expenseService);
    }

    @Test
    void updateExpense_shouldReturnUpdatedExpense() throws Exception {
        Long groupId = 1L;
        Long expenseId = 1L;

        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest();
        updateRequest.setName("Updated Expense");
        updateRequest.setDate(LocalDate.now());
        updateRequest.setCurrency("USD");
        updateRequest.setAmount(BigDecimal.valueOf(300));
        updateRequest.setDescription("Updated description");


        Expense updatedExpense = new Expense();
        ExpenseDto updatedExpenseDto = new ExpenseDto();

        when(expenseService.updateExpense(expenseId, updateRequest, userId, groupId)).thenReturn(updatedExpense);
        when(modelMapper.map(updatedExpense, ExpenseDto.class)).thenReturn(updatedExpenseDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/groups/{groupId}/expenses/{expenseId}", groupId, expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isCreated());

        verify(expenseService).updateExpense(expenseId, updateRequest, userId, groupId);
        verifyNoMoreInteractions(expenseService);
    }
}
