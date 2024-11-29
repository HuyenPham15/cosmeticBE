package com.cosmetics.restcontroller;

import com.cosmetics.dto.TransactionDTO;
import com.cosmetics.model.Transactions;
import com.cosmetics.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

//    @PostMapping("/create")
//    public Transactions createTransaction(@RequestBody TransactionDTO transactionDTO) {
//        return transactionService.createTransaction(transactionDTO);
//    }
}
