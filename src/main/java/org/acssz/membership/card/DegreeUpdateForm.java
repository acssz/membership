package org.acssz.membership.card;

import jakarta.validation.constraints.Size;

public class DegreeUpdateForm {

    @Size(max = 160, message = "{card.degree.error}")
    private String degree;

    public static DegreeUpdateForm fromExisting(String degree) {
        DegreeUpdateForm form = new DegreeUpdateForm();
        form.setDegree(degree);
        return form;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }
}
