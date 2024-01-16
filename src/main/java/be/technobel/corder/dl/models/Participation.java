package be.technobel.corder.dl.models;

import be.technobel.corder.dl.models.enums.ProductType;
import be.technobel.corder.dl.models.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(value = TemporalType.DATE)
    private LocalDate participationDate;

    private String firstName;

    private String lastName;

    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "address_id", name = "address_id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private Status status;
    @Temporal(value = TemporalType.TIMESTAMP)
    private LocalDateTime validatedDate;
    private String pictureName;
    private String pictureType;
    @Lob
    private byte[] blob;
    private String productType;
    private int satisfaction;
    private String satisfactionComment;
    private boolean acceptNewsletter;
    private boolean acceptExposure;
}
