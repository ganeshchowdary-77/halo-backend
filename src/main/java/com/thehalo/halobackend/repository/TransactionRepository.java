package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.payment.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPolicyIdOrderByTransactionDateDesc(Long policyId);

    List<Transaction> findByPolicyUserIdOrderByTransactionDateDesc(Long userId);
}
