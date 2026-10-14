package br.com.chromeprotocol;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface CareerRepository extends JpaRepository<Career, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Career c where c.id = :id")
    Optional<Career> locked(@Param("id") UUID id);
}
