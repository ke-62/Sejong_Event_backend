package com.sejong.event.account.service;

import com.sejong.event.account.dto.AccountResponse;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.support.TestFixtures;
import com.sejong.event.support.TestRequests;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계정 생성 시 bcrypt 로 암호화하고 role=organizer 로 저장한다")
    void createAccount() {
        given(userRepository.existsByUsername("booth01")).willReturn(false);
        given(passwordEncoder.encode("pw1234")).willReturn("$2a$10$hashed");

        AccountResponse response = accountService.createAccount(TestRequests.createAccount("booth01", "pw1234"));

        assertThat(response.getUsername()).isEqualTo("booth01");
        assertThat(response.getPassword()).isEqualTo("pw1234"); // 관리자 콘솔 표시용 평문
        assertThat(response.getRole()).isEqualTo("organizer");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("아이디가 중복이면 DUPLICATE_USERNAME")
    void createAccountDuplicate() {
        given(userRepository.existsByUsername("booth01")).willReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(TestRequests.createAccount("booth01", "pw1234")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_USERNAME));
    }

    @Test
    @DisplayName("계정 목록은 운영진(ORGANIZER) 만 반환한다")
    void getAccountsFiltersOrganizers() {
        given(userRepository.findAll())
                .willReturn(List.of(TestFixtures.organizer("학생회"), TestFixtures.superuser()));

        List<AccountResponse> accounts = accountService.getAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getRole()).isEqualTo("organizer");
    }

    @Test
    @DisplayName("존재하지 않는 계정을 삭제하면 USER_NOT_FOUND")
    void deleteMissingAccount() {
        given(userRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount(404L))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("계정 삭제 시 레포지토리 delete 를 호출한다")
    void deleteAccount() {
        User user = TestFixtures.organizer("학생회");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        accountService.deleteAccount(1L);

        then(userRepository).should().delete(user);
    }
}
