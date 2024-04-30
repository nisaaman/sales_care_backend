package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.PackSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author marzia
 * Created on 5/4/22 03:00 PM
 */

@Repository
public interface PackSizeRepository extends JpaRepository<PackSize, Long> {
    List<PackSize> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<PackSize> findByIdAndIsDeletedFalse(Long id);

    List<PackSize> findAllPackSizeByUomIdAndIsDeletedFalse(Long storeId);
@Query(value = "SELECT  * \n" +
        "  FROM pack_size ps \n" +
        "  where ps.organization_id = :organization \n" +
        "  and ps.pack_size = :packSize\n" +
        "  and CAST(ps.height AS DECIMAL) = CAST(:height AS DECIMAL)\n" +
        "  and CAST(ps.width AS DECIMAL) = CAST(:width AS DECIMAL)\n" +
        "  and CAST(ps.length AS DECIMAL) = CAST(:length AS DECIMAL)\n" +
        "  and ps.uom_id = :uom_id\n"+
        "  and ps.is_active is true \n" +
        "  and ps.is_deleted is false",nativeQuery = true)
    Optional<PackSize> findByOrganizationAndPackSizeAndHeightAndWidthAndLengthAndIsDeletedFalse(@Param("organization") Long organization,
                                                                                                @Param("packSize") Integer packSize,
                                                                                                @Param("height") Float height,
                                                                                                @Param("width") Float width,
                                                                                                @Param("length") Float length,
                                                                                                @Param("uom_id") Long uom_id);
    @Query(value = "SELECT  * \n" +
            "  FROM pack_size ps \n" +
            "  where ps.organization_id = :organization \n" +
            "  and ps.id != :id\n" +
            "  and ps.pack_size = :packSize\n" +
            "  and CAST(ps.height AS DECIMAL) = CAST(:height AS DECIMAL)\n" +
            "  and CAST(ps.width AS DECIMAL) = CAST(:width AS DECIMAL)\n" +
            "  and CAST(ps.length AS DECIMAL) = CAST(:length AS DECIMAL)\n" +
            "  and ps.uom_id = :uom_id\n"+
            "  and ps.is_active is true \n" +
            "  and ps.is_deleted is false",nativeQuery = true)
    Optional<PackSize> findByOrganizationAndIdIsNotAndPackSizeAndHeightAndWidthAndLengthAndIsDeletedFalse(@Param("organization") Long organization,
                                                                                                          @Param("id") Long id,
                                                                                                          @Param("packSize") Integer packSize,
                                                                                                          @Param("height") Float height,
                                                                                                          @Param("width") Float width,
                                                                                                          @Param("length") Float length,
                                                                                                          @Param("uom_id") Long uom_id);
}
