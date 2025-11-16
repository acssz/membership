(() => {
    const qrImg = document.getElementById("member-qr");
    if (!qrImg) {
        return;
    }

    const refreshQr = () => {
        const baseUrl = "/card/qr";
        const nextSrc = `${baseUrl}?ts=${Date.now()}`;
        qrImg.setAttribute("src", nextSrc);
    };

    setInterval(refreshQr, 30000);

    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible") {
            refreshQr();
        }
    });
})();
