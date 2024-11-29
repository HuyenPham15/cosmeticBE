package com.cosmetics.service;

import com.cosmetics.dto.TransactionDTO;
import com.cosmetics.model.Order;
import com.cosmetics.model.Transactions;
import com.cosmetics.repository.OrderRepository;
import com.cosmetics.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OrderRepository orderRepository;

}
