package ir.crm.identity.user;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.identity.user.dto.UserPasswordResetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{id}/password")
@Tag(name = "Users")
@RequiredArgsConstructor
public class UserPasswordController {

    private final UserService service;

    /** Admin-initiated reset (also the way to give a password to accounts created without one). */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('*','users:edit')")
    public void reset(@PathVariable Long id, @Valid @RequestBody UserPasswordResetRequest request) {
        service.resetPassword(id, request.newPassword());
    }
}
