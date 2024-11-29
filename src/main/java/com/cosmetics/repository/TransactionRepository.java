package com.cosmetics.repository;

import com.cosmetics.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, String> {
    Transactions findByTransactionID(String vnpTxnRef);

    Transactions findByOrderOrderID(String orderID);

    boolean existsByTransactionID(String transactionID);
}
