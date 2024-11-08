package cz.fungisoft.coffeecompass.repository;

import cz.fungisoft.coffeecompass.serviceimpl.comment.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;

import java.util.UUID;

/**
 * Interface to get Lists of found Comments in Pageable format. Extends Spring interface PagingAndSortingRepository.<br>
 * No implementation needed as already implemented by Spring.<br>
 * <p>
 * Used by {@link CommentService}
 * 
 * @author Michal Vaclavek
 *
 */
public interface CommentsPageableRepository extends PagingAndSortingRepository<Comment, UUID> {

    Page<Comment> findAll(Pageable pageable);
    Page<Comment> findByCoffeeSite(CoffeeSite coffeeSite, Pageable pageable);
}
