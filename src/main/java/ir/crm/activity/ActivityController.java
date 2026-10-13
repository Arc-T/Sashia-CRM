package ir.crm.activity;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/{moduleKey}/{id}/activities")
@Tag(name = "Activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*', #moduleKey + ':view')")
    public List<ActivityResponse> timeline(@PathVariable String moduleKey, @PathVariable Long id) {
        return service.timeline(moduleKey, id);
    }
}
