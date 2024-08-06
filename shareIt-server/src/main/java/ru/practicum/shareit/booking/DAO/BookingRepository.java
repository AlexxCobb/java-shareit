package ru.practicum.shareit.booking.DAO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllBookingsByBookerIdOrderByStartDesc(Long id, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long id, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long id, LocalDateTime time, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long id, LocalDateTime time, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long id, Status status, Pageable pageable);

    List<Booking> findAllBookingsByItemOwnerIdOrderByStartDesc(Long id, Pageable pageable);

    List<Booking> findAllBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long id, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findAllBookingsByItemOwnerIdAndEndBeforeOrderByStartDesc(Long id, LocalDateTime time, Pageable pageable);

    List<Booking> findAllBookingsByItemOwnerIdAndStartAfterOrderByStartDesc(Long id, LocalDateTime time, Pageable pageable);

    List<Booking> findAllBookingsByItemOwnerIdAndStatusOrderByStartDesc(Long id, Status status, Pageable pageable);

    @Query("select b from Booking b where b.item.id = :itemId and ( b.end < :time or b.start < :time) and b.status <> :status order by b.end DESC")
    List<Booking> findLastBookingByItemId(Long itemId, LocalDateTime time, Status status, Pageable pageable);

    @Query("select b from Booking b where b.item.id = :itemId and b.start > :time and b.status <> :status order by b.start ASC")
    List<Booking> findFutureBookingByItemId(Long itemId, LocalDateTime time, Status status, Pageable pageable);

    List<Booking> findAllBookingsByItemIdInAndStatusNotOrderByStartDesc(List<Long> ids, Status status);
}
