import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.babyhands.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody MemberDto memberDto) {
        return ResponseEntity.ok("Login successful");
    }

    
}
