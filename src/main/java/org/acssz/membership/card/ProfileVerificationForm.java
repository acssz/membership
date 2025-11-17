package org.acssz.membership.card;

import org.acssz.membership.member.Member;
import org.acssz.membership.member.OccupationType;
import org.acssz.membership.member.StudentDegree;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileVerificationForm {

    @NotNull(message = "{verify.form.occupation.required}")
    private OccupationType occupationType;

    private StudentDegree studentDegree;

    private MultipartFile studentIdCard;

    public static ProfileVerificationForm fromMember(Member member) {
        ProfileVerificationForm form = new ProfileVerificationForm();
        form.setOccupationType(member.getOccupationType());
        form.setStudentDegree(member.getStudentDegree());
        return form;
    }
}
