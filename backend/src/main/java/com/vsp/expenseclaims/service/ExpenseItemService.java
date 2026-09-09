package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.AddExpenseItemRequest;
import com.vsp.expenseclaims.dto.ExpenseItemResponse;
import com.vsp.expenseclaims.dto.UpdateExpenseItemRequest;
import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.ExpenseItem;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.ClaimRepository;
import com.vsp.expenseclaims.repository.ExpenseItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ExpenseItemService {

    private final ExpenseItemRepository expenseItemRepository;
    private final ClaimRepository claimRepository;
    private final AuthorizationService authorizationService;
    private final DuplicateDetectionService duplicateDetectionService;

    public ExpenseItemService(ExpenseItemRepository expenseItemRepository,
                              ClaimRepository claimRepository,
                              AuthorizationService authorizationService,
                              DuplicateDetectionService duplicateDetectionService) {
        this.expenseItemRepository = expenseItemRepository;
        this.claimRepository = claimRepository;
        this.authorizationService = authorizationService;
        this.duplicateDetectionService = duplicateDetectionService;
    }

    @Transactional
    public ExpenseItemResponse addItem(Long claimId, AddExpenseItemRequest request) {
        User currentUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canEditClaim(currentUser, claim);

        ExpenseItem item = new ExpenseItem(
                claim,
                request.getMerchantName(),
                request.getExpenseDate(),
                request.getCategory(),
                request.getAmount(),
                request.getReceiptText(),
                request.getReceiptText() != null && !request.getReceiptText().isEmpty()
        );

        item = expenseItemRepository.save(item);
        claim.getExpenseItems().add(item);
        recalculateTotal(claim);

        // Evaluate duplicate detection
        var dupEval = duplicateDetectionService.evaluateAndFlag(item);

        ExpenseItemResponse response = new ExpenseItemResponse(item);
        response.setDuplicateWarning(dupEval.isDuplicate());
        response.setSimilarityScore(dupEval.similarityScore());
        return response;
    }

    @Transactional
    public ExpenseItemResponse updateItem(Long claimId, Long itemId, UpdateExpenseItemRequest request) {
        User currentUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canEditClaim(currentUser, claim);

        ExpenseItem item = expenseItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Expense item not found with ID: " + itemId));

        if (!item.getClaim().getId().equals(claimId)) {
            throw new BusinessException("Expense item does not belong to claim ID: " + claimId);
        }

        item.setMerchantName(request.getMerchantName());
        item.setAmount(request.getAmount());
        item.setExpenseDate(request.getExpenseDate());
        item.setCategory(request.getCategory());
        if (request.getReceiptText() != null) {
            item.setReceiptText(request.getReceiptText());
        }

        item = expenseItemRepository.save(item);
        recalculateTotal(claim);

        var dupEval = duplicateDetectionService.evaluateAndFlag(item);

        ExpenseItemResponse response = new ExpenseItemResponse(item);
        response.setDuplicateWarning(dupEval.isDuplicate());
        response.setSimilarityScore(dupEval.similarityScore());
        return response;
    }

    @Transactional
    public void deleteItem(Long claimId, Long itemId) {
        User currentUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canEditClaim(currentUser, claim);

        ExpenseItem item = expenseItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Expense item not found with ID: " + itemId));

        if (!item.getClaim().getId().equals(claimId)) {
            throw new BusinessException("Expense item does not belong to claim ID: " + claimId);
        }

        claim.getExpenseItems().remove(item);
        expenseItemRepository.delete(item);
        recalculateTotal(claim);
    }

    private void recalculateTotal(Claim claim) {
        BigDecimal total = claim.getExpenseItems().stream()
                .map(ExpenseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        claim.setTotalAmount(total);
        claimRepository.save(claim);
    }
}
