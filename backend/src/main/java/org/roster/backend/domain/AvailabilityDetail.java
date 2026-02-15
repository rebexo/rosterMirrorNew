package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.roster.backend.domain.enums.AvailabilityStatus;
import java.time.LocalDate;

/**
 * Represents the detailed availability information of an employee for a specific date.
 * This class is linked to an {@link AvailabilityEntry} and contains the availability
 * status for a given day as well as the associated metadata defining the context of this detail.
 */
@Data
@Entity
@Table(name = "availability_details")
public class AvailabilityDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;


    /**
     * Represents the availability status of an employee for a specific date.
     * This status is defined using the {@link AvailabilityStatus} enumeration and indicates
     * the employee's availability such as UNAVAILABLE. Additional statuses may be added in
     * the future to represent other availability types (e.g., PREFERRED).
     *
     * This field is mandatory and its value is stored in the database as a string representation
     * of the enumeration.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status;



    /**
     * Represents the parent {@link AvailabilityEntry} to which this detail belongs.
     * This field establishes a mandatory many-to-one relationship, ensuring that
     * each {@link AvailabilityDetail} is associated with exactly one {@link AvailabilityEntry}.
     *
     * The foreign key column in the database is named "entry_id", and null values
     * are not permitted for this association. This field links detailed availability
     * records to their overarching availability entry metadata.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private AvailabilityEntry entry;
}