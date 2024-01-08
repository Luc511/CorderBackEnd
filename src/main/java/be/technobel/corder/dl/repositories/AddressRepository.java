package be.technobel.corder.dl.repositories;

import be.technobel.corder.dl.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
