package org.acssz.membership.card;

import org.acssz.membership.member.Member;
import org.acssz.membership.member.MemberService;
import org.acssz.membership.qr.MembershipQrTokenService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping
public class CardController {

    private final MemberService memberService;
    private final MembershipQrTokenService qrTokenService;

    public CardController(MemberService memberService, MembershipQrTokenService qrTokenService) {
        this.memberService = memberService;
        this.qrTokenService = qrTokenService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/card";
    }

    @GetMapping("/card")
    public String card(@ModelAttribute("degreeForm") DegreeUpdateForm degreeForm,
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {
        Member member = getOrCreateMember(oidcUser);
        model.addAttribute("member", member);

        if (degreeForm.getDegree() == null) {
            model.addAttribute("degreeForm", DegreeUpdateForm.fromExisting(member.getDegree()));
        }
        return "card";
    }

    @GetMapping(value = "/card/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qr(@AuthenticationPrincipal OidcUser oidcUser) {
        Member member = getOrCreateMember(oidcUser);
        byte[] image = qrTokenService.generateTokenPng(member.getSubject());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(image);
    }

    @PostMapping("/card/degree")
    public String updateDegree(
            @Valid @ModelAttribute("degreeForm") DegreeUpdateForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        Member member = getOrCreateMember(oidcUser);
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", member);
            return "card";
        }
        memberService.updateDegree(member.getSubject(), form.getDegree());
        redirectAttributes.addFlashAttribute("messageKey", "card.degree.saved");
        return "redirect:/card";
    }

    private Member getOrCreateMember(OidcUser oidcUser) {
        String displayName = oidcUser.getFullName();
        if (displayName == null) {
            displayName = oidcUser.getPreferredUsername();
        }
        if (displayName == null) {
            displayName = oidcUser.getEmail();
        }
        if (displayName == null) {
            displayName = oidcUser.getSubject();
        }
        return memberService.getOrCreate(oidcUser.getSubject(), displayName);
    }
}
