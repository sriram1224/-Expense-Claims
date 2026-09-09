package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.dto.AddExpenseItemRequest;
import com.vsp.expenseclaims.dto.ExpenseItemResponse;
import com.vsp.expenseclaims.dto.UpdateExpenseItemRequest;
import com.vsp.expenseclaims.service.ExpenseItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/claims/{claimId}/items")
@Tag(name = "Expense Items", description = "Line items within a claim")
public class ExpenseItemController {

    private final ExpenseItemService expenseItemService;

    public ExpenseItemController(ExpenseItemService expenseItemService) {
        this.expenseItemService = expenseItemService;
    }

    @PostMapping
    @Operation(summary = "Add an expense item to a draft or rejected claim")
    public ResponseEntity<ExpenseItemResponse> addItem(@PathVariable Long claimId, @Valid @RequestBody AddExpenseItemRequest request) {
        ExpenseItemResponse response = expenseItemService.addItem(claimId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update an expense item in a draft or rejected claim")
    public ResponseEntity<ExpenseItemResponse> updateItem(@PathVariable Long claimId, @PathVariable Long itemId, @Valid @RequestBody UpdateExpenseItemRequest request) {
        return ResponseEntity.ok(expenseItemService.updateItem(claimId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete an expense item from a draft claim")
    public ResponseEntity<Void> deleteItem(@PathVariable Long claimId, @PathVariable Long itemId) {
        expenseItemService.deleteItem(claimId, itemId);
        return ResponseEntity.noContent().build();
    }
}
