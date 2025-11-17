(() => {
    const occupationSelect = document.getElementById("occupationType");
    const studentBlocks = document.querySelectorAll("[data-student-only]");

    const updateVisibility = () => {
        if (!occupationSelect) {
            return;
        }
        const isStudent = occupationSelect.value === "STUDENT";
        studentBlocks.forEach((block) => {
            block.dataset.hidden = isStudent ? "false" : "true";
        });
    };

    occupationSelect?.addEventListener("change", updateVisibility);
    updateVisibility();
})();
