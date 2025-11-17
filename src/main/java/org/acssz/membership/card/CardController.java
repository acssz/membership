package org.acssz.membership.card;

import java.io.IOException;

import org.acssz.membership.member.Member;
import org.acssz.membership.member.MemberProfileUpdate;
import org.acssz.membership.member.MemberService;
import org.acssz.membership.member.OccupationType;
import org.acssz.membership.member.StudentDegree;
import org.acssz.membership.member.StudentVerificationService;
import org.acssz.membership.member.StudentVerificationStatus;
import org.acssz.membership.qr.MembershipQrTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    private final MemberService memberService;
    private final MembershipQrTokenService qrTokenService;
    private final StudentVerificationService studentVerificationService;

    public CardController(MemberService memberService, MembershipQrTokenService qrTokenService,
            StudentVerificationService studentVerificationService) {
        this.memberService = memberService;
        this.qrTokenService = qrTokenService;
        this.studentVerificationService = studentVerificationService;
    }

    @GetMapping("/")
    public String card(@AuthenticationPrincipal OidcUser oidcUser, HttpServletRequest request, Model model) {
        Member member = getOrCreateMember(requireOidcUser(oidcUser));
        StudentVerificationStatus verificationStatus = studentVerificationService.evaluate(member);
        model.addAttribute("member", member);
        model.addAttribute("verificationStatus", verificationStatus);
        model.addAttribute("currentPath", request != null ? request.getRequestURI() : "/");
        return "card";
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qr(@AuthenticationPrincipal OidcUser oidcUser) {
        Member member = getOrCreateMember(requireOidcUser(oidcUser));
        byte[] image = qrTokenService.generateTokenPng(member.getSubject());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(image);
    }

    @GetMapping("/verify")
    public String verify(@ModelAttribute("verificationForm") ProfileVerificationForm verificationForm,
            @AuthenticationPrincipal OidcUser oidcUser,
            HttpServletRequest request,
            Model model) {
        Member member = getOrCreateMember(requireOidcUser(oidcUser));
        StudentVerificationStatus verificationStatus = studentVerificationService.evaluate(member);

        if (verificationForm.getOccupationType() == null) {
            model.addAttribute("verificationForm", ProfileVerificationForm.fromMember(member));
        }

        populateVerificationModel(model, member, verificationStatus, request != null ? request.getRequestURI() : "/verify");
        return "verify";
    }

    @PostMapping("/verify")
    public String submitVerification(
            @Valid @ModelAttribute("verificationForm") ProfileVerificationForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal OidcUser oidcUser,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        Member member = getOrCreateMember(requireOidcUser(oidcUser));
        StudentVerificationStatus verificationStatus = studentVerificationService.evaluate(member);
        validateStudentFields(form, bindingResult, verificationStatus);

        if (bindingResult.hasErrors()) {
            populateVerificationModel(model, member, verificationStatus, request != null ? request.getRequestURI() : "/verify");
            return "verify";
        }

        MemberProfileUpdate update;
        try {
            update = toProfileUpdate(form);
        } catch (IllegalStateException exception) {
            bindingResult.rejectValue("studentIdCard", "verify.form.studentIdCard.io");
            populateVerificationModel(model, member, verificationStatus, request != null ? request.getRequestURI() : "/verify");
            return "verify";
        }
        memberService.updateProfile(member.getSubject(), update);
        redirectAttributes.addFlashAttribute("messageKey", "verify.form.saved");
        return "redirect:/";
    }

    private void populateVerificationModel(Model model, Member member, StudentVerificationStatus status, String currentPath) {
        model.addAttribute("member", member);
        model.addAttribute("verificationStatus", status);
        model.addAttribute("occupationTypes", OccupationType.values());
        model.addAttribute("studentDegrees", StudentDegree.values());
        model.addAttribute("currentPath", StringUtils.hasText(currentPath) ? currentPath : "/verify");
    }

    private void validateStudentFields(ProfileVerificationForm form, BindingResult bindingResult,
            StudentVerificationStatus verificationStatus) {
        if (form.getOccupationType() != OccupationType.STUDENT) {
            form.setStudentDegree(null);
            return;
        }
        if (!bindingResult.hasFieldErrors("studentDegree") && form.getStudentDegree() == null) {
            bindingResult.rejectValue("studentDegree", "verify.form.program.required");
        }
        boolean hasUpload = form.getStudentIdCard() != null && !form.getStudentIdCard().isEmpty();
        if (!hasUpload && verificationStatus.requiresEvidenceUpload()) {
            bindingResult.rejectValue("studentIdCard", "verify.form.studentIdCard.required");
        }
    }

    private MemberProfileUpdate toProfileUpdate(ProfileVerificationForm form) {
        byte[] studentCardBytes = readBytes(form.getStudentIdCard());
        String contentType = (studentCardBytes != null && studentCardBytes.length > 0 && form.getStudentIdCard() != null)
                ? form.getStudentIdCard().getContentType()
                : null;
        String originalFilename = form.getStudentIdCard() != null ? form.getStudentIdCard().getOriginalFilename() : null;
        StudentDegree studentDegree = form.getOccupationType() == OccupationType.STUDENT ? form.getStudentDegree() : null;
        return new MemberProfileUpdate(
                form.getOccupationType(),
                studentDegree,
                studentCardBytes,
                contentType,
                originalFilename);
    }

    private byte[] readBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read uploaded file", ex);
        }
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
        return memberService.getOrCreate(oidcUser.getSubject(), displayName, oidcUser.getEmail());
    }

    private OidcUser requireOidcUser(OidcUser oidcUser) {
        if (oidcUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OIDC identity required");
        }
        return oidcUser;
    }
}
