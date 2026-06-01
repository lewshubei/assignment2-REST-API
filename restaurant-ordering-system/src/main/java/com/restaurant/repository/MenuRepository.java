package com.restaurant.repository;

import com.restaurant.entity.MenuItem;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
public class MenuRepository {

	@PersistenceContext
	private EntityManager em;

	// 1. Retrieve all menu items
	public List<MenuItem> findAll() {
		TypedQuery<MenuItem> q = em.createQuery("from MenuItem", MenuItem.class);
		return q.getResultList();
	}

	// 2. Retrieve a menu item by ID
	public MenuItem findById(Long id) {
		return em.find(MenuItem.class, id);
	}

	// 3. Save or update a menu item
	@Transactional
	public MenuItem save(MenuItem item) {
		MenuItem merged = em.merge(item);
		return merged;
	}

	// 4. Delete a menu item by ID
	@Transactional
	public void deleteById(Long id) {
		int deleted = em.createQuery("delete from MenuItem m where m.id = :id")
				.setParameter("id", id)
				.executeUpdate();
		// no-op if deleted == 0
	}

	// 5. Search by keyword (name, category, description)
	public List<MenuItem> searchByKeyword(String keyword) {
		TypedQuery<MenuItem> q = em.createQuery(
				"from MenuItem m where lower(m.name) like :kw or lower(m.category) like :kw or lower(m.description) like :kw",
				MenuItem.class);
		q.setParameter("kw", "%" + keyword.toLowerCase().trim() + "%");
		return q.getResultList();
	}

	// 6. Search by keyword and category (category optional)
	public List<MenuItem> searchByKeywordAndCategory(String keyword, String category) {
		StringBuilder jpql = new StringBuilder("from MenuItem m where 1=1");
		if (keyword != null && !keyword.trim().isEmpty()) {
			jpql.append(" and (lower(m.name) like :kw or lower(m.category) like :kw or lower(m.description) like :kw)");
		}
		if (category != null && !category.trim().isEmpty()) {
			jpql.append(" and lower(m.category) = :cat");
		}

		TypedQuery<MenuItem> q = em.createQuery(jpql.toString(), MenuItem.class);
		if (keyword != null && !keyword.trim().isEmpty()) q.setParameter("kw", "%" + keyword.toLowerCase().trim() + "%");
		if (category != null && !category.trim().isEmpty()) q.setParameter("cat", category.toLowerCase().trim());
		return q.getResultList();
	}

	// 7. Find by category (case-insensitive)
	public List<MenuItem> findByCategoryIgnoreCase(String category) {
		TypedQuery<MenuItem> q = em.createQuery("from MenuItem m where lower(m.category) = :cat", MenuItem.class);
		q.setParameter("cat", category.toLowerCase());
		return q.getResultList();
	}

	// 8. Find by availability
	public List<MenuItem> findByAvailabilityIgnoreCase(String status) {
		TypedQuery<MenuItem> q = em.createQuery("from MenuItem m where lower(m.availability) = :status", MenuItem.class);
		q.setParameter("status", status.toLowerCase());
		return q.getResultList();
	}

}