package com.techsolution.stockquery.infrastructure.persistence;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.domain.repository.StockQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaStockQueryRepository implements StockQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StockView> findAll() {
        String jpql = "SELECT new com.techsolution.stockquery.domain.model.StockView(" +
                "s.productId, s.productName, s.quantityAvailable, s.lastUpdated) " +
                "FROM StockViewEntity s ORDER BY s.productName";
        
        TypedQuery<StockView> query = entityManager.createQuery(jpql, StockView.class);
        return query.getResultList();
    }

    @Override
    public Optional<StockView> findByProductId(UUID productId) {
        String jpql = "SELECT new com.techsolution.stockquery.domain.model.StockView(" +
                "s.productId, s.productName, s.quantityAvailable, s.lastUpdated) " +
                "FROM StockViewEntity s WHERE s.productId = :productId";
        
        TypedQuery<StockView> query = entityManager.createQuery(jpql, StockView.class);
        query.setParameter("productId", productId);
        
        List<StockView> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}

