package com.example.JavaFurim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.Category;

@Repository

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByName(String name);
}