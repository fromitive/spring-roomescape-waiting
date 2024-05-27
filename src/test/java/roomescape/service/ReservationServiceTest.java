package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.ReservationFailException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationServiceTest {
    private final ReservationService service;
    private final ReservationRepository repository;

    @Autowired
    public ReservationServiceTest(final ReservationService service, final ReservationRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    private long getReservationSize() {
        return service.findEntireReservations().getData().size();
    }

    @Test
    @DisplayName("예약 목록을 반환한다.")
    void given_when_findEntireReservations_then_returnReservationResponses() {
        //when, then
        assertThat(service.findEntireReservations().getData().size()).isEqualTo(10);
    }

    @Test
    @DisplayName("예약이 성공하면 결과값과 함께 Db에 저장된다.")
    void given_reservationRequestWithInitialSize_when_register_then_returnReservationResponseAndSaveDb() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2999-01-01"), 1L, 1L, 1L);
        //when
        final ReservationResponse reservationResponse = service.register(reservationRequest);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(reservationResponse.id()).isEqualTo(afterCreateSize);
        assertThat(afterCreateSize).isEqualTo(initialSize + 1);
    }

    @Test
    @DisplayName("존재하는 예약을 삭제하면 Db에도 삭제된다.")
    void given_initialSize_when_delete_then_deletedItemInDb() {
        //given
        long initialSize = getReservationSize();
        //when
        service.delete(7L);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }

    @Test
    @DisplayName("이전 날짜로 예약 할 경우 예외가 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithPastDate_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("1999-01-01"), 1L, 1L, 1L);
        //when, then
        assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(ReservationFailException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("themeId가 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithNotExistThemeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 99L,
                1L);
        //when, then
        assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(
                InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("timeId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithNotExistTimeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 99L, 1L,
                1L);
        //when, then
        assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(
                InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("memberId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithNotExistMemberId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 1L,
                99L);
        //when, then
        assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(
                InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록을 반환한다.")
    void given_member_when_findMemberReservations_then_returnReservationMineResponses() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when, then
        assertThat(service.findMemberReservations(member).getData()).hasSize(8);
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록 중 대기중인 예약이 있을경우 대기번호를 포함한 문자를 반환한다.")
    void given_member_when_findMemberReservationsWithWaitingAndGetMessage_then_containsWaitingNumber() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when
        final ReservationsMineResponse waitingMineResponse = service.findMemberReservations(member).getData().get(7);
        final String message = waitingMineResponse.status();
        //then
        assertThat(message).isEqualTo("2번째 예약대기");
    }

    @Test
    @DisplayName("사용자 Id 테마 Id 시작 및 종료 날짜로 예약 목록을 반환한다.")
    void given_memberIdAndThemeIdAndDateFromAndDateTo_when_findReservations_then_returnReservationResponses() {
        //given
        Long themeId = 2L;
        Long memberId = 1L;
        LocalDate dateFrom = LocalDate.parse("2024-04-30");
        LocalDate dateTo = LocalDate.parse("2024-05-01");
        //when
        final ResponsesWrapper<ReservationResponse> reservationResponses = service.findReservations(themeId, memberId, dateFrom, dateTo);
        //then
        assertThat(reservationResponses.getData()).hasSize(3);
    }

    @Test
    @DisplayName("이미 예약이 되어있는 날짜와 시간 및 테마에 다른 사용자가 예약 등록을 할 경우 예약이 저장된다.")
    void given_reservationRequest_when_registerAlreadyReservedWithDifferentMemberId_then_createdWithStatusIsWaiting() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-04-30"), 1L, 1L, 2L);
        //when
        final ReservationResponse reservationResponse = service.register(reservationRequest);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(reservationResponse.id()).isEqualTo(afterCreateSize);
        assertThat(afterCreateSize).isEqualTo(initialSize + 1);
    }

    @Test
    @DisplayName("예약이 제거되면 우선 대기중인 상태의 예약이 예약 상태가 된다.")
    void given_when_deleteReservationHasWaitingReservation_then_stateChangedToReserved() {
        //given, when
        service.delete(8L);
        //then
        assertThat(repository.findById(9L).get().getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("회원 Id가 일치하지 않는 대기중인 예약을 제거할 수 없다.")
    void given_differentMemberId_when_deleteByIdWithWaiting_then_notDeleted() {
        //given, when
        long initialSize = getReservationSize();
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "duck", Role.USER);
        //when
        service.deleteByIdWithWaiting(9L, member);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("대기중인 예약을 제거할 수 있다.")
    void given_when_deleteWaitingById_then_deleted() {
        //given, when
        long initialSize = getReservationSize();
        //when
        service.deleteWaitingById(9L);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }

    @Test
    @DisplayName("예약 대기 목록을 반환한다.")
    void given_when_findEntireWaitingReservationList_then_ReservationWaitingResponse() {
        //given, when
        final ResponsesWrapper<ReservationWaitingResponse> waitingReservations = service.findEntireWaitingReservations();
        //then
        assertThat(waitingReservations.getData()).hasSize(2);
    }
}
