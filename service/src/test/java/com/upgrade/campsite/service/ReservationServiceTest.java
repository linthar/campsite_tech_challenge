package com.upgrade.campsite.service;

import com.upgrade.campsite.exception.ServiceException;
import com.upgrade.campsite.model.Reservation;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.utils.DatesValidator;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@MicronautTest
class ReservationServiceTest {

    @Inject
    private ReservationService service;

    @Inject
    private OccupiedDateService occupiedDateServiceMock;

    @MockBean(OccupiedDateService.class)
    OccupiedDateService occupiedDateServiceMock() {
        return mock(OccupiedDateService.class);
    }

    @Inject
    private ReservationRepository repositoryMock;

    @MockBean(ReservationRepository.class)
    ReservationRepository repositoryMock() {
        return mock(ReservationRepository.class);
    }

    @Inject
    private DatesValidator datesValidatorMock;

    @MockBean(DatesValidator.class)
    DatesValidator datesValidatorMock() {
        return mock(DatesValidator.class);
    }

    @Inject
    private AvailabilityService availabilityServiceMock;

    @MockBean(AvailabilityService.class)
    AvailabilityService availabilityServiceMock() {
        return mock(AvailabilityService.class);
    }


    final LocalDate TODAY = LocalDate.now();
    final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    // tests uses "valid" dates, dates validity are tested in DatesValidatorTest class


    @Test
    void create() {
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = arrivalDate.plusDays(1);

        // asserts that dates where available
        when(availabilityServiceMock.isAvailableBetweenDates(arrivalDate, departureDate)).thenReturn(true);

        service.create(email, fullname, arrivalDate, departureDate);

        // repository save call
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(repositoryMock, times(1)).save(reservationCaptor.capture());

        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(email, savedReservation.getEmail());
        assertEquals(fullname, savedReservation.getFullname());
        assertEquals(arrivalDate, savedReservation.getArrivalDate());
        assertEquals(departureDate, savedReservation.getDepartureDate());

        List<LocalDate> expectedDates = Arrays.asList(arrivalDate, departureDate);
        verify(occupiedDateServiceMock, times(1)).saveAll(savedReservation.getId(), expectedDates);

        // asserts that dates where checked by datesValidatorMock
        verify(datesValidatorMock, times(1)).validateReservationDates(arrivalDate, departureDate);


    }

    @Test
    void createWhenThereAreNoVacantDates() {
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = arrivalDate.plusDays(1);

        // Some dates are taken in the period [arrivalDate, departureDate]
        when(availabilityServiceMock.isAvailableBetweenDates(arrivalDate, departureDate)).thenReturn(false);

        try {
            service.create(email, fullname, arrivalDate, departureDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("provided dates period is no free, check please check availability for details", e.getMessage());
        }
    }


    @Test
    void findByID() {
        UUID id = UUID.randomUUID();
        Reservation entityMock = mock(Reservation.class);
        when(repositoryMock.findById(id)).thenReturn(Optional.of(entityMock));
        Optional<Reservation> oResult = service.findByID(id);

        assertTrue(oResult.isPresent());
        assertEquals(entityMock, oResult.get());
    }

    @Test
    void delete() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(occupiedDateServiceMock, times(1)).deleteAllForReservationID(id);
        verify(repositoryMock, times(1)).deleteById(id);
    }

    @Test
    void updateWhenIdNotFound() {
        UUID id = UUID.randomUUID();
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = arrivalDate.plusDays(1);

        // Id doesn't exists in DB
        when(repositoryMock.findById(id)).thenReturn(Optional.empty());

        Optional<Reservation> oResult = service.update(id, email, fullname, arrivalDate, departureDate);

        //Optional.empty() is translated to 404 in RestController
        assertTrue(oResult.isEmpty());
    }

    @Test
    void updateWhenDatesNotChanged() {
        UUID id = UUID.randomUUID();
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = arrivalDate.plusDays(1);

        Reservation found = new Reservation(id, email + "_other", fullname + "_other", arrivalDate, departureDate);
        // Id found in DB (only email & fullname are different that the received in update method)
        when(repositoryMock.findById(id)).thenReturn(Optional.of(found));

        // Only email & fullname are different (dates does not change)... nothing to be done in OccupiedDates
        Optional<Reservation> oResult = service.update(id, email, fullname, arrivalDate, departureDate);

        //updated  entity must be present
        assertTrue(oResult.isPresent());
        Reservation updatedReservation = oResult.get();

        // check that updated data is Ok
        assertEquals(id, updatedReservation.getId());
        assertEquals(email, updatedReservation.getEmail());
        assertEquals(fullname, updatedReservation.getFullname());
        assertEquals(arrivalDate, updatedReservation.getArrivalDate());
        assertEquals(departureDate, updatedReservation.getDepartureDate());

        verify(repositoryMock, times(1)).update(updatedReservation);
    }

    @Test
    void updateWhenBothDatesHaveChanged() {
        LocalDate oldArrivalDate = TODAY.plusDays(1);
        LocalDate olDepartureDate = TODAY.plusDays(1);
        LocalDate newArrivalDate = TODAY.plusDays(5);
        LocalDate newDepartureDate = TODAY.plusDays(6);

        inner_updateDatesHaveChanged(oldArrivalDate, olDepartureDate, newArrivalDate, newDepartureDate);
    }

