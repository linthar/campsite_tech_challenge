package com.upgrade.campsite.service;

import com.upgrade.campsite.model.OccupiedDate;
import com.upgrade.campsite.repository.OccupiedDateRepository;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MicronautTest
class OccupiedDateServiceTest {

    @Inject
    private OccupiedDateService service;

    @Inject
    OccupiedDateRepository occupiedDateRepositoryMock;

    @MockBean(OccupiedDateRepository.class)
    OccupiedDateRepository occupiedDateRepositoryMock() {
        return mock(OccupiedDateRepository.class);
    }

    final LocalDate TODAY = LocalDate.now();
    ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    @Test
    void findAllBetweenDates() {
        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 10));
        List<OccupiedDate> answerMock = new ArrayList<OccupiedDate>();

        when(occupiedDateRepositoryMock.findAllBetweenDates(fromDate, toDate)).thenReturn(answerMock);

        List<OccupiedDate> result = service.findAllBetweenDates(fromDate, toDate);
        // same answer expected
        assertEquals(answerMock, result);
    }

    @Test
    void isOccupied() {
        LocalDate date = TODAY.plusDays(RANDOM.nextInt(1, 10));
        when(occupiedDateRepositoryMock.existsById(date)).thenReturn(true);
        assertTrue(service.isOccupied(date));
    }

    @Test
    void existAnyBetweenDates() {
        LocalDate fromDate = TODAY.plusDays(RANDOM.nextInt(1, 10));
        LocalDate toDate = fromDate.plusDays(RANDOM.nextInt(1, 10));
        List<OccupiedDate> answerMock = new ArrayList<OccupiedDate>();

        when(occupiedDateRepositoryMock.existAnyBetweenDates(fromDate, toDate)).thenReturn(false);

        assertFalse(service.existAnyBetweenDates(fromDate, toDate));
    }


    @Test
    void saveAllEmptyList() {
        List<LocalDate> allDates = new ArrayList<>();

        UUID reservationID = UUID.randomUUID();
        service.saveAll(reservationID, allDates);

        // asserts that repository was no be called to save the data
        // empty list does not save nothing into DB
        verify(occupiedDateRepositoryMock, times(0)).save(any());
    }


    @Test
    void saveAllOneDate() {
        LocalDate date1 = TODAY.plusDays(RANDOM.nextInt(1, 10));
        List<LocalDate> allDates = Arrays.asList(date1);

        UUID reservationID = UUID.randomUUID();
        service.saveAll(reservationID, allDates);

        // asserts that date1 was sent to repository as OccupiedDate
        ArgumentCaptor<OccupiedDate> argument = ArgumentCaptor.forClass(OccupiedDate.class);
        verify(occupiedDateRepositoryMock, times(1)).save(argument.capture());
        assertEquals(date1, argument.getValue().getDate());
    }


    @Test
    void saveAllSeveralDates() {
        // store more than 1 date
        List<LocalDate> allDates = new ArrayList<>();
        for (int i = 0; i < RANDOM.nextInt(1, 5); i++) {
            LocalDate date = TODAY.plusDays(RANDOM.nextInt(1, 10));
            allDates.add(date);
        }

        UUID reservationID = UUID.randomUUID();
        service.saveAll(reservationID, allDates);

        // asserts that date1 was sent to repository as OccupiedDate
        ArgumentCaptor<OccupiedDate> arguments = ArgumentCaptor.forClass(OccupiedDate.class);

        // check that all dates where sent to the repository to be stored in DB
        verify(occupiedDateRepositoryMock, times(allDates.size())).save(arguments.capture());
        for (int i = 0; i < allDates.size(); i++) {
            assertEquals(allDates.get(i), arguments.getAllValues().get(i).getDate());
        }

    }


    @Test
    void deleteAllForReservationID() {
        UUID reservationID = UUID.randomUUID();
        service.deleteAllForReservationID(reservationID);
        verify(occupiedDateRepositoryMock, times(1)).deleteByReservationID(reservationID);
    }


}