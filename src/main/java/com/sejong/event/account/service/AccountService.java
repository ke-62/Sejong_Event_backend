package com.sejong.event.account.service;

import com.sejong.event.account.dto.AccountResponse;
import com.sejong.event.account.dto.CreateAccountRequest;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AccountResponse> getAccounts() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ORGANIZER)
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .organization(request.getOrganization())
                .role(Role.ORGANIZER)
                .build();

        userRepository.save(user);
        // 관리자 콘솔에 평문 비밀번호 표시 (API 명세 요구사항)
        return AccountResponse.from(user, request.getPassword());
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        User user = userRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
