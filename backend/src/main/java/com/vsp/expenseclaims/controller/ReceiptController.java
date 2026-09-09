package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.dto.ReceiptParseRequest;
import com.vsp.expenseclaims.dto.ReceiptParseResponse;
import com.vsp.expenseclaims.service.ReceiptParsingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts")
@Tag(name = "Receipts", description = "Smart receipt text parsing and extraction")
public class ReceiptController {

    private final ReceiptParsingService receiptParsingService;

    public ReceiptController(ReceiptParsingService receiptParsingService) {
        this.receiptParsingService = receiptParsingService;
    }

    @PostMapping("/parse")
    @Operation(summary = "Parse messy receipt text into structured expense data")
    public ResponseEntity<ReceiptParseResponse> parseReceipt(@Valid @RequestBody ReceiptParseRequest request) {
        ReceiptParseResponse response = receiptParsingService.parse(request.getReceiptText());
        return ResponseEntity.ok(response);
    }
}
