(() => {
    const qrImg = document.getElementById("member-qr");
    const installButton = document.querySelector("[data-pwa-install]");
    let deferredInstallPrompt = null;

    const refreshQr = () => {
        if (!qrImg) {
            return;
        }
        const baseUrl = "/qr";
        const nextSrc = `${baseUrl}?ts=${Date.now()}`;
        qrImg.setAttribute("src", nextSrc);
    };

    if (qrImg) {
        setInterval(refreshQr, 30000);
        document.addEventListener("visibilitychange", () => {
            if (document.visibilityState === "visible") {
                refreshQr();
            }
        });
    }

    const toggleButton = document.querySelector("[data-secret-toggle]");
    const secretFields = document.querySelectorAll("[data-secret]");

    if (!toggleButton || secretFields.length === 0) {
        return;
    }

    const showLabel = toggleButton.dataset.showLabel || "Show";
    const hideLabel = toggleButton.dataset.hideLabel || "Hide";
    const labelElement = toggleButton.querySelector("[data-secret-label]");
    const iconElement = toggleButton.querySelector("[data-secret-icon]");
    let visible = false;

    const syncSecrets = () => {
        secretFields.forEach((field) => {
            const maskValue = field.dataset.maskValue || "⋆⋆⋆";
            if (visible) {
                field.textContent = field.dataset.secretValue || "";
                field.classList.add("secret--visible");
            } else {
                field.textContent = maskValue;
                field.classList.remove("secret--visible");
            }
        });
        if (labelElement) {
            labelElement.textContent = visible ? hideLabel : showLabel;
        }
        if (iconElement) {
            iconElement.classList.toggle("fa-eye", !visible);
            iconElement.classList.toggle("fa-eye-slash", visible);
        }
        toggleButton.setAttribute("aria-pressed", visible ? "true" : "false");
        toggleButton.setAttribute("aria-label", visible ? hideLabel : showLabel);
    };

    toggleButton.addEventListener("click", () => {
        visible = !visible;
        syncSecrets();
    });

    syncSecrets();

    window.addEventListener("beforeinstallprompt", (event) => {
        event.preventDefault();
        deferredInstallPrompt = event;
        if (installButton) {
            installButton.hidden = false;
        }
    });

    installButton?.addEventListener("click", async () => {
        if (!deferredInstallPrompt) {
            return;
        }
        installButton.disabled = true;
        deferredInstallPrompt.prompt();
        const choiceResult = await deferredInstallPrompt.userChoice;
        deferredInstallPrompt = null;
        installButton.hidden = choiceResult.outcome === "accepted";
        installButton.disabled = false;
    });

    window.addEventListener("appinstalled", () => {
        if (installButton) {
            installButton.hidden = true;
        }
        deferredInstallPrompt = null;
    });
})();
