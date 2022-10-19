package edu.tomm.webreporter.repository;

import edu.tomm.webreporter.domain.Report;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Report entity.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    @Query("select report from Report report where report.user.login = ?#{principal.username}")
    List<Report> findByUserIsCurrentUser();

    default Optional<Report> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Report> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Report> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct report from Report report left join fetch report.user",
        countQuery = "select count(distinct report) from Report report"
    )
    Page<Report> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct report from Report report left join fetch report.user")
    List<Report> findAllWithToOneRelationships();

    @Query("select report from Report report left join fetch report.user where report.id =:id")
    Optional<Report> findOneWithToOneRelationships(@Param("id") Long id);
}
