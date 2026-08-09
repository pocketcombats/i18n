package com.example.app;

import com.pocketcombats.i18n.LocalizedString;
import jakarta.persistence.EntityManagerFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/**
 * Each call runs in its own transaction and clears the persistence context, so a read really goes
 * back to the database rather than returning the instance that was just persisted.
 */
final class TransactionTemplateHelper {

    private final EntityManagerFactory entityManagerFactory;
    private final TransactionTemplate transactionTemplate;

    TransactionTemplateHelper(ApplicationContext context) {
        this.entityManagerFactory = context.getBean(EntityManagerFactory.class);
        this.transactionTemplate = new TransactionTemplate(
                new JpaTransactionManager(entityManagerFactory));
    }

    Long persist(@Nullable LocalizedString title) {
        Long id = transactionTemplate.execute(status -> {
            try (var entityManager = entityManagerFactory.createEntityManager()) {
                entityManager.joinTransaction();
                Item item = new Item();
                item.setTitle(title);
                entityManager.persist(item);
                entityManager.flush();
                return item.getId();
            }
        });
        return Objects.requireNonNull(id);
    }

    @Nullable LocalizedString findTitle(Long id) {
        return transactionTemplate.execute(status -> {
            try (var entityManager = entityManagerFactory.createEntityManager()) {
                entityManager.joinTransaction();
                return entityManager.find(Item.class, id).getTitle();
            }
        });
    }
}