    @Test
    void updateWhenArrivalDateHaveChanged() {
        LocalDate oldArrivalDate = TODAY.plusDays(5);
        LocalDate olDepartureDate = TODAY.plusDays(5);
        LocalDate newArrivalDate = TODAY.plusDays(4);
        LocalDate newDepartureDate = olDepartureDate;

        inner_updateDatesHaveChanged(oldArrivalDate, olDepartureDate, newArrivalDate, newDepartureDate);
    }

    @Test
    void updateWhenDepartureDateHaveChanged() {
        LocalDate oldArrivalDate = TODAY.plusDays(5);
        LocalDate olDepartureDate = TODAY.plusDays(5);
        LocalDate newArrivalDate = oldArrivalDate;
        LocalDate newDepartureDate = TODAY.plusDays(6);

        inner_updateDatesHaveChanged(oldArrivalDate, olDepartureDate, newArrivalDate, newDepartureDate);
    }


    void inner_updateDatesHaveChanged(LocalDate oldArrivalDate, LocalDate olDepartureDate, LocalDate newArrivalDate, LocalDate newDepartureDate) {
        UUID id = UUID.randomUUID();
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        String fullname = "fullname_" + RANDOM.nextInt(1000);

        Reservation found = new Reservation(id, email + "other", fullname + "other", oldArrivalDate, olDepartureDate);
        // Id found in DB (all fields are different that received in update method)
        when(repositoryMock.findById(id)).thenReturn(Optional.of(found));

        // asserts that dates where NO Occupied Dates in the requested period (ALL DATES ARE FREE)
        when(availabilityServiceMock.isAvailableForUpdateBetweenDates(id, newArrivalDate, newDepartureDate)).thenReturn(true);

        // dates have changed .. must change OccupiedDates also
        // and there new dates period is vacant
        Optional<Reservation> oResult = service.update(id, email, fullname, newArrivalDate, newDepartureDate);

        //updated  entity must be present
        assertTrue(oResult.isPresent());
        Reservation updatedReservation = oResult.get();

        // check that updated data is Ok
        assertEquals(id, updatedReservation.getId());
        assertEquals(email, updatedReservation.getEmail());
        assertEquals(fullname, updatedReservation.getFullname());
        assertEquals(newArrivalDate, updatedReservation.getArrivalDate());
        assertEquals(newDepartureDate, updatedReservation.getDepartureDate());

        verify(repositoryMock, times(1)).update(updatedReservation);

        // dates must be verified and valid
        verify(datesValidatorMock, times(1)).validateReservationDates(newArrivalDate, newDepartureDate);

        // all occupiedDates for Id (old arrival, departure Dates)  must be deleted
        verify(occupiedDateServiceMock, times(1)).deleteAllForReservationID(updatedReservation.getId());

        // and the new ones (new arrival, departure Dates) must be added
        List<LocalDate> expectedDates = Arrays.asList(newArrivalDate, newDepartureDate);
        verify(occupiedDateServiceMock, times(1)).saveAll(updatedReservation.getId(), expectedDates);
    }


    @Test
    void updateWhenDatesHaveChangedAndThereIsNoVacanciesInDatesPeriod() {
        UUID id = UUID.randomUUID();
        String email = "Email_" + RANDOM.nextInt(1000) + "@a.com";
        String fullname = "fullname_" + RANDOM.nextInt(1000);
        LocalDate arrivalDate = TODAY.plusDays(1);
        LocalDate departureDate = arrivalDate.plusDays(1);

        Reservation found = new Reservation(id, email + "other", fullname + "other", arrivalDate.plusDays(3), departureDate.plusDays(3));
        // Id found in DB (all fields are different that received in update method)
        when(repositoryMock.findById(id)).thenReturn(Optional.of(found));


        // asserts that dates where Occupied Dates in the requested period (THERE ARE NO VACANCIIES)
        when(occupiedDateServiceMock.existAnyBetweenDates(arrivalDate, departureDate)).thenReturn(true);

        // dates have changed .. must change OccupiedDates also
        // and there new dates period HAS NO VACANCIES

        try {
            service.update(id, email, fullname, arrivalDate, departureDate);
        } catch (ServiceException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("provided dates period is no free, check please check availability for details", e.getMessage());
        }
    }


    @Test
    void createDatesBetweenListForToday() {

        List<LocalDate> dates = service.createDatesBetweenList(TODAY, TODAY);
        assertEquals(1, dates.size());
        assertTrue(dates.contains(TODAY));
    }

    @Test
    void createDatesBetweenListForPeriod() {
        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(5);

        // must generate 6 days period (fromDate and 5 more)
        List<LocalDate> dates = service.createDatesBetweenList(fromDate, toDate);
        assertEquals(6, dates.size());

        for (int i = 0; i < 6; i++) {
            LocalDate d = fromDate.plusDays(i);
            assertTrue(dates.contains(d));
        }
    }

//
//    @Test
//    void checkVacanciesForDatesOk() {
//        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
//        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(0, 2));
//        // all dates are vacant in the period
//
//        when(availabilityServiceMock.isAvailableBetweenDates(fromDate, toDate)).thenReturn(true);
//
//        service.checkVacanciesForDates(fromDate, toDate);
//    }
//
//    @Test
//    void checkVacanciesForDatesWhenNoVacancy() {
//        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
//        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(0, 2));
//        // there are some taken dates in the period
//        when(availabilityServiceMock.isAvailableBetweenDates(fromDate, toDate)).thenReturn(false);
//
//        try {
//            service.checkVacanciesForDates(fromDate, toDate);
//        } catch (ServiceException e) {
//            assertEquals("provided dates period is no free, check please check availability for details", e.getMessage());
//        }
//    }

}